const {test}=require('node:test');
const assert=require('node:assert/strict');
const fs=require('node:fs');
const vm=require('node:vm');
class Element {
 constructor(tag){this.tagName=tag;this.children=[];this.value='';this.checked=false;this.disabled=false;this.hidden=false;this.attributes={};}
 appendChild(n){this.children.push(n);return n;}
 append(...nodes){nodes.forEach(n=>this.appendChild(n));}
 replaceChildren(...nodes){this.children=[];this.append(...nodes);}
 setAttribute(k,v){this.attributes[k]=v;}
}
function setup(app,request=async()=>({assignmentId:'assignment /1'})){
 const container=new Element('section');const calls=[];const window={TeacherSelectionAssignmentsPath:'/tamo/admin/assignments.jsp'};
 const context={window,document:{createElement:t=>new Element(t)},request:async(...args)=>{calls.push(args);return request(...args);}};
 const path='backend/src/main/webapp/static/js/teacher-selection.js';
 assert.ok(fs.existsSync(path),'shared TeacherSelection UI exists');vm.runInNewContext(fs.readFileSync(path,'utf8'),context);
 window.TeacherSelection.render(container,structuredClone(app));
 const all=()=>{const nodes=[];function walk(n){nodes.push(n);n.children.forEach(walk);}walk(container);return nodes;};
 const get=name=>all().find(n=>n.name===name);return {container,calls,all,get,form:()=>all().find(n=>n.tagName==='form')};
}
const application={id:'a/1',canSelectTa:true,scheduleMatching:{sections:[{sectionId:'s1',name:'<script>section</script>',status:'available'},{sectionId:'s2',name:'Pending',status:'pending'},{sectionId:'s3',name:'Conflict',status:'conflict'}]}};
const submit=ui=>ui.form().onsubmit({preventDefault(){}});
test('permission gates controls; existing draft links use the page role route',()=>{
 assert.equal(setup({...application,canSelectTa:false}).form(),undefined);
 const ui=setup({...application,canSelectTa:false,selectionAssignmentId:'x/y'});assert.equal(ui.all().find(n=>n.tagName==='a').href,'/tamo/admin/assignments.jsp?assignmentId=x%2Fy');
});
test('requires section and rationale, renders text safely and excludes conflicts',async()=>{
 const ui=setup(application);assert.equal(ui.all().find(n=>n.textContent==='<script>section</script>').tagName,'option');
 assert.equal(ui.all().find(n=>n.value==='s3').disabled,true);
 await submit(ui);assert.equal(ui.calls.length,0);ui.get('sectionId').value='s1';ui.get('note').value='short';await submit(ui);assert.equal(ui.calls.length,0);
 ui.get('note').value='A sufficient rationale';await submit(ui);assert.equal(ui.calls[0][0],'/review/a%2F1/select-ta');assert.equal(JSON.parse(ui.calls[0][1].body).confirmPending,false);
 assert.equal(ui.all().find(n=>n.tagName==='a').href,'/tamo/admin/assignments.jsp?assignmentId=assignment%20%2F1');assert.equal(ui.form(),undefined);
});
test('pending availability needs explicit confirmation and manual rationale',async()=>{
 const ui=setup(application);ui.get('sectionId').value='s2';ui.get('sectionId').onchange();assert.equal(ui.get('confirmPending').required,true);
 ui.get('note').value='A sufficient rationale';await submit(ui);assert.equal(ui.calls.length,0);
 ui.get('confirmPending').checked=true;ui.get('manualConfirmationNote').value='short';await submit(ui);assert.equal(ui.calls.length,0);
 ui.get('manualConfirmationNote').value='Confirmed directly with applicant';await submit(ui);assert.equal(JSON.parse(ui.calls[0][1].body).confirmPending,true);
});
test('failure remains retryable, double submission sends only one request',async()=>{
 let reject;const ui=setup(application,()=>new Promise((_,r)=>{reject=r;}));ui.get('sectionId').value='s1';ui.get('note').value='A sufficient rationale';const pending=submit(ui);await submit(ui);assert.equal(ui.calls.length,1);reject(new Error('No remaining places'));await pending;
 assert.ok(ui.all().some(n=>n.textContent==='No remaining places'));assert.equal(ui.all().find(n=>n.tagName==='button').disabled,false);
});

test('pending manual confirmation is capped at 1000 while selection rationale allows 4000',async()=>{
 const ui=setup(application);ui.get('sectionId').value='s2';ui.get('sectionId').onchange();ui.get('confirmPending').checked=true;ui.get('note').value='x'.repeat(4000);ui.get('manualConfirmationNote').value='x'.repeat(1001);await submit(ui);assert.equal(ui.calls.length,0);ui.get('manualConfirmationNote').value='x'.repeat(1000);await submit(ui);assert.equal(ui.calls.length,1);
});
test('existing assignment copy does not claim it remains an unpublished draft',()=>{
 const ui=setup({...application,selectionAssignmentId:'published'});assert.equal(ui.all().find(n=>n.tagName==='a').textContent,'View TA assignment');assert.ok(!ui.all().some(n=>/draft|publish it/.test(n.textContent||'')));
});

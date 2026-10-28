const {test}=require('node:test');
const assert=require('node:assert/strict');
const fs=require('node:fs');
const vm=require('node:vm');
const root='backend/src/main/webapp/';
function page(file, state={}) {
 const source=fs.readFileSync(root+'organiser/'+file+'.jsp','utf8').match(/<script>\s*([\s\S]*?)<\/script>/)[1];
 const calls=[],toasts=[];
 const context=vm.createContext({document:{querySelector:()=>({content:'/tamo'})},window:{location:{search:'?id=a'}},URLSearchParams,organiserApi:{getApplicationPeriods:()=>({then:()=>({catch(){}})}),getPipeline:()=>new Promise(()=>{}),getApplicationDetails:()=>new Promise(()=>{}),updateCandidateStatus:async(...a)=>calls.push(a),applicationAction:async(...a)=>calls.push(a)},showToast:(...a)=>toasts.push(a),prompt:()=>state.reason??null,...state});
 vm.runInContext(source.replace(/      loadDetail\(\);\s*$/,'').replace(/      loadPipeline\(\);\s*$/,''),context);
 return {context,calls,toasts,run:code=>vm.runInContext(code,context)};
}
test('pipeline rejects forbidden changes and requires selection approval for offer',async()=>{
 const p=page('pipeline');p.run("allApps=[{id:'a',status:'review',canApprove:false,canRecommend:true,selectionApproved:false}]; renderKanban=()=>{};");
 await p.run("updateStatus('a','rejected')");await p.run("updateStatus('a','offer')");assert.equal(p.calls.length,0);
 await p.run("updateStatus('a','shortlisted')");assert.equal(p.calls.length,1);
});
test('pipeline rejection requires explicit valid reason and carries it',async()=>{
 for(const reason of [null,'short','x'.repeat(4001),'Reason for rejection']){
  const p=page('pipeline',{reason});p.run("allApps=[{id:'a',status:'review',canApprove:true}]; renderKanban=()=>{};");
  await p.run("updateStatus('a','rejected')");assert.equal(p.calls.length,reason==='Reason for rejection'?1:0);if(p.calls.length)assert.equal(p.calls[0][2],reason);
 }
});
test('detail actions deny teachers rejection and validate approval reason',async()=>{
 const p=page('application-detail',{reason:'Reason for rejection'});p.run("currentApp={canApprove:false,canRecommend:true}; loadDetail=()=>{};");
 await p.run("doAction('reject')");assert.equal(p.calls.length,0);
 p.run('currentApp.canApprove=true');await p.run("doAction('reject')");assert.equal(p.calls[0][2],'Reason for rejection');
});
test('API helpers transport optional notes and preserve PUT for actions',async()=>{
 const requests=[];const context=vm.createContext({document:{querySelector:()=>({content:'/tamo'})},window:{},URLSearchParams,fetch:async(url,options)=>{requests.push({url,...options});return {ok:true,json:async()=>({})};}});
 vm.runInContext(fs.readFileSync(root+'static/js/api.js','utf8'),context);
 await vm.runInContext("organiserApi.updateCandidateStatus('a','rejected','A valid reason'); organiserApi.applicationAction('a','reject','A valid reason')",context);
 assert.equal(JSON.parse(requests[0].body).note,'A valid reason');assert.equal(JSON.parse(requests[1].body).note,'A valid reason');assert.equal(requests[1].method,'PUT');
});
test('approved selection permits offer only with update permission',async()=>{
 const p=page('pipeline');p.run("allApps=[{id:'a',status:'shortlisted',selectionApproved:true,canRecommend:false,canApprove:false}]; renderKanban=()=>{};");
 await p.run("updateStatus('a','offer')");assert.equal(p.calls.length,0);
 p.run('allApps[0].canRecommend=true');await p.run("updateStatus('a','offer')");assert.equal(p.calls.length,1);assert.equal(p.calls[0][1],'offer');
});
test('detail rejection cancels without a valid explicit reason',async()=>{
 for(const reason of [null,'  ','short','x'.repeat(4001)]) {
  const p=page('application-detail',{reason});p.run('currentApp={canApprove:true}; loadDetail=()=>{};');
  await p.run("doAction('reject')");assert.equal(p.calls.length,0);
 }
});
test('read-only application cards stay visible without drag or decision actions',()=>{
 const nodes={detailPage:{innerHTML:''},sectionFilter:{value:''}};
 const state={document:{querySelector:()=>({content:'/tamo'}),getElementById:id=>nodes[id]},escapeDisplay:a=>a,lucide:{createIcons(){}},SectionMatching:{selected:()=>null,badge:()=>''}};
 const p=page('pipeline',state);
 assert.match(p.run("renderCard({id:'a',name:'Alex',status:'review'})"),/draggable="false"/);
 let prevented=false;
 p.context.e={preventDefault(){prevented=true;},dataTransfer:{}};
 p.run("allApps=[{id:'a',status:'review'}]; onDragStart(e,'a')");assert.equal(prevented,true);
 const d=page('application-detail',state);
 d.run("renderDetail({id:'a',status:'review',canRecommend:true,canApprove:false})");
 assert.match(nodes.detailPage.innerHTML,/Shortlist/);assert.doesNotMatch(nodes.detailPage.innerHTML,/> Reject</);
 d.run("renderDetail({id:'a',status:'review'})");assert.doesNotMatch(nodes.detailPage.innerHTML,/doAction/);
 assert.match(nodes.detailPage.innerHTML,/Review Workspace/);
});

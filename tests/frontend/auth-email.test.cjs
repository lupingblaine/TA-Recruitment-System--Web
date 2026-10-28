const {test}=require('node:test');
const assert=require('node:assert/strict');
const fs=require('node:fs');
const vm=require('node:vm');
const script=()=>fs.readFileSync('backend/src/main/webapp/static/js/auth-email.js','utf8');
function page(value,storage,links=[]){const email={value};const document={getElementById:()=>email,querySelectorAll:()=>links};vm.runInNewContext(script(),{document,sessionStorage:storage});return email;}
function storage(){const values=new Map();return {setItem:(k,v)=>values.set(k,v),getItem:k=>values.get(k)||null,removeItem:k=>values.delete(k)};}
test('both create-account links carry only the current email to signup once',()=>{const saved=storage();const links=[{},{}].map(()=>({addEventListener(type,fn){this.click=fn;}}));const email=page('old@example.edu',saved,links);email.value='alex+ta@example.edu';links[1].click();assert.equal(page('',saved).value,'alex+ta@example.edu');assert.equal(page('',saved).value,'');email.value='new@example.edu';links[0].click();assert.equal(page('',saved).value,'new@example.edu');});
test('blocked browser storage does not break navigation or signup',()=>{const blocked={setItem(){throw Error('blocked');},getItem(){throw Error('blocked');},removeItem(){throw Error('blocked');}};const links=[{addEventListener(type,fn){this.click=fn;}}];page('a@example.edu',blocked,links);assert.doesNotThrow(()=>links[0].click());assert.equal(page('',blocked).value,'');});

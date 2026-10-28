const { test, before, after } = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const { chromium } = require(process.env.PLAYWRIGHT_MODULE || 'playwright');
const root = path.resolve('backend/src/main/webapp');
let browser;
before(async () => { browser = await chromium.launch({headless:true, ...(process.env.CHROMIUM_EXECUTABLE ? {executablePath:process.env.CHROMIUM_EXECUTABLE} : {})}); });
after(async () => { await browser?.close(); });
const profile = {firstName:'Alex',lastName:'Student',uciEmail:'alex@uci.edu',uciStudentId:'00123456',studentLevel:'masters',program:'Biology',graduateStartYear:2025,facultyPI:'N/A',taQuarters:0,advancedToCandidacy:'not_applicable',trainingStatus:'in_progress',sexualHarassmentTrainingCompleted:false,additionalEmployment:'None',documentedAccommodation:false,accommodationNotes:'',skills:['Python'],interests:['BIO 93'],teachingExperience:'',researchExperience:'',schedulingConflicts:'',additionalComments:'',ackSchedule:true,ackEnrollment:true,acknowledged:true,testingScenario:'synthetic test'};
async function open(t, settings={}) {
  const page = await browser.newPage({viewport:{width:settings.width||1100,height:850}});
  page.setDefaultTimeout(2000);
  t.after(()=>page.close());
  const state={gets:0,saves:[]};
  let savedProfile=settings.newProfile?{}:(settings.empty?{...profile,skills:[],interests:[]}:profile);
  const errors=[]; page.on('pageerror',e=>errors.push(e.message));
  await page.route('http://profile.test/**',async route=>{
    const req=route.request(),url=new URL(req.url());
    if(url.pathname==='/tamo/applicant/application-profile.jsp') {
      let html=fs.readFileSync(path.join(root,'applicant/application-profile.jsp'),'utf8')
        .replace(/<%= java.time.Year.now\(\).getValue\(\) %>/g,String(new Date().getFullYear()))
        .replace(/<%= request.getContextPath\(\) %>/g,'/tamo')
        .replace(/<%@[^]*?%>/g,'').replace(/<jsp:include[^>]*\/>/g,'');
      html='<!doctype html><html lang="en"><head><meta name="viewport" content="width=device-width, initial-scale=1"><meta name="ctx" content="/tamo"><link rel="stylesheet" href="/tamo/static/css/custom.css"><script src="/tamo/static/js/api.js"></script></head><body>'+html+'</body></html>';
      return route.fulfill({contentType:'text/html',body:html});
    }
    if(url.pathname.startsWith('/tamo/static/')) {
      const file=path.join(root,url.pathname.slice('/tamo/'.length));
      return route.fulfill({contentType:file.endsWith('.css')?'text/css':'text/javascript',body:fs.existsSync(file)?fs.readFileSync(file):''});
    }
    if(url.pathname.endsWith('/qualification-options')) return route.fulfill({json:settings.empty?{skills:[],courses:[]}:{skills:['Python','R','<img src=x onerror=alert(1)>'],courses:[{code:'BIO 93',name:'Biology'}]}});
    if(url.pathname.endsWith('/qualifications')) {
      if(req.method()==='PUT') {state.saves.push(req.postDataJSON());if(!settings.saveError) savedProfile=req.postDataJSON();if(settings.saveDelay) await new Promise(resolve=>setTimeout(resolve,settings.saveDelay));return route.fulfill(settings.saveError?{status:500,json:{error:'Could not save. Try again.'}}:{json:req.postDataJSON()});}
      state.gets++;
      if(settings.loadError&&state.gets===1) return route.fulfill({status:503,json:{error:'Profile unavailable'}});
      return route.fulfill({json:savedProfile});
    }
    return route.fulfill({status:404,body:''});
  });
  await page.goto('http://profile.test/tamo/applicant/application-profile.jsp');
  return {page,state,errors};
}
async function ready(page,edit=true){
  await page.waitForFunction(()=>document.getElementById('qualificationForm').getAttribute('aria-busy')==='false');
  if(edit && await page.locator('#qualificationEdit').isVisible()) await page.locator('#qualificationEdit').click();
}

test('five section links work with keyboard and focus their headings',async t=>{
  const {page,errors}=await open(t); await ready(page);
  if(process.env.PROFILE_SCREENSHOT) await page.screenshot({path:process.env.PROFILE_SCREENSHOT.replace('mobile','desktop'),fullPage:true});
  const nav=page.getByRole('navigation',{name:'Application profile sections'});
  assert.equal(await nav.getByRole('link').count(),5);
  const link=nav.getByRole('link').nth(2);await link.focus();await page.keyboard.press('Enter');
  assert.equal(await page.evaluate(()=>document.activeElement.id),'profile-interests');
  assert.equal(await page.locator('#qualificationSkills img').count(),0);
  assert.deepEqual(errors,[]);
});
test('graduate year requirement follows student level without clearing existing values',async t=>{
  const {page}=await open(t);await ready(page);
  const year=page.locator('[name=graduateStartYear]');assert.equal(await year.getAttribute('required'),'');
  await page.locator('[name=studentLevel]').selectOption('undergraduate');assert.equal(await year.getAttribute('required'),null);
  assert.match(await page.locator('#graduateYearHint').innerText(),/optional/i);
  await page.locator('[name=studentLevel]').selectOption('phd');assert.equal(await year.inputValue(),'2025');assert.match(await page.locator('#graduateYearHint').innerText(),/required/i);
});
test('invalid fields produce actionable summary and no save request',async t=>{
  const {page,state}=await open(t);await ready(page);
  await page.locator('[name=uciStudentId]').fill('123');await page.locator('[name=uciEmail]').fill('alex@example.com');
  await page.getByRole('button',{name:'Save application profile',exact:true}).click();
  const summary=page.locator('#qualificationErrors');await summary.waitFor({state:'visible'});
  assert.equal(await summary.getByRole('link').count(),2);assert.equal(state.saves.length,0);
  await summary.getByRole('link').first().click();assert.equal(await page.evaluate(()=>document.activeElement.name),'uciEmail');
  await page.locator('[name=uciEmail]').fill('alex@uci.edu');await page.locator('[name=uciStudentId]').fill('00123456');
  await page.getByRole('button',{name:'Save application profile',exact:true}).click();
  await page.waitForFunction(()=>document.getElementById('qualificationMessage').textContent.includes('saved'));
  assert.equal(state.saves.length,1);
});
test('save preserves booleans, numeric types, leading zeros, choices and testing metadata',async t=>{
  const {page,state}=await open(t);await ready(page);
  await page.locator('[name=firstName]').fill('Alexandra');
  assert.match(await page.locator('#qualificationMessage').innerText(),/unsaved/i);
  await page.getByRole('button',{name:'Save application profile',exact:true}).click();
  await page.waitForFunction(()=>document.getElementById('qualificationMessage').textContent.includes('saved'));
  assert.match(await page.locator('#qualificationSaveHint').innerText(),/saved/);
  const saved=state.saves[0];assert.equal(saved.firstName,'Alexandra');assert.equal(saved.uciStudentId,'00123456');assert.equal(saved.taQuarters,0);assert.equal(saved.graduateStartYear,2025);assert.equal(saved.documentedAccommodation,false);assert.equal(saved.sexualHarassmentTrainingCompleted,false);assert.deepEqual(saved.skills,['Python']);assert.deepEqual(saved.interests,['BIO 93']);assert.equal(saved.testingScenario,'synthetic test');assert.equal(saved.acknowledged,true);
});
test('failed save retains edits and restores save button',async t=>{
  const {page}=await open(t,{saveError:true});await ready(page);
  await page.locator('[name=firstName]').fill('Keep this name');await page.getByRole('button',{name:'Save application profile',exact:true}).click();
  await page.waitForFunction(()=>document.getElementById('qualificationMessage').textContent.includes('Could not save'));
  assert.equal(await page.locator('[name=firstName]').inputValue(),'Keep this name');assert.equal(await page.locator('#qualificationSave').isEnabled(),true);
});
test('failed initial load keeps form disabled and retry hydrates once',async t=>{
  const {page,state}=await open(t,{loadError:true});
  await page.getByRole('button',{name:'Retry loading profile'}).waitFor({state:'visible'});
  assert.equal(await page.locator('#qualificationFields').evaluate(fieldset=>fieldset.disabled),true);
  assert.equal(await page.locator('[name=firstName]').isDisabled(),true);
  await page.getByRole('button',{name:'Retry loading profile'}).click();await ready(page);assert.equal(state.gets,2);assert.equal(await page.locator('[name=skills]').count(),3);
});
test('empty options show guidance; mobile layout fits at 390px and 320px',async t=>{
  const {page}=await open(t,{empty:true,width:390});await ready(page);
  assert.match(await page.locator('#qualificationSkills').innerText(),/no skills/i);assert.match(await page.locator('#qualificationInterests').innerText(),/no courses/i);
  for(const width of [390,320]){await page.setViewportSize({width,height:850});assert.equal(await page.evaluate(()=>document.documentElement.scrollWidth<=window.innerWidth),true);}
  if(process.env.PROFILE_SCREENSHOT) await page.screenshot({path:process.env.PROFILE_SCREENSHOT,fullPage:true});
});

test('saving announces busy state, disables editing and ignores duplicate submission',async t=>{
  const {page,state}=await open(t,{saveDelay:500});await ready(page);
  await page.getByRole('button',{name:'Save application profile',exact:true}).click();
  assert.equal(await page.locator('#qualificationForm').getAttribute('aria-busy'),'true');
  assert.equal(await page.locator('[name=firstName]').isDisabled(),true);
  assert.match(await page.locator('#qualificationMessage').innerText(),/saving/i);
  await page.locator('#qualificationForm').evaluate(form=>form.dispatchEvent(new Event('submit',{bubbles:true,cancelable:true})));
  await page.waitForFunction(()=>document.getElementById('qualificationMessage').textContent.includes('saved'));
  assert.equal(state.saves.length,1);assert.equal(await page.locator('#qualificationForm').isVisible(),false);
  assert.equal(await page.getByRole('button',{name:'Edit application profile',exact:true}).isVisible(),true);
});

test('saved profile opens as text and Edit explicitly enables the form',async t=>{
  const {page}=await open(t);await ready(page,false);
  assert.equal(await page.locator('#qualificationForm').isVisible(),false);
  const view=page.locator('#qualificationView');assert.equal(await view.isVisible(),true);
  assert.match(await view.innerText(),/Alex/);assert.match(await view.innerText(),/00123456/);
  assert.equal(await view.locator('input,select,textarea').count(),0);
  const nav=page.getByRole('navigation',{name:'Application profile sections'});
  await nav.getByRole('link').nth(2).focus();await page.keyboard.press('Enter');
  assert.equal(await page.evaluate(()=>document.activeElement.id),'view-profile-interests');
  if(process.env.PROFILE_VIEW_SCREENSHOT) await page.screenshot({path:process.env.PROFILE_VIEW_SCREENSHOT,fullPage:true});
  for(const width of [390,320]){await page.setViewportSize({width,height:850});assert.equal(await page.evaluate(()=>document.documentElement.scrollWidth<=window.innerWidth),true);}
  await page.getByRole('button',{name:'Edit application profile',exact:true}).click();
  assert.equal(await page.locator('#qualificationForm').isVisible(),true);
  assert.equal(await page.locator('[name=firstName]').inputValue(),'Alex');
});
test('save returns to updated text view; Cancel restores the latest saved values without a request',async t=>{
  const {page,state}=await open(t);await ready(page);
  await page.locator('[name=firstName]').fill('Alexandra');
  await page.getByRole('button',{name:'Save application profile',exact:true}).click();
  await page.locator('#qualificationView').waitFor({state:'visible'});
  assert.match(await page.locator('#qualificationView').innerText(),/Alexandra/);
  assert.equal(await page.locator('#qualificationForm').isVisible(),false);
  await page.getByRole('button',{name:'Edit application profile',exact:true}).click();
  await page.locator('[name=firstName]').fill('Discard this');
  await page.locator('[name=skills][value=Python]').uncheck();
  await page.getByRole('button',{name:'Cancel editing',exact:true}).click();
  assert.match(await page.locator('#qualificationView').innerText(),/Alexandra/);
  assert.equal(state.saves.length,1);
  await page.getByRole('button',{name:'Edit application profile',exact:true}).click();
  assert.equal(await page.locator('[name=firstName]').inputValue(),'Alexandra');
  assert.equal(await page.locator('[name=skills][value=Python]').isChecked(),true);
  await page.reload();await ready(page,false);
  assert.match(await page.locator('#qualificationView').innerText(),/Alexandra/);
  assert.equal(await page.locator('#qualificationForm').isVisible(),false);
});
test('first-time profile opens in edit mode',async t=>{
  const {page}=await open(t,{newProfile:true});await ready(page,false);
  assert.equal(await page.locator('#qualificationForm').isVisible(),true);
  assert.equal(await page.locator('[name=firstName]').isEnabled(),true);
  assert.equal(await page.locator('#qualificationView').isVisible(),false);
});

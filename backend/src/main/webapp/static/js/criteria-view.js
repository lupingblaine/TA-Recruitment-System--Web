window.CriteriaView={render(container,result){
 const h=document.createElement('h2');h.className='font-bold';h.textContent='Qualification matching · explained criteria';container.appendChild(h);
 const p=document.createElement('p');p.textContent=result?(result.status.replaceAll('_',' ')+' · '+(result.score==null?'No score':result.score+'%')+' · '+result.message):'Needs confirmation';container.appendChild(p);
 if(result)for(const [key,label]of [['matched','Matched'],['missingRequired','Missing required criteria'],['unmatchedPreferred','Unmatched preferences']]){const h=document.createElement('h3');h.className='font-medium mt-3';h.textContent=label;container.appendChild(h);const list=document.createElement('ul');for(const value of result[key]||[]){const li=document.createElement('li');li.textContent=value;list.appendChild(li);}if(!list.children.length){const li=document.createElement('li');li.textContent='None';list.appendChild(li);}container.appendChild(list);}
}};

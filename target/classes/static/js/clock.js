const API='';
let clockStaffList=[];
let clockSelectedStaff=null;
let clockPinVal='';

async function initClock(){
  updateClockDisplay();
  setInterval(updateClockDisplay,1000);
  await loadClockStaff();
  await loadAttendance();
  setInterval(loadAttendance,30000);
}

function updateClockDisplay(){
  const n=new Date();
  const t=n.getHours().toString().padStart(2,'0')+':'+n.getMinutes().toString().padStart(2,'0')+':'+n.getSeconds().toString().padStart(2,'0');
  const d=n.toLocaleDateString('en-MY',{weekday:'long',year:'numeric',month:'long',day:'numeric'});
  document.getElementById('topTime').textContent=t;
  document.getElementById('topDate').textContent=d;
}

async function loadClockStaff(){
  try{
    const res=await fetch(`${API}/api/staff`);
    clockStaffList=await res.json();
    const el=document.getElementById('clockStaffList');
    const colors=['#2D4A2D','#4A7C59','#0C447C','#633806','#3C3489'];
    el.innerHTML=clockStaffList.map((s,i)=>`
      <div class="staff-item" onclick="clockSelectStaff(${s.id},'${s.name}')">
        <div class="s-avatar" style="background:${colors[i%colors.length]};">${s.name.split(' ').map(p=>p[0]).join('').substring(0,2).toUpperCase()}</div>
        <div><div class="s-name">${s.name}</div><div class="s-role">${s.role?.charAt(0)+s.role?.slice(1).toLowerCase()}</div></div>
      </div>`).join('');
  }catch(e){document.getElementById('clockStaffList').innerHTML='<div class="error-msg" style="display:block;">Cannot load staff.</div>';}
}

function clockSelectStaff(id,name){
  clockSelectedStaff={id,name};
  document.getElementById('clockStep1').style.display='none';
  document.getElementById('clockStep2').style.display='block';
  document.getElementById('clockSelectedName').textContent=name;
  clockPinVal=''; updateClockPin();
}

function clockBackToStep1(){
  document.getElementById('clockStep1').style.display='block';
  document.getElementById('clockStep2').style.display='none';
  clockSelectedStaff=null; clockPinVal='';
}

function clockPin(n){if(clockPinVal.length>=4)return;clockPinVal+=n;updateClockPin();}
function clockDel(){clockPinVal=clockPinVal.slice(0,-1);updateClockPin();}
function clockClear(){clockPinVal='';updateClockPin();}
function updateClockPin(){
  for(let i=0;i<4;i++){const d=document.getElementById(`cp${i}`);if(d)d.className='pin-dot'+(i<clockPinVal.length?' filled':'');}
}

async function doClockIn(){
  if(!clockSelectedStaff||clockPinVal.length<4){showClockMsg('Please enter your 4-digit PIN.','err');return;}
  try{
    const res=await fetch(`${API}/api/clock/in`,{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({staffId:clockSelectedStaff.id,pin:clockPinVal})});
    const data=await res.json();
    if(res.ok){showClockMsg('✓ '+data.message,'ok');clockPinVal='';updateClockPin();loadAttendance();setTimeout(clockBackToStep1,2000);}
    else showClockMsg(data.error||'Failed.','err');
  }catch(e){showClockMsg('Cannot connect to server.','err');}
}

async function doClockOut(){
  if(!clockSelectedStaff||clockPinVal.length<4){showClockMsg('Please enter your 4-digit PIN.','err');return;}
  try{
    const res=await fetch(`${API}/api/clock/out`,{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({staffId:clockSelectedStaff.id,pin:clockPinVal})});
    const data=await res.json();
    if(res.ok){showClockMsg('✓ '+data.message+' ('+data.hoursWorked+'h)','ok');clockPinVal='';updateClockPin();loadAttendance();setTimeout(clockBackToStep1,2000);}
    else showClockMsg(data.error||'Failed.','err');
  }catch(e){showClockMsg('Cannot connect to server.','err');}
}

function showClockMsg(msg,type){
  const el=document.getElementById('clockMsg');
  el.textContent=msg;
  el.style.color=type==='ok'?'var(--mm)':'#791F1F';
}

async function loadAttendance(){
  try{
    const res=await fetch(`${API}/api/clock/today`);
    const records=await res.json();
    const el=document.getElementById('attendanceTable');
    if(!records.length){el.innerHTML='<div style="text-align:center;padding:20px;color:var(--text-secondary);font-size:12px;">No records yet today</div>';return;}
    el.innerHTML=records.map(r=>{
      const statusClass=r.clockOut?'pill-gray':r.breakStart&&!r.breakEnd?'pill-amber':'pill-green';
      const statusText=r.clockOut?'Out':r.breakStart&&!r.breakEnd?'Break':'In';
      return`<div class="attendance-row">
        <div style="font-size:13px;font-weight:500;">${r.staff?.name||'--'}</div>
        <div><span class="pill ${statusClass}" style="font-size:10px;">${statusText}</span></div>
        <div style="font-family:var(--font-mono);font-size:12px;">${r.clockIn?r.clockIn.substring(11,16):'--'}</div>
        <div style="font-size:12px;">${r.hoursWorked?r.hoursWorked+'h':'--'}</div>
      </div>`;
    }).join('');
  }catch(e){}
}

initClock();

const API='';
// ── Customer login ──────────────────────────────────────
async function login(){
  const email=document.getElementById('email').value.trim();
  const pass=document.getElementById('password').value;
  const btn=document.getElementById('loginBtn');
  const err=document.getElementById('errorAlert');
  if(!email||!pass){showError('Please fill in all fields.');return;}
  btn.textContent='Signing in...'; btn.disabled=true;
  try{
    const res=await fetch(`${API}/api/auth/login`,{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({email,password:pass})});
    const data=await res.json();
    if(res.ok){localStorage.setItem('mb_customer',JSON.stringify(data));window.location.href='menu.html';}
    else showError(data.error||'Invalid email or password.');
  }catch(e){showError('Cannot connect to server. Make sure Spring Boot is running.');}
  finally{btn.textContent='Sign in';btn.disabled=false;}
}
function showError(msg){const el=document.getElementById('errorAlert');el.textContent=msg;el.style.display='block';}
document.getElementById('password')?.addEventListener('keydown',e=>{if(e.key==='Enter')login();});

// ── Staff login (PIN) ─────────────────────────────────
let staffList=[];
let selectedStaff=null;
let staffPin='';

async function loadStaff(){
  try{
    const res=await fetch(`${API}/api/staff`);
    staffList=await res.json();
    renderStaffList();
  }catch(e){
    const el=document.getElementById('staffList');
    if(el) el.innerHTML='<div class="error-msg" style="display:block;">Cannot load staff list.</div>';
  }
}

function renderStaffList(){
  const el=document.getElementById('staffList');
  if(!el)return;
  const colors=['#2D4A2D','#4A7C59','#0C447C','#633806','#3C3489'];
  el.innerHTML=staffList.map((s,i)=>`
    <div class="staff-option" onclick="selectStaff(${s.id},'${s.name}','${s.role}')">
      <div style="width:36px;height:36px;border-radius:50%;background:${colors[i%colors.length]};color:white;display:flex;align-items:center;justify-content:center;font-size:12px;font-weight:500;flex-shrink:0;">${s.name.split(' ').map(p=>p[0]).join('').substring(0,2).toUpperCase()}</div>
      <div><div style="font-size:13px;font-weight:500;color:var(--text-primary);">${s.name}</div><div style="font-size:11px;color:var(--text-secondary);">${s.role.charAt(0)+s.role.slice(1).toLowerCase()}</div></div>
    </div>`).join('');
}

function selectStaff(id,name,role){
  selectedStaff={id,name,role};
  document.getElementById('step1').style.display='none';
  document.getElementById('step2').style.display='block';
  document.getElementById('selectedName').textContent=name;
  staffPin=''; updatePinDots();
}

function backToStep1(){
  document.getElementById('step1').style.display='block';
  document.getElementById('step2').style.display='none';
  selectedStaff=null; staffPin='';
}

function pinPress(n){if(staffPin.length>=4)return;staffPin+=n;updatePinDots();}
function pinDel(){staffPin=staffPin.slice(0,-1);updatePinDots();}
function pinClear(){staffPin='';updatePinDots();}
function updatePinDots(){
  for(let i=0;i<4;i++){const d=document.getElementById(`d${i}`);if(d)d.className='pin-dot'+(i<staffPin.length?' filled':'');}
}

async function staffLogin(){
  if(!selectedStaff||staffPin.length<4){alert('Please enter your 4-digit PIN.');return;}
  try{
    const res=await fetch(`${API}/api/auth/staff-login`,{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({staffId:selectedStaff.id,pin:staffPin})});
    const data=await res.json();
    if(res.ok){localStorage.setItem('mb_staff',JSON.stringify(data));window.location.href='pos.html';}
    else{const el=document.getElementById('errorAlert');el.textContent=data.error||'Invalid PIN.';el.style.display='block';staffPin='';updatePinDots();}
  }catch(e){alert('Cannot connect to server.');}
}

if(document.getElementById('staffList')) loadStaff();

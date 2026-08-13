const API='';
let currentFilter='all';
const staff=JSON.parse(localStorage.getItem('mb_staff')||'{}');

// Set staff info in topbar
if(staff.name){
  document.getElementById('staffAvatar').textContent=(staff.name.split(' ').map(p=>p[0]).join('').substring(0,2).toUpperCase());
  document.getElementById('staffLabel').textContent=staff.name+' · '+(staff.role?.charAt(0)+staff.role?.slice(1).toLowerCase());
}

function setFilter(f,btn){
  currentFilter=f;
  document.querySelectorAll('.filter-btn').forEach(b=>b.classList.remove('active'));
  btn.classList.add('active');
  loadOrders();
}

async function loadOrders(){
  try{
    const url=currentFilter==='all'?`${API}/api/orders/active`:`${API}/api/orders?status=${currentFilter}`;
    const res=await fetch(url);
    const orders=await res.json();
    renderOrders(orders);
  }catch(e){document.getElementById('ordersList').innerHTML='<div class="error-msg" style="display:block;">Cannot connect to API.</div>';}
}

async function loadStats(){
  try{
    const res=await fetch(`${API}/api/orders/stats/today`);
    const s=await res.json();
    document.getElementById('sTotalOrders').textContent=s.totalOrders||0;
    document.getElementById('sQueue').textContent=s.activeOrders||0;
    document.getElementById('sRevenue').textContent='RM '+(s.revenue||'0.00');
  }catch(e){}
}

function renderOrders(orders){
  const list=document.getElementById('ordersList');
  if(!orders.length){list.innerHTML='<div style="text-align:center;padding:40px;color:var(--text-secondary);">No orders in this view</div>';return;}
  list.innerHTML=orders.map(o=>{
    const pct=o.status==='NEW'?10:o.status==='PREPARING'?60:100;
    const waited=Math.round((Date.now()-new Date(o.createdAt).getTime())/60000);
    const wClass=waited<15?'wait-ok':waited<25?'wait-warn':'wait-late';
    const wLabel=waited+'m'+(waited>=25?' · urgent':waited>=15?' · slow':'');
    const btnClass=o.status==='NEW'?'status-btn btn-new':o.status==='PREPARING'?'status-btn btn-prep':'status-btn btn-done';
    const btnLabel=o.status==='NEW'?'Start preparing':o.status==='PREPARING'?'Mark ready':'Complete';
    return`<div class="order-card${waited>=25&&o.status!=='COMPLETED'?' urgent':''}">
      <div class="progress-bar"><div class="progress-fill" style="width:${pct}%"></div></div>
      <div class="oc-head">
        <span class="oc-num">${o.orderNumber}</span>
        <span class="pill ${o.orderType==='DINE_IN'?'pill-green':'pill-blue'}">${o.orderType==='DINE_IN'?'Dine in'+(o.tableNumber?' · '+o.tableNumber:''):'Takeaway'}</span>
        <span class="wait-pill ${wClass}">${wLabel}</span>
      </div>
      <div class="oc-items">
        ${(o.items||[]).map(i=>`<div class="oc-item"><span class="oc-qty">x${i.quantity}</span><span>${i.menuItemId}</span><span style="color:var(--text-secondary);font-size:12px;margin-left:6px;">${[i.size,i.milkType,i.temperature].filter(Boolean).join(' · ')}</span></div>`).join('')}
      </div>
      <div class="oc-foot">
        <span style="font-size:12px;color:var(--text-secondary);">Total: <strong style="color:var(--text-primary);">RM ${o.totalAmount}</strong> · ${o.payment?.method||''}</span>
        <button class="${btnClass}" onclick="advanceStatus(${o.id},'${o.status}')">${btnLabel}</button>
      </div>
    </div>`;
  }).join('');
}

async function advanceStatus(id,status){
  const next=status==='NEW'?'PREPARING':status==='PREPARING'?'READY':'COMPLETED';
  try{
    await fetch(`${API}/api/orders/${id}/status`,{method:'PATCH',headers:{'Content-Type':'application/json'},body:JSON.stringify({status:next})});
    loadOrders(); loadStats();
  }catch(e){alert('Failed to update order.');}
}

function updateClock(){
  const n=new Date();
  document.getElementById('posTime').textContent=n.getHours().toString().padStart(2,'0')+':'+n.getMinutes().toString().padStart(2,'0');
}

updateClock(); setInterval(updateClock,30000);
loadOrders(); loadStats();
setInterval(()=>{loadOrders();loadStats();},15000);

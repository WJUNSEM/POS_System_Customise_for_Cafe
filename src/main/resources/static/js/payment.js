const API='';
let selectedMethod='fpx';
const checkout=JSON.parse(localStorage.getItem('mb_checkout')||'{}');
const cart=checkout.cart||[];

function init(){
  if(!cart.length){window.location.href='menu.html';return;}
  document.getElementById('payTotal').textContent=`RM ${parseFloat(checkout.total||0).toFixed(2)}`;
  document.getElementById('payBtnTotal').textContent=`RM ${parseFloat(checkout.total||0).toFixed(2)}`;
  const mini=document.getElementById('miniSummary');
  mini.innerHTML=cart.map(i=>`
    <div class="order-mini">
      <span>${i.name} <span style="color:var(--text-secondary);font-size:12px;">x${i.qty}</span></span>
      <span style="font-weight:500;">RM ${(parseFloat(i.price)*i.qty).toFixed(2)}</span>
    </div>`).join('');
}

function selectPay(el,method){
  document.querySelectorAll('.pay-opt').forEach(o=>o.classList.remove('selected'));
  el.classList.add('selected');
  selectedMethod=method;
  document.getElementById('providerCard').style.display=method==='ewallet'?'block':'none';
}

async function placeOrder(){
  const btn=document.getElementById('payBtn');
  btn.textContent='Placing order...'; btn.disabled=true;
  const customer=JSON.parse(localStorage.getItem('mb_customer')||'null');
  const body={
    customerId:customer?.id||null,
    staffId:JSON.parse(localStorage.getItem('mb_staff')||'null')?.id||null,
    orderType:checkout.orderType||'dine_in',
    tableNumber:checkout.tableNumber||null,
    specialNotes:checkout.specialNotes||null,
    promoCode:checkout.promoCode||null,
    discountAmount:checkout.discountAmount||0,
    loyaltyUsed:checkout.loyaltyUsed||0,
    items:cart.map(i=>({menuItemId:i.id,quantity:i.qty,unitPrice:parseFloat(i.price),size:i.size||null,milkType:i.milk||null,temperature:i.temp||null,sweetnessLevel:i.sweetness||50}))
  };
  try{
    const res=await fetch(`${API}/api/orders`,{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify(body)});
    const data=await res.json();
    if(res.ok){
      const paymentRes=await fetch(`${API}/api/payments`,{
        method:'POST',
        headers:{'Content-Type':'application/json'},
        body:JSON.stringify({
          orderId:data.orderId,
          method:selectedMethod,
          provider:document.getElementById('provider')?.value||null,
          amount:parseFloat(checkout.total||0),
          transactionRef:'MB-'+Date.now()
        })
      });
      if(!paymentRes.ok){
        const paymentData=await paymentRes.json().catch(()=>({}));
        alert(paymentData.error||'Order created, but payment recording failed. Please contact staff.');
      }
      localStorage.removeItem('mb_cart');
      localStorage.removeItem('mb_checkout');
      localStorage.setItem('mb_lastOrder',data.orderNumber);
      document.getElementById('orderNumDisplay').textContent=data.orderNumber;
      document.getElementById('successOverlay').style.display='flex';
    }else{alert(data.error||'Failed to place order.');}
  }catch(e){alert('Cannot connect to server. Is Spring Boot running?');}
  finally{btn.textContent='Place order · '+document.getElementById('payBtnTotal').textContent;btn.disabled=false;}
}

init();

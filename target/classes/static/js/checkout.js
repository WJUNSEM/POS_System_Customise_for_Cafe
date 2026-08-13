const API = '';

let cart = JSON.parse(localStorage.getItem('mb_cart') || '[]');

let orderType = 'dine_in';
let promoDiscount = 0;
let loyaltyUsed = false;
let loyaltyAmt = 0;


/* =========================================================
   PRICE
   ========================================================= */

function getPrice(item) {

    const price = Number(
        item.price ??
        item.basePrice ??
        item.unitPrice
    );

    return Number.isFinite(price)
        ? price
        : 0;
}


/* =========================================================
   SAVE CART
   ========================================================= */

function saveCart() {

    localStorage.setItem(
        'mb_cart',
        JSON.stringify(cart)
    );
}


/* =========================================================
   INCREASE QUANTITY
   ========================================================= */

function increaseQty(index) {

    if (!cart[index]) return;

    cart[index].qty =
        (Number(cart[index].qty) || 1) + 1;

    saveCart();

    renderCart();
}


/* =========================================================
   DECREASE QUANTITY
   ========================================================= */

function decreaseQty(index) {

    if (!cart[index]) return;

    const currentQty =
        Number(cart[index].qty) || 1;

    if (currentQty > 1) {

        cart[index].qty =
            currentQty - 1;

    } else {

        // Quantity reaches 0 → remove item
        cart.splice(index, 1);
    }

    saveCart();

    renderCart();
}


/* =========================================================
   REMOVE ITEM
   ========================================================= */

function removeItem(index) {

    if (!cart[index]) return;

    cart.splice(index, 1);

    saveCart();

    renderCart();
}


/* =========================================================
   TOTAL CALCULATION
   ========================================================= */

function calcTotals() {

    const sub = cart.reduce(
        (sum, item) => {

            const price =
                getPrice(item);

            const qty =
                Number(item.qty) || 0;

            return sum + price * qty;

        },
        0
    );


    const svc =
        Number(
            (sub * 0.06).toFixed(2)
        );


    const disc =
        Number(promoDiscount) || 0;


    const loyal =
        loyaltyUsed
            ? Number(loyaltyAmt) || 0
            : 0;


    const total =
        Math.max(
            0,
            Number(
                (
                    sub +
                    svc -
                    disc -
                    loyal
                ).toFixed(2)
            )
        );


    return {
        sub,
        svc,
        disc,
        loyal,
        total
    };
}


/* =========================================================
   RENDER CART
   ========================================================= */

function renderCart() {

    const grid =
        document.getElementById(
            'orderItems'
        );


    const emptyCart =
        document.getElementById(
            'emptyCart'
        );


    const checkoutContent =
        document.getElementById(
            'checkoutContent'
        );


    if (!cart.length) {

        emptyCart.style.display =
            'block';

        checkoutContent.style.display =
            'none';

        updateSummary();

        return;
    }


    emptyCart.style.display =
        'none';

    checkoutContent.style.display =
        'block';


    grid.innerHTML =
        cart.map((item, index) => {

            const price =
                getPrice(item);

            const qty =
                Number(item.qty) || 1;


            const itemTotal =
                (
                    price * qty
                ).toFixed(2);


            return `

                <div class="order-row">

                    <div class="order-icon">
                        🍵
                    </div>


                    <div class="order-info">

                        <div class="order-name">

                            ${item.name || 'Unknown Item'}

                        </div>


                        <div class="order-opts">
                            ${item.opts || ''}
                        </div>


                        <div class="cart-edit">

                            <div class="qty-control">

                                <button
                                    class="qty-btn"
                                    onclick="decreaseQty(${index})"
                                    type="button"
                                >
                                    −
                                </button>


                                <span class="qty-number">
                                    ${qty}
                                </span>


                                <button
                                    class="qty-btn"
                                    onclick="increaseQty(${index})"
                                    type="button"
                                >
                                    +
                                </button>

                            </div>


                            <button
                                class="remove-btn"
                                onclick="removeItem(${index})"
                                type="button"
                            >
                                Remove
                            </button>

                        </div>

                    </div>


                    <div class="order-price">
                        RM ${itemTotal}
                    </div>

                </div>

            `;

        }).join('');


    updateSummary();
}


/* =========================================================
   UPDATE SUMMARY
   ========================================================= */

function updateSummary() {

    const {
        sub,
        svc,
        disc,
        loyal,
        total
    } = calcTotals();


    document.getElementById(
        'subtotalVal'
    ).textContent =
        `RM ${sub.toFixed(2)}`;


    document.getElementById(
        'svcVal'
    ).textContent =
        `RM ${svc.toFixed(2)}`;


    document.getElementById(
        'discRow'
    ).style.display =
        disc > 0
            ? 'flex'
            : 'none';


    document.getElementById(
        'discVal'
    ).textContent =
        `-RM ${disc.toFixed(2)}`;


    document.getElementById(
        'loyalRow'
    ).style.display =
        loyal > 0
            ? 'flex'
            : 'none';


    document.getElementById(
        'loyalVal2'
    ).textContent =
        `-RM ${loyal.toFixed(2)}`;


    document.getElementById(
        'totalVal'
    ).textContent =
        `RM ${total.toFixed(2)}`;


    document.getElementById(
        'checkoutTotal'
    ).textContent =
        `RM ${total.toFixed(2)}`;
}


/* =========================================================
   ORDER TYPE
   ========================================================= */

function selectType(type) {

    orderType = type;


    document.getElementById(
        'dineIn'
    ).classList.toggle(
        'selected',
        type === 'dine_in'
    );


    document.getElementById(
        'takeaway'
    ).classList.toggle(
        'selected',
        type === 'takeaway'
    );


    document.getElementById(
        'tableInput'
    ).style.display =
        type === 'dine_in'
            ? 'block'
            : 'none';
}


/* =========================================================
   PROMO
   ========================================================= */

const PROMO_CODES = {

    MATCHA10: 0.10,

    WELCOME: 0.15,

    MB5: 0.05

};


function applyPromo() {

    const code =
        document.getElementById(
            'promoInput'
        ).value
        .trim()
        .toUpperCase();


    const msg =
        document.getElementById(
            'promoMsg'
        );


    const {
        sub
    } = calcTotals();


    if (PROMO_CODES[code]) {

        promoDiscount =
            Number(
                (
                    sub *
                    PROMO_CODES[code]
                ).toFixed(2)
            );


        msg.className =
            'promo-msg promo-ok';


        msg.textContent =
            `✓ ${
                Math.round(
                    PROMO_CODES[code] * 100
                )
            }% discount applied! ` +
            `(-RM ${
                promoDiscount.toFixed(2)
            })`;

    } else {

        promoDiscount = 0;


        msg.className =
            'promo-msg promo-err';


        msg.textContent =
            'Invalid code. Try: MATCHA10, WELCOME, or MB5';
    }


    updateSummary();
}


/* =========================================================
   LOYALTY
   ========================================================= */

function togglePoints() {

    loyaltyUsed =
        !loyaltyUsed;


    const btn =
        document.getElementById(
            'ptsBtn'
        );


    btn.textContent =
        loyaltyUsed
            ? 'Remove'
            : 'Use';


    btn.classList.toggle(
        'used',
        loyaltyUsed
    );


    updateSummary();
}


/* =========================================================
   PROCEED TO PAYMENT
   ========================================================= */

function proceedToPayment() {

    if (!cart.length) {

        alert(
            'Your cart is empty.'
        );

        return;
    }


    const {
        total,
        sub,
        svc,
        disc,
        loyal
    } = calcTotals();


    const tableNum =
        document.getElementById(
            'tableNumber'
        )?.value || '';


    const notes =
        document.getElementById(
            'specialNotes'
        ).value;


    if (
        orderType === 'dine_in' &&
        !tableNum
    ) {

        alert(
            'Please enter your table number.'
        );

        return;
    }


    const checkoutData = {

        cart,

        orderType,

        tableNumber:
            tableNum,

        specialNotes:
            notes,

        promoCode:
            document.getElementById(
                'promoInput'
            ).value
            .trim()
            .toUpperCase() || null,

        discountAmount:
            disc,

        loyaltyUsed:
            loyal,

        subtotal:
            sub,

        serviceCharge:
            svc,

        total:
            total
    };


    localStorage.setItem(
        'mb_checkout',
        JSON.stringify(
            checkoutData
        )
    );


    window.location.href =
        'payment.html';
}


/* =========================================================
   LOYALTY CUSTOMER
   ========================================================= */

const customer =
    JSON.parse(
        localStorage.getItem(
            'mb_customer'
        ) || 'null'
    );


if (
    customer &&
    customer.loyaltyPoints > 0
) {

    loyaltyAmt =
        Number(
            (
                customer.loyaltyPoints *
                0.5
            ).toFixed(2)
        );


    document.getElementById(
        'loyaltyBanner'
    ).style.display =
        'flex';


    document.getElementById(
        'loyaltyPts'
    ).textContent =
        customer.loyaltyPoints;


    document.getElementById(
        'loyaltyVal'
    ).textContent =
        `RM ${loyaltyAmt.toFixed(2)}`;
}


/* =========================================================
   START
   ========================================================= */

renderCart();

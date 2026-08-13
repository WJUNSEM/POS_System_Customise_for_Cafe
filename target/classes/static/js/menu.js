const API = '';

/* =========================================================
   CART
   ========================================================= */

let cart = loadCart();


/* =========================================================
   LOAD CART FROM LOCAL STORAGE
   ========================================================= */

function loadCart() {

    try {

        const saved =
            JSON.parse(
                localStorage.getItem('mb_cart') || '[]'
            );

        if (!Array.isArray(saved)) {
            return [];
        }

        return saved
            .map(item => {

                const price =
                    Number(
                        item.price ??
                        item.basePrice ??
                        item.unitPrice
                    );

                const qty =
                    Number(item.qty) || 1;

                return {
                    id: item.id,
                    name: item.name || 'Unknown Item',
                    price: Number.isFinite(price)
                        ? price
                        : 0,
                    qty: Math.max(1, qty),
                    opts: item.opts || ''
                };

            })
            .filter(item => item.id != null);

    } catch (error) {

        console.error(
            'Unable to load cart:',
            error
        );

        return [];
    }
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
   MENU LOADING
   ========================================================= */

async function loadMenu(category = 'all', btn = null) {

    /*
     * Update active category button
     */
    document
        .querySelectorAll('.cat')
        .forEach(button => {
            button.classList.remove('active');
        });


    if (btn) {
        btn.classList.add('active');
    }


    /*
     * Build API URL
     */
    const url =
        category === 'all'
            ? `${API}/api/menu`
            : `${API}/api/menu/category/${category}`;


    /*
     * UI elements
     */
    const loadingMsg =
        document.getElementById(
            'loadingMsg'
        );

    const errorMsg =
        document.getElementById(
            'errorMsg'
        );

    const menuGrid =
        document.getElementById(
            'menuGrid'
        );


    /*
     * Show loading
     */
    if (loadingMsg) {
        loadingMsg.style.display = 'flex';
    }

    if (errorMsg) {
        errorMsg.style.display = 'none';
    }

    if (menuGrid) {
        menuGrid.innerHTML = '';
    }


    try {

        const response =
            await fetch(url);


        /*
         * Handle HTTP errors
         */
        if (!response.ok) {

            let errorText =
                `Server returned ${response.status}`;

            try {

                const body =
                    await response.text();

                if (body) {
                    errorText += `: ${body}`;
                }

            } catch (e) {
                // Ignore response parsing error
            }

            throw new Error(errorText);
        }


        const items =
            await response.json();


        /*
         * Make sure API returned an array
         */
        if (!Array.isArray(items)) {

            throw new Error(
                'Invalid menu response from server.'
            );
        }


        renderMenu(items);

    } catch (error) {

        console.error(
            'Menu API error:',
            error
        );


        if (errorMsg) {

            errorMsg.style.display =
                'block';

            errorMsg.textContent =
                'Unable to load menu. ' +
                'Please check that the server and database are running.';
        }

    } finally {

        if (loadingMsg) {
            loadingMsg.style.display = 'none';
        }
    }
}


/* =========================================================
   ESCAPE HTML
   ========================================================= */

function escapeHtml(value) {

    if (value === null || value === undefined) {
        return '';
    }

    return String(value)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#039;');
}


/* =========================================================
   GET MENU ITEM PRICE
   ========================================================= */

function getMenuPrice(item) {

    const price =
        Number(
            item.basePrice ??
            item.price ??
            item.unitPrice
        );


    if (!Number.isFinite(price)) {
        return null;
    }


    return price;
}


/* =========================================================
   RENDER MENU
   ========================================================= */

function renderMenu(items) {

    const grid =
        document.getElementById(
            'menuGrid'
        );


    if (!grid) {
        return;
    }


    /*
     * Empty category
     */
    if (!items.length) {

        grid.innerHTML =
            '<div class="empty-msg">' +
            'No items in this category' +
            '</div>';

        return;
    }


    /*
     * Create menu cards
     */
    grid.innerHTML =
        items.map((item, index) => {

            const price =
                getMenuPrice(item);


            const priceText =
                price === null
                    ? 'Price unavailable'
                    : `RM ${price.toFixed(2)}`;


            const popular =
                item.popular ||
                item.isPopular;


            return `

                <div class="menu-card">

                    ${
                        popular
                            ? '<span class="popular-badge">Popular</span>'
                            : ''
                    }


                    <div class="card-icon">
                        🍵
                    </div>


                    <div class="card-name">
                        ${escapeHtml(item.name)}
                    </div>


                    <div class="card-desc">
                        ${escapeHtml(
                            item.description || ''
                        )}
                    </div>


                    <div class="card-footer">

                        <span class="card-price">
                            ${priceText}
                        </span>


                        <button
                            class="add-btn"
                            type="button"
                            data-menu-index="${index}"
                        >
                            +
                        </button>

                    </div>

                </div>

            `;

        }).join('');


    /*
     * Attach click events
     *
     * This is safer than putting the entire
     * JSON object directly inside onclick="..."
     */
    grid
        .querySelectorAll(
            '.add-btn'
        )
        .forEach(button => {

            button.addEventListener(
                'click',
                function () {

                    const index =
                        Number(
                            this.dataset.menuIndex
                        );


                    const item =
                        items[index];


                    if (item) {
                        addToCart(item);
                    }

                }
            );

        });
}


/* =========================================================
   ADD TO CART
   ========================================================= */

function addToCart(item) {

    /*
     * Validate item
     */
    if (!item || item.id == null) {

        console.error(
            'Invalid menu item:',
            item
        );

        alert(
            'Unable to add this item to cart.'
        );

        return;
    }


    /*
     * Get price
     */
    const price =
        getMenuPrice(item);


    /*
     * Prevent NaN from entering cart
     */
    if (price === null) {

        console.error(
            'Invalid menu item price:',
            item
        );

        alert(
            'This menu item does not have a valid price.'
        );

        return;
    }


    /*
     * Item options
     *
     * If you later add size/add-ons,
     * items with different options can
     * remain separate.
     */
    const opts =
        item.opts || '';


    /*
     * Find an existing identical item
     */
    const existingIndex =
        cart.findIndex(cartItem => {

            return (
                String(cartItem.id) ===
                String(item.id)
                &&
                (cartItem.opts || '') ===
                opts
            );

        });


    if (existingIndex !== -1) {

        /*
         * Item already exists:
         * increase quantity
         */
        cart[existingIndex].qty =
            (
                Number(
                    cart[existingIndex].qty
                ) || 1
            ) + 1;


        /*
         * Make sure price is current
         */
        cart[existingIndex].price =
            price;

    } else {

        /*
         * New cart item
         */
        cart.push({

            id:
                item.id,

            name:
                item.name || 'Unknown Item',

            price:
                price,

            qty:
                1,

            opts:
                opts

        });

    }


    /*
     * Save
     */
    saveCart();


    /*
     * Update cart bar
     */
    updateCartBar();


    /*
     * Optional visual feedback
     */
    showAddedMessage(
        item.name
    );
}


/* =========================================================
   UPDATE CART BAR
   ========================================================= */

function updateCartBar() {

    /*
     * Total quantity
     *
     * Example:
     * Matcha Latte x3
     * Avocado Toast x2
     *
     * Cart quantity = 5
     */
    const totalQty =
        cart.reduce(
            (sum, item) => {

                return (
                    sum +
                    (
                        Number(item.qty) || 0
                    )
                );

            },
            0
        );


    /*
     * Total price
     */
    const total =
        cart.reduce(
            (sum, item) => {

                const price =
                    Number(item.price) || 0;

                const qty =
                    Number(item.qty) || 0;


                return (
                    sum +
                    price * qty
                );

            },
            0
        );


    /*
     * Cart count at top
     */
    const cartCount =
        document.getElementById(
            'cartCount'
        );


    if (cartCount) {

        cartCount.textContent =
            totalQty;
    }


    /*
     * Cart bubble
     */
    const cartQty =
        document.getElementById(
            'cartQty'
        );


    if (cartQty) {

        cartQty.textContent =
            totalQty;
    }


    /*
     * Cart total
     */
    const cartTotal =
        document.getElementById(
            'cartTotal'
        );


    if (cartTotal) {

        cartTotal.textContent =
            'RM ' +
            total.toFixed(2);
    }


    /*
     * Cart item label
     */
    const cartItems =
        document.getElementById(
            'cartItems'
        );


    if (cartItems) {

        if (totalQty === 0) {

            cartItems.textContent =
                'No items yet';

        } else if (totalQty === 1) {

            cartItems.textContent =
                cart[0].name;

        } else {

            cartItems.textContent =
                `${totalQty} items`;
        }
    }


    /*
     * Show / hide cart bar
     */
    const cartBar =
        document.getElementById(
            'cartBar'
        );


    if (cartBar) {

        cartBar.style.display =
            totalQty > 0
                ? 'flex'
                : 'none';
    }
}


/* =========================================================
   SHOW "ADDED TO CART" MESSAGE
   ========================================================= */

function showAddedMessage(itemName) {

    /*
     * Use existing message if available
     */
    let message =
        document.getElementById(
            'cartAddedMessage'
        );


    /*
     * Create message if it doesn't exist
     */
    if (!message) {

        message =
            document.createElement(
                'div'
            );


        message.id =
            'cartAddedMessage';


        message.style.position =
            'fixed';

        message.style.bottom =
            '100px';

        message.style.right =
            '20px';

        message.style.padding =
            '10px 16px';

        message.style.borderRadius =
            '8px';

        message.style.background =
            '#355E3B';

        message.style.color =
            'white';

        message.style.fontSize =
            '13px';

        message.style.zIndex =
            '9999';

        message.style.boxShadow =
            '0 4px 12px rgba(0,0,0,0.15)';


        document.body.appendChild(
            message
        );
    }


    message.textContent =
        `${itemName} added to cart`;


    message.style.display =
        'block';


    clearTimeout(
        window.cartMessageTimer
    );


    window.cartMessageTimer =
        setTimeout(
            () => {

                message.style.display =
                    'none';

            },
            1500
        );
}


/* =========================================================
   CLEAR CART
   ========================================================= */

function clearCart() {

    cart = [];

    saveCart();

    updateCartBar();
}


/* =========================================================
   OPEN CART
   ========================================================= */

function openCart() {

    window.location.href =
        'checkout.html';
}


/* =========================================================
   INITIALISE
   ========================================================= */

updateCartBar();

loadMenu('all');

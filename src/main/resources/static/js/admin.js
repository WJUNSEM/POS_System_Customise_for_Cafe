function adminLogin() {
    const username = document.getElementById('username').value.trim();
    const password = document.getElementById('password').value;

    fetch('/api/admin/login', {
        method: 'POST',
        headers: {'Content-Type':'application/json'},
        body: JSON.stringify({username, password})
    })
    .then(async r => {
        const data = await r.json();
        if (!r.ok || !data.success) throw new Error(data.error || 'Login failed');
        localStorage.setItem('mb_admin_logged_in', 'true');
        localStorage.setItem('mb_admin_username', data.username);
        location.href = 'admin-dashboard.html';
    })
    .catch(err => {
        const box = document.getElementById('error');
        if (box) {
            box.style.display = 'block';
            box.textContent = err.message;
        } else {
            alert(err.message);
        }
    });
}

function requireAdmin() {
    if (localStorage.getItem('mb_admin_logged_in') !== 'true') {
        location.href = 'admin-login.html';
    }
}

function adminLogout() {
    localStorage.removeItem('mb_admin_logged_in');
    localStorage.removeItem('mb_admin_username');
    location.href = 'admin-login.html';
}

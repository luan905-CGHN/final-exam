// Lấy token từ cookie (sẽ lưu sau khi đăng nhập)
function getToken() {
    return document.cookie
        .split('; ')
        .find(row => row.startsWith('token='))
        ?.split('=')[1];
}

// Format số tiền VNĐ: 500000 → 500.000 ₫
function formatCurrency(amount) {
    return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND'
    }).format(amount);
}

// Format ngày: 2026-05-15T10:00:00 → 15/05/2026 10:00
function formatDate(dateStr) {
    const date = new Date(dateStr);
    return date.toLocaleString('vi-VN');
}

// Gọi API với token tự động
async function apiCall(url, method = 'GET', body = null) {
    const token = getToken();
    const options = {
        method,
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
        }
    };
    if (body) options.body = JSON.stringify(body);

    const response = await fetch(url, options);
    return response.json();
}

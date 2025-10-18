(function () {
    if (!window.DASHBOARD) return;

    const { revenue, category } = window.DASHBOARD;

    // Revenue (line)
    const revCtx = document.getElementById('revenueChart');
    if (revCtx) {
        new Chart(revCtx, {
            type: 'line',
            data: {
                labels: revenue.labels,
                datasets: [{
                    label: 'Revenue',
                    data: revenue.values,
                    fill: true,
                    tension: 0.35
                }]
            },
            options: {
                plugins: { legend: { display: false } },
                scales: { y: { beginAtZero: true } }
            }
        });
    }

    // Sales by Category (doughnut)
    const catCtx = document.getElementById('categoryChart');
    if (catCtx) {
        new Chart(catCtx, {
            type: 'doughnut',
            data: { labels: category.labels, datasets: [{ data: category.values }] },
            options: { plugins: { legend: { position: 'right' } } }
        });
    }
})();
(function () {
    if (!window.DASHBOARD) return;

    const { revenue, category } = window.DASHBOARD;

    // Revenue (line)
    const revCtx = document.getElementById('revenueChart');
    if (revCtx) {
        new Chart(revCtx, {
            type: 'line',
            data: {
                labels: revenue.labels,
                datasets: [{
                    label: 'Revenue',
                    data: revenue.values,
                    fill: true,
                    tension: 0.35
                }]
            },
            options: {
                plugins: { legend: { display: false } },
                scales: { y: { beginAtZero: true } }
            }
        });
    }

    // Sales by Category (doughnut)
    const catCtx = document.getElementById('categoryChart');
    if (catCtx) {
        new Chart(catCtx, {
            type: 'doughnut',
            data: { labels: category.labels, datasets: [{ data: category.values }] },
            options: { plugins: { legend: { position: 'right' } } }
        });
    }
})();

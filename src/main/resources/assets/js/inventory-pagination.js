/**
 * Inventory Pagination Script (Simplified Version)
 */

(function() {
    'use strict';
    
    const ITEMS_PER_PAGE = 10;
    let currentPage = 1;
    let totalPages = 1;
    let filteredRows = [];
    
    // Wait for DOM to be ready
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }
    
    function init() {
        setTimeout(() => {
            const tbody = document.querySelector('tbody');
            if (!tbody) return;
            
            const rows = tbody.querySelectorAll('tr');
            if (rows.length === 0) return;
            
            filteredRows = Array.from(rows);
            totalPages = Math.ceil(filteredRows.length / ITEMS_PER_PAGE);
            
            createPagination();
            goToPage(1);
            
            window.refreshPagination = refreshPagination;
        }, 300);
    }
    
    function createPagination() {
        const table = document.querySelector('.table-responsive');
        if (!table) return;
        
        const card = table.closest('.card');
        if (!card) return;
        
        const existing = document.getElementById('paginationWrapper');
        if (existing) existing.remove();
        
        // Create pagination wrapper
        const wrapper = document.createElement('div');
        wrapper.id = 'paginationWrapper';
        wrapper.className = 'card-footer bg-light';
        
        wrapper.innerHTML = `
            <div class="row align-items-center">
                <div class="col-md-6 mb-2 mb-md-0">
                    <small class="text-muted">
                        Hiển thị <strong id="pageStart">1</strong> - <strong id="pageEnd">10</strong> 
                        trong tổng số <strong id="totalRows">${filteredRows.length}</strong> sản phẩm
                    </small>
                </div>
                <div class="col-md-6">
                    <nav>
                        <ul class="pagination pagination-sm justify-content-end mb-0" id="paginationButtons">
                            <!-- Buttons will be inserted here -->
                        </ul>
                    </nav>
                </div>
            </div>
        `;
        
        card.appendChild(wrapper);
        renderButtons();
    }
    
    function renderButtons() {
        const container = document.getElementById('paginationButtons');
        if (!container) return;
        
        let html = '';
        
        // Previous button
        html += `
            <li class="page-item ${currentPage === 1 ? 'disabled' : ''}">
                <a class="page-link" href="#" onclick="window.paginationGoToPage(${currentPage - 1}); return false;">
                    &laquo;
                </a>
            </li>
        `;
        
        // Page numbers
        const maxButtons = 5;
        let startPage = Math.max(1, currentPage - 2);
        let endPage = Math.min(totalPages, startPage + maxButtons - 1);
        
        if (endPage - startPage < maxButtons - 1) {
            startPage = Math.max(1, endPage - maxButtons + 1);
        }
        
        // First page
        if (startPage > 1) {
            html += `
                <li class="page-item">
                    <a class="page-link" href="#" onclick="window.paginationGoToPage(1); return false;">1</a>
                </li>
            `;
            if (startPage > 2) {
                html += `<li class="page-item disabled"><span class="page-link">...</span></li>`;
            }
        }
        
        // Middle pages
        for (let i = startPage; i <= endPage; i++) {
            html += `
                <li class="page-item ${i === currentPage ? 'active' : ''}">
                    <a class="page-link" href="#" onclick="window.paginationGoToPage(${i}); return false;">${i}</a>
                </li>
            `;
        }
        
        // Last page
        if (endPage < totalPages) {
            if (endPage < totalPages - 1) {
                html += `<li class="page-item disabled"><span class="page-link">...</span></li>`;
            }
            html += `
                <li class="page-item">
                    <a class="page-link" href="#" onclick="window.paginationGoToPage(${totalPages}); return false;">${totalPages}</a>
                </li>
            `;
        }
        
        // Next button
        html += `
            <li class="page-item ${currentPage === totalPages ? 'disabled' : ''}">
                <a class="page-link" href="#" onclick="window.paginationGoToPage(${currentPage + 1}); return false;">
                    &raquo;
                </a>
            </li>
        `;
        
        container.innerHTML = html;
    }
    
    function goToPage(page) {
        if (page < 1 || page > totalPages) return;
        
        currentPage = page;
        
        // Hide all rows first
        filteredRows.forEach(row => row.style.display = 'none');
        
        // Show rows for current page
        const start = (page - 1) * ITEMS_PER_PAGE;
        const end = Math.min(start + ITEMS_PER_PAGE, filteredRows.length);
        
        for (let i = start; i < end; i++) {
            filteredRows[i].style.display = '';
        }
        
        // Update info
        document.getElementById('pageStart').textContent = start + 1;
        document.getElementById('pageEnd').textContent = end;
        document.getElementById('totalRows').textContent = filteredRows.length;
        
        // Update buttons
        renderButtons();
        
        // Re-attach delete button listeners for visible rows
        if (typeof window.attachDeleteListeners === 'function') {
            window.attachDeleteListeners();
        }
        
        // Scroll to top of table
        const card = document.querySelector('.card:has(.table-responsive)');
        if (card) {
            card.scrollIntoView({ behavior: 'smooth', block: 'start' });
        }
    }
    
    function refreshPagination() {
        const tbody = document.querySelector('tbody');
        if (!tbody) return;
        
        const allRows = tbody.querySelectorAll('tr');
        filteredRows = Array.from(allRows).filter(row => {
            const display = window.getComputedStyle(row).display;
            return display !== 'none';
        });
        
        totalPages = Math.ceil(filteredRows.length / ITEMS_PER_PAGE);
        if (totalPages === 0) totalPages = 1;
        
        currentPage = 1;
        
        const wrapper = document.getElementById('paginationWrapper');
        if (filteredRows.length === 0) {
            if (wrapper) wrapper.style.display = 'none';
        } else {
            if (wrapper) wrapper.style.display = '';
            goToPage(1);
        }
    }
    
    // Expose goToPage globally
    window.paginationGoToPage = goToPage;
    window.refreshPagination = refreshPagination;
    
})();


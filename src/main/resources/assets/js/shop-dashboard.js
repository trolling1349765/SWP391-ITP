// Shop Dashboard JavaScript

(function () {
    'use strict';
    
    // Initialize dashboard when DOM is loaded
    document.addEventListener('DOMContentLoaded', function() {
        initializeDashboard();
    });
    
    function initializeDashboard() {
        // Initialize tooltips
        var tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
        var tooltipList = tooltipTriggerList.map(function (tooltipTriggerEl) {
            return new bootstrap.Tooltip(tooltipTriggerEl);
        });
        
        // Initialize pagination
        initializePagination();
        
        // Initialize status change handlers
        initializeStatusChangeHandlers();
        
        // Initialize search and filter
        initializeSearchAndFilter();
    }
    
    function initializePagination() {
        const pageSizeSelect = document.getElementById('pageSizeSelect');
        if (pageSizeSelect) {
            pageSizeSelect.addEventListener('change', function() {
                const currentUrl = new URL(window.location);
                currentUrl.searchParams.set('size', this.value);
                currentUrl.searchParams.set('page', '1'); // Reset to first page
                window.location.href = currentUrl.toString();
            });
        }
    }
    
    function initializeStatusChangeHandlers() {
        const statusForms = document.querySelectorAll('form[action*="/status"]');
        statusForms.forEach(form => {
            const select = form.querySelector('select[name="status"]');
            if (select) {
                select.addEventListener('change', function() {
                    // Show confirmation
                    const productId = form.action.match(/\/products\/(\d+)\/status/)[1];
                    const newStatus = this.value;
                    const productName = this.closest('tr').querySelector('td:nth-child(2)').textContent.trim();
                    
                    if (confirm(`Bạn có chắc chắn muốn đổi trạng thái sản phẩm "${productName}" sang ${newStatus}?`)) {
                        form.submit();
                    } else {
                        // Reset to original value
                        this.value = this.dataset.originalValue || 'HIDDEN';
                    }
                });
                
                // Store original value
                select.dataset.originalValue = select.value;
            }
        });
    }
    
    function initializeSearchAndFilter() {
        const searchInput = document.getElementById('searchInput');
        if (searchInput) {
            searchInput.addEventListener('input', debounce(function() {
                filterProducts();
            }, 300));
        }
        
        const filterSelects = document.querySelectorAll('.filter-select');
        filterSelects.forEach(select => {
            select.addEventListener('change', function() {
                filterProducts();
            });
        });
    }
    
    function filterProducts() {
        const searchTerm = document.getElementById('searchInput')?.value.toLowerCase() || '';
        const statusFilter = document.getElementById('statusFilter')?.value || '';
        const categoryFilter = document.getElementById('categoryFilter')?.value || '';
        
        const rows = document.querySelectorAll('#productsTable tbody tr');
        
        rows.forEach(row => {
            const productName = row.querySelector('td:nth-child(2)')?.textContent.toLowerCase() || '';
            const productStatus = row.querySelector('td:nth-child(6) .badge')?.textContent.trim() || '';
            const productCategory = row.querySelector('td:nth-child(3)')?.textContent.toLowerCase() || '';
            
            let showRow = true;
            
            // Search filter
            if (searchTerm && !productName.includes(searchTerm)) {
                showRow = false;
            }
            
            // Status filter
            if (statusFilter && productStatus !== statusFilter) {
                showRow = false;
            }
            
            // Category filter
            if (categoryFilter && !productCategory.includes(categoryFilter.toLowerCase())) {
                showRow = false;
            }
            
            row.style.display = showRow ? '' : 'none';
        });
        
        // Update visible count
        updateVisibleCount();
    }
    
    function updateVisibleCount() {
        const visibleRows = document.querySelectorAll('#productsTable tbody tr[style=""]').length;
        const totalRows = document.querySelectorAll('#productsTable tbody tr').length;
        
        const countElement = document.getElementById('visibleCount');
        if (countElement) {
            countElement.textContent = `${visibleRows}/${totalRows}`;
        }
    }
    
    // Utility function for debouncing
    function debounce(func, wait) {
        let timeout;
        return function executedFunction(...args) {
            const later = () => {
                clearTimeout(timeout);
                func(...args);
            };
            clearTimeout(timeout);
            timeout = setTimeout(later, wait);
        };
    }
    
    // Export functions to global scope
    window.DashboardUtils = {
        filterProducts: filterProducts,
        updateVisibleCount: updateVisibleCount
    };
    
})();

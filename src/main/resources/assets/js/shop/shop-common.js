// Shop Common JavaScript Functions

// Global variables
let currentPage = 1;
let itemsPerPage = 10;
let currentFilter = '';

// Initialize shop pages
document.addEventListener('DOMContentLoaded', function() {
    initializeShopPage();
});

// Initialize shop page based on current page
function initializeShopPage() {
    const currentPath = window.location.pathname;
    
    if (currentPath.includes('/dashboard')) {
        initializeDashboard();
    } else if (currentPath.includes('/inventory')) {
        initializeInventory();
    } else if (currentPath.includes('/ProductDetail')) {
        initializeProductDetail();
    } else if (currentPath.includes('/updateProduct')) {
        initializeUpdateProduct();
    }
}

// Dashboard specific functions
function initializeDashboard() {
    // Initialize dashboard specific functionality
    console.log('Dashboard initialized');
    
    // Add any dashboard-specific initialization here
    initializeDashboardCards();
    initializeDashboardCharts();
}

function initializeDashboardCards() {
    // Animate dashboard cards on load
    const cards = document.querySelectorAll('.card');
    cards.forEach((card, index) => {
        card.style.animationDelay = `${index * 0.1}s`;
        card.classList.add('fade-in');
    });
}

function initializeDashboardCharts() {
    // Initialize any charts if present
    const chartElements = document.querySelectorAll('[data-chart]');
    chartElements.forEach(element => {
        // Chart initialization logic here
        console.log('Chart element found:', element);
    });
}

// Inventory specific functions
function initializeInventory() {
    console.log('Inventory initialized');
    
    // Initialize inventory table
    initializeInventoryTable();
    initializeInventoryFilters();
    initializeInventoryPagination();
}

function initializeInventoryTable() {
    const table = document.querySelector('.inventory-table');
    if (table) {
        // Add hover effects to table rows
        const rows = table.querySelectorAll('tbody tr');
        rows.forEach(row => {
            row.addEventListener('mouseenter', function() {
                this.style.backgroundColor = '#f8f9fa';
            });
            
            row.addEventListener('mouseleave', function() {
                this.style.backgroundColor = '';
            });
        });
    }
}

function initializeInventoryFilters() {
    // Initialize filter functionality
    const filterSelects = document.querySelectorAll('select[id*="Filter"]');
    filterSelects.forEach(select => {
        select.addEventListener('change', function() {
            applyFilters();
        });
    });
    
    const searchInputs = document.querySelectorAll('input[type="search"]');
    searchInputs.forEach(input => {
        input.addEventListener('input', debounce(function() {
            applyFilters();
        }, 300));
    });
}

function initializeInventoryPagination() {
    // Initialize pagination if present
    const paginationLinks = document.querySelectorAll('.pagination a');
    paginationLinks.forEach(link => {
        link.addEventListener('click', function(e) {
            e.preventDefault();
            const page = this.getAttribute('data-page');
            if (page) {
                loadPage(parseInt(page));
            }
        });
    });
}

// Product Detail specific functions
function initializeProductDetail() {
    console.log('Product Detail initialized');
    
    // Initialize product detail functionality
    initializeProductImage();
    initializeSerialList();
    initializeActionButtons();
}

function initializeProductImage() {
    const productImage = document.querySelector('.product-image');
    if (productImage) {
        // Add image zoom functionality
        productImage.addEventListener('click', function() {
            showImageModal(this.src);
        });
    }
}

function initializeSerialList() {
    const serialList = document.querySelector('.serial-list');
    if (serialList) {
        // Add copy functionality to serial items
        const serialItems = serialList.querySelectorAll('.serial-item');
        serialItems.forEach(item => {
            item.addEventListener('click', function() {
                const serialCode = this.querySelector('.serial-code');
                if (serialCode) {
                    copyToClipboard(serialCode.textContent);
                    showToast('Serial code copied to clipboard!');
                }
            });
        });
    }
}

function initializeActionButtons() {
    // Initialize action buttons
    const deleteButton = document.querySelector('button[onclick*="deleteProduct"]');
    if (deleteButton) {
        deleteButton.addEventListener('click', function() {
            const productId = this.getAttribute('onclick').match(/\d+/)[0];
            confirmDeleteProduct(productId);
        });
    }
}

// Update Product specific functions
function initializeUpdateProduct() {
    console.log('Update Product initialized');
    
    // Initialize update product functionality
    initializeUpdateForm();
    initializeImagePreview();
}

function initializeUpdateForm() {
    const form = document.querySelector('form');
    if (form) {
        // Add form validation
        form.addEventListener('submit', function(e) {
            if (!validateUpdateForm()) {
                e.preventDefault();
                showToast('Please fill in all required fields', 'error');
            }
        });
    }
}

function initializeImagePreview() {
    const imageInput = document.querySelector('input[type="file"][accept*="image"]');
    if (imageInput) {
        imageInput.addEventListener('change', function() {
            const file = this.files[0];
            if (file) {
                previewImage(file);
            }
        });
    }
}

// Common utility functions
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

function copyToClipboard(text) {
    if (navigator.clipboard) {
        navigator.clipboard.writeText(text).then(() => {
            console.log('Text copied to clipboard');
        });
    } else {
        // Fallback for older browsers
        const textArea = document.createElement('textarea');
        textArea.value = text;
        document.body.appendChild(textArea);
        textArea.select();
        document.execCommand('copy');
        document.body.removeChild(textArea);
    }
}

function showToast(message, type = 'success') {
    // Create toast element
    const toast = document.createElement('div');
    toast.className = `toast align-items-center text-white bg-${type === 'error' ? 'danger' : 'success'} border-0`;
    toast.setAttribute('role', 'alert');
    toast.innerHTML = `
        <div class="d-flex">
            <div class="toast-body">${message}</div>
            <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"></button>
        </div>
    `;
    
    // Add to page
    document.body.appendChild(toast);
    
    // Show toast
    const bsToast = new bootstrap.Toast(toast);
    bsToast.show();
    
    // Remove after hide
    toast.addEventListener('hidden.bs.toast', function() {
        document.body.removeChild(toast);
    });
}

function showImageModal(imageSrc) {
    // Create modal for image preview
    const modal = document.createElement('div');
    modal.className = 'modal fade';
    modal.innerHTML = `
        <div class="modal-dialog modal-lg modal-dialog-centered">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title">Product Image</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                </div>
                <div class="modal-body text-center">
                    <img src="${imageSrc}" class="img-fluid" alt="Product Image">
                </div>
            </div>
        </div>
    `;
    
    document.body.appendChild(modal);
    
    const bsModal = new bootstrap.Modal(modal);
    bsModal.show();
    
    modal.addEventListener('hidden.bs.modal', function() {
        document.body.removeChild(modal);
    });
}

function confirmDeleteProduct(productId) {
    if (confirm('Are you sure you want to delete this product? This action cannot be undone.')) {
        // Call delete function
        deleteProduct(productId);
    }
}

function deleteProduct(productId) {
    // Show loading state
    const deleteButton = document.querySelector(`button[onclick*="${productId}"]`);
    if (deleteButton) {
        deleteButton.disabled = true;
        deleteButton.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Deleting...';
    }
    
    // Make delete request
    fetch(`/itp/shop/products/${productId}`, {
        method: 'DELETE',
        headers: {
            'Content-Type': 'application/json',
        }
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            showToast('Product deleted successfully!');
            // Redirect to dashboard after a short delay
            setTimeout(() => {
                window.location.href = '/shop/dashboard';
            }, 1500);
        } else {
            showToast('Error deleting product: ' + data.message, 'error');
        }
    })
    .catch(error => {
        console.error('Error:', error);
        showToast('Error deleting product', 'error');
    })
    .finally(() => {
        // Reset button state
        if (deleteButton) {
            deleteButton.disabled = false;
            deleteButton.innerHTML = 'Xóa';
        }
    });
}

function previewImage(file) {
    const reader = new FileReader();
    reader.onload = function(e) {
        // Find existing preview or create new one
        let preview = document.querySelector('.image-preview');
        if (!preview) {
            preview = document.createElement('div');
            preview.className = 'image-preview mt-2';
            document.querySelector('input[type="file"]').parentNode.appendChild(preview);
        }
        
        preview.innerHTML = `
            <img src="${e.target.result}" class="img-thumbnail" style="max-width: 200px; max-height: 200px;" alt="Preview">
            <div class="mt-1">
                <small class="text-muted">Preview of selected image</small>
            </div>
        `;
    };
    reader.readAsDataURL(file);
}

function validateUpdateForm() {
    const requiredFields = document.querySelectorAll('input[required], select[required], textarea[required]');
    let isValid = true;
    
    requiredFields.forEach(field => {
        if (!field.value.trim()) {
            field.classList.add('is-invalid');
            isValid = false;
        } else {
            field.classList.remove('is-invalid');
        }
    });
    
    return isValid;
}

function applyFilters() {
    // Get current filter values
    const productTypeFilter = document.getElementById('productTypeFilter');
    const statusFilter = document.getElementById('statusFilter');
    const searchInput = document.getElementById('searchInput');
    
    const filters = {
        productType: productTypeFilter ? productTypeFilter.value : '',
        status: statusFilter ? statusFilter.value : '',
        search: searchInput ? searchInput.value : ''
    };
    
    // Apply filters to table
    filterTable(filters);
}

function filterTable(filters) {
    const table = document.querySelector('.inventory-table tbody');
    if (!table) return;
    
    const rows = table.querySelectorAll('tr');
    
    rows.forEach(row => {
        let showRow = true;
        
        // Check product type filter
        if (filters.productType) {
            const productTypeCell = row.querySelector('[data-product-type]');
            if (productTypeCell && productTypeCell.getAttribute('data-product-type') !== filters.productType) {
                showRow = false;
            }
        }
        
        // Check status filter
        if (filters.status) {
            const statusCell = row.querySelector('[data-status]');
            if (statusCell && statusCell.getAttribute('data-status') !== filters.status) {
                showRow = false;
            }
        }
        
        // Check search filter
        if (filters.search) {
            const searchText = row.textContent.toLowerCase();
            if (!searchText.includes(filters.search.toLowerCase())) {
                showRow = false;
            }
        }
        
        row.style.display = showRow ? '' : 'none';
    });
}

function loadPage(page) {
    currentPage = page;
    
    // Update URL without page reload
    const url = new URL(window.location);
    url.searchParams.set('page', page);
    window.history.pushState({}, '', url);
    
    // Load page content
    loadPageContent(page);
}

function loadPageContent(page) {
    // Show loading state
    const tableBody = document.querySelector('.inventory-table tbody');
    if (tableBody) {
        tableBody.innerHTML = '<tr><td colspan="100%" class="text-center"><div class="spinner-border" role="status"></div></td></tr>';
    }
    
    // Simulate API call (replace with actual API call)
    setTimeout(() => {
        // Reload the page with new page parameter
        window.location.reload();
    }, 500);
}

// Export functions for global access
window.shopCommon = {
    showToast,
    copyToClipboard,
    confirmDeleteProduct,
    deleteProduct,
    previewImage,
    validateUpdateForm,
    applyFilters,
    loadPage
};

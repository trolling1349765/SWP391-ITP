// Inventory JavaScript Functions

function deleteProduct(productId) {
    // Tạo Bootstrap modal confirmation
    const modalHtml = `
        <div class="modal fade" id="deleteConfirmModal" tabindex="-1" aria-labelledby="deleteConfirmModalLabel" aria-hidden="true">
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title" id="deleteConfirmModalLabel">Xác nhận xóa sản phẩm</h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                    </div>
                    <div class="modal-body">
                        <p>Bạn có chắc chắn muốn xóa sản phẩm này không?</p>
                        <p class="text-danger"><strong>Hành động này không thể hoàn tác!</strong></p>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Hủy</button>
                        <button type="button" class="btn btn-danger" onclick="confirmDelete(${productId})">Xóa sản phẩm</button>
                    </div>
                </div>
            </div>
        </div>
    `;
    
    // Xóa modal cũ nếu có
    const existingModal = document.getElementById('deleteConfirmModal');
    if (existingModal) {
        existingModal.remove();
    }
    
    // Thêm modal mới vào body
    document.body.insertAdjacentHTML('beforeend', modalHtml);
    
    // Hiển thị modal
    const modal = new bootstrap.Modal(document.getElementById('deleteConfirmModal'));
    modal.show();
}

function confirmDelete(productId) {
    // Gửi request DELETE
    fetch(`/shop/products/${productId}`, {
        method: 'DELETE',
        headers: {
            'Content-Type': 'application/json',
        }
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            // Hiển thị thông báo thành công
            showAlert('Sản phẩm đã được xóa thành công!', 'success');
            
            // Reload trang sau 1 giây
            setTimeout(() => {
                window.location.reload();
            }, 1000);
        } else {
            showAlert('Lỗi khi xóa sản phẩm: ' + (data.message || 'Không xác định'), 'danger');
        }
    })
    .catch(error => {
        console.error('Error:', error);
        showAlert('Lỗi khi xóa sản phẩm: ' + error.message, 'danger');
    })
    .finally(() => {
        // Đóng modal
        const modal = bootstrap.Modal.getInstance(document.getElementById('deleteConfirmModal'));
        if (modal) {
            modal.hide();
        }
    });
}

function showAlert(message, type) {
    const alertHtml = `
        <div class="alert alert-${type} alert-dismissible fade show" role="alert">
            ${message}
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
    `;
    
    // Thêm alert vào đầu trang
    const container = document.querySelector('.container-fluid .row .col-md-9');
    if (container) {
        container.insertAdjacentHTML('afterbegin', alertHtml);
        
        // Tự động ẩn sau 5 giây
        setTimeout(() => {
            const alert = container.querySelector('.alert');
            if (alert) {
                const bsAlert = new bootstrap.Alert(alert);
                bsAlert.close();
            }
        }, 5000);
    }
}

// Filter functions
function filterInventory() {
    const productType = document.getElementById('filterProductType').value;
    const faceValue = document.getElementById('filterFaceValue').value;
    const stockStatus = document.getElementById('filterStockStatus').value;
    const searchTerm = document.getElementById('searchInput').value.toLowerCase();
    
    const rows = document.querySelectorAll('#inventoryTable tbody tr');
    
    rows.forEach(row => {
        const productName = row.cells[1].textContent.toLowerCase();
        const productTypeCell = row.cells[2].textContent.toLowerCase();
        const priceCell = row.cells[3].textContent;
        const stockCell = row.cells[4].textContent;
        
        let showRow = true;
        
        // Filter by product type
        if (productType && !productTypeCell.includes(productType.toLowerCase())) {
            showRow = false;
        }
        
        // Filter by face value
        if (faceValue && !priceCell.includes(faceValue)) {
            showRow = false;
        }
        
        // Filter by stock status
        if (stockStatus) {
            const stockValue = parseInt(stockCell);
            if (stockStatus === 'high' && stockValue <= 10) {
                showRow = false;
            } else if (stockStatus === 'low' && (stockValue > 10 || stockValue === 0)) {
                showRow = false;
            } else if (stockStatus === 'out' && stockValue > 0) {
                showRow = false;
            }
        }
        
        // Filter by search term
        if (searchTerm && !productName.includes(searchTerm)) {
            showRow = false;
        }
        
        row.style.display = showRow ? '' : 'none';
    });
}

// Export CSV function
function exportInventoryCSV() {
    const table = document.getElementById('inventoryTable');
    const rows = Array.from(table.querySelectorAll('tr'));
    
    let csvContent = '';
    
    rows.forEach(row => {
        const cells = Array.from(row.querySelectorAll('th, td'));
        const rowData = cells.map(cell => {
            let text = cell.textContent.trim();
            // Escape quotes and wrap in quotes if contains comma
            if (text.includes(',') || text.includes('"')) {
                text = '"' + text.replace(/"/g, '""') + '"';
            }
            return text;
        });
        csvContent += rowData.join(',') + '\n';
    });
    
    // Create and download file
    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
    const link = document.createElement('a');
    const url = URL.createObjectURL(blob);
    link.setAttribute('href', url);
    link.setAttribute('download', 'inventory_' + new Date().toISOString().split('T')[0] + '.csv');
    link.style.visibility = 'hidden';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
}

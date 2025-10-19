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
                        <p>Bạn có chắc chắn muốn xóa sản phẩm #${productId} không?</p>
                        <p class="text-danger"><strong>Hành động này không thể hoàn tác!</strong></p>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Hủy</button>
                        <button type="button" class="btn btn-danger" onclick="confirmDelete(${productId})">Xóa</button>
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
    // Gửi AJAX request để xóa sản phẩm
    fetch(`/itp/shop/products/${productId}`, {
        method: 'DELETE',
        headers: {
            'Content-Type': 'application/json',
            'X-Requested-With': 'XMLHttpRequest'
        }
    })
    .then(response => {
        if (response.ok) {
            // Đóng modal
            const modal = bootstrap.Modal.getInstance(document.getElementById('deleteConfirmModal'));
            modal.hide();
            
            // Hiển thị thông báo thành công
            showAlert('success', 'Đã xóa sản phẩm thành công!');
            
            // Reload trang sau 1 giây
            setTimeout(() => {
                location.reload();
            }, 1000);
        } else {
            throw new Error('Có lỗi xảy ra khi xóa sản phẩm');
        }
    })
    .catch(error => {
        console.error('Error:', error);
        showAlert('danger', 'Có lỗi xảy ra: ' + error.message);
    });
}

function showAlert(type, message) {
    const alertHtml = `
        <div class="alert alert-${type} alert-dismissible fade show" role="alert">
            ${message}
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
    `;
    
    // Thêm alert vào đầu container
    const container = document.querySelector('.col-md-9.col-lg-10');
    container.insertAdjacentHTML('afterbegin', alertHtml);
    
    // Tự động ẩn alert sau 5 giây
    setTimeout(() => {
        const alert = container.querySelector('.alert');
        if (alert) {
            const bsAlert = new bootstrap.Alert(alert);
            bsAlert.close();
        }
    }, 5000);
}

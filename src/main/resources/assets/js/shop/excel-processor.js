/**
 * Excel Processor
 * Handles Excel file parsing, validation, and preview generation
 */
class ExcelProcessor {
    constructor() {
        this.loadingManager = null;
        this.formManager = null;
        this.currentSerials = [];
        this.duplicateChecker = null;
        
        this.init();
    }

    init() {
        // Initialize managers
        this.loadingManager = new ExcelLoadingManager();
        this.formManager = window.formManager;
        this.duplicateChecker = new DuplicateChecker();
        
        // Bind methods
        this.previewExcelFile = this.previewExcelFile.bind(this);
        this.parseExcelFile = this.parseExcelFile.bind(this);
        this.showExcelPreview = this.showExcelPreview.bind(this);
    }

    previewExcelFile(input) {
        const file = input.files[0];
        if (!file) {
            this.hideExcelPreview();
            return;
        }
        
        // Show loading
        this.loadingManager.showLoading();
        
        const reader = new FileReader();
        reader.onload = (e) => {
            try {
                const data = new Uint8Array(e.target.result);
                const workbook = XLSX.read(data, { type: 'array' });
                const worksheet = workbook.Sheets[workbook.SheetNames[0]];
                const jsonData = XLSX.utils.sheet_to_json(worksheet, { header: 1 });
                
                // Simulate processing steps
                this.simulateProcessingSteps(() => {
                    this.loadingManager.hideLoading();
                    this.parseExcelFile(jsonData);
                });
                
            } catch (error) {
                this.loadingManager.showError('Lỗi đọc file Excel: ' + error.message);
            }
        };
        
        reader.readAsArrayBuffer(file);
    }

    simulateProcessingSteps(callback) {
        setTimeout(() => {
            this.loadingManager.updateStep(1);
            setTimeout(() => {
                this.loadingManager.updateStep(2);
                setTimeout(() => {
                    this.loadingManager.updateStep(3);
                    setTimeout(callback, 800);
                }, 800);
            }, 800);
        }, 800);
    }

    parseExcelFile(data) {
        if (data.length < 2) {
            this.showExcelPreviewError('File Excel không có dữ liệu hoặc định dạng không đúng');
            return;
        }
        
        const headers = data[0];
        const expectedHeaders = ['Serial Code', 'Secret Code'];
        
        // Validate headers
        if (!ExcelProcessingUtils.validateHeaders(headers, expectedHeaders)) {
            this.showExcelPreviewError('File Excel không đúng định dạng. Cần có các cột: ' + expectedHeaders.join(', '));
            return;
        }
        
        // Sanitize and extract data
        const sanitizedData = ExcelProcessingUtils.sanitizeData(data);
        const serials = ExcelProcessingUtils.extractSerials(sanitizedData, headers);
        
        if (serials.length === 0) {
            this.showExcelPreviewError('Không tìm thấy dữ liệu serial nào trong file Excel');
            return;
        }
        
        // Check for duplicates and validate
        this.processSerials(serials);
    }

    processSerials(serials) {
        let validCount = 0;
        let errorCount = 0;
        let duplicateCount = 0;
        
        // Process each serial
        serials.forEach(serial => {
            if (!serial.serialCode || !serial.secretCode) {
                serial.status = 'error';
                errorCount++;
            } else {
                // Check for duplicates within the file
                const isDuplicateInFile = this.checkDuplicateInFile(serial, serials);
                
                if (isDuplicateInFile) {
                    serial.status = 'duplicate';
                    duplicateCount++;
                } else {
                    serial.status = 'ready';
                    validCount++;
                }
            }
        });
        
        this.currentSerials = serials;
        
        // Check duplicates in database
        this.duplicateChecker.checkDatabaseDuplicates(serials, validCount, errorCount, duplicateCount)
            .then(result => {
                this.showExcelPreview(result.serials, result.validCount, result.errorCount, result.duplicateCount, result.dbDuplicateCount);
            })
            .catch(error => {
                console.error('Error checking duplicates:', error);
                this.showExcelPreview(serials, validCount, errorCount, duplicateCount);
            });
    }

    checkDuplicateInFile(currentSerial, allSerials) {
        return allSerials.some(s => 
            s !== currentSerial && 
            s.serialCode === currentSerial.serialCode
        );
    }

    showExcelPreview(serials, validCount, errorCount, duplicateCount = 0, dbDuplicateCount = 0) {
        const previewDiv = document.getElementById('excelPreview');
        if (!previewDiv) return;
        
        // Update form manager state
        if (this.formManager) {
            this.formManager.updateSubmitButtonState(validCount, duplicateCount, dbDuplicateCount);
        }
        
        // Generate preview HTML
        const previewHTML = this.generatePreviewHTML(serials, validCount, errorCount, duplicateCount, dbDuplicateCount);
        
        previewDiv.innerHTML = previewHTML;
        previewDiv.style.display = 'block';
    }

    generatePreviewHTML(serials, validCount, errorCount, duplicateCount, dbDuplicateCount) {
        const totalDuplicates = duplicateCount + dbDuplicateCount;
        
        let tableHTML = `
            <div class="card mt-3">
                <div class="card-header">
                    <h6 class="mb-0">Danh sách serials sẽ được thêm vào</h6>
                </div>
                <div class="card-body p-0">
                    <div class="table-responsive">
                        <table class="table table-sm table-striped mb-0">
                            <thead class="table-light">
                                <tr>
                                    <th>STT</th>
                                    <th>Serial Code</th>
                                    <th>Secret Code</th>
                                    <th>Trạng thái</th>
                                </tr>
                            </thead>
                            <tbody>
        `;
        
        serials.forEach((serial, index) => {
            const statusBadge = this.getStatusBadge(serial.status);
            
            tableHTML += `
                <tr>
                    <td>${index + 1}</td>
                    <td>${serial.serialCode}</td>
                    <td>${serial.secretCode}</td>
                    <td>${statusBadge}</td>
                </tr>
            `;
        });
        
        tableHTML += `
                            </tbody>
                        </table>
                    </div>
                </div>
                <div class="card-footer">
                    <small class="text-muted">
                        Tổng cộng: ${serials.length} serials sẽ được thêm vào
                        ${validCount > 0 ? ` (${validCount} hợp lệ)` : ''}
                        ${totalDuplicates > 0 ? ` (${totalDuplicates} trùng)` : ''}
                        ${errorCount > 0 ? ` (${errorCount} có lỗi)` : ''}
                    </small>
                </div>
            </div>
        `;
        
        return tableHTML;
    }

    getStatusBadge(status) {
        switch(status) {
            case 'ready':
                return '<span class="badge bg-success">Sẵn sàng</span>';
            case 'duplicate':
                return '<span class="badge bg-warning">Trùng trong file</span>';
            case 'duplicate_db':
                return '<span class="badge bg-danger">Trùng trong DB</span>';
            case 'error':
            default:
                return '<span class="badge bg-danger">Có lỗi</span>';
        }
    }

    showExcelPreviewError(message) {
        const previewDiv = document.getElementById('excelPreview');
        if (!previewDiv) return;
        
        previewDiv.innerHTML = `
            <div class="alert alert-danger mt-3">
                <strong>Lỗi:</strong> ${message}
            </div>
        `;
        previewDiv.style.display = 'block';
        
        // Update form manager state
        if (this.formManager) {
            this.formManager.updateSubmitButtonState(0, 0, 0);
        }
    }

    hideExcelPreview() {
        const previewDiv = document.getElementById('excelPreview');
        if (previewDiv) {
            previewDiv.style.display = 'none';
            previewDiv.innerHTML = '';
        }
        
        // Reset form manager state
        if (this.formManager) {
            this.formManager.updateSubmitButtonState(0, 0, 0);
        }
    }

    clearFileInput() {
        const fileInput = document.getElementById('serialFileInput');
        const fileInfo = document.getElementById('fileInfo');
        
        if (fileInput) {
            fileInput.value = '';
        }
        
        if (fileInfo) {
            fileInfo.innerHTML = '';
        }
        
        this.hideExcelPreview();
        
        // Reset form manager state
        if (this.formManager) {
            this.formManager.updateSubmitButtonState(0, 0, 0);
        }
    }
}

/**
 * Duplicate Checker
 * Handles checking for duplicate serials in the database
 */
class DuplicateChecker {
    constructor() {
        this.apiEndpoint = '/itp/shop/check-serial-duplicates';
    }

    async checkDatabaseDuplicates(serials, validCount, errorCount, duplicateCount) {
        const validSerials = serials.filter(s => s.status === 'ready');
        
        if (validSerials.length === 0) {
            return {
                serials,
                validCount,
                errorCount,
                duplicateCount,
                dbDuplicateCount: 0
            };
        }
        
        try {
            const serialCodes = validSerials.map(s => s.serialCode);
            const response = await fetch(this.apiEndpoint, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(serialCodes)
            });
            
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            
            const data = await response.json();
            
            // Update serial status based on database check
            serials.forEach(serial => {
                if (serial.status === 'ready' && data.duplicates.includes(serial.serialCode)) {
                    serial.status = 'duplicate_db';
                }
            });
            
            const dbDuplicateCount = data.duplicates.length;
            const finalValidCount = validCount - dbDuplicateCount;
            
            return {
                serials,
                validCount: finalValidCount,
                errorCount,
                duplicateCount,
                dbDuplicateCount
            };
            
        } catch (error) {
            console.error('Error checking duplicates:', error);
            return {
                serials,
                validCount,
                errorCount,
                duplicateCount,
                dbDuplicateCount: 0
            };
        }
    }
}

// Initialize Excel processor when DOM is ready
document.addEventListener('DOMContentLoaded', function() {
    window.excelProcessor = new ExcelProcessor();
});

// Export for use in other modules
window.ExcelProcessor = ExcelProcessor;
window.DuplicateChecker = DuplicateChecker;

/**
 * Excel Loading Manager
 * Handles loading states and progress indicators for Excel processing
 */
class ExcelLoadingManager {
    constructor() {
        this.currentStep = 0;
        this.maxSteps = 3;
        this.steps = [
            { icon: '', text: 'Đang đọc file...' },
            { icon: '', text: 'Đang phân tích dữ liệu...' },
            { icon: '', text: 'Hoàn thành!' }
        ];
    }

    showLoading() {
        const container = document.querySelector('.excel-processing-container') || 
                        document.getElementById('excelPreview');
        
        if (!container) return;

        const overlay = document.createElement('div');
        overlay.className = 'excel-loading-overlay';
        overlay.id = 'excelLoadingOverlay';
        
        overlay.innerHTML = `
            <div class="excel-loading-spinner"></div>
            <div class="excel-loading-text">Đang xử lý file Excel...</div>
            <div class="excel-loading-steps">
                ${this.steps.map((step, index) => `
                    <div class="excel-loading-step" data-step="${index}">
                        <div class="excel-loading-step-icon">${step.icon}</div>
                        <div class="excel-loading-step-text">${step.text}</div>
                    </div>
                `).join('')}
            </div>
        `;
        
        container.appendChild(overlay);
        this.updateStep(0);
    }

    updateStep(stepIndex) {
        this.currentStep = stepIndex;
        const steps = document.querySelectorAll('.excel-loading-step');
        
        steps.forEach((step, index) => {
            step.classList.remove('active', 'completed');
            
            if (index < stepIndex) {
                step.classList.add('completed');
            } else if (index === stepIndex) {
                step.classList.add('active');
            }
        });
    }

    hideLoading() {
        const overlay = document.getElementById('excelLoadingOverlay');
        if (overlay) {
            overlay.remove();
        }
    }

    showError(message) {
        this.hideLoading();
        
        const container = document.querySelector('.excel-processing-container') || 
                        document.getElementById('excelPreview');
        
        if (!container) return;

        const errorDiv = document.createElement('div');
        errorDiv.className = 'error-state';
        errorDiv.innerHTML = `
            <span class="error-icon">!</span>
            <strong>Lỗi:</strong> ${message}
        `;
        
        container.appendChild(errorDiv);
    }
}

/**
 * Success Animation Manager
 * Handles success animations and notifications
 */
class SuccessAnimationManager {
    constructor() {
        this.isShowing = false;
    }

    showSuccess(message, subtext = '') {
        if (this.isShowing) return;
        
        this.isShowing = true;
        
        const overlay = document.createElement('div');
        overlay.className = 'success-animation';
        overlay.id = 'successAnimation';
        
        overlay.innerHTML = `
            <div class="success-animation-icon">✓</div>
            <div class="success-animation-text">${message}</div>
            ${subtext ? `<div class="success-animation-subtext">${subtext}</div>` : ''}
        `;
        
        document.body.appendChild(overlay);
        
        // Auto remove after 3 seconds
        setTimeout(() => {
            this.hideSuccess();
        }, 3000);
    }

    hideSuccess() {
        const animation = document.getElementById('successAnimation');
        if (animation) {
            animation.style.animation = 'successPop 0.3s ease-in reverse';
            setTimeout(() => {
                animation.remove();
                this.isShowing = false;
            }, 300);
        }
    }
}

/**
 * Performance Optimizer
 * Handles performance optimizations for large datasets
 */
class PerformanceOptimizer {
    constructor() {
        this.batchSize = 100;
        this.delay = 10;
    }

    processInBatches(items, processor, callback) {
        let index = 0;
        const results = [];
        
        const processBatch = () => {
            const batch = items.slice(index, index + this.batchSize);
            
            batch.forEach((item, batchIndex) => {
                const result = processor(item, index + batchIndex);
                results.push(result);
            });
            
            index += this.batchSize;
            
            if (index < items.length) {
                setTimeout(processBatch, this.delay);
            } else {
                callback(results);
            }
        };
        
        processBatch();
    }

    debounce(func, wait) {
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

    throttle(func, limit) {
        let inThrottle;
        return function() {
            const args = arguments;
            const context = this;
            if (!inThrottle) {
                func.apply(context, args);
                inThrottle = true;
                setTimeout(() => inThrottle = false, limit);
            }
        };
    }
}

/**
 * Excel Processing Utilities
 * Utility functions for Excel file processing
 */
class ExcelProcessingUtils {
    static validateHeaders(headers, expectedHeaders) {
        return expectedHeaders.every(header => headers.includes(header));
    }

    static sanitizeData(data) {
        return data.map(row => 
            row.map(cell => 
                cell ? String(cell).trim() : ''
            )
        );
    }

    static extractSerials(data, headers) {
        const serials = [];
        const serialCodeIndex = headers.indexOf('Serial Code');
        const secretCodeIndex = headers.indexOf('Secret Code');
        
        for (let i = 1; i < data.length; i++) {
            const row = data[i];
            if (row.length < 2) continue;
            
            const serialCode = row[serialCodeIndex] || '';
            const secretCode = row[secretCodeIndex] || '';
            
            if (serialCode && secretCode) {
                serials.push({
                    serialCode: serialCode.trim(),
                    secretCode: secretCode.trim(),
                    rowIndex: i + 1
                });
            }
        }
        
        return serials;
    }

    static checkDuplicatesInFile(serials) {
        const seen = new Set();
        const duplicates = [];
        
        serials.forEach((serial, index) => {
            if (seen.has(serial.serialCode)) {
                duplicates.push({
                    ...serial,
                    duplicateIndex: index
                });
            } else {
                seen.add(serial.serialCode);
            }
        });
        
        return duplicates;
    }

    static formatFileSize(bytes) {
        if (bytes === 0) return '0 Bytes';
        
        const k = 1024;
        const sizes = ['Bytes', 'KB', 'MB', 'GB'];
        const i = Math.floor(Math.log(bytes) / Math.log(k));
        
        return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
    }

    static getFileType(fileName) {
        const extension = fileName.split('.').pop().toLowerCase();
        const types = {
            'xlsx': 'Excel 2007+',
            'xls': 'Excel 97-2003',
            'csv': 'CSV'
        };
        
        return types[extension] || 'Unknown';
    }
}

// Export classes for use in other modules
window.ExcelLoadingManager = ExcelLoadingManager;
window.SuccessAnimationManager = SuccessAnimationManager;
window.PerformanceOptimizer = PerformanceOptimizer;
window.ExcelProcessingUtils = ExcelProcessingUtils;

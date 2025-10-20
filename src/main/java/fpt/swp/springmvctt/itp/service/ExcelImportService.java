package fpt.swp.springmvctt.itp.service;

import fpt.swp.springmvctt.itp.dto.request.ExcelImportForm;
import fpt.swp.springmvctt.itp.dto.response.ImportResult;
import org.springframework.web.multipart.MultipartFile;

public interface ExcelImportService {
    
    /**
     * Import serials and quantities from Excel file
     * @param form Excel import form containing productId and file
     * @return ImportResult with detailed information
     */
    ImportResult importSerialsFromExcel(ExcelImportForm form);
    
    /**
     * Import serials and quantities from Excel file (legacy method)
     * @param form Excel import form containing productId and file
     * @return Number of records imported successfully
     */
    @Deprecated
    int importSerialsFromExcelLegacy(ExcelImportForm form);
    
    /**
     * Validate Excel file format before import
     * @param file Excel file to validate
     * @return true if format is valid
     */
    boolean validateExcelFormat(MultipartFile file);
    
    /**
     * Generate Excel template for serial import
     * @param productId Product ID to generate template for
     * @return Excel file as byte array
     */
    byte[] generateExcelTemplate(Long productId);
}

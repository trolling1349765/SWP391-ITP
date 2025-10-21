package fpt.swp.springmvctt.itp.service;

import fpt.swp.springmvctt.itp.dto.request.ExcelImportForm;
import fpt.swp.springmvctt.itp.dto.response.ImportResult;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface ExcelImportService {
    
    /**
     * Import serials and quantities from Excel file
     * @param form Excel import form containing productId and file
     * @return ImportResult with detailed information
     */
    ImportResult importSerialsFromExcel(ExcelImportForm form);
    
    /**
     * Preview Excel file without saving to database
     * @param form Excel import form
     * @return Map containing preview data and JSON file path
     */
    Map<String, Object> previewExcelImport(ExcelImportForm form);
    
    /**
     * Get serial data from JSON file for a product
     * @param productId Product ID
     * @return List of serial data maps
     */
    List<Map<String, Object>> getProductSerials(Long productId);
    
    /**
     * Get the latest JSON file for a product
     * @param productId Product ID
     * @return JSON file path or null if not found
     */
    String getLatestJsonFile(Long productId);
    
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

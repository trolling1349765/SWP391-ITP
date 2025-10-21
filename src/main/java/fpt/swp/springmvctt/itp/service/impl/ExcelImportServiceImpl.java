package fpt.swp.springmvctt.itp.service.impl;

import fpt.swp.springmvctt.itp.dto.request.ExcelImportForm;
import fpt.swp.springmvctt.itp.dto.request.StockForm;
import fpt.swp.springmvctt.itp.dto.response.ImportResult;
import fpt.swp.springmvctt.itp.entity.Product;
import fpt.swp.springmvctt.itp.entity.ProductStore;
import fpt.swp.springmvctt.itp.entity.enums.ProductStatus;
import fpt.swp.springmvctt.itp.entity.enums.ProductType;
import fpt.swp.springmvctt.itp.repository.ProductRepository;
import fpt.swp.springmvctt.itp.repository.ProductStoreRepository;
import fpt.swp.springmvctt.itp.service.ExcelImportService;
import fpt.swp.springmvctt.itp.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.JsonNode;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExcelImportServiceImpl implements ExcelImportService {

    private final ProductRepository productRepository;
    private final ProductStoreRepository productStoreRepository;
    private final InventoryService inventoryService;

    @Override
    public ImportResult importSerialsFromExcel(ExcelImportForm form) {
        try (Workbook workbook = WorkbookFactory.create(form.getExcelFile().getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            
            List<Map<String, Object>> serials = new ArrayList<>();
            List<String> errors = new ArrayList<>();
            List<String> warnings = new ArrayList<>();
            List<String> duplicateSerials = new ArrayList<>();
            List<String> invalidSerials = new ArrayList<>();
            
            int importedCount = 0;
            int skippedCount = 0;
            Set<String> processedSerials = new HashSet<>();
            BigDecimal expectedFaceValue = null;
            
            int totalRows = sheet.getLastRowNum(); // Total rows including header
            
            // Skip header row (row 0)
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                
                String serialCode = null;
                try {
                    serialCode = getCellValueAsString(row.getCell(0));
                    String secretCode = getCellValueAsString(row.getCell(1));
                    Integer quantity = getCellValueAsInteger(row.getCell(2));
                    BigDecimal faceValue = getCellValueAsBigDecimal(row.getCell(3));
                    String information = getCellValueAsString(row.getCell(4));
                    
                    // Validation
                    if (serialCode == null || serialCode.trim().isEmpty()) {
                        String errorMsg = "Dòng " + (i + 1) + ": Serial code trống";
                        errors.add(errorMsg);
                        invalidSerials.add("Dòng " + (i + 1));
                        skippedCount++;
                        continue;
                    }
                    
                    if (quantity == null || quantity < 0) {
                        String errorMsg = "Dòng " + (i + 1) + ": Số lượng không hợp lệ (" + quantity + ")";
                        errors.add(errorMsg);
                        invalidSerials.add(serialCode);
                        skippedCount++;
                        continue;
                    }
                    
                    if (processedSerials.contains(serialCode)) {
                        String warningMsg = "Dòng " + (i + 1) + ": Serial code trùng lặp (" + serialCode + ")";
                        warnings.add(warningMsg);
                        duplicateSerials.add(serialCode);
                        skippedCount++;
                        continue;
                    }
                    
                    // Check if serial already exists in database
                    if (productStoreRepository.findByProductIdAndSerialCode(form.getProductId(), serialCode).isPresent()) {
                        if (!form.isOverrideExisting()) {
                            String warningMsg = "Dòng " + (i + 1) + ": Serial code đã tồn tại (" + serialCode + ")";
                            warnings.add(warningMsg);
                            duplicateSerials.add(serialCode);
                            skippedCount++;
                            continue;
                        }
                    }
                    
                    // Validate face value consistency for telecom cards
                    Product product = productRepository.findById(form.getProductId()).orElse(null);
                    if (product != null && isTelecomCard(product.getProductType())) {
                        if (faceValue == null) {
                            String errorMsg = "Dòng " + (i + 1) + ": Face value bắt buộc cho thẻ điện thoại";
                            errors.add(errorMsg);
                            invalidSerials.add(serialCode);
                            skippedCount++;
                            continue;
                        }
                        
                        // Check if face value matches product price (all cards should have same face value)
                        if (i == 1) {
                            // First row - store the expected face value for this import
                            expectedFaceValue = faceValue;
                        } else {
                            // Subsequent rows - check consistency with first row
                            if (expectedFaceValue != null && !expectedFaceValue.equals(faceValue)) {
                                String errorMsg = "Dòng " + (i + 1) + ": Face value " + faceValue + " không khớp với mệnh giá " + expectedFaceValue;
                                errors.add(errorMsg);
                                invalidSerials.add(serialCode);
                                skippedCount++;
                                continue;
                            }
                        }
                    }
                    
                    // Create serial data for JSON storage
                    Map<String, Object> serialData = new HashMap<>();
                    serialData.put("serialCode", serialCode);
                    serialData.put("secretCode", secretCode);
                    serialData.put("quantity", quantity);
                    serialData.put("faceValue", faceValue != null ? faceValue.doubleValue() : 0);
                    serialData.put("information", information != null ? information : "");
                    serialData.put("status", "AVAILABLE");
                    serialData.put("importDate", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                    serialData.put("isSold", false);
                    serialData.put("soldDate", null);
                    serialData.put("soldTo", null);
                    serials.add(serialData);
                    
                    // Create StockForm and add to inventory
                    StockForm stockForm = new StockForm();
                    stockForm.setProductId(form.getProductId());
                    stockForm.setSerial(serialCode);
                    stockForm.setCode(secretCode);
                    stockForm.setQuantity(quantity);
                    stockForm.setFaceValue(faceValue);
                    stockForm.setInfomation(information);
                    stockForm.setStatus("AVAILABLE");
                    
                    inventoryService.addOrUpdateStock(stockForm);
                    processedSerials.add(serialCode);
                    importedCount++;
                    
                    log.info("Row {}: Successfully imported serial {}", i + 1, serialCode);
                    
                } catch (Exception e) {
                    String errorMsg = "Dòng " + (i + 1) + ": Lỗi import serial - " + e.getMessage();
                    errors.add(errorMsg);
                    invalidSerials.add(serialCode != null ? serialCode : "Dòng " + (i + 1));
                    skippedCount++;
                    log.error("Row {}: Error importing serial: {}", i + 1, e.getMessage());
                }
            }
            
            // Create JSON file for persistent storage
            String jsonFileName = "serials_" + form.getProductId() + "_" + System.currentTimeMillis() + ".json";
            String jsonFilePath = saveSerialsToJson(form.getProductId(), serials, errors, warnings, duplicateSerials, invalidSerials, jsonFileName);
            log.info("Created JSON file for product {}: {}", form.getProductId(), jsonFilePath);
            
            return new ImportResult(totalRows, importedCount, skippedCount, errors, warnings, duplicateSerials, invalidSerials);
            
        } catch (IOException e) {
            log.error("Error reading Excel file: {}", e.getMessage());
            throw new RuntimeException("Error reading Excel file: " + e.getMessage());
        }
    }
    
    @Override
    @Deprecated
    public int importSerialsFromExcelLegacy(ExcelImportForm form) {
        ImportResult result = importSerialsFromExcel(form);
        return result.getImportedCount();
    }
    
    @Override
    public Map<String, Object> previewExcelImport(ExcelImportForm form) {
        Map<String, Object> result = new HashMap<>();
        try (Workbook workbook = WorkbookFactory.create(form.getExcelFile().getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            
            List<Map<String, Object>> serials = new ArrayList<>();
            List<String> errors = new ArrayList<>();
            List<String> warnings = new ArrayList<>();
            List<String> duplicateSerials = new ArrayList<>();
            List<String> invalidSerials = new ArrayList<>();
            
            int importedCount = 0;
            int skippedCount = 0;
            Set<String> processedSerials = new HashSet<>();
            BigDecimal expectedFaceValue = null;
            
            int totalRows = sheet.getLastRowNum();
            
            // Process Excel data (similar to import but without saving to database)
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                
                String serialCode = null;
                try {
                    serialCode = getCellValueAsString(row.getCell(0));
                    String secretCode = getCellValueAsString(row.getCell(1));
                    Integer quantity = getCellValueAsInteger(row.getCell(2));
                    BigDecimal faceValue = getCellValueAsBigDecimal(row.getCell(3));
                    String information = getCellValueAsString(row.getCell(4));
                    
                    // Validation (same as import method)
                    if (serialCode == null || serialCode.trim().isEmpty()) {
                        String errorMsg = "Dòng " + (i + 1) + ": Serial code trống";
                        errors.add(errorMsg);
                        invalidSerials.add("Dòng " + (i + 1));
                        skippedCount++;
                        continue;
                    }
                    
                    if (quantity == null || quantity < 0) {
                        String errorMsg = "Dòng " + (i + 1) + ": Số lượng không hợp lệ (" + quantity + ")";
                        errors.add(errorMsg);
                        invalidSerials.add(serialCode);
                        skippedCount++;
                        continue;
                    }
                    
                    if (processedSerials.contains(serialCode)) {
                        String warningMsg = "Dòng " + (i + 1) + ": Serial code trùng lặp (" + serialCode + ")";
                        warnings.add(warningMsg);
                        duplicateSerials.add(serialCode);
                        skippedCount++;
                        continue;
                    }
                    
                    // Create serial data for JSON
                    Map<String, Object> serialData = new HashMap<>();
                    serialData.put("serialCode", serialCode);
                    serialData.put("secretCode", secretCode);
                    serialData.put("quantity", quantity);
                    serialData.put("faceValue", faceValue != null ? faceValue.doubleValue() : 0);
                    serialData.put("information", information != null ? information : "");
                    serialData.put("status", "AVAILABLE");
                    serialData.put("importDate", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                    serialData.put("isSold", false);
                    serialData.put("soldDate", null);
                    serialData.put("soldTo", null);
                    
                    serials.add(serialData);
                    processedSerials.add(serialCode);
                    importedCount++;
                    
                } catch (Exception e) {
                    String errorMsg = "Dòng " + (i + 1) + ": Lỗi đọc dữ liệu - " + e.getMessage();
                    errors.add(errorMsg);
                    invalidSerials.add(serialCode != null ? serialCode : "Dòng " + (i + 1));
                    skippedCount++;
                }
            }
            
            // Create JSON file
            String jsonFileName = "serials_" + form.getProductId() + "_" + System.currentTimeMillis() + ".json";
            String jsonFilePath = saveSerialsToJson(form.getProductId(), serials, errors, warnings, duplicateSerials, invalidSerials, jsonFileName);
            
            result.put("success", true);
            result.put("totalRows", totalRows);
            result.put("importedCount", importedCount);
            result.put("skippedCount", skippedCount);
            result.put("serials", serials);
            result.put("errors", errors);
            result.put("warnings", warnings);
            result.put("duplicateSerials", duplicateSerials);
            result.put("invalidSerials", invalidSerials);
            result.put("jsonFilePath", jsonFilePath);
            result.put("jsonFileName", jsonFileName);
            
        } catch (IOException e) {
            log.error("Error reading Excel file: {}", e.getMessage());
            result.put("success", false);
            result.put("error", "Error reading Excel file: " + e.getMessage());
        }
        
        return result;
    }
    
    private String saveSerialsToJson(Long productId, List<Map<String, Object>> serials, 
                                   List<String> errors, List<String> warnings, 
                                   List<String> duplicateSerials, List<String> invalidSerials, 
                                   String fileName) throws IOException {
        
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode rootNode = mapper.createObjectNode();
        
        // Product info
        rootNode.put("productId", productId);
        rootNode.put("importDate", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        rootNode.put("totalRows", serials.size() + errors.size() + warnings.size());
        rootNode.put("importedCount", serials.size());
        rootNode.put("skippedCount", errors.size() + warnings.size());
        
        // Serials array
        ArrayNode serialsArray = mapper.createArrayNode();
        for (Map<String, Object> serial : serials) {
            ObjectNode serialNode = mapper.createObjectNode();
            serialNode.put("serialCode", (String) serial.get("serialCode"));
            serialNode.put("secretCode", (String) serial.get("secretCode"));
            serialNode.put("quantity", (Integer) serial.get("quantity"));
            serialNode.put("faceValue", (Double) serial.get("faceValue"));
            serialNode.put("information", (String) serial.get("information"));
            serialNode.put("status", (String) serial.get("status"));
            serialNode.put("importDate", (String) serial.get("importDate"));
            serialNode.put("isSold", (Boolean) serial.get("isSold"));
            serialNode.putNull("soldDate");
            serialNode.putNull("soldTo");
            serialsArray.add(serialNode);
        }
        rootNode.set("serials", serialsArray);
        
        // Errors and warnings
        ArrayNode errorsArray = mapper.createArrayNode();
        errors.forEach(errorsArray::add);
        rootNode.set("errors", errorsArray);
        
        ArrayNode warningsArray = mapper.createArrayNode();
        warnings.forEach(warningsArray::add);
        rootNode.set("warnings", warningsArray);
        
        ArrayNode duplicatesArray = mapper.createArrayNode();
        duplicateSerials.forEach(duplicatesArray::add);
        rootNode.set("duplicateSerials", duplicatesArray);
        
        ArrayNode invalidArray = mapper.createArrayNode();
        invalidSerials.forEach(invalidArray::add);
        rootNode.set("invalidSerials", invalidArray);
        
        // Save to file
        Path jsonDir = Paths.get("src/main/resources/assets/json");
        Files.createDirectories(jsonDir);
        Path jsonFile = jsonDir.resolve(fileName);
        
        mapper.writerWithDefaultPrettyPrinter().writeValue(jsonFile.toFile(), rootNode);
        
        return jsonFile.toString();
    }
    
    @Override
    public List<Map<String, Object>> getProductSerials(Long productId) {
        try {
            System.out.println("DEBUG: Getting serials for product ID: " + productId);
            String jsonFilePath = getLatestJsonFile(productId);
            if (jsonFilePath == null) {
                System.out.println("DEBUG: No JSON file found for product ID: " + productId);
                log.warn("No JSON file found for product ID: {}", productId);
                return new ArrayList<>();
            }
            System.out.println("DEBUG: Found JSON file: " + jsonFilePath);
            
            ObjectMapper mapper = new ObjectMapper();
            Path jsonPath = Paths.get(jsonFilePath);
            
            if (!Files.exists(jsonPath)) {
                log.warn("JSON file does not exist: {}", jsonFilePath);
                return new ArrayList<>();
            }
            
            JsonNode rootNode = mapper.readTree(jsonPath.toFile());
            JsonNode serialsNode = rootNode.get("serials");
            
            if (serialsNode == null || !serialsNode.isArray()) {
                log.warn("No serials array found in JSON file: {}", jsonFilePath);
                return new ArrayList<>();
            }
            
            List<Map<String, Object>> serials = new ArrayList<>();
            for (JsonNode serialNode : serialsNode) {
                Map<String, Object> serial = new HashMap<>();
                serial.put("serialCode", serialNode.get("serialCode").asText());
                serial.put("secretCode", serialNode.get("secretCode").asText());
                serial.put("quantity", serialNode.get("quantity").asInt());
                serial.put("faceValue", serialNode.get("faceValue").asDouble());
                serial.put("information", serialNode.get("information").asText());
                serial.put("status", serialNode.get("status").asText());
                serial.put("importDate", serialNode.get("importDate").asText());
                serial.put("isSold", serialNode.get("isSold").asBoolean());
                serial.put("soldDate", serialNode.get("soldDate").isNull() ? null : serialNode.get("soldDate").asText());
                serial.put("soldTo", serialNode.get("soldTo").isNull() ? null : serialNode.get("soldTo").asText());
                serials.add(serial);
            }
            
            log.info("Loaded {} serials for product ID: {}", serials.size(), productId);
            return serials;
            
        } catch (Exception e) {
            log.error("Error reading serial data for product ID {}: {}", productId, e.getMessage());
            return new ArrayList<>();
        }
    }
    
    @Override
    public String getLatestJsonFile(Long productId) {
        try {
            System.out.println("DEBUG: Looking for JSON files for product ID: " + productId);
            Path jsonDir = Paths.get("src/main/resources/assets/json");
            if (!Files.exists(jsonDir)) {
                System.out.println("DEBUG: JSON directory does not exist: " + jsonDir);
                log.warn("JSON directory does not exist: {}", jsonDir);
                return null;
            }
            System.out.println("DEBUG: JSON directory exists: " + jsonDir);
            
            // Find all JSON files for this product
            List<Path> jsonFiles = Files.list(jsonDir)
                    .filter(path -> path.getFileName().toString().startsWith("serials_" + productId + "_"))
                    .filter(path -> path.getFileName().toString().endsWith(".json"))
                    .sorted((p1, p2) -> {
                        try {
                            // Sort by timestamp (latest first)
                            String name1 = p1.getFileName().toString();
                            String name2 = p2.getFileName().toString();
                            long timestamp1 = Long.parseLong(name1.substring(name1.lastIndexOf("_") + 1, name1.lastIndexOf(".")));
                            long timestamp2 = Long.parseLong(name2.substring(name2.lastIndexOf("_") + 1, name2.lastIndexOf(".")));
                            return Long.compare(timestamp2, timestamp1);
                        } catch (Exception e) {
                            return 0;
                        }
                    })
                    .collect(Collectors.toList());
            
            System.out.println("DEBUG: Found " + jsonFiles.size() + " JSON files for product ID " + productId);
            if (jsonFiles.isEmpty()) {
                System.out.println("DEBUG: No JSON files found for product ID: " + productId);
                log.warn("No JSON files found for product ID: {}", productId);
                return null;
            }
            
            String latestFile = jsonFiles.get(0).toString();
            System.out.println("DEBUG: Latest JSON file for product ID " + productId + ": " + latestFile);
            log.info("Found latest JSON file for product ID {}: {}", productId, latestFile);
            return latestFile;
            
        } catch (Exception e) {
            log.error("Error finding JSON file for product ID {}: {}", productId, e.getMessage());
            return null;
        }
    }

    @Override
    public boolean validateExcelFormat(MultipartFile file) {
        try {
            if (!file.getOriginalFilename().toLowerCase().endsWith(".xlsx")) {
                return false;
            }
            
            try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
                if (workbook.getNumberOfSheets() == 0) {
                    return false;
                }
                
                Sheet sheet = workbook.getSheetAt(0);
                Row headerRow = sheet.getRow(0);
                if (headerRow == null) {
                    return false;
                }
                
                // Check header columns
                String[] expectedHeaders = {"Serial Code", "Secret Code", "Quantity", "Face Value", "Information"};
                for (int i = 0; i < expectedHeaders.length; i++) {
                    String cellValue = getCellValueAsString(headerRow.getCell(i));
                    if (!expectedHeaders[i].equalsIgnoreCase(cellValue)) {
                        log.warn("Expected header '{}' but found '{}'", expectedHeaders[i], cellValue);
                        return false;
                    }
                }
                
                return true;
            }
        } catch (Exception e) {
            log.error("Error validating Excel format: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public byte[] generateExcelTemplate(Long productId) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Import Template");
            
            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {"Serial Code", "Secret Code", "Quantity", "Face Value", "Information"};
            
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // Add sample data based on product type
            if (productId == 0L) {
                // Generic template
                Row sampleRow1 = sheet.createRow(1);
                sampleRow1.createCell(0).setCellValue("1234567890123456");
                sampleRow1.createCell(1).setCellValue("ABCD1234");
                sampleRow1.createCell(2).setCellValue(1);
                sampleRow1.createCell(3).setCellValue(10000);
                sampleRow1.createCell(4).setCellValue("Thẻ điện thoại 10K");
                
                Row sampleRow2 = sheet.createRow(2);
                sampleRow2.createCell(0).setCellValue("1234567890123457");
                sampleRow2.createCell(1).setCellValue("ABCD1235");
                sampleRow2.createCell(2).setCellValue(1);
                sampleRow2.createCell(3).setCellValue(10000);
                sampleRow2.createCell(4).setCellValue("Thẻ điện thoại 10K");
            } else {
                // Specific product template
                Product product = productRepository.findById(productId).orElse(null);
                if (product != null && isTelecomCard(product.getProductType())) {
                    Row sampleRow1 = sheet.createRow(1);
                    sampleRow1.createCell(0).setCellValue("1234567890123456");
                    sampleRow1.createCell(1).setCellValue("ABCD1234");
                    sampleRow1.createCell(2).setCellValue(1);
                    sampleRow1.createCell(3).setCellValue(product.getPrice().intValue());
                    sampleRow1.createCell(4).setCellValue("Thẻ " + product.getPrice().intValue() + "K");
                    
                    Row sampleRow2 = sheet.createRow(2);
                    sampleRow2.createCell(0).setCellValue("1234567890123457");
                    sampleRow2.createCell(1).setCellValue("ABCD1235");
                    sampleRow2.createCell(2).setCellValue(1);
                    sampleRow2.createCell(3).setCellValue(product.getPrice().intValue());
                    sampleRow2.createCell(4).setCellValue("Thẻ " + product.getPrice().intValue() + "K");
                } else {
                    // Default sample
                    Row sampleRow1 = sheet.createRow(1);
                    sampleRow1.createCell(0).setCellValue("SR-001");
                    sampleRow1.createCell(1).setCellValue("SEC-001");
                    sampleRow1.createCell(2).setCellValue(1);
                    sampleRow1.createCell(3).setCellValue(10000);
                    sampleRow1.createCell(4).setCellValue("Mô tả serial 1");
                }
            }
            
            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            // Convert to byte array
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
            
        } catch (IOException e) {
            log.error("Error generating Excel template: {}", e.getMessage());
            throw new RuntimeException("Error generating Excel template: " + e.getMessage());
        }
    }
    
    private String getCellValueAsString(Cell cell) {
        if (cell == null) return null;
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    return String.valueOf((long) cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return null;
        }
    }
    
    private Integer getCellValueAsInteger(Cell cell) {
        if (cell == null) return null;
        
        switch (cell.getCellType()) {
            case NUMERIC:
                return (int) cell.getNumericCellValue();
            case STRING:
                try {
                    return Integer.parseInt(cell.getStringCellValue().trim());
                } catch (NumberFormatException e) {
                    return null;
                }
            default:
                return null;
        }
    }
    
    private BigDecimal getCellValueAsBigDecimal(Cell cell) {
        if (cell == null) return null;
        
        switch (cell.getCellType()) {
            case NUMERIC:
                return BigDecimal.valueOf(cell.getNumericCellValue());
            case STRING:
                try {
                    return new BigDecimal(cell.getStringCellValue().trim());
                } catch (NumberFormatException e) {
                    return null;
                }
            default:
                return null;
        }
    }
    
    /**
     * Check if the product type is a telecom card (phone card or data package)
     */
    private boolean isTelecomCard(ProductType productType) {
        return productType == ProductType.VIETTEL ||
               productType == ProductType.MOBIFONE ||
               productType == ProductType.VINAPHONE ||
               productType == ProductType.VIETTEL_DATA ||
               productType == ProductType.MOBIFONE_DATA ||
               productType == ProductType.VINAPHONE_DATA;
    }
}

package fpt.swp.springmvctt.itp.service;

import fpt.swp.springmvctt.itp.dto.request.ExcelImportForm;
import fpt.swp.springmvctt.itp.dto.response.ImportResult;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface ExcelImportService {

    ImportResult importSerialsFromExcel(ExcelImportForm form);
    Map<String, Object> previewExcelImport(ExcelImportForm form);
    List<Map<String, Object>> getProductSerials(Long productId);
    String getLatestJsonFile(Long productId);
    @Deprecated
    int importSerialsFromExcelLegacy(ExcelImportForm form);
    boolean validateExcelFormat(MultipartFile file);
    byte[] generateExcelTemplate(Long productId);
}

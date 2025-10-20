package fpt.swp.springmvctt.itp.dto.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotNull;

@Data
public class ExcelImportForm {
    @NotNull
    private Long productId;
    @NotNull
    private MultipartFile excelFile;
    private boolean overrideExisting = false;
}

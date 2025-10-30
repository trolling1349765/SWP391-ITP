package fpt.swp.springmvctt.itp.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImportResult {
    private int totalRows;
    private int importedCount;
    private int skippedCount;
    private List<String> errors;
    private List<String> warnings;
    private List<String> duplicateSerials;
    private List<String> invalidSerials;
    
    public ImportResult(int totalRows, int importedCount, int skippedCount) {
        this.totalRows = totalRows;
        this.importedCount = importedCount;
        this.skippedCount = skippedCount;
        this.errors = List.of();
        this.warnings = List.of();
        this.duplicateSerials = List.of();
        this.invalidSerials = List.of();
    }
}

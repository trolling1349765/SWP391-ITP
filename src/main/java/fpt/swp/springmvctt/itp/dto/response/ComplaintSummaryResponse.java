package fpt.swp.springmvctt.itp.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ComplaintSummaryResponse {
    private String title;
    private long count;
    private String status; // PENDING / PROCESSING / DONE
}

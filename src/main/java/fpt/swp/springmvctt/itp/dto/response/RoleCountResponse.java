package fpt.swp.springmvctt.itp.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RoleCountResponse {
    private String role;
    private long count;
}

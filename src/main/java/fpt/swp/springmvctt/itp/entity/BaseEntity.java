package fpt.swp.springmvctt.itp.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@MappedSuperclass
public abstract class BaseEntity {
    @Column(name = "create_by")
    private String createBy;

    @Column(name = "create_at" )
    private LocalDate createAt;

    @Column(name = "update_at")
    private LocalDate updateAt;

    @Column(name = "is_deleted")
    private Boolean isDeleted;

    @Column(name = "delete_by")
    private String deleteBy;
}

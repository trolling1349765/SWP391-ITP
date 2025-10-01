package fpt.swp.springmvctt.itp.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@MappedSuperclass
public abstract class BaseEntity {
    @Column(name = "create_by")
    private String createBy;

    @Column(name = "create_at")
    @Temporal(TemporalType.TIMESTAMP) // để ánh xạ đúng kiểu DATETIME
    private Date createAt;

    @Column(name = "update_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updateAt;

    @Column(name = "is_deleted")
    private Boolean isDeleted;

    @Column(name = "delete_by")
    private String deleteBy;
}

package fpt.swp.springmvctt.itp.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@MappedSuperclass
@Getter @Setter
public abstract class BaseEntity {
    @Column(name="create_at")
    private LocalDateTime createAt;
    @Column(name="update_at")
    private LocalDateTime updateAt;
    @Column(name="is_deleted")
    private Boolean isDeleted;
    @Column(name="create_by")
    private String createBy;
    @Column(name="update_by")
    private String updateBy;
    @Column(name="delete_by")
    private String deleteBy;

    @PrePersist protected void onCreate() {
        if (createAt == null)
            createAt = LocalDateTime.now();
        if (isDeleted == null)
            isDeleted = false;
    }
    @PreUpdate  protected void onUpdate() {
        updateAt = LocalDateTime.now(); }
}

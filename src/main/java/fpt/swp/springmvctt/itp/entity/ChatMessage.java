package fpt.swp.springmvctt.itp.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "chat_messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100)
    private String email;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(length = 255)
    private String attachment;

    @Column(length = 50)
    private String status;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}


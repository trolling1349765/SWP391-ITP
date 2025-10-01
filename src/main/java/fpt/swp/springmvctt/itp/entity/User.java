package fpt.swp.springmvctt.itp.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String  username;

    @Column(unique = true, nullable = false, length = 100)
    private String  email;

    @Column(nullable = false, length = 255)
    private String  password;

    @Column(length = 20)
    private String  Phone;

    @Column()
    private double  balance;

    @Column()
    private String  status;


}

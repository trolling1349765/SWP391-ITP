package fpt.swp.springmvctt.itp.entity;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

public class User {

    @Id

    private Long id;
    private String  name;

}

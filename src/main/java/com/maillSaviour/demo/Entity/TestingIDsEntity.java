package com.maillSaviour.demo.Entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "Testing_id")
@Data
public class TestingIDsEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;
}
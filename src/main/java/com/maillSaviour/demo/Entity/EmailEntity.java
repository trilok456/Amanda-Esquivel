package com.maillSaviour.demo.Entity;

import jakarta.persistence.*;
import lombok.Data;
    @Entity
    @Table(name = "emails")
    @Data
    public class EmailEntity {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(name = "email", nullable = false, length = 255)
        private String email;

    @ManyToOne
    @JoinColumn(name = "body_id", nullable = false)
    private BodyEntity body;
}
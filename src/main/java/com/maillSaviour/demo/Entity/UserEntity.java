package com.maillSaviour.demo.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "M0BXpZs")
@Getter
@Setter
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String username;

    private String password;

    private String sessionId;

    private String host;

    private String name;

    private Boolean isAssigned;

    private int emailCount;

    private LocalDate validUntil;

    private int emailLimit;

    private int linkNumber;
}
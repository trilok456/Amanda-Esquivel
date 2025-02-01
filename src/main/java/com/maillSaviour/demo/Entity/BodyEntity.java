package com.maillSaviour.demo.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
@Entity
@Table(name = "bodies")
@Getter
@Setter
public class BodyEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "body", nullable = false, unique = true, length = 10000)
    private String body;

    @Column(name = "user_ka_naam", nullable = false, length = 100)
    private String userKaNaam;

    @OneToMany(mappedBy = "body", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EmailEntity> emails = new ArrayList<>();
}

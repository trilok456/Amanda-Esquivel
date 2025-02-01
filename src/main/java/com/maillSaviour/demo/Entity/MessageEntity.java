package com.maillSaviour.demo.Entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "Messages")
@Getter
@Setter
public class MessageEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String messageName;

    @Column(unique = true)
    private String meassageContent;
}
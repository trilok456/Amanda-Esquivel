package com.maillSaviour.demo.Entity.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmailRequestDto {
    @NotBlank
    @Email
    private String sentFrom;

    @NotBlank
    @Size(min = 19, max = 19)
    private String appPass;

    @NotBlank
    @Size(max = 150)
    private String subject;

    @NotBlank
    @Size(max = 10000)
    private String body;

    private String eMails;

    @NotBlank
    @Size(min = 3, max = 40)
    private String firstName;

    private String sessionId;
}
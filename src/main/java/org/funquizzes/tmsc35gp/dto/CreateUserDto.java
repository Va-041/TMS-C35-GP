package org.funquizzes.tmsc35gp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateUserDto {

    private String name;
    private String username;
    private String password;
}

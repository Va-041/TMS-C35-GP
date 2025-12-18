package org.funquizzes.tmsc35gp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateProfileDto {

    private String name;
    private String username;
    private String email;
    private String biography;
    private boolean isPublicProfile;
    private String avatarUrl;
}
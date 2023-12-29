package jwt.security.user.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChangePasswordReq {

    private String currentPassword;
    private String newPassword;
    private String confirmationPassword;
}

package jwt.security.auth.dto;

import jwt.security.domain.user.Role;
import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class RegisterReq {

  private String name;
  private String email;
  private String password;
  private Role role;
}

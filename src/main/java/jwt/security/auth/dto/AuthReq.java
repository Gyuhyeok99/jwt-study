package jwt.security.auth.dto;

import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class AuthReq {

  private String email;
  private String password;
}

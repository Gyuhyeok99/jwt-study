package jwt.security.demo;

import io.swagger.v3.oas.annotations.Hidden;
import jwt.security.config.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/demo-controller")
@Hidden
public class DemoController {

  @GetMapping
  public ApiResponse<String> sayHello() {
    return ApiResponse.onSuccess("Hello from secured endpoint");
  }

}

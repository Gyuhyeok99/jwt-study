package jwt.security.demo;

import io.swagger.v3.oas.annotations.Hidden;
import jwt.security.config.ApiResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @GetMapping
    @PreAuthorize("hasAuthority('admin:read')")
    public ApiResponse<String> get() {
        return ApiResponse.onSuccess("GET:: admin controller");
    }
    @PostMapping
    @PreAuthorize("hasAuthority('admin:create')")
    @Hidden
    public ApiResponse<String> post() {
        return ApiResponse.onSuccess("POST:: admin controller");
    }
    @PutMapping
    @PreAuthorize("hasAuthority('admin:update')")
    @Hidden
    public ApiResponse<String> put() {
        return ApiResponse.onSuccess("PUT:: admin controller");
    }
    @DeleteMapping
    @PreAuthorize("hasAuthority('admin:delete')")
    @Hidden
    public ApiResponse<String> delete() {
        return ApiResponse.onSuccess("DELETE:: admin controller");
    }
}

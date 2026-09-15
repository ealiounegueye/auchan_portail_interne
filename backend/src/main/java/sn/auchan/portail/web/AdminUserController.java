package sn.auchan.portail.web;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import sn.auchan.portail.dto.AccessRequest;
import sn.auchan.portail.dto.UserRequest;
import sn.auchan.portail.dto.UserResponse;
import sn.auchan.portail.service.UserService;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final UserService userService;

    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<UserResponse> findAll(Authentication authentication) {
        return userService.findAll(authentication.getName());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse create(@Valid @RequestBody UserRequest request, Authentication authentication) {
        return userService.create(request, authentication.getName());
    }

    @PutMapping("/{id}")
    public UserResponse update(
            @PathVariable Long id,
            @Valid @RequestBody UserRequest request,
            Authentication authentication
    ) {
        return userService.update(id, request, authentication.getName());
    }

    @PutMapping("/{id}/access")
    public UserResponse updateAccess(
            @PathVariable Long id,
            @RequestBody AccessRequest request,
            Authentication authentication
    ) {
        return userService.updateAccess(id, request, authentication.getName());
    }
}

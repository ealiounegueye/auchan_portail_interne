package sn.auchan.portail.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.auchan.portail.domain.UserAccount;
import sn.auchan.portail.dto.AuthResponse;
import sn.auchan.portail.dto.LoginRequest;
import sn.auchan.portail.dto.UserResponse;
import sn.auchan.portail.repository.UserAccountRepository;
import sn.auchan.portail.security.JwtService;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserAccountRepository users;
    private final JwtService jwtService;
    private final UserService userService;

    public AuthService(
            AuthenticationManager authenticationManager,
            UserAccountRepository users,
            JwtService jwtService,
            UserService userService
    ) {
        this.authenticationManager = authenticationManager;
        this.users = users;
        this.jwtService = jwtService;
        this.userService = userService;
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        UserAccount user = users.findByEmailIgnoreCase(auth.getName())
                .orElseThrow();
        return new AuthResponse(jwtService.generateToken(user), userService.toResponse(user));
    }

    @Transactional(readOnly = true)
    public UserResponse me(String email) {
        UserAccount user = users.findByEmailIgnoreCase(email)
                .orElseThrow();
        return userService.toResponse(user);
    }
}

package com.thanapon.bbl_training_service.service.imp;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.thanapon.bbl_training_service.dto.request.LoginRequestDto;
import com.thanapon.bbl_training_service.dto.response.LoginResponseDto;
import com.thanapon.bbl_training_service.entity.UserEntity;
import com.thanapon.bbl_training_service.exception.InvalidCredentialsException;
import com.thanapon.bbl_training_service.repository.UserRepository;
import com.thanapon.bbl_training_service.security.JwtService;
import com.thanapon.bbl_training_service.service.AuthService;

@Service
public class AuthServiceImp implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final String dummyPasswordHash;

    public AuthServiceImp(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        // A fixed, validly-formatted hash with no known matching plaintext.
        // Compared against on every login where the username doesn't exist,
        // so lookup cost (BCrypt is deliberately slow) can't be used to
        // enumerate valid usernames via response timing.
        this.dummyPasswordHash = passwordEncoder.encode("no-such-password-will-ever-match-this");
    }

    @Override
    public LoginResponseDto login(LoginRequestDto loginRequestDto) {
        UserEntity user = userRepository.findByUsername(loginRequestDto.getUsername()).orElse(null);
        String hashToCompare = user != null ? user.getPassword() : dummyPasswordHash;
        boolean passwordMatches = passwordEncoder.matches(loginRequestDto.getPassword(), hashToCompare);

        if (user == null || !passwordMatches) {
            throw new InvalidCredentialsException("Invalid username or password");
        }

        String token = jwtService.generateToken(user.getId(), user.getUsername());

        return new LoginResponseDto(token);
    }
}

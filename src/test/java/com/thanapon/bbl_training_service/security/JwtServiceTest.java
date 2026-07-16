package com.thanapon.bbl_training_service.security;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private final JwtService jwtService = new JwtService("", "", 3_600_000L);

    @Test
    void generateToken_shouldProduceTokenContainingUserIdAndUsername() {
        String token = jwtService.generateToken(1L, "Bret");

        assertThat(jwtService.extractUserId(token)).isEqualTo(1L);
        assertThat(jwtService.extractUsername(token)).isEqualTo("Bret");
    }

    @Test
    void isTokenValid_shouldReturnTrue_forFreshlyGeneratedToken() {
        String token = jwtService.generateToken(1L, "Bret");

        assertThat(jwtService.isTokenValid(token)).isTrue();
    }

    @Test
    void isTokenValid_shouldReturnFalse_forGarbageToken() {
        assertThat(jwtService.isTokenValid("not-a-real-token")).isFalse();
    }

    @Test
    void isTokenValid_shouldReturnFalse_whenSignedWithADifferentKeyPair() {
        // No keys configured on either instance, so each one generates its own
        // independent ephemeral RSA key pair - exactly what's needed here.
        JwtService otherJwtService = new JwtService("", "", 3_600_000L);
        String token = otherJwtService.generateToken(1L, "Bret");

        assertThat(jwtService.isTokenValid(token)).isFalse();
    }

    @Test
    void isTokenValid_shouldReturnFalse_forExpiredToken() {
        JwtService expiredJwtService = new JwtService("", "", -1_000L);
        String token = expiredJwtService.generateToken(1L, "Bret");

        assertThat(expiredJwtService.isTokenValid(token)).isFalse();
    }

    @Test
    void constructor_shouldUseConfiguredKeyPair_whenBase64KeysAreProvided() throws NoSuchAlgorithmException {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair keyPair = generator.generateKeyPair();
        String privateKeyBase64 = Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());
        String publicKeyBase64 = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());

        JwtService configuredJwtService = new JwtService(privateKeyBase64, publicKeyBase64, 3_600_000L);
        String token = configuredJwtService.generateToken(1L, "Bret");

        assertThat(configuredJwtService.isTokenValid(token)).isTrue();
        assertThat(configuredJwtService.extractUserId(token)).isEqualTo(1L);
    }
}

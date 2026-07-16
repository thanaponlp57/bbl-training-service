package com.thanapon.bbl_training_service.validation;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.HandlerMapping;

import jakarta.servlet.http.HttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class PathExcludedIdResolverTest {

    @Mock
    private HttpServletRequest request;

    private PathExcludedIdResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new PathExcludedIdResolver(request);
    }

    @Test
    void resolve_shouldReturnId_whenPathVariablePresent() {
        given(request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE))
                .willReturn(Map.of("id", "1"));

        assertThat(resolver.resolve("id")).isEqualTo(1L);
    }

    @Test
    void resolve_shouldThrow_whenPathVariableIsMissing() {
        given(request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE)).willReturn(Map.of());

        assertThatThrownBy(() -> resolver.resolve("id")).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void resolve_shouldThrow_whenAttributeIsNotAMap() {
        given(request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE)).willReturn(null);

        assertThatThrownBy(() -> resolver.resolve("id")).isInstanceOf(IllegalStateException.class);
    }
}

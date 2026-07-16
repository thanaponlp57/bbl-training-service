package com.thanapon.bbl_training_service.validation;

import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerMapping;

import jakarta.servlet.http.HttpServletRequest;

@Component
public class PathExcludedIdResolver {

    private final HttpServletRequest request;

    public PathExcludedIdResolver(HttpServletRequest request) {
        this.request = request;
    }

    public long resolve(String pathVariable) {
        Object attribute = request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        if (!(attribute instanceof Map<?, ?> pathVariables) || pathVariables.get(pathVariable) == null) {
            throw new IllegalStateException(
                    "\"" + pathVariable + "\" path variable is required to exclude the current record from a uniqueness check");
        }

        return Long.parseLong(pathVariables.get(pathVariable).toString());
    }
}

package pe.edu.utp.proyecto_integrador_casachantilly.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import pe.edu.utp.proyecto_integrador_casachantilly.comun.dto.ApiErrorData;
import pe.edu.utp.proyecto_integrador_casachantilly.comun.dto.ApiResponse;

import java.io.IOException;

@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RestAuthenticationEntryPoint.class);

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException, ServletException {
        log.warn("No autenticado en {} {}: {}",
                request.getMethod(),
                request.getRequestURI(),
                authException.getMessage());
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        ApiResponse<ApiErrorData> body = new ApiResponse<>(
                false,
                "No autenticado",
                ApiErrorData.of("UNAUTHORIZED", request.getRequestURI()));
        objectMapper.writeValue(response.getWriter(), body);
    }
}

package pe.edu.utp.proyecto_integrador_casachantilly.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import pe.edu.utp.proyecto_integrador_casachantilly.comun.dto.ApiErrorData;
import pe.edu.utp.proyecto_integrador_casachantilly.comun.dto.ApiResponse;

import java.io.IOException;

@Component
public class RestAccessDeniedHandler implements AccessDeniedHandler {

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException)
            throws IOException, ServletException {
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        ApiResponse<ApiErrorData> body = new ApiResponse<>(
                false,
                "No autorizado para este recurso",
                ApiErrorData.of("FORBIDDEN", request.getRequestURI()));
        objectMapper.writeValue(response.getWriter(), body);
    }
}

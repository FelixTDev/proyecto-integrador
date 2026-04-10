package pe.edu.utp.proyecto_integrador_casachantilly.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.utp.proyecto_integrador_casachantilly.auth.repositorio.SesionRepository;

import java.time.LocalDateTime;

@Component
public class SesionCleanupScheduler {

    private static final Logger log = LoggerFactory.getLogger(SesionCleanupScheduler.class);

    @Autowired private SesionRepository sesionRepository;

    @Scheduled(fixedRate = 600_000)
    @Transactional
    public void limpiarSesionesExpiradas() {
        try {
            LocalDateTime ahora = LocalDateTime.now();
            int expiradas = sesionRepository.expirarSesionesActivas(ahora, "EXPIRADA_INACTIVIDAD");
            if (expiradas > 0) {
                log.info("Sesiones activas expiradas: {}", expiradas);
            }
        } catch (Exception e) {
            log.warn("Error al expirar sesiones: {}", e.getMessage());
        }
    }

    @Scheduled(cron = "0 15 3 * * *")
    @Transactional
    public void limpiarHistorialAntiguo() {
        try {
            LocalDateTime corte = LocalDateTime.now().minusDays(30);
            int eliminadas = sesionRepository.eliminarSesionesRevocadasAntiguas(corte);
            if (eliminadas > 0) {
                log.info("Sesiones revocadas antiguas eliminadas: {}", eliminadas);
            }
        } catch (Exception e) {
            log.warn("Error al limpiar historial de sesiones: {}", e.getMessage());
        }
    }
}

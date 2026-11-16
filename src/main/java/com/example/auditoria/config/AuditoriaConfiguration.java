package com.example.auditoria.config;

import com.example.auditoria.usecase.CerrarHallazgoUseCase;
import com.example.auditoria.usecase.ConsultarHallazgoUseCase;
import com.example.auditoria.usecase.ConsultarHistorialUseCase;
import com.example.auditoria.usecase.IniciarRemediacionUseCase;
import com.example.auditoria.usecase.ReabrirHallazgoUseCase;
import com.example.auditoria.usecase.RegistrarHallazgoUseCase;
import com.example.auditoria.usecase.impl.CerrarHallazgoService;
import com.example.auditoria.usecase.impl.ConsultarHallazgoService;
import com.example.auditoria.usecase.impl.ConsultarHistorialService;
import com.example.auditoria.usecase.impl.IniciarRemediacionService;
import com.example.auditoria.usecase.impl.ReabrirHallazgoService;
import com.example.auditoria.usecase.impl.RegistrarHallazgoService;
import com.example.auditoria.usecase.port.HallazgoRepositoryPort;
import com.example.auditoria.usecase.port.HistorialAuditoriaPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Wiring explicito (Frameworks & Drivers). Los casos de uso no conocen Spring, asi que la
 * transaccion se aplica aqui envolviendo cada caso de uso de escritura con TransactionTemplate.
 * Asi el UPDATE del hallazgo y el INSERT en la bitacora ocurren en la MISMA transaccion.
 */
@Configuration
public class AuditoriaConfiguration {

    @Bean
    public RegistrarHallazgoUseCase registrarHallazgoUseCase(HallazgoRepositoryPort repo, HistorialAuditoriaPort historial,
                                                             TransactionTemplate tx) {
        RegistrarHallazgoService service = new RegistrarHallazgoService(repo, historial);
        return (titulo, descripcion, area, severidad, fecha, usuario) ->
                tx.execute(s -> service.ejecutar(titulo, descripcion, area, severidad, fecha, usuario));
    }

    @Bean
    public IniciarRemediacionUseCase iniciarRemediacionUseCase(HallazgoRepositoryPort repo, HistorialAuditoriaPort historial,
                                                               TransactionTemplate tx) {
        IniciarRemediacionService service = new IniciarRemediacionService(repo, historial);
        return (id, responsable, fechaLimite, notas, usuario) ->
                tx.executeWithoutResult(s -> service.ejecutar(id, responsable, fechaLimite, notas, usuario));
    }

    @Bean
    public CerrarHallazgoUseCase cerrarHallazgoUseCase(HallazgoRepositoryPort repo, HistorialAuditoriaPort historial,
                                                       TransactionTemplate tx) {
        CerrarHallazgoService service = new CerrarHallazgoService(repo, historial);
        return (id, usuario) -> tx.executeWithoutResult(s -> service.ejecutar(id, usuario));
    }

    @Bean
    public ReabrirHallazgoUseCase reabrirHallazgoUseCase(HallazgoRepositoryPort repo, HistorialAuditoriaPort historial,
                                                         TransactionTemplate tx) {
        ReabrirHallazgoService service = new ReabrirHallazgoService(repo, historial);
        return (id, motivo, usuario) -> tx.executeWithoutResult(s -> service.ejecutar(id, motivo, usuario));
    }

    @Bean
    public ConsultarHallazgoUseCase consultarHallazgoUseCase(HallazgoRepositoryPort repo) {
        return new ConsultarHallazgoService(repo);
    }

    @Bean
    public ConsultarHistorialUseCase consultarHistorialUseCase(HallazgoRepositoryPort repo, HistorialAuditoriaPort historial) {
        return new ConsultarHistorialService(repo, historial);
    }
}

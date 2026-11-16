package com.example.auditoria.config;

import com.example.auditoria.usecase.CerrarHallazgoUseCase;
import com.example.auditoria.usecase.ConsultarHallazgoUseCase;
import com.example.auditoria.usecase.IniciarRemediacionUseCase;
import com.example.auditoria.usecase.ReabrirHallazgoUseCase;
import com.example.auditoria.usecase.RegistrarHallazgoUseCase;
import com.example.auditoria.usecase.impl.CerrarHallazgoService;
import com.example.auditoria.usecase.impl.ConsultarHallazgoService;
import com.example.auditoria.usecase.impl.IniciarRemediacionService;
import com.example.auditoria.usecase.impl.ReabrirHallazgoService;
import com.example.auditoria.usecase.impl.RegistrarHallazgoService;
import com.example.auditoria.usecase.port.HallazgoRepositoryPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Wiring explicito (Frameworks & Drivers). Los casos de uso no conocen Spring, asi que la
 * transaccion se aplica aqui envolviendo cada caso de uso de escritura con TransactionTemplate.
 */
@Configuration
public class AuditoriaConfiguration {

    @Bean
    public RegistrarHallazgoUseCase registrarHallazgoUseCase(HallazgoRepositoryPort repo, TransactionTemplate tx) {
        RegistrarHallazgoService service = new RegistrarHallazgoService(repo);
        return (titulo, descripcion, area, severidad, fecha) ->
                tx.execute(s -> service.ejecutar(titulo, descripcion, area, severidad, fecha));
    }

    @Bean
    public IniciarRemediacionUseCase iniciarRemediacionUseCase(HallazgoRepositoryPort repo, TransactionTemplate tx) {
        IniciarRemediacionService service = new IniciarRemediacionService(repo);
        return (id, responsable, fechaLimite, notas) ->
                tx.executeWithoutResult(s -> service.ejecutar(id, responsable, fechaLimite, notas));
    }

    @Bean
    public CerrarHallazgoUseCase cerrarHallazgoUseCase(HallazgoRepositoryPort repo, TransactionTemplate tx) {
        CerrarHallazgoService service = new CerrarHallazgoService(repo);
        return id -> tx.executeWithoutResult(s -> service.ejecutar(id));
    }

    @Bean
    public ReabrirHallazgoUseCase reabrirHallazgoUseCase(HallazgoRepositoryPort repo, TransactionTemplate tx) {
        ReabrirHallazgoService service = new ReabrirHallazgoService(repo);
        return (id, motivo) -> tx.executeWithoutResult(s -> service.ejecutar(id, motivo));
    }

    @Bean
    public ConsultarHallazgoUseCase consultarHallazgoUseCase(HallazgoRepositoryPort repo) {
        return new ConsultarHallazgoService(repo);
    }
}

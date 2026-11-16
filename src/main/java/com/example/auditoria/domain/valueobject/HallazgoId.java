package com.example.auditoria.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

/** Value Object con identidad tipada: evita confundir el id de un hallazgo con cualquier otro UUID o String. */
public record HallazgoId(UUID valor) {

    public HallazgoId {
        Objects.requireNonNull(valor, "HallazgoId no puede ser nulo");
    }

    public static HallazgoId nuevo() {
        return new HallazgoId(UUID.randomUUID());
    }

    public static HallazgoId de(String texto) {
        return new HallazgoId(UUID.fromString(texto));
    }

    @Override
    public String toString() {
        return valor.toString();
    }
}

package com.gr4.owlaudit.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Composite primary key for {@link AsignacionRegla} linking an {@link Asignacion} with a {@link Regla}.
 */
public class AsignacionReglaId implements Serializable {
    private Long asignacionId;
    private Long reglaId;

    public AsignacionReglaId() {}

    public AsignacionReglaId(Long asignacionId, Long reglaId) {
        this.asignacionId = asignacionId;
        this.reglaId = reglaId;
    }

    public Long getAsignacionId() { return asignacionId; }
    public void setAsignacionId(Long asignacionId) { this.asignacionId = asignacionId; }
    public Long getReglaId() { return reglaId; }
    public void setReglaId(Long reglaId) { this.reglaId = reglaId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AsignacionReglaId that = (AsignacionReglaId) o;
        return Objects.equals(asignacionId, that.asignacionId) &&
               Objects.equals(reglaId, that.reglaId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(asignacionId, reglaId);
    }
}


package com.gr4.owlaudit.dao;

import com.gr4.owlaudit.model.Auditoria;

public interface AuditoriaDAO {
    void registrarAuditoriaEnElHistorial(Auditoria auditoria);
}

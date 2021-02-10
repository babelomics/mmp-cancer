package com.fujitsu.mmp.msusermanagement.repositories;

import com.fujitsu.mmp.msusermanagement.entities.DiagnosticPanelSet;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DiagnosticPanelSetRepository extends MongoRepository<DiagnosticPanelSet, String>, DiagnosticPanelSetRepositoryCustom {
    DiagnosticPanelSet findByDiagnosticPanelSetIdentifier(String diagnosticPanelSetIdentifier);

    boolean existsByDiagnosticPanelSetIdentifier(String diagnosticPanelSetIdentifier);

    boolean existsByName(String name);

    DiagnosticPanelSet findByName(String name);
}
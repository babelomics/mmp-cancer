package com.fujitsu.mmp.msusermanagement.repositories;

import com.fujitsu.mmp.msusermanagement.entities.DiagnosticPanel;
import com.fujitsu.mmp.msusermanagement.constants.EStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiagnosticPanelRepository extends MongoRepository<DiagnosticPanel, String>, DiagnosticPanelRepositoryCustom {

    List<DiagnosticPanel> findDiagnosticPanelByDiagnosticPanelSetIdentifier(String diagnosticPanelSetIdentifier);

    List <DiagnosticPanel> findByDiagnosticPanelIdentifier(String diagnosticPanelIdentifier);

    List<DiagnosticPanel> findDiagnosticPanelByDiagnosticPanelSetIdentifierAndDiagnosticPanelIdentifier(String diagnosticPanelSetIdentifier, String diagnosticPanelIdentifier);

    List<DiagnosticPanel> findDiagnosticPanelByParentIdsAndDiagnosticPanelSetIdentifier(String diagnosticPanelIdentifier, String diagnosticPanelSetIdentifier);

    DiagnosticPanel findDiagnosticPanelByDiagnosticPanelSetIdentifierAndDiagnosticPanelIdentifierAndStatus(String diagnosticPanelSetIdentifier, String diagnosticPanelIdentifier, EStatus status);

    DiagnosticPanel findByGuid(String guid);

    DiagnosticPanel findDiagnosticPanelByGuid(String guid);

    DiagnosticPanel findDiagnosticPanelByPreviousVersion(String guid);

    List <DiagnosticPanel>  findByDiagnosticPanelSetIdentifierAndName(String diagnosticPanelSetIdentifier, String name);

    List<DiagnosticPanel> findDiagnosticPanelByParentIdsAndDiagnosticPanelSetIdentifierAndStatus(String diagnosticPanelId, String panelSetId, EStatus current);

    List<DiagnosticPanel> findByDiagnosticPanelSetIdentifier(String diagnosticPanelSetIdentifier);

    List<DiagnosticPanel> findByDiagnosticPanelSetIdentifierAndDiagnosticPanelIdentifier(String diagnosticPanelSetIdentifier, String diagnosticPanelIdentifier);

    boolean existsByDiagnosticPanelSetIdentifierAndNameAndStatus(String diagnosticPanelSetIdentifier, String name, EStatus current);
}
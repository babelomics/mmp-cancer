package com.fujitsu.mmp.msusermanagement.repositories;

import com.fujitsu.mmp.msusermanagement.dto.diagnosticPanel.filters.FilterDiagnosticPanelSetDTO;
import com.fujitsu.mmp.msusermanagement.entities.DiagnosticPanelSet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface DiagnosticPanelSetRepositoryCustom {
    Page<DiagnosticPanelSet> findDiagnosticPanelSetByFilters (FilterDiagnosticPanelSetDTO filterDiagnosticPanelSetDTO, Pageable page);
}

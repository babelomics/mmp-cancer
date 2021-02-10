package com.fujitsu.mmp.msusermanagement.repositories;

import com.fujitsu.mmp.msusermanagement.dto.diagnosticPanel.filters.FilterDiagnosticPanelDTO;
import com.fujitsu.mmp.msusermanagement.entities.DiagnosticPanel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface DiagnosticPanelRepositoryCustom {
    Page<DiagnosticPanel> findDiagnosticPanelByFilters
            (FilterDiagnosticPanelDTO filterDiagnosticPanelDTO, Pageable page, String diagnosticPanelSetIdentifier, List<String> allParents);
}

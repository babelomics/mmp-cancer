package com.fujitsu.mmp.msusermanagement.entities;

import com.fujitsu.mmp.msusermanagement.constants.EInheritance;
import com.fujitsu.mmp.msusermanagement.constants.EMode;
import com.fujitsu.mmp.msusermanagement.constants.EType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiagnosticPanelFeature {
    private String diagnosticPanelFeatureIdentifier;
    private EType type;
    private EInheritance inheritance;
    private EMode mode;
}


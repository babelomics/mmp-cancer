package com.fujitsu.mmp.msusermanagement.entities;

import com.fujitsu.mmp.msusermanagement.constants.ESource;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiagnosticPanelAssociation {
    private String value;
    private ESource source;
}

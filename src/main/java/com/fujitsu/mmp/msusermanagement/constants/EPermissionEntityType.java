package com.fujitsu.mmp.msusermanagement.constants;

@lombok.Getter
public enum EPermissionEntityType {
    PROJECT("project"),
    PROJECTS("projects"),
    ANALYSIS("analysis"),
    ANALYSES("analyses"),
    INDIVIDUAL("individual"),
    INDIVIDUALS("individuals"),
    SAMPLE("sample"),
    SAMPLES("samples"),
    DRUG("drug"),
    DRUGS("drugs"),
    DIAGNOSTIC_PANEL("diagnostic-panel"),
    DIAGNOSTIC_PANELS("diagnostic-panels"),
    PERMISSIONS("permissions");

    private String value;

    EPermissionEntityType(String value) {
        this.value = value;
    }
}

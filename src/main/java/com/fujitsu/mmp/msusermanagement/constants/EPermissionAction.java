package com.fujitsu.mmp.msusermanagement.constants;

@lombok.Getter
public enum EPermissionAction {
    READ("read"),
    CREATE("create"),
    UPDATE("update"),
    DELETE("delete");

    private String value;

    EPermissionAction(String value) {
        this.value = value;
    }
}

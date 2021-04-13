package com.fujitsu.mmp.msusermanagement.dto.permission;

import com.fujitsu.mmp.msusermanagement.constants.EPermissionAction;
import com.fujitsu.mmp.msusermanagement.constants.EPermissionEntityType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PermissionDTO {
    private String name;
    private String action;
    private String entityType;
    private String entityId;

    public PermissionDTO(String action, String entityType, String entityId) {
        this.action = action;
        this.entityType = entityType;
        this.entityId = entityId;
    }
}

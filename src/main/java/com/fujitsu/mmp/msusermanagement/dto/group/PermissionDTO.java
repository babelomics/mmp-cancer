package com.fujitsu.mmp.msusermanagement.dto.group;

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
}

package com.fujitsu.mmp.msusermanagement.dto.group;


import com.fujitsu.mmp.msusermanagement.dto.permission.PermissionDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupDTO {
    private String guid;
    private String groupId;
    private String name;
    private String description;
    private String projectId;
    private List<PermissionDTO> permissions;
    private List<String> users;
    private List<String> permissionsNameList;
}

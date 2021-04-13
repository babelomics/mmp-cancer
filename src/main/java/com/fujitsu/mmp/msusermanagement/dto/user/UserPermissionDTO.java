package com.fujitsu.mmp.msusermanagement.dto.user;

import com.fujitsu.mmp.msusermanagement.dto.permission.PermissionDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserPermissionDTO {
    private String userType;
    private List<PermissionDTO> permissionList;
}

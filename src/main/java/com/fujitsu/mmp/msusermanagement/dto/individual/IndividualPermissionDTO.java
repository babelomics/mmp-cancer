package com.fujitsu.mmp.msusermanagement.dto.individual;

import com.fujitsu.mmp.msusermanagement.entities.Permission;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IndividualPermissionDTO {
    private String userId;
    private String userName;
    private List<Permission> permissions;
}

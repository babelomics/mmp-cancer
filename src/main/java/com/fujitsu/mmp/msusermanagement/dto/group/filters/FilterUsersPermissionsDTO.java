package com.fujitsu.mmp.msusermanagement.dto.group.filters;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FilterUsersPermissionsDTO {
    private String userId;
    private String userName;
    private String permission;
    private String group;
    private String search;

}

package com.fujitsu.mmp.msusermanagement.dto.group.filters;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FilterGroupsDTO {
    private String groupId;
    private String name;
    private String description;
    private String permission;
    private String search;
}

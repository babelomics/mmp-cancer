package com.fujitsu.mmp.msusermanagement.entities;


import com.fujitsu.mmp.msusermanagement.dto.group.PermissionDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "groups")
public class Group {
    @Id
    private String guid;
    private String groupId;
    private String name;
    private String description;
    private String projectId;
    private List<PermissionDTO> permissions;
    private List<String> users;

}

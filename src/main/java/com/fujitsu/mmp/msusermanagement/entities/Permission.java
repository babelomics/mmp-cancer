package com.fujitsu.mmp.msusermanagement.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "permissions")
public class Permission {
    @Id
    private String id;
    private String action;
    private String entityType;
    private String entityId;
    private String userId;

    public Permission(String action, String entityType, String entityId, String userId) {
        this.action = action;
        this.entityType = entityType;
        this.entityId = entityId;
        this.userId = userId;
    }
}

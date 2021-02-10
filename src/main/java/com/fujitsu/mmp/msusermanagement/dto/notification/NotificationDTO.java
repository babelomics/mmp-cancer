package com.fujitsu.mmp.msusermanagement.dto.notification;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {
    private Long version;
    private String type;
    private String identifier;
}

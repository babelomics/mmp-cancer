package com.fujitsu.mmp.msusermanagement.controllers;

import com.fujitsu.mmp.msusermanagement.dto.NotificationDTO;
import com.fujitsu.mmp.msusermanagement.dto.UserDTO;
import com.fujitsu.mmp.msusermanagement.services.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/api/notifications")
@RestController
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    /**
     * List all the notifications entity in the bbdd
     * @return list of all the users notifications found
     */
    @GetMapping("")
    public ResponseEntity<List<NotificationDTO>> getAllNotifications(){
        return notificationService.findAll();
    }

    /**
     * Delete a notification from the system.
     * @param identifier identifier of the notification entity
     */
    @DeleteMapping("/id/{identifier}")
    @CrossOrigin(origins = "*")
    public ResponseEntity<Void> delete(@PathVariable String identifier) {
        return notificationService.delete(identifier);
    }
}

package com.fujitsu.mmp.msusermanagement.services;

import com.fujitsu.mmp.msusermanagement.dto.notification.NotificationDTO;
import com.fujitsu.mmp.msusermanagement.entities.Notification;
import com.fujitsu.mmp.msusermanagement.mappers.NotificationMapper;
import com.fujitsu.mmp.msusermanagement.repositories.NotificationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;

    public NotificationService(NotificationRepository notificationRepository, NotificationMapper notificationMapper) {
        this.notificationRepository = notificationRepository;
        this.notificationMapper = notificationMapper;
    }

    public ResponseEntity<NotificationDTO> createNotification(NotificationDTO notificationDTO) {
        HttpStatus responseStatus = HttpStatus.CREATED;
        NotificationDTO responseBody = null;

        if (notificationDTO.getIdentifier() == null) {
            responseStatus = HttpStatus.UNPROCESSABLE_ENTITY;
        } else {
            Notification entityToCheckUnique = notificationRepository.findByIdentifier(notificationDTO.getIdentifier());

            if (entityToCheckUnique != null) {
                responseStatus = HttpStatus.CONFLICT;
            } else {
                Notification entity = notificationMapper.dtoToEntity(notificationDTO);
                entity = notificationRepository.save(entity);
                responseBody = notificationMapper.entityToDTO(entity);
            }
        }

        return new ResponseEntity<>(responseBody, responseStatus);
    }

    public ResponseEntity<List<NotificationDTO>> findAll() {
        HttpStatus responseStatus = HttpStatus.OK;
        List<NotificationDTO> responseBody = null;

        List<Notification> listEntity = notificationRepository.findAll();
        if (listEntity.isEmpty()) {
            responseStatus = HttpStatus.NOT_FOUND;
        } else {
            responseBody = notificationMapper.listEntityToListDto(listEntity);
        }
        return new ResponseEntity<>(responseBody, responseStatus);
    }

    public ResponseEntity<Void> delete(String identifier) {
        HttpStatus responseStatus = HttpStatus.NO_CONTENT;

        Notification elementToDelete = notificationRepository.findByIdentifier(identifier);
        if (elementToDelete == null) {
            responseStatus = HttpStatus.NOT_FOUND;
        } else {
            notificationRepository.delete(elementToDelete);
        }

        return new ResponseEntity<>(responseStatus);
    }

}

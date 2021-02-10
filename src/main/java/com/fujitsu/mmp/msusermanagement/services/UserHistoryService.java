package com.fujitsu.mmp.msusermanagement.services;

import com.fujitsu.mmp.msusermanagement.dto.user.UserHistoryDTO;
import com.fujitsu.mmp.msusermanagement.entities.UserHistory;
import com.fujitsu.mmp.msusermanagement.mappers.UserHistoryMapper;
import com.fujitsu.mmp.msusermanagement.repositories.UserHistoryRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class UserHistoryService {

    private final UserHistoryRepository userHistoryRepository;
    private final UserHistoryMapper userHistoryMapper;

    public UserHistoryService(UserHistoryRepository userHistoryRepository, UserHistoryMapper userHistoryMapper) {
        this.userHistoryRepository = userHistoryRepository;
        this.userHistoryMapper = userHistoryMapper;
    }

    public ResponseEntity<UserHistoryDTO> createUserHistory(UserHistoryDTO userHistoryDTO) {
        HttpStatus responseStatus = HttpStatus.CREATED;
        UserHistoryDTO responseBody = null;

        if (userHistoryDTO.getIdentifier() == null) {
            responseStatus = HttpStatus.UNPROCESSABLE_ENTITY;
        } else {
            UserHistory entityToCheckUnique = userHistoryRepository.findByIdentifier(userHistoryDTO.getIdentifier());
            if (entityToCheckUnique != null) {
                responseStatus = HttpStatus.CONFLICT;
            } else {
                userHistoryDTO.setDateDeleted(new Date());
                UserHistory entity = userHistoryMapper.dtoToEntity(userHistoryDTO);
                entity = userHistoryRepository.save(entity);
                responseBody = userHistoryMapper.entityToDTO(entity);
            }
        }
        return new ResponseEntity<>(responseBody, responseStatus);
    }
}

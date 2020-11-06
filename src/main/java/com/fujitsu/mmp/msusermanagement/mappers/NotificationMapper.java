package com.fujitsu.mmp.msusermanagement.mappers;

import com.fujitsu.mmp.msusermanagement.dto.NotificationDTO;
import com.fujitsu.mmp.msusermanagement.dto.UserHistoryDTO;
import com.fujitsu.mmp.msusermanagement.entities.Notification;
import com.fujitsu.mmp.msusermanagement.entities.UserHistory;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class NotificationMapper {
    private final ModelMapper modelMapper;

    public NotificationMapper() {
        this.modelMapper = new ModelMapper();
    }

    public Notification dtoToEntity(NotificationDTO dto) {
        return modelMapper.map(dto, Notification.class);
    }

    public NotificationDTO entityToDTO(Notification entity) {return modelMapper.map(entity, NotificationDTO.class);}

    public List<Notification> listDTOToListEntity(List<NotificationDTO> notificationDTO){
        return notificationDTO.stream().map(this::dtoToEntity).collect(Collectors.toList());
    }

    public List<NotificationDTO> listEntityToListDto(List<Notification> listEntity) {
        return listEntity.stream().map(this::entityToDTO).collect(Collectors.toList());
    }
}

package com.fujitsu.mmp.msusermanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AvailabilityDrugDTO {
    List<String> standardName;
    Boolean isAvailable;
    private String userId;
}

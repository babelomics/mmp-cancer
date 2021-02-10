package com.fujitsu.mmp.msusermanagement.dto.drug;

import com.fujitsu.mmp.msusermanagement.dto.drug.filters.FilterDrugDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AvailabilityDrugDTO {
    private List<String> standardName;
    private Boolean isAvailable;
    private String userId;
    private Boolean isAllSelected;
    private FilterDrugDTO filters;
}

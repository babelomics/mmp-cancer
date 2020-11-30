package com.fujitsu.mmp.msusermanagement.dto.filters;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FilterDrugDTO {
    private String standardName;
    private String commonName;
    private Double costMin;
    private Double costMax;
    private Boolean isAvailable;
    @DateTimeFormat(pattern = "dd/MM/yyyy")
    private Date dateModifiedStart;
    @DateTimeFormat(pattern = "dd/MM/yyyy")
    private Date dateModifiedEnd;
    private Boolean isDeleted;
    private String search;
}

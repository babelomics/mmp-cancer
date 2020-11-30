package com.fujitsu.mmp.msusermanagement.dto.pandrugsapi;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DrugResponse {
    private String standardName;
    private String showName;
    private List<SourceNameResponse> sourceName;
}


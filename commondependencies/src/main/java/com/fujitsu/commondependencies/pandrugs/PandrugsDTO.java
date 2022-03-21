package com.fujitsu.commondependencies.pandrugs;

import lombok.Data;

import java.util.List;

@Data
public class PandrugsDTO {

    private String standardName;
    private String showName;
    private List<SourceNameDTO> sourceName;

}

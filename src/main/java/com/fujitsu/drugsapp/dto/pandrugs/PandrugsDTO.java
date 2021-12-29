package com.fujitsu.drugsapp.dto.pandrugs;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class PandrugsDTO {

    private String standardName;
    private String showName;
    private List<SourceNameDTO> sourceName;

}

package com.fujitsu.mmp.msusermanagement.apis.genomicdictionaryapi;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Gene {
    private String geneId;
    private String hgnc;
    private String biotype;
    private String seqRegion;
    private long start;
    private long end;
    private String strand;
    private String description;
    private List<Transcript> transcripts;
    private Boolean isChildren;
}

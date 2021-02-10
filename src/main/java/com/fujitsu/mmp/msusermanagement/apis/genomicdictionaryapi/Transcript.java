package com.fujitsu.mmp.msusermanagement.apis.genomicdictionaryapi;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Transcript {
    private String transcriptId;
    private String name;
    private List<String> refseq;
    private String biotype;
    private String seqRegion;
    private long start;
    private long end;
    private String strand;
    private String geneId;
    private Boolean canonical;
    private Boolean isChildren;
}

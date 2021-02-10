package com.fujitsu.mmp.msusermanagement.apis.genomicdictionaryapi;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MetaAssembly {
    private String schemaVersion;
    private String gencodeVersion;
    private Assembly assembly;
}

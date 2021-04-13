package com.fujitsu.mmp.msusermanagement.entities;

import com.fujitsu.mmp.msusermanagement.constants.EObserved;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HumanPhenotype {
    private String phenotypeId;
    private EObserved observed;
    private Set<String> modifiers;
    private String comment;
}

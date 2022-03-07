package com.fujitsu.commondependencies.services;

import com.fujitsu.commondependencies.repositories.DrugNameRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class DrugNameService {

    private DrugNameRepository drugNameRepository;

}

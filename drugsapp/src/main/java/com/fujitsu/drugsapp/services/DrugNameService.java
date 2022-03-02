package com.fujitsu.drugsapp.services;

import com.fujitsu.drugsapp.repositories.DrugNameRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class DrugNameService {

    private DrugNameRepository drugNameRepository;

}

package com.fujitsu.drugsapp.services;

import com.fujitsu.drugsapp.entities.Drug;
import com.fujitsu.drugsapp.entities.DrugName;
import com.fujitsu.drugsapp.repositories.DrugNameRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@AllArgsConstructor
@Service
public class DrugNameService {

    private DrugNameRepository drugNameRepository;

}

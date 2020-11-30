package com.fujitsu.mmp.msusermanagement.repositories;

import com.fujitsu.mmp.msusermanagement.dto.filters.FilterDrugDTO;
import com.fujitsu.mmp.msusermanagement.entities.Drug;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DrugCustomRepository {
    Page<Drug> findDrugsByFilters (FilterDrugDTO filterDrugDTO, Pageable page);
}

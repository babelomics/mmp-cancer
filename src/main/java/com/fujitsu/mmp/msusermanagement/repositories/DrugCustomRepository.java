package com.fujitsu.mmp.msusermanagement.repositories;

import com.fujitsu.mmp.msusermanagement.dto.drug.filters.FilterDrugDTO;
import com.fujitsu.mmp.msusermanagement.entities.Drug;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface DrugCustomRepository {
    Page<Drug> findDrugsByFilters (FilterDrugDTO filterDrugDTO, Pageable page);

    public List<Drug> findDrugsByFiltersWithNoPage (FilterDrugDTO filterDrugDTO);
}

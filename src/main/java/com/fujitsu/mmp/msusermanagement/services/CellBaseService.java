package com.fujitsu.mmp.msusermanagement.services;

import com.fujitsu.mmp.msusermanagement.dto.cellbaseapi.CellBaseDTO;
import com.fujitsu.mmp.msusermanagement.dto.pandrugsapi.Registry;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class CellBaseService {

    public ResponseEntity<CellBaseDTO> validate(CellBaseDTO cellBaseDTO) {
        HttpStatus responseStatus = HttpStatus.OK;
        CellBaseDTO responseBody = cellBaseDTO;
        return new ResponseEntity<>(responseBody, responseStatus);
    }

    public Boolean isValid(String cellBaseURL) {
        //TO DO. How to check if Cellbase service is ok?
        return true;
    }
}

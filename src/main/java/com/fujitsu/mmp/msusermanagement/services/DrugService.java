package com.fujitsu.mmp.msusermanagement.services;

import com.fujitsu.mmp.msusermanagement.dto.drug.AvailabilityDrugDTO;
import com.fujitsu.mmp.msusermanagement.dto.drug.DrugDTO;
import com.fujitsu.mmp.msusermanagement.dto.drug.filters.FilterDrugDTO;
import com.fujitsu.mmp.msusermanagement.apis.pandrugsapi.DrugResponse;
import com.fujitsu.mmp.msusermanagement.apis.pandrugsapi.SourceNameResponse;
import com.fujitsu.mmp.msusermanagement.entities.AlternativeNames;
import com.fujitsu.mmp.msusermanagement.entities.Drug;
import com.fujitsu.mmp.msusermanagement.entities.DrugHistory;
import com.fujitsu.mmp.msusermanagement.mappers.DrugMapper;
import com.fujitsu.mmp.msusermanagement.repositories.DrugHistoryRepository;
import com.fujitsu.mmp.msusermanagement.repositories.DrugRepository;
import com.fujitsu.mmp.msusermanagement.utility.JWTUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
public class DrugService {

    private final WebClient webClient;

    @Autowired
    DrugRepository drugRepository;

    @Autowired
    DrugHistoryRepository drugHistoryRepostory;

    @Autowired
    DrugMapper drugMapper;

    @Autowired
    JWTUtility jwtUtility;

    @Autowired
    public DrugService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://www.pandrugs.org").build();
    }

    public List<Drug> createOrUpdateDrugs() {

        List<Drug> modifiedDrugList = new ArrayList<>();

        List<DrugHistory> drugHistoryList = new ArrayList<>();

        List<DrugResponse> drugResponseList = this.webClient
                .get()
                .uri("/pandrugs-backend/api/genedrug/drug/")
                .retrieve()
                .bodyToFlux(DrugResponse.class)
                .collectList()
                .block();

        List<Drug> drugDBList = drugRepository.findAll();

        if (drugDBList.isEmpty()) {
            List<DrugDTO> drugDTOList = drugResponseList
                    .stream()
                    .map(drugResponse -> {
                        DrugDTO drugDTO = new DrugDTO();
                        drugDTO.setStandardName(drugResponse.getStandardName());
                        drugDTO.setCommonName(drugResponse.getShowName());
                        drugDTO.setAlternativeNames(convertSourceNameToAlternativeName(drugResponse.getSourceName()));
                        drugDTO.setAvailable(false);
                        drugDTO.setCreationDate(new Date());
                        return drugDTO;
                    }).collect(Collectors.toList());
            drugRepository.saveAll(drugMapper.listDtoToListEntity(drugDTOList));
        } else {
            Set<String> drugDBIds = drugDBList
                    .stream()
                    .map(Drug::getStandardName)
                    .collect(Collectors.toSet());

            List<Drug> newDrugs = drugResponseList
                    .stream()
                    .filter(drugRest -> !drugDBIds.contains(drugRest.getStandardName()))
                    .map(drugResponse -> {
                        Drug drug = new Drug();
                        drug.setStandardName(drugResponse.getStandardName());
                        drug.setCommonName(drugResponse.getShowName());
                        drug.setAlternativeNames(convertSourceNameToAlternativeName(drugResponse.getSourceName()));
                        drug.setCreationDate(new Date());
                        drug.setAvailable(false);
                        return drug;
                    })
                    .collect(Collectors.toList());

            if(!newDrugs.isEmpty()) {
                drugRepository.saveAll(newDrugs);
            }

            Set<String> drugResponseIds = drugResponseList
                    .stream()
                    .map(DrugResponse::getStandardName)
                    .collect(Collectors.toSet());

            List<Drug> deletedDrugs = drugDBList
                    .stream()
                    .filter(drugDB -> !drugResponseIds.contains(drugDB.getStandardName()))
                    .filter(drugDB -> drugDB.getDeletionDate() == null)
                    .map(drug -> {
                        drug.setDeletionDate(new Date());
                        return drug;
                    })
                    .collect(Collectors.toList());

            if(!deletedDrugs.isEmpty()) {
                drugRepository.saveAll(deletedDrugs);
            }

            Set<String> drugDeletedDBIds = drugDBList
                    .stream()
                    .filter(drug -> drug.getDeletionDate() != null)
                    .map(Drug::getStandardName)
                    .collect(Collectors.toSet());

            List<Drug> backDrugs = drugResponseList
                    .stream()
                    .filter(drugRest -> drugDeletedDBIds.contains(drugRest.getStandardName()))
                    .map(drugResponse -> {
                        Drug drug = new Drug();
                        drug.setStandardName(drugResponse.getStandardName());
                        drug.setCommonName(drugResponse.getShowName());
                        drug.setAlternativeNames(convertSourceNameToAlternativeName(drugResponse.getSourceName()));
                        drug.setCreationDate(new Date());
                        drug.setAvailable(false);
                        return drug;
                    })
                    .collect(Collectors.toList());

            if(!backDrugs.isEmpty()) {
                drugRepository.saveAll(backDrugs);
            }

            drugResponseList
                    .forEach(drugResponse -> drugDBList.stream()
                            .filter(drugDB -> drugDB.getStandardName().equals(drugResponse.getStandardName()))
                            .filter(drugDB -> drugDB.getDeletionDate() == null)
                            .forEach(drugDBFiltered -> {
                                Drug drug = new Drug();
                                DrugHistory drugHistory = new DrugHistory();
                                AtomicReference<Boolean> isOK = new AtomicReference<>(false);
                                if (!drugDBFiltered.getCommonName().equals(drugResponse.getShowName())) {
                                    drug.setId(drugDBFiltered.getId());
                                    drug.setVersion(drugDBFiltered.getVersion());
                                    drug.setStandardName(drugResponse.getStandardName());
                                    drug.setCommonName(drugResponse.getShowName());
                                    drug.setAlternativeNames(convertSourceNameToAlternativeName(drugResponse.getSourceName()));
                                    drug.setCreationDate(new Date());
                                    drug.setAvailable(false);
                                    drug.setPreviousVersion(drugDBFiltered.getVersion());

                                    drugHistory.setStandardName(drugDBFiltered.getStandardName());
                                    drugHistory.setCommonName(drugDBFiltered.getCommonName());
                                    drugHistory.setAlternativeNames(drugDBFiltered.getAlternativeNames());
                                    drugHistory.setCreationDate(drugDBFiltered.getCreationDate());
                                    drugHistory.setAvailable(drugDBFiltered.getAvailable());
                                    drugHistory.setCost(drugDBFiltered.getCost());
                                    drugHistory.setVersion(drugDBFiltered.getVersion());
                                    drugHistory.setPreviousVersion(drugDBFiltered.getPreviousVersion());
                                    drugHistory.setDeletionDate(new Date());
                                    isOK.set(true);
                                } else {
                                    if (drugDBFiltered.getAlternativeNames().size() != drugResponse.getSourceName().size()) {
                                        drug.setId(drugDBFiltered.getId());
                                        drug.setVersion(drugDBFiltered.getVersion());
                                        drug.setStandardName(drugResponse.getStandardName());
                                        drug.setCommonName(drugResponse.getShowName());
                                        drug.setAlternativeNames(convertSourceNameToAlternativeName(drugResponse.getSourceName()));
                                        drug.setCreationDate(new Date());
                                        drug.setAvailable(false);
                                        drug.setPreviousVersion(drugDBFiltered.getVersion());

                                        drugHistory.setStandardName(drugDBFiltered.getStandardName());
                                        drugHistory.setCommonName(drugDBFiltered.getCommonName());
                                        drugHistory.setAlternativeNames(drugDBFiltered.getAlternativeNames());
                                        drugHistory.setCreationDate(drugDBFiltered.getCreationDate());
                                        drugHistory.setAvailable(drugDBFiltered.getAvailable());
                                        drugHistory.setCost(drugDBFiltered.getCost());
                                        drugHistory.setVersion(drugDBFiltered.getVersion());
                                        drugHistory.setPreviousVersion(drugDBFiltered.getPreviousVersion());
                                        drugHistory.setDeletionDate(new Date());
                                        isOK.set(true);
                                    } else {
                                        drugDBFiltered.getAlternativeNames().forEach(alternativeNames ->
                                                drugResponse.getSourceName()
                                                        .stream()
                                                        .filter(sourceNameResponse -> sourceNameResponse.getDrugName().equals(alternativeNames.getName())
                                                                && !sourceNameResponse.getName().equals(alternativeNames.getSource()))
                                                        .forEach(sourceNameResponse -> {
                                                            drug.setId(drugDBFiltered.getId());
                                                            drug.setVersion(drugDBFiltered.getVersion());
                                                            drug.setStandardName(drugResponse.getStandardName());
                                                            drug.setCommonName(drugResponse.getShowName());
                                                            drug.setAlternativeNames(convertSourceNameToAlternativeName(drugResponse.getSourceName()));
                                                            drug.setCreationDate(new Date());
                                                            drug.setAvailable(false);
                                                            drug.setPreviousVersion(drugDBFiltered.getVersion());

                                                            drugHistory.setStandardName(drugDBFiltered.getStandardName());
                                                            drugHistory.setCommonName(drugDBFiltered.getCommonName());
                                                            drugHistory.setAlternativeNames(drugDBFiltered.getAlternativeNames());
                                                            drugHistory.setCreationDate(drugDBFiltered.getCreationDate());
                                                            drugHistory.setAvailable(drugDBFiltered.getAvailable());
                                                            drugHistory.setCost(drugDBFiltered.getCost());
                                                            drugHistory.setVersion(drugDBFiltered.getVersion());
                                                            drugHistory.setPreviousVersion(drugDBFiltered.getPreviousVersion());
                                                            drugHistory.setDeletionDate(new Date());
                                                            isOK.set(true);
                                                        }));
                                    }
                                }
                                if (isOK.get()) {
                                    modifiedDrugList.add(drug);
                                    drugHistoryList.add(drugHistory);
                                }
                            }));
        }

        if(!modifiedDrugList.isEmpty()){
            drugRepository.saveAll(modifiedDrugList);
        }

        if(!drugHistoryList.isEmpty()){
            drugHistoryRepostory.saveAll(drugHistoryList);
        }

        return modifiedDrugList;
    }

    public ResponseEntity<Page<DrugDTO>> findAllByPage(Pageable pageable, FilterDrugDTO filterDrugDTO) {
        HttpStatus responseStatus = HttpStatus.OK;

        Page<DrugDTO> responseBody;

        Page<Drug> pageEntity = drugRepository.findDrugsByFilters(filterDrugDTO, pageable);

        List<DrugDTO> drugDTOList = drugMapper.listEntityToListDto(pageEntity.getContent());

        responseBody = new PageImpl<>(drugDTOList, pageable, pageEntity.getTotalElements());

        return new ResponseEntity<>(responseBody, responseStatus);
    }

    public ResponseEntity<?> setAvailable(AvailabilityDrugDTO availabilityDrugDTO, HttpServletRequest httpServletRequest) {
        HttpStatus responseStatus = HttpStatus.OK;
        List<Drug> responseBody;

        String token = httpServletRequest.getHeader("Authorization");
        String username = "";

        if(token != null) {
            username = jwtUtility.getUsernameFromToken(token.substring(6));
        }

        if(availabilityDrugDTO.getIsAllSelected()) {
            responseBody = drugRepository.findDrugsByFiltersWithNoPage(availabilityDrugDTO.getFilters());
        } else {
            responseBody = drugRepository.findByStandardNameIn(availabilityDrugDTO.getStandardName());
        }

        if (availabilityDrugDTO.getIsAvailable() == null) {
            responseStatus = HttpStatus.UNPROCESSABLE_ENTITY;
        } else if (responseBody.isEmpty()) {
            responseStatus = HttpStatus.NOT_FOUND;
        } else {
            List<DrugHistory> drugHistoryList = new ArrayList<>();

            String finalUsername = username;

            List<Drug> drugList = responseBody.stream()
                    .filter(drug -> drug.getAvailable() != availabilityDrugDTO.getIsAvailable())
                    .map(drug -> {
                        Drug drugToUpdate = drug;
                        DrugHistory drugToHistory = new DrugHistory();

                        drugToHistory.setStandardName(drug.getStandardName());
                        drugToHistory.setCommonName(drug.getCommonName());
                        drugToHistory.setAlternativeNames(drug.getAlternativeNames());
                        drugToHistory.setCreationDate(drug.getCreationDate());
                        drugToHistory.setAvailable(drug.getAvailable());
                        drugToHistory.setCost(drug.getCost());
                        drugToHistory.setVersion(drug.getVersion());
                        drugToHistory.setDeletionDate(new Date());
                        drugToHistory.setPreviousVersion(drug.getPreviousVersion());
                        drugToHistory.setUserId(finalUsername);

                        drugToUpdate.setAvailable(availabilityDrugDTO.getIsAvailable());
                        drugToUpdate.setPreviousVersion(drug.getVersion());
                        drugToUpdate.setCreationDate(new Date());
                        drugToUpdate.setId(drug.getId());
                        drugToUpdate.setVersion(drug.getVersion());

                        drugHistoryList.add(drugToHistory);

                        return drugToUpdate;
                    }).collect(Collectors.toList());

                drugHistoryRepostory.saveAll(drugHistoryList);
                drugRepository.saveAll(drugList);
            }


        return new ResponseEntity<>(responseBody, responseStatus);

    }

    private List<AlternativeNames> convertSourceNameToAlternativeName (List<SourceNameResponse> sourceNameResponses){
        List<AlternativeNames> alternativeNames = sourceNameResponses
                .stream()
                .map(sourceNameResponse -> new AlternativeNames(
                        sourceNameResponse.getName(),
                        sourceNameResponse.getDrugName()
                )).collect(Collectors.toList());
        return alternativeNames;
    }

    public ResponseEntity<List<DrugDTO>> findByDrugId(String id) {
        HttpStatus responseStatus = HttpStatus.OK;
        List<DrugDTO> responseBody = new ArrayList<>();

        Optional<Drug> entity = drugRepository.findById(id);
        if (!entity.isPresent()) {
            responseStatus = HttpStatus.NOT_FOUND;
        } else {
            DrugDTO drugDTO = drugMapper.entityToDto(entity.get());
            responseBody.add(drugDTO);
            List<DrugHistory> drugHistoryList = drugHistoryRepostory.findAllByStandardName(drugDTO.getStandardName());

        drugHistoryList.stream()
                .forEach(drugHistory -> {
                        DrugDTO dto = new DrugDTO();
                        dto.setStandardName(drugHistory.getStandardName());
                        dto.setCommonName(drugHistory.getCommonName());
                        dto.setAlternativeNames(drugHistory.getAlternativeNames());
                        dto.setVersion(drugHistory.getVersion());
                        dto.setCost(drugHistory.getCost());
                        dto.setDeletionDate(drugHistory.getDeletionDate());
                        dto.setCreationDate(drugHistory.getCreationDate());
                        dto.setAvailable(drugHistory.getAvailable());
                        dto.setPreviousVersion(drugHistory.getPreviousVersion());
                        dto.setUserId(drugHistory.getUserId());
                        responseBody.add(dto);
                });
        }
        return new ResponseEntity<>(responseBody, responseStatus);
    }

    public ResponseEntity<DrugDTO> update(DrugDTO drugDTO) {
        HttpStatus responseStatus = HttpStatus.OK;
        DrugDTO responseBody = null;
        DrugHistory drugHistory = new DrugHistory();

        if (drugDTO.getStandardName() == null) {
            responseStatus = HttpStatus.UNPROCESSABLE_ENTITY;
        } else {
            Drug entity = drugRepository.findByStandardName(drugDTO.getStandardName());
            if (entity == null) {
                responseStatus = HttpStatus.NOT_FOUND;
            }  else if (!entity.getVersion().equals(drugDTO.getVersion())) {
                responseStatus = HttpStatus.CONFLICT;
            } else {
                if(!(drugDTO.getCost() == null && entity.getCost() == null) || entity.getAvailable() != drugDTO.getAvailable()) {
                    if(entity.getCost() == null || drugDTO.getCost() == null || entity.getAvailable() != drugDTO.getAvailable()){

                        drugHistory.setStandardName(entity.getStandardName());
                        drugHistory.setCommonName(entity.getCommonName());
                        drugHistory.setAlternativeNames(entity.getAlternativeNames());
                        drugHistory.setCreationDate(entity.getCreationDate());
                        drugHistory.setAvailable(entity.getAvailable());
                        drugHistory.setCost(entity.getCost());
                        drugHistory.setVersion(entity.getVersion());
                        drugHistory.setPreviousVersion(entity.getPreviousVersion());
                        drugHistory.setDeletionDate(new Date());

                        drugHistoryRepostory.save(drugHistory);

                        Drug entityToSave = new Drug();
                        entityToSave.setId(entity.getId());
                        entityToSave.setVersion(entity.getVersion());
                        entityToSave.setStandardName(drugDTO.getStandardName());
                        entityToSave.setCommonName(drugDTO.getCommonName());
                        entityToSave.setAlternativeNames(drugDTO.getAlternativeNames());
                        entityToSave.setCreationDate(new Date());
                        entityToSave.setAvailable(drugDTO.getAvailable());
                        entityToSave.setCost(drugDTO.getCost());
                        entityToSave.setPreviousVersion(entity.getVersion());

                        entityToSave = drugRepository.save(entityToSave);
                        responseBody = drugMapper.entityToDto(entityToSave);
                    } else if(!(Double.compare(entity.getCost(), drugDTO.getCost()) == 0)){

                        drugHistory.setStandardName(entity.getStandardName());
                        drugHistory.setCommonName(entity.getCommonName());
                        drugHistory.setAlternativeNames(entity.getAlternativeNames());
                        drugHistory.setCreationDate(entity.getCreationDate());
                        drugHistory.setAvailable(entity.getAvailable());
                        drugHistory.setCost(entity.getCost());
                        drugHistory.setVersion(entity.getVersion());
                        drugHistory.setPreviousVersion(entity.getPreviousVersion());
                        drugHistory.setDeletionDate(new Date());

                        drugHistoryRepostory.save(drugHistory);

                        Drug entityToSave = new Drug();
                        entityToSave.setId(entity.getId());
                        entityToSave.setVersion(entity.getVersion());
                        entityToSave.setStandardName(drugDTO.getStandardName());
                        entityToSave.setCommonName(drugDTO.getCommonName());
                        entityToSave.setAlternativeNames(drugDTO.getAlternativeNames());
                        entityToSave.setCreationDate(new Date());
                        entityToSave.setAvailable(drugDTO.getAvailable());
                        entityToSave.setCost(drugDTO.getCost());
                        entityToSave.setPreviousVersion(entity.getVersion());

                        entityToSave = drugRepository.save(entityToSave);
                        responseBody = drugMapper.entityToDto(entityToSave);
                    }
                }
            }
        }
        return new ResponseEntity<>(responseBody, responseStatus);
    }
}

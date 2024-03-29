package com.fujitsu.mmp.msusermanagement.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fujitsu.mmp.msusermanagement.constants.ESource;
import com.fujitsu.mmp.msusermanagement.constants.EStatus;
import com.fujitsu.mmp.msusermanagement.constants.EType;
import com.fujitsu.mmp.msusermanagement.dto.diagnosticPanel.DiagnosticPanelSetDTO;
import com.fujitsu.mmp.msusermanagement.dto.diagnosticPanel.exportimport.DiagnosticPanelExportImportDTO;
import com.fujitsu.mmp.msusermanagement.dto.diagnosticPanel.exportimport.DiagnosticPanelSetExportImportDTO;
import com.fujitsu.mmp.msusermanagement.dto.diagnosticPanel.filters.FilterDiagnosticPanelSetDTO;
import com.fujitsu.mmp.msusermanagement.entities.*;
import com.fujitsu.mmp.msusermanagement.mappers.DiagnosticPanelSetMapper;
import com.fujitsu.mmp.msusermanagement.repositories.*;
import com.fujitsu.mmp.msusermanagement.utility.JWTUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DiagnosticPanelSetService {

    @Autowired
    DiagnosticPanelSetRepository diagnosticPanelSetRepository;

    @Autowired
    DiagnosticPanelSetMapper diagnosticPanelSetMapper;

    @Autowired
    DiagnosticPanelRepository diagnosticPanelRepository;

    @Autowired
    JWTUtility jwtUtility;

    @Autowired
    GenomicDictionaryService genomicDictionaryService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    UserHistoryRepository userHistoryRepository;

    @Autowired
    VariantRepository variantRepository;

    @Autowired
    RegionRepository regionRepository;

    public ResponseEntity<DiagnosticPanelSetDTO> createDiagnosticPanelSet(
            DiagnosticPanelSetDTO diagnosticPanelSetDTO, HttpServletRequest httpServletRequest) {

        HttpStatus responseStatus = HttpStatus.CREATED;
        DiagnosticPanelSetDTO responseBody = null;

        String token = httpServletRequest.getHeader("Authorization");
        String username = "";

        if (token != null) {
            username = jwtUtility.getUsernameFromToken(token.substring(6));
        }

        if (diagnosticPanelSetDTO.getDiagnosticPanelSetIdentifier() == null) {
            responseStatus = HttpStatus.UNPROCESSABLE_ENTITY;
        } else if (diagnosticPanelSetRepository.existsByDiagnosticPanelSetIdentifier(diagnosticPanelSetDTO.getDiagnosticPanelSetIdentifier())) {
            responseStatus = HttpStatus.CONFLICT;
        } else if (diagnosticPanelSetRepository.existsByName(diagnosticPanelSetDTO.getName())) {
            responseStatus = HttpStatus.CONFLICT;
        } else {

            DiagnosticPanelSet diagnosticPanelSet = diagnosticPanelSetMapper.dtoToEntity(diagnosticPanelSetDTO);
            diagnosticPanelSet.setCreationDate(new Date());
            diagnosticPanelSet.setAuthor(username);
            diagnosticPanelSet.setCurrentVersion(0);

            DiagnosticPanelSet createdDiagnosticPanelSet = diagnosticPanelSetRepository.save(diagnosticPanelSet);
            responseBody = diagnosticPanelSetMapper.entityToDto(createdDiagnosticPanelSet);
        }

        return new ResponseEntity<>(responseBody, responseStatus);
    }

    public ResponseEntity<DiagnosticPanelSetDTO> findByIdentifier(String diagnosticPanelSetIdentifier) {
        HttpStatus responseStatus = HttpStatus.OK;
        DiagnosticPanelSetDTO responseBody = null;

        DiagnosticPanelSet entity = diagnosticPanelSetRepository.findByDiagnosticPanelSetIdentifier(diagnosticPanelSetIdentifier);
        if (entity == null) {
            responseStatus = HttpStatus.NOT_FOUND;
        } else {
            responseBody = diagnosticPanelSetMapper.entityToDto(entity);
            List<DiagnosticPanel> diagnosticPanels = diagnosticPanelRepository.findDiagnosticPanelByDiagnosticPanelSetIdentifier(responseBody.getDiagnosticPanelSetIdentifier());
            responseBody.setPanelsNumber(diagnosticPanels.size());
            responseBody.setIsHuman(genomicDictionaryService.isHuman(entity.getReference().getAssembly()));
        }

        return new ResponseEntity<>(responseBody, responseStatus);
    }

    public ResponseEntity<DiagnosticPanelSetDTO> update(String diagnosticPanelSetIdentifier, DiagnosticPanelSetDTO DiagnosticPanelSetDTO) {

        HttpStatus responseStatus = HttpStatus.OK;
        DiagnosticPanelSetDTO responseBody = null;

        if (DiagnosticPanelSetDTO.getDiagnosticPanelSetIdentifier() == null) {
            responseStatus = HttpStatus.UNPROCESSABLE_ENTITY;
        } else if (!diagnosticPanelSetIdentifier.equals(DiagnosticPanelSetDTO.getDiagnosticPanelSetIdentifier())) {
            responseStatus = HttpStatus.BAD_REQUEST;
        } else {
            DiagnosticPanelSet entity = diagnosticPanelSetRepository.findByDiagnosticPanelSetIdentifier(diagnosticPanelSetIdentifier);
            if (entity == null) {
                responseStatus = HttpStatus.NOT_FOUND;
            } else {
                DiagnosticPanelSet entityToSave = diagnosticPanelSetMapper.dtoToEntity(DiagnosticPanelSetDTO);
                entityToSave.setId(entity.getId());
                entityToSave = diagnosticPanelSetRepository.save(entityToSave);
                responseBody = diagnosticPanelSetMapper.entityToDto(entityToSave);
            }
        }
        return new ResponseEntity<>(responseBody, responseStatus);
    }

    public ResponseEntity<DiagnosticPanelSetDTO> delete(String diagnosticPanelSetIdentifier) {
        HttpStatus responseStatus = HttpStatus.OK;
        DiagnosticPanelSetDTO responseBody = null;

        DiagnosticPanelSet elementToDelete = diagnosticPanelSetRepository.findByDiagnosticPanelSetIdentifier(diagnosticPanelSetIdentifier);

        if (diagnosticPanelSetIdentifier == null) {
            responseStatus = HttpStatus.UNPROCESSABLE_ENTITY;
        } else if (elementToDelete == null) {
            responseStatus = HttpStatus.NOT_FOUND;
        } else {
            elementToDelete.setDeletionDate(new Date());

            List<DiagnosticPanel> diagnosticPanelList = diagnosticPanelRepository.findDiagnosticPanelByDiagnosticPanelSetIdentifier(diagnosticPanelSetIdentifier);
            diagnosticPanelList.forEach(
                    diagnosticPanel -> {
                        diagnosticPanel.setDeletionDate(new Date());
                        diagnosticPanel.setStatus(EStatus.ARCHIVED);
                    });

            diagnosticPanelRepository.saveAll(diagnosticPanelList);

            elementToDelete = diagnosticPanelSetRepository.save(elementToDelete);
            responseBody = diagnosticPanelSetMapper.entityToDto(elementToDelete);

        }

        return new ResponseEntity<>(responseBody, responseStatus);
    }

    public ResponseEntity<Page<DiagnosticPanelSetDTO>> listDiagnosticPanelSet(Pageable pageable, FilterDiagnosticPanelSetDTO filterDiagnosticPanelSetDTO) {

        HttpStatus responseStatus = HttpStatus.OK;

        Page<DiagnosticPanelSetDTO> responseBody;

        Page<DiagnosticPanelSet> pageEntity = diagnosticPanelSetRepository.findDiagnosticPanelSetByFilters(filterDiagnosticPanelSetDTO, pageable);

        List<DiagnosticPanelSetDTO> diagnosticPanelSetDTOList = diagnosticPanelSetMapper.listEntityToListDto(pageEntity.getContent());

        diagnosticPanelSetDTOList.forEach(diagnosticPanelSetDTO -> {
            List<DiagnosticPanel> diagnosticPanels =
                    diagnosticPanelRepository.findDiagnosticPanelByDiagnosticPanelSetIdentifier(diagnosticPanelSetDTO.getDiagnosticPanelSetIdentifier()).stream()
                    .filter(panel -> panel.getStatus().equals(EStatus.CURRENT))
                    .collect(Collectors.toList());

            diagnosticPanelSetDTO.setPanelsNumber(diagnosticPanels.size());
        });

        responseBody = new PageImpl<>(diagnosticPanelSetDTOList, pageable, pageEntity.getTotalElements());

        return new ResponseEntity<>(responseBody, responseStatus);
    }

    public ResponseEntity<List<DiagnosticPanelSetDTO>> findAll() {
        HttpStatus responseStatus = HttpStatus.OK;
        List<DiagnosticPanelSetDTO> responseBody = null;

        List<DiagnosticPanelSet> listEntity = diagnosticPanelSetRepository.findAll();

        if (listEntity.isEmpty()) {
            responseStatus = HttpStatus.NOT_FOUND;
        } else {
            responseBody = diagnosticPanelSetMapper.listEntityToListDto(listEntity);
        }

        return new ResponseEntity<>(responseBody, responseStatus);
    }

    public ResponseEntity<String> exportDiagnosticPanelSet(String diagnosticPanelSetIdentifier) {
        HttpStatus responseStatus = HttpStatus.OK;
        String responseBody;

        DiagnosticPanelSet diagnosticPanelSet =
                diagnosticPanelSetRepository.findByDiagnosticPanelSetIdentifier(diagnosticPanelSetIdentifier);

        List<DiagnosticPanel> diagnosticPanelList =
                diagnosticPanelRepository.findDiagnosticPanelByDiagnosticPanelSetIdentifier(diagnosticPanelSetIdentifier);

        DiagnosticPanelSetExportImportDTO exportObject = new DiagnosticPanelSetExportImportDTO();
        exportObject.setId(diagnosticPanelSet.getDiagnosticPanelSetIdentifier());
        exportObject.setName(diagnosticPanelSet.getName());
        exportObject.setDescription(diagnosticPanelSet.getDescription());
        exportObject.setReference(diagnosticPanelSet.getReference());
        exportObject.setAuthor(diagnosticPanelSet.getAuthor());
        exportObject.setCreationDate(diagnosticPanelSet.getCreationDate().toInstant().toString());
        exportObject.setCurrentVersion(diagnosticPanelSet.getCurrentVersion());
        if (diagnosticPanelSet.getDeletionDate() != null) {
            exportObject.setDeletionDate(diagnosticPanelSet.getDeletionDate().toInstant().toString());
        }
        exportObject.setExportDate(Instant.now().toString());

        List<DiagnosticPanelExportImportDTO> diagnosticPanelExportImportDTOList = new ArrayList<>();
        diagnosticPanelList.forEach(diagnosticPanel -> {
            DiagnosticPanelExportImportDTO diagnosticPanelExportImportDTO = new DiagnosticPanelExportImportDTO();
            diagnosticPanelExportImportDTO.setId(diagnosticPanel.getDiagnosticPanelIdentifier());
            diagnosticPanelExportImportDTO.setName(diagnosticPanel.getName());

            if (diagnosticPanel.getAssociations().size() != 0) {
                diagnosticPanelExportImportDTO.setAssociations(diagnosticPanel.getAssociations());
            }
            if (diagnosticPanel.getFeatures().size() != 0) {
                diagnosticPanelExportImportDTO.setFeatures(diagnosticPanel.getFeatures());
            }
            diagnosticPanelExportImportDTO.setAuthor(diagnosticPanel.getAuthor());
            diagnosticPanelExportImportDTO.setCreationDate(diagnosticPanel.getCreationDate().toInstant().toString());
            diagnosticPanelExportImportDTO.setStartVersion(diagnosticPanel.getStartVersion());
            diagnosticPanelExportImportDTO.setEndVersion(diagnosticPanel.getEndVersion());
            if (diagnosticPanel.getDeletionDate() != null) {
                diagnosticPanelExportImportDTO.setDeletionDate(diagnosticPanel.getDeletionDate().toInstant().toString());
            }
            diagnosticPanelExportImportDTO.setDescription(diagnosticPanel.getDescription());
            diagnosticPanelExportImportDTO.setParentIds(diagnosticPanel.getParentIds().size() == 0 ? new ArrayList<>() : diagnosticPanel.getParentIds());
            diagnosticPanelExportImportDTO.setGuid(diagnosticPanel.getGuid());
            diagnosticPanelExportImportDTOList.add(diagnosticPanelExportImportDTO);
        });
        exportObject.setPanels(diagnosticPanelExportImportDTOList);

        ObjectMapper objectMapper = new ObjectMapper();

        try {
            responseBody = objectMapper.writeValueAsString(exportObject);
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity<>(
                    e.getMessage(),
                    HttpStatus.EXPECTATION_FAILED);
        }

        return new ResponseEntity<>(responseBody, responseStatus);
    }

    public ResponseEntity<String> importPanelSet(MultipartFile file, String id, String name, Boolean isOverwritten) {

        ObjectMapper mapper = new ObjectMapper();
        DiagnosticPanelSet savedDiagnosticPanelSet = null;
        //TODO THROW EXCEPTION/ ADD EXCEPTIONS

        if (file.getOriginalFilename() == null) {
            return new ResponseEntity<>(
                    "Error: incorrect file name format. CUSTOM_ERROR_CODE: 400_INCORRECT_FILE_NAME",
                    HttpStatus.BAD_REQUEST);
        } else if (!getFileExtension(file.getOriginalFilename()).equals("json")) {
            return new ResponseEntity<>(
                    "Error: incorrect format. (Required .json). CUSTOM_ERROR_CODE: 400_NOT_JSON",
                    HttpStatus.BAD_REQUEST);
        } else if(id == null || id.isBlank() || name == null || name.isBlank() || !id.matches("([a-zA-Z0-9_]{3,32})")){
                return new ResponseEntity<>(
                        "Error: empty identifier or name. CUSTOM_ERROR_CODE: 422_INCORRECT_ID_OR_NAME",
                        HttpStatus.UNPROCESSABLE_ENTITY);
        } else{
            if ((diagnosticPanelSetRepository.existsByDiagnosticPanelSetIdentifier(id) || diagnosticPanelSetRepository.existsByName(name)) && !isOverwritten) {
                if (diagnosticPanelSetRepository.findByDiagnosticPanelSetIdentifier(id) != null) {
                    return new ResponseEntity<>(
                            "Error: Identifier is already taken!. CUSTOM_ERROR_CODE: 409_IDENTIFIER_ALREADY_EXITS",
                            HttpStatus.CONFLICT);
                }
                if (diagnosticPanelSetRepository.findByName(name) != null) {
                    return new ResponseEntity<>(
                            "Error: Name is already taken!. CUSTOM_ERROR_CODE: 409_NAME_ALREADY_EXITS",
                            HttpStatus.CONFLICT);
                }
            } else {
                try {
                    DiagnosticPanelSetExportImportDTO panelSet =
                            mapper.readValue(file.getBytes(), DiagnosticPanelSetExportImportDTO.class);

                    if(panelSet.getDescription() == null || panelSet.getDescription().isEmpty() || panelSet.getCreationDate() == null || panelSet.getCreationDate().isEmpty()){
                        return new ResponseEntity<>(
                                "Error: There are some empty mandatory fields!. CUSTOM_ERROR_CODE: 400_EMPTY_MANDATORY_FIELDS",
                                HttpStatus.BAD_REQUEST);
                    }

                    panelSet.setName(name);
                    panelSet.setId(id);

                    if(checkDateInFutureFromString(panelSet.getCreationDate()) || (panelSet.getDeletionDate() != null && checkDateInFutureFromString(panelSet.getDeletionDate())) || (panelSet.getExportDate() != null && checkDateInFutureFromString(panelSet.getExportDate()))){
                        return new ResponseEntity<>(
                                "Error: Dates cannot be in the future!. CUSTOM_ERROR_CODE: 422_FUTURE_DATES",
                                HttpStatus.UNPROCESSABLE_ENTITY);
                    }

                    if(panelSet.getDeletionDate() != null && getDateFromISO8601(panelSet.getCreationDate()).after(getDateFromISO8601(panelSet.getDeletionDate()))){
                        return new ResponseEntity<>(
                                "Error: creation date cannot be later than the deletion date. CUSTOM_ERROR_CODE: 422_INVALID_DATES",
                                HttpStatus.UNPROCESSABLE_ENTITY);
                    }

                    if(!checkAuthorExist(panelSet.getAuthor())){
                        return new ResponseEntity<>(
                                "Error: The Author does not exit in the system!. CUSTOM_ERROR_CODE: 422_AUTHOR",
                                HttpStatus.UNPROCESSABLE_ENTITY);
                    }

                    for (DiagnosticPanelExportImportDTO panel : panelSet.getPanels()) {

                        if(panel.getDescription() == null || panel.getDescription().isEmpty() || panel.getCreationDate() == null || panel.getName() == null || panel.getName().isEmpty()  || panel.getId() == null || panel.getId().isEmpty()){
                            return new ResponseEntity<>(
                                    "Error: There are some empty mandatory fields!. CUSTOM_ERROR_CODE: 400_EMPTY_MANDATORY_FIELDS",
                                    HttpStatus.BAD_REQUEST);
                        }

                        panel.setParentIds(panel.getParentIds().stream().distinct().collect(Collectors.toList()));

                        if(panel.getAssociations() != null) {
                            List<DiagnosticPanelAssociation> icd10List = panel.getAssociations().stream()
                                    .filter(temp -> temp.getSource().equals(ESource.ICD10))
                                    .collect(Collectors.toList());

                            List<DiagnosticPanelAssociation> hpoList = panel.getAssociations().stream()
                                    .filter(temp -> temp.getSource().equals(ESource.HPO))
                                    .collect(Collectors.toList());

                            Boolean isInvalidHPO = hpoList.stream().anyMatch(temp ->
                                    genomicDictionaryService
                                            .getHpo(temp.getValue()).getHpoId() == null) ;

                            Boolean isInvalidICD10 = icd10List.stream().anyMatch(temp ->
                                    genomicDictionaryService
                                            .getICD10(temp.getValue()).getId() == null);

                            if (isInvalidHPO || isInvalidICD10) {
                                return new ResponseEntity<>(
                                        "Error: There are panels with invalid associations (HPO, ICD-10). CUSTOM_ERROR_CODE: 422_INVALID_ASSOCIATIONS",
                                        HttpStatus.UNPROCESSABLE_ENTITY);
                            }
                        }

                        if (panel.getFeatures() != null) {

                            List<DiagnosticPanelFeature> transcriptList = panel.getFeatures().stream()
                                    .filter(temp -> temp.getType().equals(EType.TRANSCRIPT))
                                    .collect(Collectors.toList());

                            List<DiagnosticPanelFeature> geneList = panel.getFeatures().stream()
                                    .filter(temp -> temp.getType().equals(EType.GENE))
                                    .collect(Collectors.toList());

                            List<DiagnosticPanelFeature> regionList = panel.getFeatures().stream()
                                    .filter(temp -> temp.getType().equals(EType.REGION))
                                    .collect(Collectors.toList());

                            List<DiagnosticPanelFeature> variantList = panel.getFeatures().stream()
                                    .filter(temp -> temp.getType().equals(EType.VARIANT))
                                    .collect(Collectors.toList());

                            Boolean isInvalidRegion = regionList.stream().anyMatch(temp ->
                                    !isValidRegion(panelSet.getReference().getAssembly(), temp.getDiagnosticPanelFeatureIdentifier()));

                            Boolean isInvalidVariant = variantList.stream().anyMatch(temp ->
                                    !isValidVariant(temp.getDiagnosticPanelFeatureIdentifier()));

                            Boolean isInvalidTranscript = transcriptList.stream().anyMatch(temp ->
                                    genomicDictionaryService
                                            .getTranscript(panelSet.getReference().getAssembly(), temp.getDiagnosticPanelFeatureIdentifier(), Optional.ofNullable(panelSet.getReference().getEnsemblRelease())).getTranscriptId() == null);

                            Boolean isInvalidGene = geneList.stream().anyMatch(temp ->
                                    genomicDictionaryService
                                            .getGene(panelSet.getReference().getAssembly(), temp.getDiagnosticPanelFeatureIdentifier(), Optional.ofNullable(panelSet.getReference().getEnsemblRelease())).getGeneId() == null);

                            if (isInvalidTranscript || isInvalidGene || isInvalidRegion || isInvalidVariant) {
                                return new ResponseEntity<>(
                                        "Error: There are panels with invalid genomic entities (transcripts, genes, variants or regions). CUSTOM_ERROR_CODE: 422_INVALID_TRANSCRIPT",
                                        HttpStatus.UNPROCESSABLE_ENTITY);
                            }
                        }

                        if(checkDateInFutureFromString(panel.getCreationDate()) || (panel.getDeletionDate() != null && checkDateInFutureFromString(panel.getDeletionDate()))){
                            return new ResponseEntity<>(
                                    "Error: Dates cannot be in the future!. CUSTOM_ERROR_CODE: 422_FUTURE_DATES",
                                    HttpStatus.UNPROCESSABLE_ENTITY);
                        }

                        for (String parentId : panel.getParentIds()) {
                            DiagnosticPanelExportImportDTO panelAux = panelSet.getPanels().stream()
                                    .filter(panelId -> parentId.equals(panelId.getId()))
                                    .findAny()
                                    .orElse(null);

                            if(panelAux == null){
                                return new ResponseEntity<>(
                                        "Error: some panels contains references to panels that do not exist. CUSTOM_ERROR_CODE: 422_INVALID_REFERENCES",
                                        HttpStatus.UNPROCESSABLE_ENTITY);
                            }
                        }

                        if(panel.getDeletionDate() != null && getDateFromISO8601(panel.getCreationDate()).after(getDateFromISO8601(panel.getDeletionDate()))){
                            return new ResponseEntity<>(
                                    "Error: creation date cannot be later than the deletion date. CUSTOM_ERROR_CODE: 422_INVALID_DATES",
                                    HttpStatus.UNPROCESSABLE_ENTITY);
                        }
                    }

                    Map<String, List<DiagnosticPanelExportImportDTO>> currentPanelMap =
                            panelSet.getPanels().stream()
                                    .filter(diagnosticPanelExportImportDTO -> diagnosticPanelExportImportDTO.getDeletionDate() == null)
                                    .collect(Collectors.groupingBy(DiagnosticPanelExportImportDTO::getId));

                    for (Map.Entry<String, List<DiagnosticPanelExportImportDTO>> entry : currentPanelMap.entrySet()) {
                        if (entry.getValue().size() > 1) {
                            return new ResponseEntity<>(
                                    "Error: There is more than one current panel. CUSTOM_ERROR_CODE: 409_INVALID_NUMBER_CURRENT_PANELS",
                                    HttpStatus.CONFLICT);
                        }
                    }

                    Map<String, List<DiagnosticPanelExportImportDTO>> archivedPanelMap =
                            panelSet.getPanels().stream()
                                    .filter(diagnosticPanelExportImportDTO -> diagnosticPanelExportImportDTO.getDeletionDate() != null)
                                    .collect(Collectors.groupingBy(DiagnosticPanelExportImportDTO::getId));

                    archivedPanelMap.forEach((s, diagnosticPanelExportImportObjects) -> diagnosticPanelExportImportObjects
                            .sort(Comparator.comparing(DiagnosticPanelExportImportDTO::getCreationDate)));

                    for (Map.Entry<String, List<DiagnosticPanelExportImportDTO>> entry : archivedPanelMap.entrySet()) {

                        if(currentPanelMap.containsKey(entry.getKey())){

                            if (getDateFromISO8601(currentPanelMap.get(entry.getKey()).get(0).getCreationDate())
                                    .before(getDateFromISO8601(archivedPanelMap.get(entry.getKey()).get(0).getDeletionDate()))) {
                                return new ResponseEntity<>(
                                        "Error: There are some panels inside of the collection with the same name or id that coexists in the same period of time. CUSTOM_ERROR_CODE: 422_PANELS_COEXIST",
                                        HttpStatus.CONFLICT);
                            }
                        }

                        if (entry.getValue().size() > 1) {

                            for (int i = 0; i < entry.getValue().size(); i++) {

                                if (!(entry.getValue().indexOf(entry.getValue().get(i)) == entry.getValue().size() - 1)) {

                                    if (getDateFromISO8601(entry.getValue().get(i).getDeletionDate()).after(getDateFromISO8601(entry.getValue().get(i + 1).getCreationDate()))) {
                                        return new ResponseEntity<>(
                                                "Error: There are some panels inside of the collection with the same name or id that coexists in the same period of time. CUSTOM_ERROR_CODE: 422_PANELS_COEXIST",
                                                HttpStatus.CONFLICT);
                                    }
                                }
                            }
                        }
                    }

                    if (isOverwritten) {
                        DiagnosticPanelSet diagnosticPanelSet;

                        if (diagnosticPanelSetRepository.findByDiagnosticPanelSetIdentifier(id) != null) {
                            diagnosticPanelSet = diagnosticPanelSetRepository.findByDiagnosticPanelSetIdentifier(id);
                        }else{
                            diagnosticPanelSet = diagnosticPanelSetRepository.findByName(name);
                        }

                        List<DiagnosticPanel> diagnosticPanelList = diagnosticPanelRepository
                                .findByDiagnosticPanelSetIdentifier(diagnosticPanelSet.getDiagnosticPanelSetIdentifier());

                        diagnosticPanelRepository.deleteAll(diagnosticPanelList);
                        diagnosticPanelSetRepository.delete(diagnosticPanelSet);
                    }

                    DiagnosticPanelSet diagnosticPanelSet = new DiagnosticPanelSet();

                    diagnosticPanelSet.setDiagnosticPanelSetIdentifier(panelSet.getId());
                    diagnosticPanelSet.setName(panelSet.getName());
                    diagnosticPanelSet.setDescription(panelSet.getDescription());
                    diagnosticPanelSet.setReference(panelSet.getReference());
                    diagnosticPanelSet.setAuthor(panelSet.getAuthor());
                    diagnosticPanelSet.setCurrentVersion(panelSet.getCurrentVersion());
                    diagnosticPanelSet.setCreationDate(getDateFromISO8601(panelSet.getCreationDate()));
                    if(panelSet.getDeletionDate() != null) {
                        diagnosticPanelSet.setDeletionDate(getDateFromISO8601(panelSet.getDeletionDate()));
                    }
                    
                    panelSet.getPanels().forEach(panel -> {
                        DiagnosticPanel diagnosticPanel = new DiagnosticPanel();

                        diagnosticPanel.setDiagnosticPanelIdentifier(panel.getId());
                        diagnosticPanel.setName(panel.getName());
                        diagnosticPanel.setDescription(panel.getDescription());
                        diagnosticPanel.setAuthor(panel.getAuthor());
                        diagnosticPanel.setFeatures(panel.getFeatures() == null ? Collections.emptySet() : panel.getFeatures());
                        diagnosticPanel.setAssociations(panel.getAssociations() == null ? Collections.emptySet() : panel.getAssociations());
                        diagnosticPanel.setParentIds(panel.getParentIds());
                        diagnosticPanel.setDiagnosticPanelSetIdentifier(panelSet.getId());
                        diagnosticPanel.setCreationDate(getDateFromISO8601(panel.getCreationDate()));
                        diagnosticPanel.setStartVersion(panel.getStartVersion());
                        if(panel.getEndVersion() != null) {
                            diagnosticPanel.setEndVersion(panel.getEndVersion());
                        }
                        if(panel.getDeletionDate() != null){
                            diagnosticPanel.setDeletionDate(getDateFromISO8601(panel.getDeletionDate()));
                            diagnosticPanel.setStatus(EStatus.ARCHIVED);
                        }else{
                            diagnosticPanel.setStatus(EStatus.CURRENT);
                        }

                        List<DiagnosticPanelFeature> regionList = new ArrayList<>();
                        List<DiagnosticPanelFeature> variantList = new ArrayList<>();

                        if(panel.getFeatures() != null){
                            regionList = panel.getFeatures()
                                    .stream()
                                    .filter(temp -> EType.REGION.equals(temp.getType()))
                                    .collect(Collectors.toList());

                            variantList = panel.getFeatures()
                                    .stream()
                                    .filter(temp -> EType.VARIANT.equals(temp.getType()))
                                    .collect(Collectors.toList());
                        }

                        if (regionList.size() > 0) {
                            createNewRegions(regionList);
                        }

                        if (variantList.size() > 0) {
                            createNewVariants(variantList);
                        }

                        diagnosticPanelRepository.save(diagnosticPanel);

                    });

                    savedDiagnosticPanelSet = diagnosticPanelSetRepository.save(diagnosticPanelSet);

                    List<String> auxList = new ArrayList<>();

                    Map<String, List<DiagnosticPanelExportImportDTO>> allPanelMap =
                            panelSet.getPanels().stream()
                                    .collect(Collectors.groupingBy(DiagnosticPanelExportImportDTO::getId));

                    for (Map.Entry<String, List<DiagnosticPanelExportImportDTO>> entry : allPanelMap.entrySet()) {
                        if (entry.getValue().size() > 1) {
                            auxList.add(entry.getKey());
                        }
                    }

                    auxList.forEach(element -> {
                        List<DiagnosticPanel> diagnosticPanelList = diagnosticPanelRepository
                                .findDiagnosticPanelByDiagnosticPanelSetIdentifierAndDiagnosticPanelIdentifier(panelSet.getId(), element);

                        diagnosticPanelList.sort(Comparator.comparing(DiagnosticPanel::getCreationDate).reversed());

                        for(int i = 0; i < diagnosticPanelList.size(); i++){

                            if(!(diagnosticPanelList.indexOf(diagnosticPanelList.get(i)) == diagnosticPanelList.size() - 1)) {
                                DiagnosticPanel diagnosticPanel = diagnosticPanelRepository.findByGuid(diagnosticPanelList.get(i+1).getGuid());
                                diagnosticPanel.setPreviousVersion(diagnosticPanelList.get(i).getGuid());
                                diagnosticPanelRepository.save(diagnosticPanel);
                            }
                        }

                        DiagnosticPanel diagnosticPanel = diagnosticPanelRepository.findByGuid(diagnosticPanelList.get(diagnosticPanelList.size() - 1).getGuid());
                        diagnosticPanel.setPreviousVersion(diagnosticPanelList.get((diagnosticPanelList.size() - 2)).getGuid());
                        diagnosticPanelRepository.save(diagnosticPanel);
                    });

                } catch (IOException e) {
                    //TODO ADD LOG
                    e.printStackTrace();
                }
            }
        }
        return new ResponseEntity<>("Imported successfully! [ "+ (savedDiagnosticPanelSet != null ? savedDiagnosticPanelSet.getDiagnosticPanelSetIdentifier() : "unexpected error") +" ]", HttpStatus.OK);
    }

    private String getFileExtension(String fileName) {
        String extension = "";

        int i = fileName.lastIndexOf('.');
        if (i > 0) {
            extension = fileName.substring(i + 1);
        }
        return extension;
    }

    private Date getDateFromISO8601(String s) {
        TemporalAccessor ta = DateTimeFormatter.ISO_INSTANT.parse(s);
        Instant i = Instant.from(ta);
        return Date.from(i);
    }

    private Boolean checkDateInFutureFromString (String date){
        boolean isFuture = false;
        if (getDateFromISO8601(date).after(new Date())) {
            isFuture = true;
        }
        return isFuture;
    }

    private Boolean checkAuthorExist(String author) {
        boolean authorExist = false;
        if(userRepository.existsByIdentifier(author) || userHistoryRepository.existsByIdentifier(author)){
            authorExist = true;
        }
        return authorExist;
    }

    private Boolean isValidRegion (String assemblyAccession,String region) {
        boolean isValidRegion = true;
        String seqRegionId = region.split(":")[0];
        String start = region.split(":")[1];
        String end = region.split(":")[2];
        if(!isNaturalNumber(start) || !isNaturalNumber(end)){
            isValidRegion = false;
        }else{
            if(Integer.parseInt(start) >  Integer.parseInt(end)){
                isValidRegion = false;
            }else{
                if(genomicDictionaryService.checkRegion(assemblyAccession, seqRegionId, start, end, Optional.empty()).getStatusCode() != HttpStatus.OK){
                    isValidRegion = false;
                }
            }
        }
        return isValidRegion;
    }

    private Boolean isNaturalNumber (String number) {
        return number.matches("^[1-9]\\d*$");
    }

    private Boolean isValidVariant (String variant) {
        boolean isValidRegion = true;
        if(!variant.split(":")[2].matches("^[ACGT]$") || !variant.split(":")[3].matches("^[ACGT]$") || !isNaturalNumber(variant.split(":")[1])){
            isValidRegion = false;
        }
        return  isValidRegion;
    }

    private void createNewRegions(List<DiagnosticPanelFeature> diagnosticPanelFeatureList) {
        List<Region> regionList = diagnosticPanelFeatureList.stream()
                .filter(temp -> regionRepository.findByRegionIdentifier(temp.getDiagnosticPanelFeatureIdentifier()) == null)
                .map(temp -> {
                    Region region = new Region();
                    region.setRegionIdentifier(temp.getDiagnosticPanelFeatureIdentifier());
                    region.setChromosomeSequence(temp.getDiagnosticPanelFeatureIdentifier().split(":")[0]);
                    region.setInitPosition(temp.getDiagnosticPanelFeatureIdentifier().split(":")[1]);
                    region.setEndPosition(String.valueOf(Integer.parseInt(temp.getDiagnosticPanelFeatureIdentifier().split(":")[2]) + 1));
                    return region;
                })
                .collect(Collectors.toList());

        regionRepository.saveAll(regionList);
    }

    private void createNewVariants(List<DiagnosticPanelFeature> diagnosticPanelFeatureList) {
        List<Variant> variantList = diagnosticPanelFeatureList.stream()
                .filter(temp -> variantRepository.findByVariantIdentifier(temp.getDiagnosticPanelFeatureIdentifier()) == null)
                .map(temp -> {
                    Variant variant = new Variant();
                    variant.setVariantIdentifier(temp.getDiagnosticPanelFeatureIdentifier());
                    variant.setChromosomeSequence(temp.getDiagnosticPanelFeatureIdentifier().split(":")[0]);
                    variant.setAlternative(temp.getDiagnosticPanelFeatureIdentifier().split(":")[1]);
                    variant.setInitPosition(temp.getDiagnosticPanelFeatureIdentifier().split(":")[2]);
                    variant.setReference(temp.getDiagnosticPanelFeatureIdentifier().split(":")[3]);
                    variant.setIsChildren(false);
                    return variant;
                })
                .collect(Collectors.toList());

        variantRepository.saveAll(variantList);
    }
}

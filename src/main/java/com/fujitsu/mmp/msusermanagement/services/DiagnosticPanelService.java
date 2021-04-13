package com.fujitsu.mmp.msusermanagement.services;

import com.fujitsu.mmp.msusermanagement.apis.genomicdictionaryapi.*;
import com.fujitsu.mmp.msusermanagement.constants.ESource;
import com.fujitsu.mmp.msusermanagement.constants.EStatus;
import com.fujitsu.mmp.msusermanagement.constants.EType;
import com.fujitsu.mmp.msusermanagement.dto.diagnosticPanel.DiagnosticPanelDTO;
import com.fujitsu.mmp.msusermanagement.dto.diagnosticPanel.DiagnosticPanelParentChildDTO;
import com.fujitsu.mmp.msusermanagement.dto.diagnosticPanel.DiagnosticPanelTabsDTO;
import com.fujitsu.mmp.msusermanagement.dto.diagnosticPanel.filters.FilterDiagnosticPanelDTO;
import com.fujitsu.mmp.msusermanagement.entities.*;
import com.fujitsu.mmp.msusermanagement.mappers.DiagnosticPanelMapper;
import com.fujitsu.mmp.msusermanagement.mappers.DiagnosticPanelParentChildMapper;
import com.fujitsu.mmp.msusermanagement.mappers.RegionMapper;
import com.fujitsu.mmp.msusermanagement.mappers.VariantMapper;
import com.fujitsu.mmp.msusermanagement.repositories.*;
import com.fujitsu.mmp.msusermanagement.utility.JWTUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DiagnosticPanelService {

    @Autowired
    RegionRepository regionRepository;

    @Autowired
    RegionMapper regionMapper;

    @Autowired
    DiagnosticPanelMapper diagnosticPanelMapper;

    @Autowired
    DiagnosticPanelParentChildMapper diagnosticPanelParentChildMapper;

    @Autowired
    DiagnosticPanelRepository diagnosticPanelRepository;

    @Autowired
    DiagnosticPanelSetRepository diagnosticPanelSetRepository;

    @Autowired
    GenomicDictionaryService genomicDictionaryService;

    @Autowired
    JWTUtility jwtUtility;

    @Autowired
    VariantRepository variantRepository;

    @Autowired
    VariantMapper variantMapper;

    @Autowired
    UserRepository userRepository;

    @Autowired
    UserHistoryRepository userHistoryRepository;

    public ResponseEntity<Page<DiagnosticPanelDTO>> listDiagnosticPanelSet(Pageable pageable, FilterDiagnosticPanelDTO filterDiagnosticPanelDTO, String diagnosticPanelSetIdentifier) {

        HttpStatus responseStatus = HttpStatus.OK;
        Page<DiagnosticPanelDTO> responseBody = null;

        DiagnosticPanelSet diagnosticPanelSet =
                diagnosticPanelSetRepository.findByDiagnosticPanelSetIdentifier(diagnosticPanelSetIdentifier);

        if (diagnosticPanelSet == null) {
            responseStatus = HttpStatus.NOT_FOUND;
        } else {
            List<DiagnosticPanel> diagnosticPanelList = diagnosticPanelRepository.findByDiagnosticPanelSetIdentifier(diagnosticPanelSetIdentifier);

            List<String> listOfAllParents = diagnosticPanelList.stream()
                    .map(DiagnosticPanel::getGuid)
                    .filter(guid -> getDescendantsListToVersion(guid).size()>0)
                    .collect(Collectors.toList());

            Page<DiagnosticPanel> pageEntity = diagnosticPanelRepository.findDiagnosticPanelByFilters
                    (filterDiagnosticPanelDTO, pageable, diagnosticPanelSetIdentifier, listOfAllParents);

            List<DiagnosticPanelDTO> diagnosticPanelDTOList = diagnosticPanelMapper.listEntityToListDto(pageEntity.getContent());

            diagnosticPanelDTOList.forEach(diagnosticPanelDTO -> {

                diagnosticPanelDTO.setGenessNumber((int) diagnosticPanelDTO.getFeatures().stream().filter(diagnosticPanelFeature -> diagnosticPanelFeature.getType().equals(EType.GENE)).count());
                diagnosticPanelDTO.setTranscNumber((int) diagnosticPanelDTO.getFeatures().stream().filter(diagnosticPanelFeature -> diagnosticPanelFeature.getType().equals(EType.TRANSCRIPT)).count());
                diagnosticPanelDTO.setRegionsNumber((int) diagnosticPanelDTO.getFeatures().stream().filter(diagnosticPanelFeature -> diagnosticPanelFeature.getType().equals(EType.REGION)).count());
                diagnosticPanelDTO.setVariantsNumber((int) diagnosticPanelDTO.getFeatures().stream().filter(diagnosticPanelFeature -> diagnosticPanelFeature.getType().equals(EType.VARIANT)).count());
                diagnosticPanelDTO.setAscendingPanels(diagnosticPanelDTO.getParentIds() != null && diagnosticPanelDTO.getParentIds().size() != 0);

                diagnosticPanelDTO.setDescendingPanels(listOfAllParents.contains(diagnosticPanelDTO.getGuid()));
            });
            responseBody = new PageImpl<>(diagnosticPanelDTOList, pageable, pageEntity.getTotalElements());

        }
        return new ResponseEntity<>(responseBody, responseStatus);
    }

    public ResponseEntity<DiagnosticPanelDTO> createDiagnosticPanel(DiagnosticPanelTabsDTO diagnosticPanelTabsDTO, HttpServletRequest httpServletRequest) {
        HttpStatus responseStatus = HttpStatus.CREATED;
        DiagnosticPanelDTO responseBody = null;

        if (diagnosticPanelTabsDTO.getDiagnosticPanelIdentifier() == null || diagnosticPanelTabsDTO.getDiagnosticPanelSetIdentifier() == null) {
            responseStatus = HttpStatus.UNPROCESSABLE_ENTITY;
        } else {
            List<DiagnosticPanel> listToCheckId = diagnosticPanelRepository.findByDiagnosticPanelSetIdentifierAndDiagnosticPanelIdentifier(diagnosticPanelTabsDTO.getDiagnosticPanelSetIdentifier(), diagnosticPanelTabsDTO.getDiagnosticPanelIdentifier());
            List<DiagnosticPanel> listToCheckName = diagnosticPanelRepository.findByDiagnosticPanelSetIdentifierAndName(diagnosticPanelTabsDTO.getDiagnosticPanelSetIdentifier(), diagnosticPanelTabsDTO.getName());
            if (!listToCheckId.isEmpty() && listToCheckId.stream().anyMatch(temp -> EStatus.CURRENT.equals(temp.getStatus()))) {
                responseStatus = HttpStatus.CONFLICT;
            } else {
                if (!listToCheckName.isEmpty() && listToCheckName.stream().anyMatch(temp -> EStatus.CURRENT.equals(temp.getStatus()))) {
                    responseStatus = HttpStatus.CONFLICT;
                } else if (!listToCheckId.isEmpty()) {
                    listToCheckId.sort(Comparator.comparing(DiagnosticPanel::getDeletionDate).reversed());

                    DiagnosticPanel newDiagnosticPanel = createDiagnosticPanelFromDiagnosticPanelTabsDTO(diagnosticPanelTabsDTO);

                    newDiagnosticPanel.setAuthor(getAuthorFromRequest(httpServletRequest));
                    newDiagnosticPanel.setPreviousVersion(listToCheckId.get(0).getGuid());
                    newDiagnosticPanel.setStartVersion(getNextCurrentVersionFromPanelSet(diagnosticPanelTabsDTO.getDiagnosticPanelSetIdentifier()));

                    DiagnosticPanel createdDiagnosticPanel = diagnosticPanelRepository.save(newDiagnosticPanel);
                    responseBody = diagnosticPanelMapper.entityToDto(createdDiagnosticPanel);
                } else {
                    DiagnosticPanel newDiagnosticPanel = createDiagnosticPanelFromDiagnosticPanelTabsDTO(diagnosticPanelTabsDTO);
                    newDiagnosticPanel.setAuthor(getAuthorFromRequest(httpServletRequest));
                    newDiagnosticPanel.setStartVersion(getNextCurrentVersionFromPanelSet(diagnosticPanelTabsDTO.getDiagnosticPanelSetIdentifier()));

                    DiagnosticPanel createdDiagnosticPanel = diagnosticPanelRepository.save(newDiagnosticPanel);
                    responseBody = diagnosticPanelMapper.entityToDto(createdDiagnosticPanel);
                }
            }
        }
        return new ResponseEntity<>(responseBody, responseStatus);
    }

    private int getNextCurrentVersionFromPanelSet(String diagnosticPanelIdentifier) {
        DiagnosticPanelSet diagnosticPanelSet = diagnosticPanelSetRepository.findByDiagnosticPanelSetIdentifier(diagnosticPanelIdentifier);
        diagnosticPanelSet.setCurrentVersion(diagnosticPanelSet.getCurrentVersion() + 1);
        return diagnosticPanelSetRepository.save(diagnosticPanelSet).getCurrentVersion();
    }

    private String getAuthorFromRequest(HttpServletRequest httpServletRequest) {
        String token = httpServletRequest.getHeader("Authorization");
        String username = "";

        if (token != null) {
            username = jwtUtility.getUsernameFromToken(token.substring(6));
        }
        return username;
    }

    private DiagnosticPanel createDiagnosticPanelFromDiagnosticPanelTabsDTO(DiagnosticPanelTabsDTO diagnosticPanelTabsDTO) {
        DiagnosticPanel diagnosticPanel = new DiagnosticPanel();

        diagnosticPanel.setDiagnosticPanelIdentifier(diagnosticPanelTabsDTO.getDiagnosticPanelIdentifier());
        diagnosticPanel.setName(diagnosticPanelTabsDTO.getName());
        diagnosticPanel.setDescription(diagnosticPanelTabsDTO.getDescription());
        diagnosticPanel.setAuthor(diagnosticPanelTabsDTO.getAuthor());
        diagnosticPanel.setStatus(EStatus.CURRENT);
        diagnosticPanel.setCreationDate(new Date());
        diagnosticPanel.setDiagnosticPanelSetIdentifier(diagnosticPanelTabsDTO.getDiagnosticPanelSetIdentifier());

        Set<DiagnosticPanelFeature> diagnosticPanelFeatureSet =
                getDiagnosticPanelFeatureFromDiagnosticPanelTabsDTO(diagnosticPanelTabsDTO);

        Set<DiagnosticPanelAssociation> diagnosticPanelAssociationSet =
                getDiagnosticPanelAssociationFromDiagnosticPanelTabsDTO(diagnosticPanelTabsDTO);

        diagnosticPanel.setFeatures(diagnosticPanelFeatureSet);
        diagnosticPanel.setAssociations(diagnosticPanelAssociationSet);

        List<String> parentIds =
                getParentIdsFromDiagnosticPanelTabsDTO(diagnosticPanelTabsDTO);

        createDescendantsFromDiagnosticPanelTabsDTO(diagnosticPanelTabsDTO);

        diagnosticPanel.setParentIds(parentIds);

        if (diagnosticPanelTabsDTO.getVariantList().size() > 0) {
            createNewVariants(diagnosticPanelTabsDTO);
        }

        if (diagnosticPanelTabsDTO.getRegionList().size() > 0) {
            createNewRegions(diagnosticPanelTabsDTO);
        }

        return diagnosticPanel;
    }

    private void createDescendantsFromDiagnosticPanelTabsDTO(DiagnosticPanelTabsDTO diagnosticPanelTabsDTO) {

        diagnosticPanelTabsDTO.getDescendants()
                .forEach(descendantPanel -> {
                            DiagnosticPanel panel = diagnosticPanelRepository.findDiagnosticPanelByDiagnosticPanelSetIdentifierAndDiagnosticPanelIdentifierAndStatus(
                                    diagnosticPanelTabsDTO.getDiagnosticPanelSetIdentifier(), descendantPanel.getDiagnosticPanelIdentifier(), EStatus.CURRENT);

                            panel.setStatus(EStatus.ARCHIVED);
                            panel.setDeletionDate(new Date());
                            panel.setEndVersion(getNextCurrentVersionFromPanelSet(diagnosticPanelTabsDTO.getDiagnosticPanelSetIdentifier()));
                            diagnosticPanelRepository.save(panel);

                            List<String> descendantsParentIds = panel.getParentIds();
                            descendantsParentIds.add(diagnosticPanelTabsDTO.getDiagnosticPanelIdentifier());

                            DiagnosticPanel newPanel = new DiagnosticPanel();
                            newPanel.setDiagnosticPanelIdentifier(panel.getDiagnosticPanelIdentifier());
                            newPanel.setName(panel.getName());
                            newPanel.setDescription(panel.getDescription());
                            newPanel.setAuthor(panel.getAuthor());
                            newPanel.setFeatures(panel.getFeatures());
                            newPanel.setAssociations(panel.getAssociations());
                            newPanel.setStatus(EStatus.CURRENT);
                            newPanel.setCreationDate(new Date());
                            newPanel.setDiagnosticPanelSetIdentifier(panel.getDiagnosticPanelSetIdentifier());
                            newPanel.setParentIds(descendantsParentIds);
                            newPanel.setPreviousVersion(panel.getGuid());
                            newPanel.setStartVersion(getNextCurrentVersionFromPanelSet(diagnosticPanelTabsDTO.getDiagnosticPanelSetIdentifier()));

                            diagnosticPanelRepository.save(newPanel);
                        }
                );
    }

    private List<String> getParentIdsFromDiagnosticPanelTabsDTO(DiagnosticPanelTabsDTO diagnosticPanelTabsDTO) {
        List<String> parentIds = new ArrayList<>();

        diagnosticPanelTabsDTO.getAscendants().forEach(
                diagnosticPanelParentChildDTO ->
                        parentIds.add(diagnosticPanelParentChildDTO.getDiagnosticPanelIdentifier())
        );
        return parentIds;
    }

    private Set<DiagnosticPanelAssociation> getDiagnosticPanelAssociationFromDiagnosticPanelTabsDTO(DiagnosticPanelTabsDTO diagnosticPanelTabsDTO) {
        Set<DiagnosticPanelAssociation> diagnosticPanelAssociationSet = new HashSet<>();

        diagnosticPanelTabsDTO.getIcd10List().forEach(
                icd10 -> {
                    DiagnosticPanelAssociation diagnosticPanelAssociation = new DiagnosticPanelAssociation();
                    diagnosticPanelAssociation.setSource(ESource.ICD10);
                    diagnosticPanelAssociation.setValue(icd10.getId());
                    diagnosticPanelAssociationSet.add(diagnosticPanelAssociation);
                }
        );

        diagnosticPanelTabsDTO.getHpoList().forEach(
                hpo -> {
                    DiagnosticPanelAssociation diagnosticPanelAssociation = new DiagnosticPanelAssociation();
                    diagnosticPanelAssociation.setSource(ESource.HPO);
                    diagnosticPanelAssociation.setValue(hpo.getHpoId());
                    diagnosticPanelAssociationSet.add(diagnosticPanelAssociation);
                }
        );
        return diagnosticPanelAssociationSet;
    }

    public ResponseEntity<DiagnosticPanelDTO> update(String guid, DiagnosticPanelTabsDTO diagnosticPanelTabsDTO) {
        HttpStatus responseStatus = HttpStatus.OK;
        DiagnosticPanelDTO responseBody = null;

        if (guid == null) {
            responseStatus = HttpStatus.UNPROCESSABLE_ENTITY;
        } else {
            DiagnosticPanel entity = diagnosticPanelRepository.findByGuid(guid);
            if (entity == null) {
                responseStatus = HttpStatus.NOT_FOUND;
            } else if (entity.getStatus() == EStatus.ARCHIVED || entity.getDeletionDate() != null) {
                responseStatus = HttpStatus.BAD_REQUEST;
            } else {
                if (!diagnosticPanelTabsDTO.getIsNewVersion()) {
                    setDescendantList(entity.getDiagnosticPanelSetIdentifier(), entity.getDiagnosticPanelIdentifier(), diagnosticPanelTabsDTO);
                } else {
                    if (diagnosticPanelTabsDTO.getName() == null || diagnosticPanelTabsDTO.getName().isEmpty()) {
                        responseStatus = HttpStatus.BAD_REQUEST;
                    } else if (!diagnosticPanelTabsDTO.getName().equals(entity.getName()) && diagnosticPanelRepository.existsByDiagnosticPanelSetIdentifierAndNameAndStatus(diagnosticPanelTabsDTO.getDiagnosticPanelSetIdentifier(), diagnosticPanelTabsDTO.getName(), EStatus.CURRENT)) {
                        responseStatus = HttpStatus.BAD_REQUEST;
                    } else {
                        entity.setDeletionDate(new Date());
                        entity.setStatus(EStatus.ARCHIVED);
                        entity.setEndVersion(getNextCurrentVersionFromPanelSet(diagnosticPanelTabsDTO.getDiagnosticPanelSetIdentifier()));

                        Set<DiagnosticPanelFeature> diagnosticPanelFeatureSet =
                                getDiagnosticPanelFeatureFromDiagnosticPanelTabsDTO(diagnosticPanelTabsDTO);

                        Set<DiagnosticPanelAssociation> diagnosticPanelAssociationSet =
                                getDiagnosticPanelAssociationFromDiagnosticPanelTabsDTO(diagnosticPanelTabsDTO);

                        List<String> parentIds =
                                getParentIdsFromDiagnosticPanelTabsDTO(diagnosticPanelTabsDTO);

                        setDescendantList(entity.getDiagnosticPanelSetIdentifier(), entity.getDiagnosticPanelIdentifier(), diagnosticPanelTabsDTO);

                        DiagnosticPanel updatedDiagnosticPanel = new DiagnosticPanel();
                        DiagnosticPanelSet diagnosticPanelSet = diagnosticPanelSetRepository.findByDiagnosticPanelSetIdentifier(diagnosticPanelTabsDTO.getDiagnosticPanelSetIdentifier());

                        updatedDiagnosticPanel.setDiagnosticPanelIdentifier(diagnosticPanelTabsDTO.getDiagnosticPanelIdentifier());
                        updatedDiagnosticPanel.setName(diagnosticPanelTabsDTO.getName());
                        updatedDiagnosticPanel.setDescription(diagnosticPanelTabsDTO.getDescription());
                        updatedDiagnosticPanel.setAuthor(entity.getAuthor());
                        updatedDiagnosticPanel.setStatus(EStatus.CURRENT);
                        updatedDiagnosticPanel.setCreationDate(new Date());
                        updatedDiagnosticPanel.setDiagnosticPanelSetIdentifier(diagnosticPanelTabsDTO.getDiagnosticPanelSetIdentifier());
                        updatedDiagnosticPanel.setFeatures(diagnosticPanelFeatureSet);
                        updatedDiagnosticPanel.setAssociations(diagnosticPanelAssociationSet);
                        updatedDiagnosticPanel.setParentIds(parentIds);
                        updatedDiagnosticPanel.setPreviousVersion(entity.getGuid());

                        updatedDiagnosticPanel.setStartVersion(diagnosticPanelSet.getCurrentVersion());

                        diagnosticPanelRepository.save(entity);
                        DiagnosticPanel createdDiagnosticPanel = diagnosticPanelRepository.save(updatedDiagnosticPanel);

                        if (diagnosticPanelTabsDTO.getVariantList().size() > 0) {
                            createNewVariants(diagnosticPanelTabsDTO);
                        }

                        if (diagnosticPanelTabsDTO.getRegionList().size() > 0) {
                            createNewRegions(diagnosticPanelTabsDTO);
                        }

                        responseBody = diagnosticPanelMapper.entityToDto(createdDiagnosticPanel);
                    }
                }
            }
        }
        return new ResponseEntity<>(responseBody, responseStatus);
    }

    private void createNewRegions(DiagnosticPanelTabsDTO diagnosticPanelTabsDTO) {
        List<Region> regionList = diagnosticPanelTabsDTO.getRegionList().stream()
                .filter(temp -> regionRepository.findByRegionIdentifier(temp.getRegionIdentifier()) == null)
                .map(temp -> {
                    Region region = new Region();
                    region.setRegionIdentifier(temp.getRegionIdentifier());
                    region.setChromosomeSequence(temp.getChromosome());
                    region.setInitPosition(temp.getInitPosition());
                    region.setEndPosition(String.valueOf(Integer.parseInt(temp.getEndPosition()) + 1));
                    return region;
                })
                .collect(Collectors.toList());

        regionRepository.saveAll(regionList);
    }

    private void createNewVariants(DiagnosticPanelTabsDTO diagnosticPanelTabsDTO) {
        List<Variant> variantList = diagnosticPanelTabsDTO.getVariantList().stream()
                .filter(temp -> variantRepository.findByVariantIdentifier(temp.getVariantIdentifier()) == null)
                .map(temp -> {
                    Variant variant = new Variant();
                    variant.setVariantIdentifier(temp.getVariantIdentifier());
                    variant.setChromosomeSequence(temp.getChromosomeSequence());
                    variant.setAlternative(temp.getAlternative());
                    variant.setInitPosition(temp.getInitPosition());
                    variant.setReference(temp.getReference());
                    variant.setIsChildren(temp.getIsChildren());
                    return variant;
                })
                .collect(Collectors.toList());

        variantRepository.saveAll(variantList);
    }

    public ResponseEntity<DiagnosticPanelDTO> findByIdentifier(String guid) {
        HttpStatus responseStatus = HttpStatus.OK;
        DiagnosticPanelDTO responseBody = null;

        DiagnosticPanel entity = diagnosticPanelRepository.findByGuid(guid);
        if (entity == null) {
            responseStatus = HttpStatus.NOT_FOUND;
        } else {
            responseBody = diagnosticPanelMapper.entityToDto(entity);
        }

        return new ResponseEntity<>(responseBody, responseStatus);
    }

    public ResponseEntity<DiagnosticPanelTabsDTO> getDiagnosticPanelTabs(String guid) {
        HttpStatus responseStatus = HttpStatus.OK;
        DiagnosticPanelTabsDTO responseBody = new DiagnosticPanelTabsDTO();

        DiagnosticPanel entity = diagnosticPanelRepository.findByGuid(guid);

        if (entity == null) {
            responseStatus = HttpStatus.NOT_FOUND;
        } else {
            DiagnosticPanelSet diagnosticPanelSet = diagnosticPanelSetRepository.
                    findByDiagnosticPanelSetIdentifier(entity.getDiagnosticPanelSetIdentifier());

            List<DiagnosticPanel> descendantDiagnosticPanels = getDescendantsListToVersion(guid);

            List<HPO> hpoList = new ArrayList<>();

            entity.getAssociations().stream().filter(diagnosticPanelAssociation ->
                    diagnosticPanelAssociation.getSource().equals(ESource.HPO))
                    .forEach(diagnosticPanel ->
                            hpoList.add(genomicDictionaryService.getHpo(diagnosticPanel.getValue())));

            List<ICD10> icd10List = new ArrayList<>();

            entity.getAssociations().stream().filter(diagnosticPanelAssociation ->
                    diagnosticPanelAssociation.getSource().equals(ESource.ICD10))
                    .forEach(diagnosticPanel ->
                            icd10List.add(genomicDictionaryService.getICD10(diagnosticPanel.getValue())));

            List<Gene> geneList = new ArrayList<>();

            entity.getFeatures().stream().filter(diagnosticPanelFeature ->
                    diagnosticPanelFeature.getType().equals(EType.GENE))
                    .forEach(diagnosticPanel -> {
                        Gene g = genomicDictionaryService
                                .getGene(diagnosticPanelSet.getReference().getAssembly(),
                                        diagnosticPanel.getDiagnosticPanelFeatureIdentifier(),
                                        Optional.of(diagnosticPanelSet.getReference().getEnsemblRelease())
                                );
                        g.setIsChildren(false);
                        geneList.add(g);
                    });

            descendantDiagnosticPanels.forEach(
                    diagnosticPanel -> diagnosticPanel.getFeatures().stream().filter(diagnosticPanelFeature ->
                            diagnosticPanelFeature.getType().equals(EType.GENE))
                            .forEach(diagnosticPanelFeature -> {
                                Gene g = genomicDictionaryService
                                        .getGene(diagnosticPanelSet.getReference().getAssembly(),
                                                diagnosticPanelFeature.getDiagnosticPanelFeatureIdentifier(),
                                                Optional.of(diagnosticPanelSet.getReference().getEnsemblRelease())
                                        );
                                g.setIsChildren(true);
                                geneList.add(g);
                            })
            );

            List<Transcript> transcriptList = new ArrayList<>();

            entity.getFeatures().stream().filter(diagnosticPanelFeature ->
                    diagnosticPanelFeature.getType().equals(EType.TRANSCRIPT))
                    .forEach(diagnosticPanel -> {
                        Transcript t = genomicDictionaryService
                                .getTranscript(diagnosticPanelSet.getReference().getAssembly(),
                                        diagnosticPanel.getDiagnosticPanelFeatureIdentifier(),
                                        Optional.of(diagnosticPanelSet.getReference().getEnsemblRelease())
                                );
                        t.setIsChildren(false);
                        transcriptList.add(t);
                    });

            descendantDiagnosticPanels.forEach(
                    diagnosticPanel -> diagnosticPanel.getFeatures().stream().filter(diagnosticPanelFeature ->
                            diagnosticPanelFeature.getType().equals(EType.TRANSCRIPT))
                            .forEach(diagnosticPanelFeature -> {
                                Transcript t = genomicDictionaryService
                                        .getTranscript(diagnosticPanelSet.getReference().getAssembly(),
                                                diagnosticPanelFeature.getDiagnosticPanelFeatureIdentifier(),
                                                Optional.of(diagnosticPanelSet.getReference().getEnsemblRelease())
                                        );
                                t.setIsChildren(true);
                                transcriptList.add(t);
                            })
            );

            List<DiagnosticPanelParentChildDTO> ascendantList = new ArrayList<>();

            entity.getParentIds().forEach(diagnosticPanelId -> {
                List<DiagnosticPanel> diagnosticPanelList =
                        diagnosticPanelRepository.findByDiagnosticPanelSetIdentifierAndDiagnosticPanelIdentifier(diagnosticPanelSet.getDiagnosticPanelSetIdentifier(), diagnosticPanelId);

                DiagnosticPanel diagnosticPanel;

                List<DiagnosticPanel> diagnosticPanelListAux = diagnosticPanelList.stream()
                        .filter(panel -> panel.getStatus().equals(EStatus.CURRENT))
                        .collect(Collectors.toList());

                if (diagnosticPanelListAux.size() != 0) {
                    diagnosticPanel = diagnosticPanelListAux.get(0);
                } else {
                    diagnosticPanelList.sort(Comparator.comparing(DiagnosticPanel::getDeletionDate));
                    diagnosticPanel = diagnosticPanelList.get(0);
                }

                ascendantList.add(diagnosticPanelParentChildMapper.entityToDto(diagnosticPanel));
            });

            List<RegionDTO> regionList = new ArrayList<>();

            entity.getFeatures().stream()
                    .filter(diagnosticPanelFeature -> diagnosticPanelFeature.getType().equals(EType.REGION))
                    .forEach(diagnosticPanel -> {
                        Region region = regionRepository.findByRegionIdentifier(diagnosticPanel.getDiagnosticPanelFeatureIdentifier());
                        if (region != null) {
                            RegionDTO regionDTO = regionMapper.entityToDTO(region);
                            int aux = Integer.parseInt(regionDTO.getEndPosition());
                            regionDTO.setEndPosition(Integer.toString(aux - 1));
                            regionList.add(regionDTO);
                        }
                    });

            regionList.sort(Comparator.comparing(RegionDTO::getChromosome, DiagnosticPanelService::customComparator));

            List<VariantDTO> variantList = new ArrayList<>();

            entity.getFeatures().stream().filter(diagnosticPanelFeature ->
                    diagnosticPanelFeature.getType().equals(EType.VARIANT))
                    .forEach(diagnosticPanel -> {
                        Variant variant = variantRepository.findByVariantIdentifier(diagnosticPanel.getDiagnosticPanelFeatureIdentifier());
                        if (variant != null) {
                            VariantDTO variantDTO = variantMapper.entityToDTO(variant);
                            variantList.add(variantDTO);
                        }

                    });

            variantList.sort(Comparator.comparing(VariantDTO::getChromosomeSequence, DiagnosticPanelService::customComparator));

            descendantDiagnosticPanels.forEach(
                    diagnosticPanel -> diagnosticPanel.getFeatures().stream().filter(diagnosticPanelFeature ->
                            diagnosticPanelFeature.getType().equals(EType.VARIANT))
                            .forEach(diagnosticPanelFeature -> {
                                Variant variant = variantRepository.findByVariantIdentifier(diagnosticPanelFeature.getDiagnosticPanelFeatureIdentifier());
                                VariantDTO vDto = variantMapper.entityToDTO(variant);
                                vDto.setIsChildren(true);
                                variantList.add(vDto);
                            })
            );

            responseBody.setDiagnosticPanelIdentifier(entity.getDiagnosticPanelIdentifier());
            responseBody.setName(entity.getName());
            responseBody.setDescription(entity.getDescription());
            responseBody.setAuthor(getFormattedAuthor(entity.getAuthor()));
            responseBody.setCreationDate(entity.getCreationDate());
            responseBody.setDeletionDate(entity.getDeletionDate());
            responseBody.setDiagnosticPanelSetIdentifier(entity.getDiagnosticPanelSetIdentifier());
            responseBody.setHpoList(hpoList);
            responseBody.setIcd10List(icd10List);
            responseBody.setGeneList(geneList);
            responseBody.setTranscriptList(transcriptList);
            responseBody.setRegionList(regionList);
            responseBody.setVariantList(variantList);
            responseBody.setAscendants(ascendantList);
            responseBody.setDescendants(diagnosticPanelParentChildMapper.listEntityToListDto(descendantDiagnosticPanels));
            responseBody.setAssembly(diagnosticPanelSet.getReference().getAssembly());
            responseBody.setEnsmblRelease(diagnosticPanelSet.getReference().getEnsemblRelease());
            responseBody.setIsHuman(genomicDictionaryService.isHuman(diagnosticPanelSet.getReference().getAssembly()));
            responseBody.setGuid(entity.getGuid());
            responseBody.setPreviousVersion(entity.getPreviousVersion());
            responseBody.setNextVersion(searchNextVersion(entity.getGuid()));

        }
        return new ResponseEntity<>(responseBody, responseStatus);
    }

    public ResponseEntity<Void> deleteDiagnosticPanel(String diagnosticPanelSetIdentifier, String guid, Boolean toDeleteChildren) {
        HttpStatus responseStatus = HttpStatus.NO_CONTENT;

        if(guid == null){
            responseStatus = HttpStatus.UNPROCESSABLE_ENTITY;
        } else {
            DiagnosticPanel diagnosticPanel = diagnosticPanelRepository.findDiagnosticPanelByGuid(guid);
            if(diagnosticPanel == null){
                responseStatus = HttpStatus.NOT_FOUND;
            } else {
                deleteTransverse(diagnosticPanel.getDiagnosticPanelIdentifier(), diagnosticPanelSetIdentifier, toDeleteChildren);
            }
        }
        return new ResponseEntity<>(responseStatus);
    }

    private void deleteTransverse(String diagnosticPanelId, String diagnosticPanelSetIdentifier, Boolean toDeleteChildren) {

        DiagnosticPanel diagnosticPanel = diagnosticPanelRepository
                .findDiagnosticPanelByDiagnosticPanelSetIdentifierAndDiagnosticPanelIdentifierAndStatus(diagnosticPanelSetIdentifier, diagnosticPanelId, EStatus.CURRENT);

        DiagnosticPanelSet diagnosticPanelSet = diagnosticPanelSetRepository.findByDiagnosticPanelSetIdentifier(diagnosticPanelSetIdentifier);

        diagnosticPanelSet.setCurrentVersion(diagnosticPanelSet.getCurrentVersion() + 1);
        diagnosticPanelSetRepository.save(diagnosticPanelSet);

        deletePanel(diagnosticPanel, diagnosticPanelSet.getCurrentVersion());

        List<String> childrenListMultiParent = diagnosticPanelRepository
                .findDiagnosticPanelByParentIdsAndDiagnosticPanelSetIdentifierAndStatus(diagnosticPanel.getDiagnosticPanelIdentifier(), diagnosticPanel.getDiagnosticPanelSetIdentifier(), EStatus.CURRENT).stream()
                .filter(temp -> temp.getParentIds().size() > 1)
                .map(DiagnosticPanel::getDiagnosticPanelIdentifier)
                .collect(Collectors.toList());

        List<String> childrenListMonoParent = diagnosticPanelRepository
                .findDiagnosticPanelByParentIdsAndDiagnosticPanelSetIdentifierAndStatus(diagnosticPanel.getDiagnosticPanelIdentifier(), diagnosticPanel.getDiagnosticPanelSetIdentifier(), EStatus.CURRENT).stream()
                .filter(temp -> temp.getParentIds().size() <= 1)
                .map(DiagnosticPanel::getDiagnosticPanelIdentifier)
                .collect(Collectors.toList());

        childrenListMultiParent.forEach(temp -> {
            DiagnosticPanel diagnosticPanelAux = diagnosticPanelRepository
                    .findDiagnosticPanelByDiagnosticPanelSetIdentifierAndDiagnosticPanelIdentifierAndStatus(diagnosticPanelSetIdentifier, temp, EStatus.CURRENT);

            deleteAndCreateNewVersionDiagnosticPanel(diagnosticPanel.getDiagnosticPanelIdentifier(), diagnosticPanelAux);

        });

        childrenListMonoParent.forEach(temp -> {

            DiagnosticPanel diagnosticPanelAux = diagnosticPanelRepository
                    .findDiagnosticPanelByDiagnosticPanelSetIdentifierAndDiagnosticPanelIdentifierAndStatus(diagnosticPanelSetIdentifier, temp, EStatus.CURRENT);

            if(toDeleteChildren) {
                deleteTransverse(diagnosticPanelAux.getDiagnosticPanelIdentifier(), diagnosticPanelSetIdentifier, true);
            } else {
                deleteAndCreateNewVersionDiagnosticPanel(diagnosticPanel.getDiagnosticPanelIdentifier(), diagnosticPanelAux);
            }
        });
    }

    private Set<DiagnosticPanelFeature> getDiagnosticPanelFeatureFromDiagnosticPanelTabsDTO(DiagnosticPanelTabsDTO diagnosticPanelTabsDTO) {

        Set<DiagnosticPanelFeature> diagnosticPanelFeatureSet = new HashSet<>();

        diagnosticPanelTabsDTO.getGeneList().forEach(
                gene -> {
                    DiagnosticPanelFeature diagnosticPanelFeature = new DiagnosticPanelFeature();
                    diagnosticPanelFeature.setDiagnosticPanelFeatureIdentifier(gene.getGeneId());
                    diagnosticPanelFeature.setType(EType.GENE);
                    diagnosticPanelFeatureSet.add(diagnosticPanelFeature);
                }
        );

        diagnosticPanelTabsDTO.getTranscriptList().forEach(
                transcript -> {
                    DiagnosticPanelFeature diagnosticPanelFeature = new DiagnosticPanelFeature();
                    diagnosticPanelFeature.setDiagnosticPanelFeatureIdentifier(transcript.getTranscriptId());
                    diagnosticPanelFeature.setType(EType.TRANSCRIPT);
                    diagnosticPanelFeatureSet.add(diagnosticPanelFeature);
                }
        );

        diagnosticPanelTabsDTO.getVariantList().forEach(
                variant -> {
                    DiagnosticPanelFeature diagnosticPanelFeature = new DiagnosticPanelFeature();
                    diagnosticPanelFeature.setDiagnosticPanelFeatureIdentifier(variant.getVariantIdentifier());
                    diagnosticPanelFeature.setType(EType.VARIANT);
                    diagnosticPanelFeatureSet.add(diagnosticPanelFeature);
                }
        );

        diagnosticPanelTabsDTO.getRegionList().forEach(
                region -> {
                    DiagnosticPanelFeature diagnosticPanelFeature = new DiagnosticPanelFeature();
                    diagnosticPanelFeature.setDiagnosticPanelFeatureIdentifier(region.getRegionIdentifier());
                    diagnosticPanelFeature.setType(EType.REGION);
                    diagnosticPanelFeatureSet.add(diagnosticPanelFeature);
                }
        );

        return diagnosticPanelFeatureSet;
    }

    private String searchNextVersion(String guid) {
        String guidNextVersion = "";
        DiagnosticPanel diagnosticPanel = diagnosticPanelRepository.findDiagnosticPanelByPreviousVersion(guid);

        if (diagnosticPanel != null) {
            guidNextVersion = diagnosticPanel.getGuid();
        }
        return guidNextVersion;
    }

    private void setDescendantList(String panelSetId, String panelId, DiagnosticPanelTabsDTO diagnosticPanelTabObject) {

        List<String> currentDescendantPanelsIdsList = diagnosticPanelRepository
                .findDiagnosticPanelByParentIdsAndDiagnosticPanelSetIdentifierAndStatus(panelId, panelSetId, EStatus.CURRENT)
                .stream()
                .map(DiagnosticPanel::getDiagnosticPanelIdentifier)
                .collect(Collectors.toList());

        List<String> newDescendantPanelsIdsList = diagnosticPanelTabObject.getDescendants().stream()
                .map(DiagnosticPanelParentChildDTO::getDiagnosticPanelIdentifier)
                .distinct()
                .collect(Collectors.toList());

        List<String> panelsToDeleteIdsList = diagnosticPanelTabObject.getDescendants().stream()
                .filter(temp -> temp.getToDelete() != null)
                .filter(DiagnosticPanelParentChildDTO::getToDelete)
                .map(DiagnosticPanelParentChildDTO::getDiagnosticPanelIdentifier)
                .collect(Collectors.toList());

        List<String> panelsToRootIdsList = diagnosticPanelTabObject.getDescendants().stream()
                .filter(temp -> temp.getToDelete() != null)
                .filter(temp -> !temp.getToDelete())
                .map(DiagnosticPanelParentChildDTO::getDiagnosticPanelIdentifier)
                .collect(Collectors.toList());

        setNewPanels(panelSetId, panelId, currentDescendantPanelsIdsList, newDescendantPanelsIdsList);

        if(panelsToRootIdsList.size() > 0) {
            setRootPanels(panelSetId, panelId, panelsToRootIdsList);
        }

        if(panelsToDeleteIdsList.size() > 0) {
            setDeletePanels(panelSetId, panelsToDeleteIdsList);
        }
    }

    private void setNewPanels(String panelSetId, String panelId, List<String> currentDescendantPanelsIdsList, List<String> newDescendantPanelsIdsList) {
        List<String> newPanels = newDescendantPanelsIdsList.stream()
                .filter(element -> !currentDescendantPanelsIdsList.contains(element))
                .collect(Collectors.toList());

        if(newPanels.size() > 0){
            newPanels.forEach(auxPanelId -> {
                DiagnosticPanel diagnosticPanel = diagnosticPanelRepository.findDiagnosticPanelByDiagnosticPanelSetIdentifierAndDiagnosticPanelIdentifierAndStatus(
                        panelSetId, auxPanelId, EStatus.CURRENT);

                int version = getNextCurrentVersionFromPanelSet(diagnosticPanel.getDiagnosticPanelSetIdentifier());

                diagnosticPanel.setStatus(EStatus.ARCHIVED);
                diagnosticPanel.setDeletionDate(new Date());
                diagnosticPanel.setEndVersion(version);
                diagnosticPanelRepository.save(diagnosticPanel);

                DiagnosticPanel newPanel = new DiagnosticPanel();

                newPanel.setDiagnosticPanelIdentifier(diagnosticPanel.getDiagnosticPanelIdentifier());
                newPanel.setName(diagnosticPanel.getName());
                newPanel.setAuthor(diagnosticPanel.getAuthor());
                newPanel.setDescription(diagnosticPanel.getDescription());
                newPanel.setFeatures(diagnosticPanel.getFeatures());
                newPanel.setAssociations(diagnosticPanel.getAssociations());

                List<String> auxList = diagnosticPanel.getParentIds();
                auxList.add(panelId);

                newPanel.setParentIds(auxList);
                newPanel.setStatus(EStatus.CURRENT);
                newPanel.setCreationDate(new Date());
                newPanel.setDiagnosticPanelSetIdentifier(diagnosticPanel.getDiagnosticPanelSetIdentifier());
                newPanel.setPreviousVersion(diagnosticPanel.getGuid());
                newPanel.setStartVersion(version);

                diagnosticPanelRepository.save(newPanel);
            });
        }
    }

    private void setRootPanels(String panelSetId, String panelId, List<String> panelsToRootIdsList) {
        panelsToRootIdsList.forEach(auxPanelId -> {

            int version = getNextCurrentVersionFromPanelSet(panelSetId);

            DiagnosticPanel diagnosticPanel = diagnosticPanelRepository.findDiagnosticPanelByDiagnosticPanelSetIdentifierAndDiagnosticPanelIdentifierAndStatus(
                    panelSetId, auxPanelId, EStatus.CURRENT);

            diagnosticPanel.setStatus(EStatus.ARCHIVED);
            diagnosticPanel.setDeletionDate(new Date());
            diagnosticPanel.setEndVersion(version);
            diagnosticPanelRepository.save(diagnosticPanel);

            DiagnosticPanel newPanel = new DiagnosticPanel();

            newPanel.setDiagnosticPanelIdentifier(diagnosticPanel.getDiagnosticPanelIdentifier());
            newPanel.setName(diagnosticPanel.getName());
            newPanel.setAuthor(diagnosticPanel.getAuthor());
            newPanel.setDescription(diagnosticPanel.getDescription());
            newPanel.setFeatures(diagnosticPanel.getFeatures());
            newPanel.setAssociations(diagnosticPanel.getAssociations());

            List<String> auxList = diagnosticPanel.getParentIds();
            auxList.remove(panelId);

            newPanel.setParentIds(auxList);
            newPanel.setStatus(EStatus.CURRENT);
            newPanel.setCreationDate(new Date());
            newPanel.setDiagnosticPanelSetIdentifier(diagnosticPanel.getDiagnosticPanelSetIdentifier());
            newPanel.setPreviousVersion(diagnosticPanel.getGuid());
            newPanel.setStartVersion(version);

            diagnosticPanelRepository.save(newPanel);
        });
    }

    private void setDeletePanels(String panelSetId, List<String> panelsToDeleteIdsList) {
        List<String> toDeleteIdList = new ArrayList<>();

        panelsToDeleteIdsList.forEach(panelId -> {
            List<String> test = traverse(panelSetId, panelId);
            toDeleteIdList.addAll(test);
        });

        toDeleteIdList.forEach(panelId -> {
            int version = getNextCurrentVersionFromPanelSet(panelSetId);

            DiagnosticPanel diagnosticPanel = diagnosticPanelRepository.findDiagnosticPanelByDiagnosticPanelSetIdentifierAndDiagnosticPanelIdentifierAndStatus(
                    panelSetId, panelId, EStatus.CURRENT);

            diagnosticPanel.setDeletionDate(new Date());
            diagnosticPanel.setStatus(EStatus.ARCHIVED);
            diagnosticPanel.setEndVersion(version);
            diagnosticPanelRepository.save(diagnosticPanel);

            deletePanelIdFromParentArray(panelSetId, panelId);
        });
    }

    private List<String> traverse(String panelSetId, String diagnosticPanelId) {
        List<String> results = new ArrayList<>();
        results.add(diagnosticPanelId);

        List<DiagnosticPanel> diagnosticPanelList = diagnosticPanelRepository.findDiagnosticPanelByParentIdsAndDiagnosticPanelSetIdentifier(diagnosticPanelId, panelSetId);

        if (diagnosticPanelList.size() > 0) {
            List<String> diagnosticPanelIdsList = diagnosticPanelList.stream()
                    .filter(panel -> panel.getParentIds().size() <= 1)
                    .map(DiagnosticPanel::getDiagnosticPanelIdentifier)
                    .collect(Collectors.toList());

            diagnosticPanelIdsList.forEach(element -> results.addAll(traverse(panelSetId, element)));
        }
        return results;
    }

    private void deletePanelIdFromParentArray(String panelSetId, String diagnosticPanelId) {
        DiagnosticPanelSet diagnosticPanelSet = diagnosticPanelSetRepository.findByDiagnosticPanelSetIdentifier(panelSetId);
        diagnosticPanelSet.setCurrentVersion(diagnosticPanelSet.getCurrentVersion() + 1);
        diagnosticPanelSetRepository.save(diagnosticPanelSet);

        List<DiagnosticPanel> diagnosticPanelList = diagnosticPanelRepository.findDiagnosticPanelByParentIdsAndDiagnosticPanelSetIdentifierAndStatus(
                diagnosticPanelId, panelSetId, EStatus.CURRENT);

        diagnosticPanelList.forEach(diagnosticPanel -> {
            diagnosticPanel.setStatus(EStatus.ARCHIVED);
            diagnosticPanel.setDeletionDate(new Date());
            diagnosticPanel.setEndVersion(diagnosticPanelSet.getCurrentVersion());

            diagnosticPanelRepository.save(diagnosticPanel);

            DiagnosticPanel newPanel = new DiagnosticPanel();

            newPanel.setDiagnosticPanelIdentifier(diagnosticPanel.getDiagnosticPanelIdentifier());
            newPanel.setName(diagnosticPanel.getName());
            newPanel.setAuthor(diagnosticPanel.getAuthor());
            newPanel.setDescription(diagnosticPanel.getDescription());
            newPanel.setFeatures(diagnosticPanel.getFeatures());
            newPanel.setAssociations(diagnosticPanel.getAssociations());

            List<String> auxList = diagnosticPanel.getParentIds();
            auxList.remove(diagnosticPanelId);

            newPanel.setParentIds(auxList);
            newPanel.setStatus(EStatus.CURRENT);
            newPanel.setCreationDate(new Date());
            newPanel.setDiagnosticPanelSetIdentifier(diagnosticPanel.getDiagnosticPanelSetIdentifier());
            newPanel.setPreviousVersion(diagnosticPanel.getGuid());
            newPanel.setStartVersion(diagnosticPanelSet.getCurrentVersion());

            diagnosticPanelRepository.save(newPanel);
        });
    }


    private void deletePanel(DiagnosticPanel diagnosticPanel, Integer endVersion) {
        diagnosticPanel.setDeletionDate(new Date());
        diagnosticPanel.setStatus(EStatus.ARCHIVED);
        diagnosticPanel.setEndVersion(endVersion);

        diagnosticPanelRepository.save(diagnosticPanel);
    }

    private void deleteAndCreateNewVersionDiagnosticPanel (String parentPanelId, DiagnosticPanel diagnosticPanel) {
        DiagnosticPanelSet diagnosticPanelSet = diagnosticPanelSetRepository.findByDiagnosticPanelSetIdentifier(diagnosticPanel.getDiagnosticPanelSetIdentifier());
        deletePanel(diagnosticPanel, diagnosticPanelSet.getCurrentVersion());

        DiagnosticPanel newPanel = new DiagnosticPanel();

        newPanel.setDiagnosticPanelIdentifier(diagnosticPanel.getDiagnosticPanelIdentifier());
        newPanel.setName(diagnosticPanel.getName());
        newPanel.setAuthor(diagnosticPanel.getAuthor());
        newPanel.setDescription(diagnosticPanel.getDescription());
        newPanel.setFeatures(diagnosticPanel.getFeatures());
        newPanel.setAssociations(diagnosticPanel.getAssociations());

        List<String> auxList = diagnosticPanel.getParentIds();
        auxList.remove(parentPanelId);

        newPanel.setParentIds(auxList);
        newPanel.setStatus(EStatus.CURRENT);
        newPanel.setCreationDate(new Date());
        newPanel.setDiagnosticPanelSetIdentifier(diagnosticPanel.getDiagnosticPanelSetIdentifier());
        newPanel.setPreviousVersion(diagnosticPanel.getGuid());
        newPanel.setStartVersion(diagnosticPanelSet.getCurrentVersion());

        diagnosticPanelRepository.save(newPanel);
    }

    private static int customComparator(String s1, String s2) {

        String[] pt1 = s1.split("((?<=[a-z])(?=[0-9]))|((?<=[0-9])(?=[a-z]))");
        String[] pt2 = s2.split("((?<=[a-z])(?=[0-9]))|((?<=[0-9])(?=[a-z]))");

        int i = 0;
        if(Arrays.equals(pt1, pt2)) {
            return 0;
        }else{
            for(i=0;i<Math.min(pt1.length, pt2.length);i++)
                if(!pt1[i].equals(pt2[i])) {
                    if(!isNumber(pt1[i],pt2[i])) {
                        if(pt1[i].compareTo(pt2[i])>0) {
                            return 1;
                        }else{
                            return -1;
                        }
                    } else {
                        int nu1 = Integer.parseInt(pt1[i]);
                        int nu2 = Integer.parseInt(pt2[i]);
                        if(nu1>nu2) {
                            return 1;
                        } else {
                            return -1;
                        }
                    }
                }
        }

        if(pt1.length>i) {
            return 1;
        }else{
            return -1;
        }
    }

    private static Boolean isNumber(String n1, String n2) {
        try {
            Integer.parseInt(n1);
            Integer.parseInt(n2);
            return true;
        }
        catch(Exception x) {
            return false;
        }
    }

    private String getFormattedAuthor (String username){
        String formattedAuthor = "";
        if(username != null){
            User user = userRepository.findByIdentifier(username);

            if(user == null) {
                UserHistory userHistory = userHistoryRepository.findByIdentifier(username);
                formattedAuthor = userHistory != null ? userHistory.getIdentifier() + " - " + userHistory.getFirstName() + " " + userHistory.getLastName() : "Undefined";
            }else{
                formattedAuthor =  user.getIdentifier() + " - " + user.getFirstName() + " " + user.getLastName();
            }
        }
        return formattedAuthor;
    }

    private List<DiagnosticPanel> getDescendantsListToVersion (String guid){
        DiagnosticPanel diagnosticPanel = diagnosticPanelRepository.findByGuid(guid);

        DiagnosticPanelSet diagnosticPanelSet =
                diagnosticPanelSetRepository.findByDiagnosticPanelSetIdentifier(diagnosticPanel.getDiagnosticPanelSetIdentifier());

        List<DiagnosticPanel> descendants =
                diagnosticPanelRepository.findDiagnosticPanelByParentIdsAndDiagnosticPanelSetIdentifier(diagnosticPanel.getDiagnosticPanelIdentifier(),diagnosticPanel.getDiagnosticPanelSetIdentifier());

        List<DiagnosticPanel> descendantSearchVersionPanel = new ArrayList<>();

        if(diagnosticPanel.getEndVersion() == null)
        {
            int searchVersion = diagnosticPanelSet.getCurrentVersion();

            for (DiagnosticPanel diagnosticPanelDescendant : descendants) {
                if (diagnosticPanelDescendant.getEndVersion() != null && diagnosticPanelDescendant.getStartVersion() <= searchVersion && diagnosticPanelDescendant.getEndVersion() > searchVersion)
                {
                    descendantSearchVersionPanel.add(diagnosticPanelDescendant);
                }
                else if (diagnosticPanelDescendant.getEndVersion() == null && diagnosticPanelDescendant.getStartVersion() <= searchVersion) {
                    descendantSearchVersionPanel.add(diagnosticPanelDescendant);
                }
            }
        }
        else {

            int endVersion =  diagnosticPanel.getEndVersion();
            int searchVersion = diagnosticPanel.getStartVersion();

            for (DiagnosticPanel diagnosticPanelDescendant : descendants) {
                if (diagnosticPanelDescendant.getEndVersion() != null) {
                    if (diagnosticPanelDescendant.getStartVersion() >= searchVersion && diagnosticPanelDescendant.getStartVersion() < endVersion)
                    {
                        descendantSearchVersionPanel.add(diagnosticPanelDescendant);
                    } else if (diagnosticPanelDescendant.getStartVersion() < searchVersion && diagnosticPanelDescendant.getEndVersion() > searchVersion && diagnosticPanelDescendant.getEndVersion() <= endVersion) {
                        descendantSearchVersionPanel.add(diagnosticPanelDescendant);
                    }
                } else if (diagnosticPanelDescendant.getStartVersion() <= endVersion) {
                    descendantSearchVersionPanel.add(diagnosticPanelDescendant);
                }
            }
        }

        List<String> panelsId = descendantSearchVersionPanel.stream().map(DiagnosticPanel::getDiagnosticPanelIdentifier).distinct().collect(Collectors.toList());

        return  diagnosticPanelRepository.findDiagnosticPanelByDiagnosticPanelSetIdentifierAndStatusAndDiagnosticPanelIdentifierIn(diagnosticPanel.getDiagnosticPanelSetIdentifier(), EStatus.CURRENT, panelsId);
    }
}
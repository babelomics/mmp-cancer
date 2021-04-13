package com.fujitsu.mmp.msusermanagement.controllers;

import com.fujitsu.mmp.msusermanagement.apis.genomicdictionaryapi.*;
import com.fujitsu.mmp.msusermanagement.services.GenomicDictionaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600, exposedHeaders = "Authorization")
@RequestMapping("/api/genomicdictionary")
@RestController
public class GenomicDictionaryController {
    @Autowired
    GenomicDictionaryService genomicDictionaryService;

    /**
     * Check the status of the genetic dictionary service.
     * @param genomicDictionaryDTO: URL of the genetic dictionary service.
     * @return
     */
    @PostMapping("/validate")
    public ResponseEntity<?> validate(@RequestBody GenomicDictionaryDTO genomicDictionaryDTO) throws IOException {
        return genomicDictionaryService.validate(genomicDictionaryDTO);
    }

    /**
     *
     * @return
     */
    @GetMapping("/species")
    public List<Species> getSpecies(@RequestParam Optional<Integer> pageSize,@RequestParam Optional<Integer> page) {
        return genomicDictionaryService.getSpecies(pageSize, page);
    }

    /**
     *
     * @return
     */
    @GetMapping("/assembly")
    public List<Assembly> getAssembly(@RequestParam Optional<String> searchText, @RequestParam Optional<String> taxonomyId) {
        return genomicDictionaryService.getAssembly(searchText, taxonomyId);
    }

    /**
     *
     * @return
     */
    @GetMapping("/ensembl/{assemblyAccession}")
    public String getEnsemblRelease(@PathVariable String assemblyAccession) {
        return genomicDictionaryService.getEnsemlRelease(assemblyAccession);
    }

    /**
     *
     * @return
     */
    @GetMapping("/ensembl/{assemblyAccession}/meta")
    public MetaAssembly getEnsemblReleaseMeta(@PathVariable String assemblyAccession) {
        return genomicDictionaryService.getMetaAssembly(assemblyAccession);
    }

    /**
     *
     * @return
     */
    @GetMapping("/icd10/{termId}")
    public ICD10 getICD10(@PathVariable String termId) {
        return genomicDictionaryService.getICD10(termId);
    }

    /**
     *
     * @return
     */
    @GetMapping("/icd10/list")
    public List<ICD10> getICD10List (@RequestParam Optional<String> searchText,
                                     @RequestParam Optional<Integer> pageSize,@RequestParam Optional<Integer> page) {
        return genomicDictionaryService.getICD10List(searchText, pageSize, page);
    }

    /**
     *
     * @return
     */
    @GetMapping("/hpos/{hpoId}")
    public HPO getHpo(@PathVariable String hpoId) {
        return genomicDictionaryService.getHpo(hpoId);
    }

    /**
     *
     * @return
     */
    @GetMapping("/hpos/list")
    public List<HPO> getHpoList (@RequestParam Optional<String> searchText,
                                 @RequestParam Optional<Integer> pageSize,@RequestParam Optional<Integer> page) {
        return genomicDictionaryService.getHpoList(searchText, pageSize, page);
    }

    /**
     *
     * @return
     */
    @GetMapping("/hpos/phenotypicAbnormality/list")
    public List<HPO> getHpoPhenotypicAbnormalityList () {
        return genomicDictionaryService.getHpoPhenotypicAbnormalityList();
    }

    /**
     *
     * @return
     */
    @GetMapping("/assemblies/{assemblyAccession}/genes/{geneId}")
    public Gene getGene (@PathVariable String assemblyAccession, @PathVariable String geneId,
                         @RequestParam Optional<String> schemaVersion) {
        return genomicDictionaryService.getGene(assemblyAccession, geneId, schemaVersion);
    }

    /**
     *
     * @return
     */
    @GetMapping("/assemblies/{assemblyAccession}/genes")
    public List<Gene> getGeneList (@PathVariable String assemblyAccession, @RequestParam Optional<String> searchText,
                                   @RequestParam Optional<String> schemaVersion,
                                   @RequestParam Optional<Integer> pageSize,@RequestParam Optional<Integer> page) {
        return genomicDictionaryService.getGeneList(assemblyAccession, searchText, schemaVersion, pageSize, page);
    }

    /**
     *
     * @return
     */
    @GetMapping("/assemblies/{assemblyAccession}/transcripts/{transcriptId}")
    public Transcript getTranscript (@PathVariable String assemblyAccession, @PathVariable String transcriptId,
                                     @RequestParam Optional<String> schemaVersion) {
        return genomicDictionaryService.getTranscript(assemblyAccession, transcriptId, schemaVersion);
    }

    /**
     *
     * @return
     */
    @GetMapping("/assemblies/{assemblyAccession}/transcripts")
    public List<Transcript> getTranscript (@PathVariable String assemblyAccession, @RequestParam Optional<String> searchText,
                                           @RequestParam Optional<String> schemaVersion,
                                           @RequestParam Optional<Boolean> canonical,
                                           @RequestParam Optional<Integer> pageSize,@RequestParam Optional<Integer> page) {
        return genomicDictionaryService.getTranscriptList(assemblyAccession, searchText, schemaVersion, canonical,pageSize, page);
    }

    /**
     *
     * @return
     */
    @GetMapping("/assemblies/{assemblyAccession}/regions/{regions}/{start}/{end}/check")
    public ResponseEntity<String> checkRegion (@PathVariable String assemblyAccession, @PathVariable String regions,
                                             @PathVariable String start, @PathVariable String end,
                                             @RequestParam Optional<String> schemaVersion) {
        return genomicDictionaryService.checkRegion(assemblyAccession, regions, start, end, schemaVersion);
    }
}

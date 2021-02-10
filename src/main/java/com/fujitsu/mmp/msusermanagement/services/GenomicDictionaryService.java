package com.fujitsu.mmp.msusermanagement.services;

import com.fujitsu.mmp.msusermanagement.apis.genomicdictionaryapi.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Optional;


@Service
public class GenomicDictionaryService {

    private final WebClient webClient;

    @Autowired
    public GenomicDictionaryService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://iva-dev.clinbioinfosspa.es:9090/api/v1").build();
    }

    //TODO-> MODIFY THIS CODE!!
    public ResponseEntity<?> validate(GenomicDictionaryDTO genomicDictionaryDTO) throws IOException {
        HttpStatus responseStatus = HttpStatus.OK;
        GenomicDictionaryDTO responseBody = genomicDictionaryDTO;
        if (genomicDictionaryDTO.getUrl() == null) {
            responseStatus = HttpStatus.NOT_FOUND;
        } else {
            String urlString = genomicDictionaryDTO.getUrl()+"/meta";
            URL u = new URL(urlString);
            HttpURLConnection huc =  (HttpURLConnection)  u.openConnection();
            huc.setRequestMethod("GET");
            huc.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 6.0; en-US; rv:1.9.1.2) Gecko/20090729 Firefox/3.5.2 (.NET CLR 3.5.30729)");
            huc.connect();
            HttpStatus responseCode = HttpStatus.valueOf(huc.getResponseCode());
            if (responseCode != responseStatus) {
                return new ResponseEntity<>(huc.getResponseMessage(), HttpStatus.valueOf(huc.getResponseCode()));
            }
        }
        return new ResponseEntity<>(responseBody, responseStatus);
    }

    //TODO-> MODIFY THIS CODE!!
    public Boolean isValid(String genomicDictionaryURL) throws IOException {
        HttpStatus responseStatus = HttpStatus.OK;
            String urlString = genomicDictionaryURL+"/meta";
            URL u = new URL(urlString);
            HttpURLConnection huc =  (HttpURLConnection)  u.openConnection();
            huc.setRequestMethod("GET");
            huc.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 6.0; en-US; rv:1.9.1.2) Gecko/20090729 Firefox/3.5.2 (.NET CLR 3.5.30729)");
            huc.connect();
            HttpStatus responseCode = HttpStatus.valueOf(huc.getResponseCode());
            if (responseCode != responseStatus) {
                return false;
            }
        return true;
    }

    public List<Species> getSpecies(Optional<Integer> pageSize, Optional<Integer> page) {
        MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        pageSize.ifPresent(s -> queryParams.add("pageSize", s.toString()));
        page.ifPresent(s -> queryParams.add("page", s.toString()));

        return this.webClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/species/")
                        .queryParams(queryParams)
                        .build())
                .retrieve()
                .bodyToFlux(Species.class)
                .collectList()
                .block();
    }

    public List<Assembly> getAssembly(Optional<String> searchText, Optional<String> taxonomyId) {

        MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        searchText.ifPresent(s -> queryParams.add("searchText", s));
        taxonomyId.ifPresent(s -> queryParams.add("taxonomyId", s));

        return this.webClient.get()
            .uri(uriBuilder -> uriBuilder
                    .path("/assemblies/")
                    .queryParams(queryParams)
                    .build())
            .retrieve()
            .bodyToFlux(Assembly.class)
            .collectList()
            .block();
    }

    public String getEnsemlRelease(String assemblyAccession) {
        return this.webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/assemblies/{assemblyAccession}/schemaVersions")
                        .build(assemblyAccession))
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    public MetaAssembly getEnsemlReleaseMeta(String assemblyAccession) {
        return this.webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/assemblies/{assemblyAccession}/meta")
                        .build(assemblyAccession))
                .retrieve()
                .bodyToMono(MetaAssembly.class)
                .block();
    }

    public ICD10 getICD10(String termId) {
        return this.webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/icd10s/{termId}")
                        .build(termId))
                .retrieve()
                .bodyToMono(ICD10.class)
                .block();
    }

    public List<ICD10> getICD10List(Optional<String> searchText, Optional<Integer> pageSize, Optional<Integer> page) {

        MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        searchText.ifPresent(s -> queryParams.add("searchText", s));
        pageSize.ifPresent(s -> queryParams.add("pageSize", s.toString()));
        page.ifPresent(s -> queryParams.add("page", s.toString()));

        return this.webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/icd10s/")
                        .queryParams(queryParams)
                        .build())
                .retrieve()
                .bodyToFlux(ICD10.class)
                .collectList()
                .block();
    }

    public HPO getHpo(String hpoId) {
        return this.webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/hpos/{hpoId}")
                        .build(hpoId))
                .retrieve()
                .bodyToMono(HPO.class)
                .block();
    }

    public List<HPO> getHpoList(Optional<String> searchText
                                ,Optional<Integer> pageSize, Optional<Integer> page) {

        MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        searchText.ifPresent(s -> queryParams.add("searchText", s));
        pageSize.ifPresent(s -> queryParams.add("pageSize", s.toString()));
        page.ifPresent(s -> queryParams.add("page", s.toString()));

        return this.webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/hpos/")
                        .queryParams(queryParams)
                        .build())
                .retrieve()
                .bodyToFlux(HPO.class)
                .collectList()
                .block();
    }

    public Gene getGene(String assemblyAccession, String geneId, Optional<String> schemaVersion) {

        MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        schemaVersion.ifPresent(s -> queryParams.add("schemaVersion",s));

        return this.webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/assemblies/{assemblyAccession}/genes/{genId}")
                        .queryParams(queryParams)
                        .build(assemblyAccession, geneId))
                .retrieve()
                .bodyToMono(Gene.class)
                .block();
    }

    public List<Gene> getGeneList(String assemblyAccession, Optional<String> searchText,
                                  Optional<String> schemaVersion,
                                  Optional<Integer> pageSize, Optional<Integer> page) {

        MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        searchText.ifPresent(s -> queryParams.add("searchText", s));
        schemaVersion.ifPresent(s -> queryParams.add("schemaVersion",s));
        pageSize.ifPresent(s -> queryParams.add("pageSize", s.toString()));
        page.ifPresent(s -> queryParams.add("page", s.toString()));

        return this.webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/assemblies/{assemblyAccession}/genes/")
                        .queryParams(queryParams)
                        .build(assemblyAccession))
                .retrieve()
                .bodyToFlux(Gene.class)
                .collectList()
                .block();
    }

    public Transcript getTranscript(String assemblyAccession, String transcriptId, Optional<String> schemaVersion) {

        MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        schemaVersion.ifPresent(s -> queryParams.add("schemaVersion",s));

        return this.webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/assemblies/{assemblyAccession}/transcripts/{transcriptId}")
                        .queryParams(queryParams)
                        .build(assemblyAccession, transcriptId))
                .retrieve()
                .bodyToMono(Transcript.class)
                .block();
    }

    public List<Transcript> getTranscriptList(String assemblyAccession, Optional<String> searchText,
                                              Optional<String> schemaVersion, Optional<Boolean> canonical,
                                              Optional<Integer> pageSize, Optional<Integer> page) {

        MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        searchText.ifPresent(s -> queryParams.add("searchText", s));
        schemaVersion.ifPresent(s -> queryParams.add("schemaVersion",s));
        canonical.ifPresent(c -> queryParams.add("canonical", c.toString()));
        pageSize.ifPresent(s -> queryParams.add("pageSize", s.toString()));
        page.ifPresent(s -> queryParams.add("page", s.toString()));

        return this.webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/assemblies/{assemblyAccession}/transcripts/")
                        .queryParams(queryParams)
                        .build(assemblyAccession))
                .retrieve()
                .bodyToFlux(Transcript.class)
                .collectList()
                .block();
    }

    public String checkRegion(String assemblyAccession, String regions, String start,
                                            String end, Optional<String> schemaVersion) {

        MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        schemaVersion.ifPresent(s -> queryParams.add("schemaVersion",s));

        return this.webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/assemblies/{assemblyAccession}/regions/{seqRegionId}/{start}/{end}")
                        .queryParams(queryParams)
                        .build(assemblyAccession, regions, start, (Integer.parseInt(end)+1)))
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
}

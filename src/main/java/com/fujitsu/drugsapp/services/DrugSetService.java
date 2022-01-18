package com.fujitsu.drugsapp.services;

import com.fujitsu.drugsapp.entities.*;
import com.fujitsu.drugsapp.repositories.DrugRepository;
import com.fujitsu.drugsapp.repositories.DrugSetRepository;
import com.zaxxer.hikari.HikariDataSource;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import javax.persistence.Table;
import java.sql.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

@AllArgsConstructor
@Service
public class DrugSetService {

    private final DrugSetRepository drugSetRepository;
    private final DrugRepository drugRepository;
    private final DrugService drugService;
    private final DrugUpdateService drugUpdateService;
    private final DrugSourceService drugSourceService;

    @Autowired
    HikariDataSource hikariDataSource;

    private final int batchSize = 100;

    private PlatformTransactionManager transactionManager;


    public List<DrugSet> findAll(String searchText){

        List<DrugSet> drugSetList = drugSetRepository.getDrugSetWithDrugs();

        if(searchText==null) {
            return drugSetList;
        }else{

            List<DrugSet> matchedDrugSets = new ArrayList<>();

            for (DrugSet drugSet : drugSetList) {
                if (drugSet.getName().toLowerCase().contains(searchText.toLowerCase())
                        || drugSet.getDescription().toLowerCase().contains(searchText.toLowerCase()))
                    matchedDrugSets.add(drugSet);
            }
            return matchedDrugSets;
        }
    }

    public DrugSet findById(UUID uuid) {
        return drugSetRepository.findById(uuid).orElseThrow(NoSuchElementException::new);
    }

    public List<Drug> findDrugsById(UUID uuid, String searchText, Instant date) {
        DrugSet drugSet = drugSetRepository.findById(uuid).orElseThrow(NoSuchElementException::new);
        List<Drug> matchedDrugs = new ArrayList<>();
        List<Drug> drugs = drugRepository.findAll();

        if(searchText==null && date==null) {
            matchedDrugs = drugSet.getDrugs();
        }else if(searchText!=null && date==null){

            for (Drug drug : drugs) {
                if (drug.getStandardName().toLowerCase().contains(searchText.toLowerCase())
                        || drug.getCommonName().toLowerCase().contains(searchText.toLowerCase()))
                    matchedDrugs.add(drug);
            }
        }else if(searchText==null && date!=null){
            LocalDateTime localDateTime = LocalDateTime.ofInstant(date, ZoneOffset.UTC);
            for (Drug drug : drugs) {
                if (drugSet.getUpdatedAt().isEqual(localDateTime))
                    matchedDrugs.add(drug);
            }
        }else {
            LocalDateTime localDateTime = LocalDateTime.ofInstant(date, ZoneOffset.UTC);
            List<Drug> matchedDrugsAux = new ArrayList<>();

            for (Drug drug : drugs) {
                if (drugSet.getUpdatedAt().isEqual(localDateTime))
                    matchedDrugsAux.add(drug);
            }

            for(int i=0; i<matchedDrugsAux.size(); ++i){
                if(matchedDrugsAux.get(i).getStandardName().toLowerCase().contains(searchText.toLowerCase())
                        || matchedDrugsAux.get(i).getCommonName().toLowerCase().contains(searchText.toLowerCase()))
                    matchedDrugs.add(drugs.get(i));
            }
        }

        return matchedDrugs;
    }

    public DrugSet findByName(String name){

        List<DrugSet> drugSetList = drugSetRepository.findAll();

        for(DrugSet drugSet : drugSetList){
            if(name.equals(drugSet.getName())){
                return drugSet;
            }
        }

        return null;
    }

    public DrugSet saveDrugSet(DrugSet drugSet){
        List<Drug> newDrugs = drugSet.getDrugs();

        DrugUpdate drugUpdate = registerUpdate(drugSet.getId());

        registerDrugSet(drugSet, newDrugs, drugUpdate);

        return drugSet;
    }

    public void deleteDrugSet(UUID uuid){
        drugSetRepository.deleteById(uuid);
    }

    public DrugUpdate registerUpdate(UUID drugSetId){
        DrugUpdate drugUpdate = new DrugUpdate();
        drugUpdate.setDrugSetId(drugSetId);

        return drugUpdate;
    }

    public void registerDrugSet(DrugSet drugSet, List<Drug> newDrugs, DrugUpdate drugUpdate){

        boolean exists = false;
        List<DrugSource> drugSourceList = drugSourceService.findAll();
        List<DrugSource> drugSourcesToSave = drugSourceService.findAll();

        List<Drug> drugList = drugService.findAll();
        List<DrugName> drugNameList = new ArrayList<>();

        for(int i = 0; i<newDrugs.size(); ++i) {

                exists = drugService.existByStandardName(drugList, newDrugs.get(i));

                if (!exists) {

                    for (DrugName drugName : newDrugs.get(i).getDrugNames()) {
                        drugName.setDrug(newDrugs.get(i));

                        if(!drugSourceService.existByShortName(drugSourceList,drugName.getDrugSource())) {
                            drugSourcesToSave.add(drugName.getDrugSource());
                            drugSourceList.add(drugName.getDrugSource());
                        }else{
                            DrugSource drugSource = drugSourceService.findByShortName(drugSourceList, drugName.getDrugSource().getShortName());
                            drugName.setDrugSource(drugSource);
                        }

                        drugNameList.add(drugName);
                    }
                    newDrugs.get(i).setStartUpdate(drugUpdate.getId());
                    newDrugs.get(i).setDrugSet(drugSet);

                    drugList.add(newDrugs.get(i));
            }
        }

        transactionalDrugSetSave(drugUpdate, drugSet, newDrugs, drugSourcesToSave, drugNameList);
    }

    public void transactionalDrugSetSave(DrugUpdate drugUpdate, DrugSet drugSet, List<Drug> drugData, List<DrugSource> drugSourceList, List<DrugName> drugNameList){

        Connection connection = null;
        PreparedStatement statement = null;

        try{

            connection = hikariDataSource.getConnection();
            connection.setAutoCommit(false);

            String sqlDrugUpdate = String.format(
                    "INSERT INTO drug_update (id, created_at, description, drug_set_id, next_update_id, previous_update_id, user_id) " +
                            "VALUES (?::UUID, ?, ?, ?::UUID,?::UUID, ?::UUID, ?::UUID)",
                    DrugUpdate.class.getAnnotation(Table.class).name()
            );

            statement = connection.prepareStatement(sqlDrugUpdate);

            saveDrugUpdateByJdbc(drugUpdate, statement);

            String sqlDrugSet = String.format(
                    "INSERT INTO drug_set (id, created_at, description, name, updated_at) " +
                            "VALUES (?::UUID, ?, ?, ?, ?)",
                    DrugSet.class.getAnnotation(Table.class).name()
            );

            statement = connection.prepareStatement(sqlDrugSet);

            saveDrugSetByJdbc(drugSet, statement);

            String sqlDrugs = String.format(
                    "INSERT INTO drug (id, common_name, end_update, next_version, previous_version, standard_name, start_update, drugset_id) " +
                            "VALUES (?::UUID, ?, ?::UUID, ?::UUID, ?::UUID, ?, ?::UUID, ?::UUID)",
                    Drug.class.getAnnotation(Table.class).name()
            );

            statement = connection.prepareStatement(sqlDrugs);

            saveDrugsByJdbc(drugData, statement);

            String sqlDrugSources = String.format(
                    "INSERT INTO drug_source (id, short_name, url) " +
                            "VALUES (?::UUID, ?, ?)",
                    DrugSource.class.getAnnotation(Table.class).name()
            );

            statement = connection.prepareStatement(sqlDrugSources);

            saveDrugSourcesByJdbc(drugSourceList, statement);

            String sqlDrugNames = String.format(
                    "INSERT INTO drug_name (id, name, drug_id, drug_source_id) " +
                            "VALUES (?::UUID, ?, ?::UUID, ?::UUID)",
                    DrugName.class.getAnnotation(Table.class).name()
            );

            statement = connection.prepareStatement(sqlDrugNames);
            saveDrugNamesByJdbc(drugNameList, statement);

            connection.commit();

        } catch(SQLException sqlex){
            try {
                connection.rollback();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }finally {
            try {
                if(null != connection) {
                    statement.close();
                    connection.close();
                }
            }
            catch (SQLException sqlex) {
                sqlex.printStackTrace();
            }
        }
    }

    public void saveDrugUpdateByJdbc(DrugUpdate drugUpdateData, PreparedStatement statement){

        try {
            statement.clearParameters();
            statement.setString(1, drugUpdateData.getId().toString());
            statement.setTimestamp(2, Timestamp.valueOf(drugUpdateData.getCreatedAt()));

            if(drugUpdateData.getDescription() == null){
                statement.setString(3, drugUpdateData.getDescription());
            }else {
                statement.setNull(3, Types.NULL);
            }

            statement.setString(4, drugUpdateData.getDrugSetId().toString());

            if(drugUpdateData.getNextUpdateId() != null){
                statement.setString(5, drugUpdateData.getNextUpdateId().toString());
            }else {
                statement.setNull(5, Types.NULL);
            }

            if(drugUpdateData.getPreviousUpdateId() != null){
                statement.setString(6, drugUpdateData.getPreviousUpdateId().toString());
            }else {
                statement.setNull(6, Types.NULL);
            }

            if(drugUpdateData.getUserId() != null){
                statement.setString(7, drugUpdateData.getUserId().toString());
            }else {
                statement.setNull(7, Types.NULL);
            }
            statement.executeUpdate();

        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void saveDrugSetByJdbc(DrugSet drugSetData, PreparedStatement statement){

        try {
                statement.clearParameters();
                statement.setString(1, drugSetData.getId().toString());
                statement.setTimestamp(2, Timestamp.valueOf(drugSetData.getCreatedAt()));
                statement.setString(3, drugSetData.getDescription());
                statement.setString(4, drugSetData.getName());
                if(drugSetData.getUpdatedAt() != null){
                    statement.setTimestamp(5, Timestamp.valueOf(drugSetData.getUpdatedAt()));
                }else {
                    statement.setNull(5, Types.NULL);
                }
                statement.executeUpdate();

        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void saveDrugsByJdbc(List<Drug> drugData, PreparedStatement statement){
        int counter = 0;

        try {
            for (Drug drug : drugData) {
                statement.clearParameters();
                statement.setString(1, drug.getId().toString());
                statement.setString(2, drug.getCommonName());

                if (drug.getEndUpdate() != null) {
                    statement.setString(3, drug.getEndUpdate().toString());
                } else {
                    statement.setNull(3, Types.NULL);
                }

                if (drug.getNextVersion() != null) {
                    statement.setString(4, drug.getNextVersion().toString());
                } else {
                    statement.setNull(4, Types.NULL);
                }

                if (drug.getNextVersion() != null) {
                    statement.setString(5, drug.getPreviousVersion().toString());
                } else {
                    statement.setNull(5, Types.NULL);
                }

                statement.setString(6, drug.getStandardName());

                if (drug.getStartUpdate() != null) {
                    statement.setString(7, drug.getStartUpdate().toString());
                } else {
                    statement.setNull(7, Types.NULL);
                }

                statement.setString(8, drug.getDrugSet().getId().toString());
                statement.addBatch();
                if ((counter + 1) % batchSize == 0 || (counter + 1) == drugData.size()) {
                    statement.executeBatch();
                    statement.clearBatch();
                }
                counter++;
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void saveDrugNamesByJdbc(List<DrugName> drugNameData, PreparedStatement statement){

        int counter = 0;

        try{

            for (DrugName drugName : drugNameData) {
                statement.clearParameters();
                statement.setString(1, drugName.getId().toString());
                statement.setString(2, drugName.getName());
                statement.setString(3, drugName.getDrug().getId().toString());
                statement.setString(4, drugName.getDrugSource().getId().toString());


                statement.addBatch();
                if ((counter + 1) % batchSize == 0 || (counter + 1) == drugNameData.size()) {
                    statement.executeBatch();
                    statement.clearBatch();
                }
                counter++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void saveDrugSourcesByJdbc(List<DrugSource> drugSourceData, PreparedStatement statement){

        int counter = 0;

        try{
            for (DrugSource drugSource : drugSourceData) {
                statement.clearParameters();
                statement.setString(1, drugSource.getId().toString());
                statement.setString(2, drugSource.getShortName());

                if(drugSource.getUrl() != null) {
                    statement.setURL(3, drugSource.getUrl());
                }else{
                    statement.setNull(3, Types.NULL);
                }
                statement.addBatch();

                if ((counter + 1) % batchSize == 0 || (counter + 1) == drugSourceData.size()) {
                    statement.executeBatch();
                    statement.clearBatch();
                }
                counter++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public List<DrugUpdate> getDrugSetUpdates(UUID drugSetId){
        return drugUpdateService.findByDrugSetId(drugSetId);
    }

    public DrugSet updateDrugSet(DrugSet drugSet){

        List<Drug> newDrugs = drugSet.getDrugs();
        drugSet.setDrugs(null);

        DrugUpdate drugUpdate = new DrugUpdate();
        drugUpdate.setDrugSetId(drugSet.getId());
        List<DrugUpdate> drugUpdateList = drugUpdateService.findAll();
        drugUpdateList.get(drugUpdateList.size()-1).setNextUpdateId(drugUpdate.getId());
        drugUpdate.setPreviousUpdateId(drugUpdateList.get(drugUpdateList.size()-1).getId());
        drugUpdateService.saveDrugUpdate(drugUpdate);
        drugUpdateService.updateDrugUpdate(drugUpdateList.get(drugUpdateList.size()-1));
        drugSet.setUpdatedAt(drugUpdate.getCreatedAt());

        drugSetRepository.save(drugSet);

        registerDrugSet(drugSet, newDrugs, drugUpdate);

        return drugSet;
    }

    public boolean existById(UUID uuid){
        return drugSetRepository.existsById(uuid);
    }

    public boolean existByName(DrugSet drugSet){
        List<DrugSet> findDrugSet = drugSetRepository.findAll();

        for (DrugSet set : findDrugSet) {
            if (drugSet.getName().toLowerCase().equals(set.getName().toLowerCase()))
                return true;
        }

        return false;
    }

}

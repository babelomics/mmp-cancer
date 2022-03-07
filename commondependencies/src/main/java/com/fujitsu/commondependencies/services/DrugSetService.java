package com.fujitsu.commondependencies.services;

import com.fujitsu.commondependencies.entities.*;
import com.fujitsu.commondependencies.repositories.DrugRepository;
import com.fujitsu.commondependencies.repositories.DrugSetRepository;
import com.zaxxer.hikari.HikariDataSource;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;


import javax.persistence.Table;
import java.net.URL;
import java.sql.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

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

    @Autowired
    public JdbcTemplate jdbcTemplate;

    private final int batchSize = 100;

    public List<DrugSet> findAll(String searchText){

        List<DrugSet> drugSetList = findAllByQuery();

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

        if(searchText==null && date==null) {
            matchedDrugs = drugSet.getDrugs();
        }else{

            String sql;

            if(searchText!=null && date==null){

                sql = "SELECT * FROM drug d " +
                        "JOIN drug_name dn ON dn.drug_id=d.id " +
                        "JOIN drug_source ds ON dn.drug_source_id=ds.id " +
                        "WHERE standard_name ~* '"+searchText.replaceAll("'","''")+"'"; //Replace ' with '' to avoid syntax errorx

            }else if(searchText==null && date!=null){

                LocalDateTime localDateTime = LocalDateTime.ofInstant(date, ZoneOffset.UTC);

                sql = "SELECT * FROM drug d " +
                        "JOIN drug_name dn ON dn.drug_id=d.id " +
                        "JOIN drug_source ds ON dn.drug_source_id=ds.id " +
                        "WHERE d.start_update IN (SELECT id from drug_update " +
                        "du where du.created_at >= '"+ localDateTime + "')";

            }else {
                LocalDateTime localDateTime = LocalDateTime.ofInstant(date, ZoneOffset.UTC);

                sql = "SELECT * FROM drug d " +
                        "JOIN drug_name dn ON dn.drug_id=d.id " +
                        "JOIN drug_source ds ON dn.drug_source_id=ds.id " +
                        "WHERE d.standard_name ~* '" +
                        searchText + "' AND d.start_update IN (SELECT id from " +
                        "drug_update du where du.created_at >= '"+ localDateTime + "')";

            }

            Connection connection = null;
            Statement statement;
            ResultSet rs;

            try {
                connection = hikariDataSource.getConnection();
                statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                rs = statement.executeQuery(sql);

                while(rs.next())
                {
                    Drug drug = new Drug();
                    DrugName drugName = new DrugName();
                    DrugSource drugSource = new DrugSource();
                    List<DrugName> drugNameList = new ArrayList<>();
                    drug.setId(rs.getObject(1,UUID.class));
                    drug.setCommonName(rs.getObject("common_name", String.class));
                    drug.setStandardName(rs.getObject("standard_name", String.class));
                    drug.setEndUpdate(rs.getObject("end_update",UUID.class));
                    drug.setStartUpdate(rs.getObject("start_update",UUID.class));
                    drug.setNextVersion(rs.getObject("next_version",UUID.class));
                    drug.setPreviousVersion(rs.getObject("previous_version",UUID.class));

                    drugName.setId(rs.getObject(9, UUID.class));
                    drugName.setName(rs.getObject("name",String.class));
                    drugName.setDrug(drug);

                    drugSource.setId(rs.getObject(13, UUID.class));
                    drugSource.setShortName(rs.getObject("short_name", String.class));

                    if(rs.getString("url") != null) {
                        drugSource.setUrl(rs.getObject("url", URL.class));
                    }
                    drugName.setDrugSource(drugSource);

                    drugNameList.add(drugName);

                    drug.setDrugNames(drugNameList);

                    matchedDrugs.add(drug);
                }

            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                try {
                    assert connection != null;
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        return matchedDrugs;
    }

    public DrugSet findByName(String name){

        DrugSet matchedDrugset = new DrugSet();

        String sql = "SELECT * FROM drug_set " +
                "WHERE name='"+ name.replaceAll("'","''")+"'"; //Replace ' with '' to avoid syntax errorx
        Connection connection = null;
        Statement statement;
        ResultSet rs;

        try {
            connection = hikariDataSource.getConnection();
            statement = connection.createStatement();
            rs = statement.executeQuery(sql);

            while(rs.next()) {
                matchedDrugset.setId(rs.getObject("id", UUID.class));
                matchedDrugset.setName(rs.getObject("name", String.class));
                matchedDrugset.setCreatedAt(rs.getObject("created_at", LocalDateTime.class));
                matchedDrugset.setDescription(rs.getObject("description", String.class));
                matchedDrugset.setUpdatedAt(rs.getObject("updated_at", LocalDateTime.class));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                assert connection != null;
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return matchedDrugset;
    }

    public void saveDrugSet(DrugSet drugSet){
        List<Drug> newDrugs = drugSet.getDrugs();

        DrugUpdate drugUpdate = registerUpdate(drugSet.getId());

        registerDrugSet(drugSet, newDrugs, drugUpdate);
    }

    public DrugUpdate registerUpdate(UUID drugSetId){
        DrugUpdate drugUpdate = new DrugUpdate();
        drugUpdate.setDrugSetId(drugSetId);

        return drugUpdate;
    }

    public void registerDrugSet(DrugSet drugSet, List<Drug> newDrugs, DrugUpdate drugUpdate){

        boolean exists;
        List<DrugSource> drugSourceList = drugSourceService.findAll();
        List<DrugSource> drugSourcesToSave = drugSourceService.findAll();

        List<Drug> drugList = drugService.findAll();
        List<DrugName> drugNameList = new ArrayList<>();

        for (Drug newDrug : newDrugs) {

            exists = drugService.existByStandardName(newDrug);

            if (!exists) {

                for (DrugName drugName : newDrug.getDrugNames()) {
                    drugName.setDrug(newDrug);

                    if (!drugSourceService.existByShortName(drugSourceList, drugName.getDrugSource())) {
                        drugSourcesToSave.add(drugName.getDrugSource());
                        drugSourceList.add(drugName.getDrugSource());
                    } else {
                        DrugSource drugSource = drugSourceService.getByShortName(drugSourceList, drugName.getDrugSource());
                        drugName.setDrugSource(drugSource);
                    }

                    drugNameList.add(drugName);
                }
                newDrug.setStartUpdate(drugUpdate.getId());
                newDrug.setDrugSet(drugSet);

                drugList.add(newDrug);
            }
        }

        transactionalDrugSetSave(drugUpdate, drugSet, newDrugs, drugSourcesToSave, drugNameList);
    }

    public void updateDrugSetContent(DrugSet drugSet, List<Drug> newDrugs, DrugUpdate oldDrugUpdate, DrugUpdate newDrugUpdate){

        List<DrugSource> drugSourceList = drugSourceService.findAll();
        List<DrugSource> drugSourcesToSave = new ArrayList<>();

        List<Drug> drugList = drugService.findAll();
        Set<String> unavailableItems = newDrugs.stream()
                .map(Drug::getStandardName)
                .collect(Collectors.toSet());

        List<Drug> deletedDrugs = drugList.stream()
                .filter(e -> !unavailableItems.contains(e.getStandardName())).toList();

        List<Drug> drugsToUpdate = new ArrayList<>();
        List<Drug> drugsToInclude = new ArrayList<>();
        List<DrugName> drugNameList = new ArrayList<>();

        for (Drug newDrug : newDrugs) {

            Drug oldDrug = drugService.getByStandardName(newDrug);
            boolean hasChanged = false;

            if (oldDrug != null) {

                if(!oldDrug.getCommonName().equals(newDrug.getCommonName())){
                    hasChanged = true;
                }

                if(oldDrug.getDrugNames().size() != newDrug.getDrugNames().size()){
                    hasChanged = true;
                }

                if(hasChanged) {

                    newDrug.setStartUpdate(newDrugUpdate.getId());
                    newDrug.setDrugSet(drugSet);
                    newDrug.setId(UUID.randomUUID());

                    oldDrug.setNextVersion(newDrug.getId());
                    newDrug.setPreviousVersion(oldDrug.getId());

                    drugsToUpdate.add(oldDrug);
                    drugsToInclude.add(newDrug);

                    for (DrugName drugName : newDrug.getDrugNames()) {
                        drugName.setDrug(newDrug);

                        if (!drugSourceService.existByShortName(drugSourceList, drugName.getDrugSource())) {
                            drugSourcesToSave.add(drugName.getDrugSource());
                            drugSourceList.add(drugName.getDrugSource());
                        } else {
                            DrugSource drugSource = drugSourceService.getByShortName(drugName.getDrugSource());
                            drugName.setDrugSource(drugSource);
                        }

                        drugNameList.add(drugName);
                    }

                }
            }
        }


        if (deletedDrugs.size() > 0){

            for(Drug drug : deletedDrugs){
                drug.setEndUpdate(newDrugUpdate.getId());
                drugsToUpdate.add(drug);
            }
        }


        if(drugsToUpdate.size() > 0 || drugsToInclude.size() > 0) {
            transactionalDrugSetUpdate(oldDrugUpdate, newDrugUpdate, drugSet, drugsToInclude, drugsToUpdate, drugSourcesToSave, drugNameList);
        }
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
                assert connection != null;
                connection.rollback();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }finally {
            try {
                if(null != connection) {
                    assert statement != null;
                    statement.close();
                    connection.close();
                }
            }
            catch (SQLException sqlex) {
                sqlex.printStackTrace();
            }
        }
    }

    public void transactionalDrugSetUpdate(DrugUpdate oldDrugUpdate, DrugUpdate newDrugUpdate, DrugSet drugSet, List<Drug> drugsToInclude, List<Drug> drugsToUpdate, List<DrugSource> drugSourceList, List<DrugName> drugNameList){

        Connection connection = null;
        PreparedStatement statement = null;

        try{

            connection = hikariDataSource.getConnection();
            connection.setAutoCommit(false);


            String sqlNewDrugUpdate = String.format(
                    "INSERT INTO drug_update (id, created_at, description, drug_set_id, next_update_id, previous_update_id, user_id) " +
                            "VALUES (?::UUID, ?, ?, ?::UUID,?::UUID, ?::UUID, ?::UUID)",
                    DrugUpdate.class.getAnnotation(Table.class).name()
            );

            statement = connection.prepareStatement(sqlNewDrugUpdate);

            saveDrugUpdateByJdbc(newDrugUpdate, statement);

            String sqlOldDrugUpdate = String.format(
                    "UPDATE drug_update SET created_at=?, description=?, drug_set_id=?::UUID, next_update_id=?::UUID, previous_update_id=?::UUID, user_id=?::UUID " +
                            "WHERE id=?::UUID",
                    DrugUpdate.class.getAnnotation(Table.class).name()
            );

            statement = connection.prepareStatement(sqlOldDrugUpdate);

            updateDrugUpdateByJdbc(oldDrugUpdate, statement);

            String sqlDrugSet = String.format(
                    "UPDATE drug_set SET created_at=?, description=?, name=?, updated_at=? " +
                            "WHERE id=?::UUID",
                    DrugSet.class.getAnnotation(Table.class).name()
            );

            statement = connection.prepareStatement(sqlDrugSet);

            updateDrugSetByJdbc(drugSet, statement);

            if(drugsToInclude.size() > 0) {

                String sqlDrugsToInsert = String.format(
                        "INSERT INTO drug (id, common_name, end_update, next_version, previous_version, standard_name, start_update, drugset_id) " +
                                "VALUES (?::UUID, ?, ?::UUID, ?::UUID, ?::UUID, ?, ?::UUID, ?::UUID)",
                        Drug.class.getAnnotation(Table.class).name()
                );
                statement = connection.prepareStatement(sqlDrugsToInsert);


                saveDrugsByJdbc(drugsToInclude, statement);
            }

            if(drugsToUpdate.size() > 0) {

                String sqlDrugsToUpdate = String.format(
                        "UPDATE drug SET common_name=?, end_update=?::UUID, next_version=?::UUID, previous_version=?::UUID, standard_name=?, start_update=?::UUID, drugset_id=?::UUID " +
                                "WHERE id=?::UUID",
                        Drug.class.getAnnotation(Table.class).name()
                );

                statement = connection.prepareStatement(sqlDrugsToUpdate);

                updateDrugsByJdbc(drugSet, drugsToUpdate, statement);
            }

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
                assert connection != null;
                connection.rollback();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }finally {
            try {
                if(null != connection) {
                    assert statement != null;
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

    public void updateDrugUpdateByJdbc(DrugUpdate drugUpdateData, PreparedStatement statement){

        try {
            statement.clearParameters();
            statement.setTimestamp(1, Timestamp.valueOf(drugUpdateData.getCreatedAt()));

            if(drugUpdateData.getDescription() == null){
                statement.setString(2, drugUpdateData.getDescription());
            }else {
                statement.setNull(2, Types.NULL);
            }

            statement.setString(3, drugUpdateData.getDrugSetId().toString());

            if(drugUpdateData.getNextUpdateId() != null){
                statement.setString(4, drugUpdateData.getNextUpdateId().toString());
            }else {
                statement.setNull(4, Types.NULL);
            }

            if(drugUpdateData.getPreviousUpdateId() != null){
                statement.setString(5, drugUpdateData.getPreviousUpdateId().toString());
            }else {
                statement.setNull(5, Types.NULL);
            }

            if(drugUpdateData.getUserId() != null){
                statement.setString(6, drugUpdateData.getUserId().toString());
            }else {
                statement.setNull(6, Types.NULL);
            }

            statement.setString(7, drugUpdateData.getId().toString());

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

    public void updateDrugSetByJdbc(DrugSet drugSetData, PreparedStatement statement){

        try {
            statement.clearParameters();
            statement.setTimestamp(1, Timestamp.valueOf(drugSetData.getCreatedAt()));
            statement.setString(2, drugSetData.getDescription());
            statement.setString(3, drugSetData.getName());
            if(drugSetData.getUpdatedAt() != null){
                statement.setTimestamp(4, Timestamp.valueOf(drugSetData.getUpdatedAt()));
            }else {
                statement.setNull(4, Types.NULL);
            }

            statement.setString(5, drugSetData.getId().toString());

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

                if (drug.getPreviousVersion() != null) {
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

    public void updateDrugsByJdbc(DrugSet drugSet, List<Drug> drugData, PreparedStatement statement){
        int counter = 0;

        try {
            for (Drug drug : drugData) {
                statement.clearParameters();
                statement.setString(1, drug.getCommonName());

                if (drug.getEndUpdate() != null) {
                    statement.setString(2, drug.getEndUpdate().toString());
                } else {
                    statement.setNull(2, Types.NULL);
                }

                if (drug.getNextVersion() != null) {
                    statement.setString(3, drug.getNextVersion().toString());
                } else {
                    statement.setNull(3, Types.NULL);
                }

                if (drug.getPreviousVersion() != null) {
                    statement.setString(4, drug.getPreviousVersion().toString());
                } else {
                    statement.setNull(4, Types.NULL);
                }

                statement.setString(5, drug.getStandardName());

                if (drug.getStartUpdate() != null) {
                    statement.setString(6, drug.getStartUpdate().toString());
                } else {
                    statement.setNull(6, Types.NULL);
                }

                statement.setString(7, drugSet.getId().toString());
                statement.setString(8, drug.getId().toString());

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

    public void updateDrugSourcesByJdbc(List<DrugSource> drugSourceData, PreparedStatement statement){

        int counter = 0;

        try{
            for (DrugSource drugSource : drugSourceData) {
                statement.clearParameters();
                statement.setString(1, drugSource.getShortName());

                if(drugSource.getUrl() != null) {
                    statement.setURL(2, drugSource.getUrl());
                }else{
                    statement.setNull(2, Types.NULL);
                }
                statement.setString(3, drugSource.getId().toString());

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

    public void updateDrugSet(DrugSet drugSet){

        List<Drug> newDrugs = drugSet.getDrugs();
        drugSet.setDrugs(null);

        DrugUpdate drugUpdate = new DrugUpdate();
        drugUpdate.setDrugSetId(drugSet.getId());
        List<DrugUpdate> drugUpdateList = drugUpdateService.findAll();

        Comparator<DrugUpdate> comparator = Comparator.comparing(DrugUpdate::getCreatedAt);

        drugUpdateList.sort(comparator);

        drugUpdateList.get(drugUpdateList.size()-1).setNextUpdateId(drugUpdate.getId());
        drugUpdate.setPreviousUpdateId(drugUpdateList.get(drugUpdateList.size()-1).getId());

        drugSet.setUpdatedAt(drugUpdate.getCreatedAt());

        updateDrugSetContent(drugSet, newDrugs, drugUpdateList.get(drugUpdateList.size()-1), drugUpdate);
    }

    public boolean existByName(DrugSet drugSet){

        String sql = "SELECT * FROM drug_set " +
                "WHERE name='"+drugSet.getName().replaceAll("'","''")+"'"; //Replace ' with '' to avoid syntax errorx
        Connection connection = null;
        Statement statement;
        ResultSet rs;

        try {
            connection = hikariDataSource.getConnection();
            statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rs = statement.executeQuery(sql);
            return rs.first();


        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                assert connection != null;
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return false;

    }

    public List<DrugSet> findAllByQuery(){
        String sql = "SELECT id, name, description, created_at, updated_at FROM drug_set";

        return jdbcTemplate.query(sql,
                new BeanPropertyRowMapper(DrugSet.class));
    }

}

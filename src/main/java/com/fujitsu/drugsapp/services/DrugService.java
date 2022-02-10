package com.fujitsu.drugsapp.services;

import com.fujitsu.drugsapp.entities.Drug;
import com.fujitsu.drugsapp.entities.DrugName;
import com.fujitsu.drugsapp.entities.DrugSet;
import com.fujitsu.drugsapp.entities.DrugSource;
import com.fujitsu.drugsapp.repositories.DrugRepository;
import com.zaxxer.hikari.HikariDataSource;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@Service
public class DrugService {

    private final DrugRepository drugRepository;

    @Autowired
    protected HikariDataSource hikariDataSource;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    public List<Drug> findAll(){
        return drugRepository.findAll();
    }

    public Drug findById(UUID id){

        String sql = "SELECT * FROM drug d " +
                "JOIN drug_name dn ON dn.drug_id=d.id " +
                "JOIN drug_source ds ON dn.drug_source_id=ds.id " +
                "WHERE d.id='"+id+"'";

        Connection connection = null;
        Statement statement;
        ResultSet rs;
        Drug drug = new Drug();
        DrugName drugName = new DrugName();
        DrugSource drugSource = new DrugSource();
        List<DrugName> drugNameList = new ArrayList<>();

        try {
            connection = hikariDataSource.getConnection();
            statement = connection.createStatement();
            rs = statement.executeQuery(sql);

            while(rs.next())
            {
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


        return drug;
    }

    public boolean existByStandardName(Drug drug){

        String sql = "SELECT * FROM drug " +
                "WHERE standard_name='"+drug.getStandardName().replaceAll("'","''")+"'"; //Replace ' with '' to avoid syntax errorx
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

    public Drug getByStandardName(Drug drug){

        String sql = "SELECT * FROM drug d " +
                "JOIN drug_name dn ON dn.drug_id=d.id " +
                "JOIN drug_source ds ON dn.drug_source_id=ds.id " +
                "WHERE d.standard_name='"+drug.getStandardName().replaceAll("'","''")+"'";

        Connection connection = null;
        Statement statement;
        ResultSet rs;
        Drug matchedDrug = new Drug();
        DrugName drugName = new DrugName();
        DrugSource drugSource = new DrugSource();
        List<DrugName> drugNameList = new ArrayList<>();

        try {
            connection = hikariDataSource.getConnection();
            statement = connection.createStatement();
            rs = statement.executeQuery(sql);

            while(rs.next())
            {
                matchedDrug.setId(rs.getObject(1,UUID.class));
                matchedDrug.setCommonName(rs.getObject("common_name", String.class));
                matchedDrug.setStandardName(rs.getObject("standard_name", String.class));
                matchedDrug.setEndUpdate(rs.getObject("end_update",UUID.class));
                matchedDrug.setStartUpdate(rs.getObject("start_update",UUID.class));
                matchedDrug.setNextVersion(rs.getObject("next_version",UUID.class));
                matchedDrug.setPreviousVersion(rs.getObject("previous_version",UUID.class));

                drugName.setId(rs.getObject(9, UUID.class));
                drugName.setName(rs.getObject("name",String.class));
                drugName.setDrug(matchedDrug);

                drugSource.setId(rs.getObject(13, UUID.class));
                drugSource.setShortName(rs.getObject("short_name", String.class));

                if(rs.getString("url") != null) {
                    drugSource.setUrl(rs.getObject("url", URL.class));
                }
                drugName.setDrugSource(drugSource);

                drugNameList.add(drugName);

                matchedDrug.setDrugNames(drugNameList);
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


        return matchedDrug;
    }

}

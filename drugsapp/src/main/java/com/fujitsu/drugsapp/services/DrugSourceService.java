package com.fujitsu.drugsapp.services;

import com.fujitsu.drugsapp.entities.DrugSource;
import com.fujitsu.drugsapp.repositories.DrugSourceRepository;
import com.zaxxer.hikari.HikariDataSource;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@Service
public class DrugSourceService {

    private DrugSourceRepository drugSourceRepository;

    @Autowired
    HikariDataSource hikariDataSource;

    @Autowired
    public JdbcTemplate jdbcTemplate;

    public List<DrugSource> findAll(){ return drugSourceRepository.findAll(); }

    public DrugSource getByShortName(DrugSource drugSource){

        String sql = "SELECT * FROM drug_source " +
                "WHERE short_name='"+drugSource.getShortName().replaceAll("'","''")+"'"; //Replace ' with '' to avoid syntax errorx
        Connection connection = null;
        Statement statement;
        ResultSet rs;
        DrugSource matchedDrugSource = new DrugSource();

        try {
            connection = hikariDataSource.getConnection();
            statement = connection.createStatement();
            rs = statement.executeQuery(sql);

            while(rs.next())
            {
                matchedDrugSource.setId(rs.getObject("id",UUID.class));
                matchedDrugSource.setShortName(rs.getObject("short_name", String.class));
                String urlString = rs.getObject("url", String.class);

                if (urlString!=null){
                    URL url = new URL(urlString);
                    matchedDrugSource.setUrl(url);
                }
            }

        } catch (SQLException | MalformedURLException e) {
            e.printStackTrace();
        } finally {
            try {
                assert connection != null;
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }


        return matchedDrugSource;
    }

    public DrugSource getByShortName(List<DrugSource> drugSourceList, DrugSource drugSource){

        for(DrugSource source : drugSourceList){
            if(drugSource.getShortName().equalsIgnoreCase(source.getShortName())){
                return source;
            }
        }

        return null;
    }

    public boolean existByShortName(DrugSource drugSource){

        String sql = "SELECT * FROM drug_source " +
                "WHERE short_name='"+drugSource.getShortName().replaceAll("'","''")+"'"; //Replace ' with '' to avoid syntax errorx
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

    public boolean existByShortName(List<DrugSource> findDrugSource, DrugSource drugSource){

        for (DrugSource source : findDrugSource) {
            if (drugSource.getShortName().equalsIgnoreCase(source.getShortName()))
                return true;
        }

        return false;
    }
}

package dk.sdu.mmmi.persistence;

import dk.sdu.mmmi.commonPersistence.iPersistanceService;
import dk.sdu.mmmi.commonPersistence.PersistenceSPI;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


@Component
@ComponentScan
public class PersistenceControlSystem implements iPersistanceService, PersistenceSPI {

    JdbcTemplate jdbcTemplate = new JdbcTemplate(getDatasource());

    @Override
    public boolean saveOperation(String productionId, int operationSequenceId, String operation, String assetName, LocalDateTime timestamp) {
        try {
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS productions(" +
                    "id SERIAL, productionId VARCHAR(255), operation VARCHAR(255), assetName VARCHAR(255), operationSeqId INT, timestamp DATETIME);");

            jdbcTemplate.update("INSERT INTO productions (productionId, operation, assetName, operationSeqId, timestamp) " +
                    "VALUES (?, ?, ?, ?, ?);", productionId, operation, assetName, operationSequenceId, timestamp);
        } catch (Exception e) {
            System.out.println(e);
        }
        return true;
    }

    @Override
    public List<TreeMap<Integer, String>> getPersistedProductions() {
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS productions(" +
                "id SERIAL, productionId VARCHAR(255), operation VARCHAR(255), assetName VARCHAR(255), operationSeqId INT, timestamp DATETIME);");


        TreeMap<String, List<Map<String, Object>>> productionHistory = new TreeMap<>();
        //productionHistory<productionId, row>

        List<Map<String, Object>> rows = jdbcTemplate.queryForList("SELECT * FROM productions;");
        for (Map row : rows) {
            String rowProductionId = (String) row.get("productionId");

            if (!productionHistory.containsKey(rowProductionId)) {
                ArrayList<Map<String, Object>> list = new ArrayList();
                list.add(row);
                productionHistory.put(rowProductionId, list);
            } else {
                productionHistory.get(rowProductionId).add(row);
            }
        }
        List<TreeMap<Integer, String>> persistedProductions = new ArrayList<>();

        for (List<Map<String, Object>> production : productionHistory.values()) {
            TreeMap productionMap = new TreeMap();
            for (Map<String, Object> operationRow : production) {
                productionMap.put(operationRow.get("operationSeqId"), operationRow.get("assetName")+":"+operationRow.get("operation"));
            }
            persistedProductions.add(productionMap);
        }
        return persistedProductions;
    }

    @Override
    public Map<String, Object> getProductionMetaData(int index) {
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS productions(" +
                "id SERIAL, productionId VARCHAR(255), operation VARCHAR(255), assetName VARCHAR(255), operationSeqId INT, timestamp DATETIME);");

        List<Map<String, Object>> rows = jdbcTemplate.queryForList("SELECT `productionId`, MIN(`timestamp`) min_ts, MAX(`timestamp`) max_ts " +
                "FROM `productions`" +
                "GROUP BY `productionId`" +
                "ORDER BY `min_ts`;");

        return rows.get(index);

    }

    private DriverManagerDataSource getDatasource() {
        DriverManagerDataSource ds = new DriverManagerDataSource(
                "jdbc:mysql://localhost:3306/drone_production",
                "root",
                "password"
        );
        ds.setDriverClassName("com.mysql.cj.jdbc.Driver");
        return ds;
    }

}

package ru.sbrf.jira.clickhouse.exporter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.yandex.clickhouse.ClickHouseConnection;
import ru.yandex.clickhouse.ClickHouseDataSource;
import ru.yandex.clickhouse.ClickHouseStatement;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Component
public class IssueRepository {
    private final ClickHouseDataSource dataSource;
    private final IssueFieldManager typeAdapter;
    private final PluginConfigurationAdapter configuration;
    private final String TABLE_NAME = "jira_events";
    private static final Logger logger = LoggerFactory.getLogger(IssueRepository.class);

    @Autowired
    public IssueRepository(ClickHouseDataSource dataSource, IssueFieldManager typeAdapter, PluginConfigurationAdapter configuration) {
        this.dataSource = dataSource;
        this.typeAdapter = typeAdapter;
        this.configuration = configuration;
    }

    public void prepareTable() {
        try (ClickHouseConnection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            try (ResultSet resultSet = metaData.getTables(null, null, TABLE_NAME, null)) {
                if (!resultSet.next()) {
                    createTableWithColumns(connection, getFields());
                } else {
                    updateTableColumns(connection, getFields());
                }
            }
        } catch (SQLException e) {
            logger.error("SQL error: {}", e.getMessage());
        }
    }

    private void updateTableColumns(ClickHouseConnection connection, Collection<String> columns) throws SQLException {
        DatabaseMetaData metadata = connection.getMetaData();
        Map<String, String> existingColumns = new HashMap<>();

        try (ResultSet resultSet = metadata.getColumns(null, null, TABLE_NAME, null)) {
            while (resultSet.next()) {
                String columnName = resultSet.getString("COLUMN_NAME");
                String typeName = resultSet.getString("TYPE_NAME");
                existingColumns.put(columnName, typeName);
            }
        }

        for (String column : columns) {
            String type = existingColumns.get(column);
            if (type == null) {

                type = typeAdapter.getFieldType(column);
                if (type == null) {
                    continue;
                }

                String sql = String.format("ALTER TABLE %s ADD COLUMN %s %s;", TABLE_NAME, column, type);
                try (ClickHouseStatement statement = connection.createStatement()) {
                    logger.debug("Executing sql {}", sql);
                    statement.execute(sql);
                }
            } else if (!type.equalsIgnoreCase(typeAdapter.getFieldType(column))) {
                String sql = String.format("ALTER TABLE %s ALTER COLUMN %s TYPE %s;", TABLE_NAME, column, type);
                try (ClickHouseStatement statement = connection.createStatement()) {
                    logger.debug("Executing sql {}", sql);
                    statement.execute(sql);
                }
            }
        }
    }

    private void createTableWithColumns(ClickHouseConnection connection, Collection<String> columns) throws SQLException {
        StringBuilder sql = new StringBuilder("CREATE TABLE ");
        sql.append(TABLE_NAME);
        sql.append('(');

        for (String column : columns) {
            String type = typeAdapter.getFieldType(column);
            if (type == null) {
                continue;
            }

            sql.append(column);
            sql.append(' ');
            sql.append(type);
            sql.append(',');
        }

        sql.append("PRIMARY KEY(id)");
        sql.append(')');
        sql.append("ENGINE = MergeTree();");

        String resultSql = sql.toString();
        logger.debug("Executing sql {}", resultSql);

        try (ClickHouseStatement statement = connection.createStatement()) {
            statement.execute(resultSql);
        }
    }

    public void addEventData(Map<String, Object> eventData) {
        /*StringBuilder sqlBuilder = new StringBuilder("")

        try (ClickHouseConnection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement()) {
            statement.execute(String.format("insert into jira_events values ('%s', '%s', '%s', '%s')", issueEvent.getTime().toString(), issueEvent.getUser().getName().toString(), issueEvent.getProject().getName().toString(), issue.getKey().toString()));
            System.out.println("wrote to db");
        } catch (SQLException e) {
            e.printStackTrace();
        }

        for (Map.Entry<String, Object> entry : eventData.entrySet()) {

        }*/
    }

    private List<String> getFields() {
        Set<String> allowedFields = new HashSet<>();
        allowedFields.add("id");
        Object fieldsValue = configuration.getValue("issue_fields");
        if (fieldsValue != null) {
            allowedFields.addAll((Collection<String>) fieldsValue);
        }

        Set<String> fields = new LinkedHashSet<>();
        fields.add("id");
        fields.addAll(typeAdapter.getAllIssueFields());
        fields.retainAll(allowedFields);
        return new ArrayList<>(fields);
    }
}

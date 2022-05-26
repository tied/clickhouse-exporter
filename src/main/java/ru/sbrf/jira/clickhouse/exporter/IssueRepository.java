package ru.sbrf.jira.clickhouse.exporter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.sbrf.jira.clickhouse.configuration.PluginConfiguration;
import ru.sbrf.jira.clickhouse.configuration.PluginConfigurationRepository;
import ru.sbrf.jira.clickhouse.jirautil.IssueEventField;
import ru.sbrf.jira.clickhouse.jirautil.IssueEventFieldFactory;
import ru.yandex.clickhouse.ClickHouseConnection;
import ru.yandex.clickhouse.ClickHouseDataSource;
import ru.yandex.clickhouse.ClickHouseStatement;

import java.sql.*;
import java.util.*;

@Component
public class IssueRepository {
    private final ClickHouseDataSource dataSource;
    private final IssueEventFieldFactory fieldFactory;
    private final PluginConfigurationRepository configurationRepository;
    private static final Logger logger = LoggerFactory.getLogger(IssueRepository.class);

    @Autowired
    public IssueRepository(ClickHouseDataSource dataSource, IssueEventFieldFactory fieldFactory, PluginConfigurationRepository configurationRepository) {
        this.dataSource = dataSource;
        this.fieldFactory = fieldFactory;
        this.configurationRepository = configurationRepository;
    }

    public void prepareTable() {
        PluginConfiguration configuration = configurationRepository.get();

        try (ClickHouseConnection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            try (ResultSet resultSet = metaData.getTables(null, connection.getSchema(), configuration.getEventsTableName(), null)) {
                if (!resultSet.next()) {
                    createTableWithColumns(connection, configuration.getIssueFields());
                } else {
                    updateTableColumns(connection, configuration.getIssueFields());
                }
            }
        } catch (SQLException e) {
            logger.error("SQL error: {}", e.getMessage());
        }
    }

    private void updateTableColumns(ClickHouseConnection connection, Collection<String> columns) throws SQLException {
        DatabaseMetaData metadata = connection.getMetaData();
        Map<String, String> existingColumns = new HashMap<>();
        PluginConfiguration configuration = configurationRepository.get();

        try (ResultSet resultSet = metadata.getColumns(null, connection.getSchema(), configuration.getEventsTableName(), null)) {
            while (resultSet.next()) {
                String columnName = resultSet.getString("COLUMN_NAME");
                String typeName = resultSet.getString("TYPE_NAME");
                existingColumns.put(columnName, typeName);
            }
        }

        for (String column : columns) {
            String type = existingColumns.get(column);
            if (type == null) {
                type = getFieldType(column);
                if (type == null) {
                    continue;
                }

                String sql = String.format("ALTER TABLE %s ADD COLUMN %s %s;", configuration.getEventsTableName(), column, type);
                try (ClickHouseStatement statement = connection.createStatement()) {
                    logger.debug("Executing sql {}", sql);
                    statement.execute(sql);
                }
            } else {
                String fieldType = getFieldType(column);
                if (!type.equalsIgnoreCase(fieldType)) {
                    String sql = String.format("ALTER TABLE %s ALTER COLUMN %s TYPE %s;", configuration.getEventsTableName(), column, fieldType);
                    try (ClickHouseStatement statement = connection.createStatement()) {
                        logger.debug("Executing sql {}", sql);
                        statement.execute(sql);
                    }
                }
            }
        }
    }

    private void createTableWithColumns(ClickHouseConnection connection, Collection<String> columns) throws SQLException {
        PluginConfiguration configuration = configurationRepository.get();

        StringBuilder sql = new StringBuilder("CREATE TABLE ");
        sql.append(configuration.getEventsTableName());
        sql.append('(');

        for (String column : columns) {
            String type = getFieldType(column);
            if (type == null) {
                continue;
            }

            sql.append(column);
            sql.append(' ');
            sql.append(type);
            sql.append(',');
        }

        sql.append("PRIMARY KEY(event_id)");
        sql.append(')');
        sql.append("ENGINE = MergeTree();");

        String resultSql = sql.toString();
        logger.debug("Executing sql {}", resultSql);

        try (ClickHouseStatement statement = connection.createStatement()) {
            statement.execute(resultSql);
        }
    }

    public void addEventData(Map<String, Object> eventData) {
        PluginConfiguration configuration = configurationRepository.get();
        StringBuilder sql = new StringBuilder("INSERT INTO ");
        sql.append(configuration.getEventsTableName());
        sql.append(' ');

        sql.append('(');
        List<String> fields = configuration.getIssueFields();
        sql.append(String.join(",", fields));
        sql.append(')');

        sql.append(" VALUES (");
        String[] questions = new String[fields.size()];
        Arrays.fill(questions, "?");
        sql.append(String.join(",", questions));
        sql.append(')');
        sql.append(';');

        try (ClickHouseConnection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < fields.size(); i++) {
                String field = fields.get(i);
                statement.setObject(i + 1, eventData.get(field));
            }

            statement.execute();
        } catch (SQLException e) {
            logger.error("SQL error: {}", e.getMessage());
        }
    }

    public Timestamp getEarliestEditDateForIssue(String issueKey) {
        PluginConfiguration configuration = configurationRepository.get();

        String sql = String.format("SELECT timestamp FROM %s WHERE issue_id = ? ORDER BY timestamp LIMIT 1", configuration.getEventsTableName());

        try (ClickHouseConnection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, issueKey);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getTimestamp(1);
                }
            }
        } catch (SQLException e) {
            logger.error("SQL error: {}", e.getMessage());
        }

        return null;
    }

    private String getFieldType(String column) {
        List<IssueEventField> fields = fieldFactory.getFields();
        for (IssueEventField field : fields) {
            if (field.getId().equals(column)) {
                return field.getDbType();
            }
        }

        return null;
    }
}

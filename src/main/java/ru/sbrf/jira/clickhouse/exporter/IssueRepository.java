package ru.sbrf.jira.clickhouse.exporter;

import com.atlassian.jira.issue.Issue;
import com.google.common.base.Strings;
import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.yandex.clickhouse.ClickHouseConnection;
import ru.yandex.clickhouse.ClickHouseDataSource;
import ru.yandex.clickhouse.ClickHouseStatement;

import java.sql.*;
import java.util.*;
import java.util.Date;

@Component
public class IssueRepository {
    private final ClickHouseDataSource dataSource;
    private final IssueFieldManager fieldManager;
    private final PluginConfigurationAdapter configuration;
    private final String TABLE_NAME = "jira_events";
    private static final Logger logger = LoggerFactory.getLogger(IssueRepository.class);

    @Autowired
    public IssueRepository(ClickHouseDataSource dataSource, IssueFieldManager fieldManager, PluginConfigurationAdapter configuration) {
        this.dataSource = dataSource;
        this.fieldManager = fieldManager;
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
            String type;
            type = existingColumns.get(column);
            if (type == null) {
                if ("timestamp".equals(column)) {
                    type = "TimeStamp";
                } else if ("event_id".equals(column)) {
                    type = "UUID";
                } else {
                    type = fieldManager.getFieldType(column);
                    if (type == null) {
                        continue;
                    }
                }

                String sql = String.format("ALTER TABLE %s ADD COLUMN %s %s;", TABLE_NAME, column, type);
                try (ClickHouseStatement statement = connection.createStatement()) {
                    logger.debug("Executing sql {}", sql);
                    statement.execute(sql);
                }
            } else if (!type.equalsIgnoreCase(fieldManager.getFieldType(column))) {
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
            String type;
            if ("timestamp".equals(column)) {
                type = "TimeStamp";
            } else if ("event_id".equals(column)) {
                type = "UUID";
            } else {
                type = fieldManager.getFieldType(column);
                if (type == null) {
                    continue;
                }
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

    public void addEventData(Date time, Issue issue) {
        StringBuilder sql = new StringBuilder("INSERT INTO ");
        sql.append(TABLE_NAME);
        sql.append(' ');

        sql.append('(');
        List<String> fields = getFields();
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
                Object value;
                if ("timestamp".equals(field)) {
                    value = new Timestamp(time.getTime());
                } else if ("event_id".equals(field)) {
                    value = UUID.randomUUID();
                } else {
                    value = fieldManager.getFieldValue(issue, field);
                }
                statement.setObject(i + 1, value);
            }

            statement.execute();
        } catch (SQLException e) {
            logger.error("SQL error: {}", e.getMessage());
        }
    }

    private List<String> getFields() {
        Set<String> allowedFields = new HashSet<>();
        allowedFields.add("event_id");
        allowedFields.add("issue_id");
        allowedFields.add("timestamp");
        Object fieldsValue = configuration.getValue("issue_fields");
        if (fieldsValue != null) {
            allowedFields.addAll((Collection<String>) fieldsValue);
        }

        Set<String> fields = new LinkedHashSet<>();
        fields.add("event_id");
        fields.add("issue_id");
        fields.add("timestamp");
        fields.addAll(fieldManager.getAllIssueFields());
        fields.retainAll(allowedFields);
        return new ArrayList<>(fields);
    }
}

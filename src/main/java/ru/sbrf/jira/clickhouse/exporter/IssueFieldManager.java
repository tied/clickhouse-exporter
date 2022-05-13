package ru.sbrf.jira.clickhouse.exporter;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class IssueFieldManager {
    private final FieldManager fieldManager;
    private static final Logger logger = LoggerFactory.getLogger(IssueFieldManager.class);

    @Autowired
    public IssueFieldManager(@ComponentImport FieldManager fieldManager) {
        this.fieldManager = fieldManager;
    }

    public String getFieldType(String fieldId) {
        if ("id".equals(fieldId)) {
            return "Int64";
        }

        if (fieldManager.isCustomFieldId(fieldId)) {
            CustomField customField = fieldManager.getCustomField(fieldId);
            if (customField == null) {
                logger.warn("Invalid field {}", fieldId);
                return null;
            }

            String key = customField.getCustomFieldType().getKey();
            CustomFieldType customFieldType = CustomFieldType.getTypeByKey(key);
            if (customFieldType == null) {
                logger.warn("Invalid key {} for field {}", key, fieldId);
                return null;
            }

            return customFieldType.getClickhouseType();
        }

        Field field = fieldManager.getField(fieldId);
        if (field == null) {
            logger.warn("Unknown field {}", fieldId);
            return null;
        }

        SystemFieldType systemFieldType = SystemFieldType.getTypeByKey(fieldId);
        if (systemFieldType == null) {
            logger.debug("No type mapping for system field {}", fieldId);
            return null;
        }

        return systemFieldType.getClickhouseType();
    }

    public Object getFieldValue(Issue issue, String fieldId) {
        if ("id".equals(fieldId)) {
            return issue.getId();
        }

        if (fieldManager.isCustomFieldId(fieldId)) {
            CustomField customField = fieldManager.getCustomField(fieldId);
            if (customField == null) {
                logger.warn("Invalid field {}", fieldId);
                return null;
            }

            return customField.getValue(issue);
        }

        Field field = fieldManager.getField(fieldId);
        if (field == null) {
            logger.warn("Unknown field {}", fieldId);
            return null;
        }

        SystemFieldType systemFieldType = SystemFieldType.getTypeByKey(fieldId);
        if (systemFieldType == null) {
            logger.debug("No type mapping for system field {}", fieldId);
            return null;
        }

        return systemFieldType.getFieldValue(issue);
    }

    public boolean isSystemField(String fieldId) {
        return fieldManager.getField(fieldId) != null && !fieldManager.isCustomFieldId(fieldId);
    }

    public List<String> getAllIssueFields() {
        return fieldManager.getAllSearchableFields().stream().map(Field::getId).collect(Collectors.toList());
    }

    public String getFieldName(String fieldId) {
        Field field = fieldManager.getField(fieldId);
        if (field != null) {
            return field.getName();
        }

        return null;
    }
}

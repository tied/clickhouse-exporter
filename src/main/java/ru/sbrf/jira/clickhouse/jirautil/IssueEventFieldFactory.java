package ru.sbrf.jira.clickhouse.jirautil;

import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.label.Label;
import com.atlassian.jira.project.version.Version;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class IssueEventFieldFactory {
    private final FieldManager fieldManager;
    private static final Logger logger = LoggerFactory.getLogger(IssueEventFieldFactory.class);

    @Autowired
    public IssueEventFieldFactory(@ComponentImport FieldManager fieldManager) {
        this.fieldManager = fieldManager;
    }

    public List<IssueEventField> getFields() {
        List<IssueEventField> fields = new ArrayList<>();

        // Обязательные поля
        fields.add(new IssueEventField("event_id", "Идентификатор события", (Issue issue) -> null, true, "UUID"));
        fields.add(new IssueEventField("timestamp", "Время события", (Issue issue) -> null, true, "DateTime"));
        fields.add(new IssueEventField("issue_id", "Ключ Issue", Issue::getKey, true, "String"));

        // Системные поля
        fields.add(new IssueEventField("project", getSystemFieldName("project"), (Issue issue) -> issue.getProjectObject().getKey(), false, "String"));
        fields.add(new IssueEventField("summary", getSystemFieldName("summary"), Issue::getSummary, false, "String"));
        fields.add(new IssueEventField("issuetype", getSystemFieldName("issuetype"), IssueEventFieldFactory::getIssueType, false, "String"));
        fields.add(new IssueEventField("status", getSystemFieldName("status"), (Issue issue) -> issue.getStatus().getName(), false, "String"));
        fields.add(new IssueEventField("priority", getSystemFieldName("priority"), IssueEventFieldFactory::getPriority, false, "String"));
        fields.add(new IssueEventField("resolution", getSystemFieldName("resolution"), IssueEventFieldFactory::getResolution, false, "String"));
        fields.add(new IssueEventField("assignee", getSystemFieldName("assignee"), Issue::getAssigneeId, false, "String"));
        fields.add(new IssueEventField("reporter", getSystemFieldName("reporter"), Issue::getReporterId, false, "String"));
        fields.add(new IssueEventField("creator", getSystemFieldName("creator"), Issue::getCreatorId, false, "String"));
        fields.add(new IssueEventField("created", getSystemFieldName("created"), Issue::getCreated, false, "DateTime"));
        fields.add(new IssueEventField("updated", getSystemFieldName("updated"), Issue::getUpdated, false, "DateTime"));
        fields.add(new IssueEventField("resolutiondate", getSystemFieldName("resolutiondate"), Issue::getResolutionDate, false, "DateTime"));
        fields.add(new IssueEventField("versions", getSystemFieldName("versions"), (Issue issue) -> issue.getAffectedVersions().stream().map(Version::getName).collect(Collectors.toList()), false, "Array(String)"));
        fields.add(new IssueEventField("fixVersions", getSystemFieldName("fixVersions"), (Issue issue) -> issue.getFixVersions().stream().map(Version::getName).collect(Collectors.toList()), false, "Array(String)"));
        fields.add(new IssueEventField("components", getSystemFieldName("components"), (Issue issue) -> issue.getComponents().stream().map(ProjectComponent::getName).collect(Collectors.toList()), false, "Array(String)"));
        fields.add(new IssueEventField("duedate", getSystemFieldName("duedate"), Issue::getDueDate,false, "DateTime"));
        fields.add(new IssueEventField("environment", getSystemFieldName("environment"), Issue::getEnvironment, false, "String"));
        fields.add(new IssueEventField("description", getSystemFieldName("description"), Issue::getDescription, false, "String"));
        fields.add(new IssueEventField("security", getSystemFieldName("security"), Issue::getSecurityLevelId, false, "Int64"));
        fields.add(new IssueEventField("labels", getSystemFieldName("labels"), (Issue issue) -> issue.getLabels().stream().map(Label::getLabel).collect(Collectors.toList()), false, "Array(String)"));

        // Пользовательские поля
        for (Field field : fieldManager.getAllSearchableFields()) {
            String id = field.getId();
            if (!fieldManager.isCustomFieldId(id)) {
                continue;
            }

            CustomIssueFieldType fieldType = CustomIssueFieldType.getTypeByKey(fieldManager.getCustomField(id).getCustomFieldType().getKey());
            if (fieldType == null) {
                logger.warn("Unknown custom field type for field {}", id);
                continue;
            }

            String name = field.getName();
            String dbType = fieldType.getClickhouseType();
            fields.add(new IssueEventField(id, name, (Issue event) -> getCustomFieldValue(id, fieldManager, event), false, dbType));

        }

        return fields;
    }

    private String getSystemFieldName(String id) {
        return fieldManager.getField(id).getName();
    }

    private static String getPriority(Issue issue) {
        if (issue.getPriority() == null) {
            return null;
        }

        return issue.getPriority().getName();
    }

    private static Object getCustomFieldValue(String id, FieldManager fieldManager, Issue issue) {
        if (!fieldManager.isCustomFieldId(id)) {
            return null;
        }

        return issue.getCustomFieldValue(fieldManager.getCustomField(id));
    }

    private static String getIssueType(Issue issue) {
        if (issue.getIssueType() == null) {
            return null;
        }

        return issue.getIssueType().getName();
    }

    private static String getResolution(Issue issue) {
        if (issue.getResolution() == null) {
            return null;
        }

        return issue.getResolution().getName();
    }
}

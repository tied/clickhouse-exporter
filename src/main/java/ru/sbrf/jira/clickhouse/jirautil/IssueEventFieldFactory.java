package ru.sbrf.jira.clickhouse.jirautil;

import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.event.issue.IssueEvent;
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

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
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
        fields.add(new IssueEventField("event_id", "Идентификатор события", (IssueEvent event) -> UUID.randomUUID(), true, "UUID"));
        fields.add(new IssueEventField("timestamp", "Время события", (IssueEvent event) -> new Timestamp(event.getTime().getTime()), true, "DateTime"));
        fields.add(new IssueEventField("issue_id", "Ключ Issue", (IssueEvent event) -> event.getIssue().getKey(), true, "String"));

        // Системные поля
        fields.add(new IssueEventField("project", getSystemFieldName("project"), (IssueEvent event) -> event.getProject().getKey(), false, "String"));
        fields.add(new IssueEventField("summary", getSystemFieldName("summary"), (IssueEvent event) -> event.getIssue().getSummary(), false, "String"));
        fields.add(new IssueEventField("issuetype", getSystemFieldName("issuetype"), IssueEventFieldFactory::getIssueType, false, "String"));
        fields.add(new IssueEventField("status", getSystemFieldName("status"), (IssueEvent event) -> event.getIssue().getStatus().getName(), false, "String"));
        fields.add(new IssueEventField("priority", getSystemFieldName("priority"), IssueEventFieldFactory::getPriority, false, "String"));
        fields.add(new IssueEventField("resolution", getSystemFieldName("resolution"), IssueEventFieldFactory::getResolution, false, "String"));
        fields.add(new IssueEventField("assignee", getSystemFieldName("assignee"), (IssueEvent event) -> event.getIssue().getAssigneeId(), false, "String"));
        fields.add(new IssueEventField("reporter", getSystemFieldName("reporter"), (IssueEvent event) -> event.getIssue().getReporterId(), false, "String"));
        fields.add(new IssueEventField("creator", getSystemFieldName("creator"), (IssueEvent event) -> event.getIssue().getCreatorId(), false, "String"));
        fields.add(new IssueEventField("created", getSystemFieldName("created"), (IssueEvent event) -> event.getIssue().getCreated(), false, "DateTime"));
        fields.add(new IssueEventField("updated", getSystemFieldName("updated"), (IssueEvent event) -> event.getIssue().getUpdated(), false, "DateTime"));
        fields.add(new IssueEventField("resolutiondate", getSystemFieldName("resolutiondate"), (IssueEvent event) -> event.getIssue().getResolutionDate(), false, "DateTime"));
        fields.add(new IssueEventField("versions", getSystemFieldName("versions"), (IssueEvent event) -> event.getIssue().getAffectedVersions().stream().map(Version::getName).collect(Collectors.toList()), false, "Array(String)"));
        fields.add(new IssueEventField("fixVersions", getSystemFieldName("fixVersions"), (IssueEvent event) -> event.getIssue().getFixVersions().stream().map(Version::getName).collect(Collectors.toList()), false, "Array(String)"));
        fields.add(new IssueEventField("components", getSystemFieldName("components"), (IssueEvent event) -> event.getIssue().getComponents().stream().map(ProjectComponent::getName).collect(Collectors.toList()), false, "Array(String)"));
        fields.add(new IssueEventField("duedate", getSystemFieldName("duedate"), (IssueEvent event) -> event.getIssue().getDueDate(), false, "DateTime"));
        fields.add(new IssueEventField("environment", getSystemFieldName("environment"), (IssueEvent event) -> event.getIssue().getEnvironment(), false, "String"));
        fields.add(new IssueEventField("description", getSystemFieldName("description"), (IssueEvent event) -> event.getIssue().getDescription(), false, "String"));
        fields.add(new IssueEventField("security", getSystemFieldName("security"), (IssueEvent event) -> event.getIssue().getSecurityLevelId(), false, "Int64"));
        fields.add(new IssueEventField("labels", getSystemFieldName("labels"), (IssueEvent event) -> event.getIssue().getLabels().stream().map(Label::getLabel).collect(Collectors.toList()), false, "Array(String)"));

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
            fields.add(new IssueEventField(id, name, (IssueEvent event) -> getCustomFieldValue(id, fieldManager, event), false, dbType));

        }

        return fields;
    }

    private String getSystemFieldName(String id) {
        return fieldManager.getField(id).getName();
    }

    private static String getPriority(IssueEvent event) {
        Issue issue = event.getIssue();
        if (issue.getPriority() == null) {
            return null;
        }

        return issue.getPriority().getName();
    }

    private static Object getCustomFieldValue(String id, FieldManager fieldManager, IssueEvent event) {
        if (!fieldManager.isCustomFieldId(id)) {
            return null;
        }

        Issue issue = event.getIssue();
        return issue.getCustomFieldValue(fieldManager.getCustomField(id));
    }

    private static String getIssueType(IssueEvent event) {
        Issue issue = event.getIssue();
        if (issue.getIssueType() == null) {
            return null;
        }

        return issue.getIssueType().getName();
    }

    private static String getResolution(IssueEvent event) {
        Issue issue = event.getIssue();
        if (issue.getResolution() == null) {
            return null;
        }

        return issue.getResolution().getName();
    }
}

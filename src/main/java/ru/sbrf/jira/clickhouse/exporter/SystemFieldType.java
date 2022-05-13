package ru.sbrf.jira.clickhouse.exporter;

import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.label.Label;
import com.atlassian.jira.project.version.Version;

import java.util.stream.Collectors;

public enum SystemFieldType {
    PROJECT("project", "Int64", Issue::getProjectId),
    SUMMARY("summary", "String", Issue::getSummary),
    ISSUE_TYPE("issuetype", "String", Issue::getIssueTypeId),
    STATUS("status", "String", Issue::getStatusId),
    PRIORITY("priority", "String", (Issue issue) -> issue.getPriority() != null ? issue.getPriority().getId() : null),
    RESOLUTION("resolution", "String", Issue::getResolutionId),
    ASSIGNEE("assignee", "String", Issue::getAssigneeId),
    REPORTER("reporter", "String", Issue::getReporterId),
    CREATOR("creator", "String", Issue::getCreatorId),
    CREATED("created", "DateTime", Issue::getCreated),
    UPDATED("updated", "DateTime", Issue::getUpdated),
    RESOLUTION_DATE("resolutiondate", "DateTime", Issue::getResolutionDate),
    VERSIONS("versions", "String", (Issue issue) -> issue.getAffectedVersions().stream().map((Version version) -> version.getId().toString()).collect(Collectors.joining(","))),
    FIX_VERSIONS("fixVersions", "String", (Issue issue) -> issue.getFixVersions().stream().map((Version version) -> version.getId().toString()).collect(Collectors.joining(","))),
    COMPONENTS("components", "String", (Issue issue) -> issue.getComponents().stream().map((ProjectComponent component) -> component.getId().toString()).collect(Collectors.joining(","))),
    DUE_DATE("duedate", "DateTime", Issue::getDueDate),
    ENVIRONMENT("environment", "String", Issue::getEnvironment),
    DESCRIPTION("description", "String", Issue::getDescription),
    SECURITY("security", "Int64", Issue::getSecurityLevelId),
    LABELS("labels", "String", (Issue issue) -> issue.getLabels().stream().map((Label label) -> label.getId().toString()).collect(Collectors.joining(",")));

    private final String key;
    private final String clickhouseType;
    private final IssueFieldGetter fieldGetter;

    SystemFieldType(final String key, String clickhouseType, IssueFieldGetter fieldGetter) {
        this.key = key;
        this.clickhouseType = clickhouseType;
        this.fieldGetter = fieldGetter;
    }

    public static SystemFieldType getTypeByKey(String key) {
        for (SystemFieldType type : values()) {
            if (type.key.equals(key)) {
                return type;
            }
        }

        return null;
    }

    public String getClickhouseType() {
        return clickhouseType;
    }

    public Object getFieldValue(Issue issue) {
        return fieldGetter.getValue(issue);
    }
}

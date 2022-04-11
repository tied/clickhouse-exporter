package ru.sbrf.jira.clickhouse.exporter;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.plugin.spring.scanner.annotation.imports.JiraImport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class IssueFieldManager {
    @JiraImport
    private final FieldManager fieldManager;

    @Autowired
    public IssueFieldManager(FieldManager fieldManager) {
        this.fieldManager = fieldManager;
    }

    public List<IssueField> getIssueFields() {
        return Arrays.asList(
                new IssueField("project", fieldManager.getField("project").getName(), Issue::getProjectId),
                new IssueField("summary", fieldManager.getField("summary").getName(), Issue::getSummary),
                new IssueField("issuetype", fieldManager.getField("issuetype").getName(), Issue::getIssueType),
                new IssueField("status", fieldManager.getField("status").getName(), Issue::getStatus),
                new IssueField("priority", fieldManager.getField("priority").getName(), Issue::getPriority),
                new IssueField("resolution", fieldManager.getField("resolution").getName(), Issue::getResolution),
                new IssueField("assignee", fieldManager.getField("assignee").getName(), Issue::getAssignee),
                new IssueField("reporter", fieldManager.getField("reporter").getName(), Issue::getReporter),
                new IssueField("creator", fieldManager.getField("creator").getName(), Issue::getCreator),
                new IssueField("created", fieldManager.getField("created").getName(), Issue::getCreated),
                new IssueField("updated", fieldManager.getField("updated").getName(), Issue::getUpdated),
                new IssueField("resolutiondate", fieldManager.getField("resolutiondate").getName(), Issue::getResolutionDate),
                new IssueField("duedate", fieldManager.getField("duedate").getName(), Issue::getDueDate),
                new IssueField("security", fieldManager.getField("security").getName(), Issue::getSecurityLevel)
                );
    }
}

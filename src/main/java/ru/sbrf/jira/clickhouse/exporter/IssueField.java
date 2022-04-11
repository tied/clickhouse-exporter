package ru.sbrf.jira.clickhouse.exporter;

import com.atlassian.jira.issue.Issue;

public class IssueField {
    private final String id;
    private final String name;
    private final IssueFieldGetter fieldGetter;

    public IssueField(String id, String name, IssueFieldGetter fieldGetter) {
        this.id = id;
        this.name = name;
        this.fieldGetter = fieldGetter;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Object getValue(Issue issue) {
        return fieldGetter.getValue(issue);
    }
}

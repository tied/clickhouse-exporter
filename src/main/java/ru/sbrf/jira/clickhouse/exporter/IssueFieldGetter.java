package ru.sbrf.jira.clickhouse.exporter;

import com.atlassian.jira.issue.Issue;

public interface IssueFieldGetter {
    Object getValue(Issue issue);
}

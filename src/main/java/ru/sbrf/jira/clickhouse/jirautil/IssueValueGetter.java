package ru.sbrf.jira.clickhouse.jirautil;

import com.atlassian.jira.issue.Issue;

public interface IssueValueGetter {
    Object getValue(Issue issue);
}

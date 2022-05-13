package ru.sbrf.jira.clickhouse.exporter;

public class IssueField {
    private final String id;
    private final String name;

    public IssueField(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}

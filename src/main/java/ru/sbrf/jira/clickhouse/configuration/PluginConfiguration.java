package ru.sbrf.jira.clickhouse.configuration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Класс для работы с конфигурацией плагина
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class PluginConfiguration {
    @XmlElement(name = "jdbc_url")
    private String dbUrl;
    @XmlElement(name = "events_table")
    private String eventsTableName;
    @XmlElement(name = "project_code")
    private String projectCode;
    @XmlElement(name = "issue_types")
    private List<String> issueTypes;
    @XmlElement(name = "issue_fields")
    private List<String> issueFields;

    public String getDbUrl() {
        return dbUrl;
    }

    public String getEventsTableName() {
        return eventsTableName;
    }

    public String getProjectCode() {
        return projectCode;
    }

    public List<String> getIssueTypes() {
        return issueTypes;
    }

    public List<String> getIssueFields() {
        return issueFields;
    }
}

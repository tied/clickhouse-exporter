package ru.sbrf.jira.clickhouse.jirautil;


/**
 * Класс, предстваляющий поле события
 */
public class IssueEventField {
    /**
     * Название поля в БД
     */
    private final String id;
    /**
     * Локализованное название поля
     */
    private final String name;
    /**
     * Метод для получения значения поля из события
     */
    private final IssueValueGetter valueGetter;
    /**
     * Можно ли выключить запись поля в базу
     */
    private final boolean isRequired;
    /**
     * Тип данных Clickhouse
     */
    private final String dbType;

    public IssueEventField(String id, String name, IssueValueGetter valueGetter, boolean isRequired, String dbType) {
        this.id = id;
        this.name = name;
        this.valueGetter = valueGetter;
        this.isRequired = isRequired;
        this.dbType = dbType;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public IssueValueGetter getValueGetter() {
        return valueGetter;
    }

    public boolean isRequired() {
        return isRequired;
    }

    public String getDbType() {
        return dbType;
    }
}

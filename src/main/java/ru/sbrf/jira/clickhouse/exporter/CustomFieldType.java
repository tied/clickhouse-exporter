package ru.sbrf.jira.clickhouse.exporter;

public enum CustomFieldType {
    CASCADING_SELECT("com.atlassian.jira.plugin.system.customfieldtypes:cascadingselect", "String"),
    DATE_PICKER("com.atlassian.jira.plugin.system.customfieldtypes:datepicker", "DateTime"),
    DATE_TIME("com.atlassian.jira.plugin.system.customfieldtypes:datetime", "DateTime"),
    FLOAT("com.atlassian.jira.plugin.system.customfieldtypes:float", "Float64"),
    GROUP_PICKER("com.atlassian.jira.plugin.system.customfieldtypes:grouppicker", "String"),
    IMPORT_ID("com.atlassian.jira.plugin.system.customfieldtypes:importid", "String"),
    LABELS("com.atlassian.jira.plugin.system.customfieldtypes:labels", "String"),
    MULTI_CHECKBOXES("com.atlassian.jira.plugin.system.customfieldtypes:multicheckboxes", "String"),
    MULTI_GROUP_PICKER("com.atlassian.jira.plugin.system.customfieldtypes:multigrouppicker", "String"),
    MULTI_SELECT("com.atlassian.jira.plugin.system.customfieldtypes:multiselect", "String"),
    MULTI_USER_PICKER("com.atlassian.jira.plugin.system.customfieldtypes:multiuserpicker", "String"),
    MULTI_VERSION("com.atlassian.jira.plugin.system.customfieldtypes:multiversion", "String"),
    PROJECT("com.atlassian.jira.plugin.system.customfieldtypes:project", "String"),
    RADIO_BUTTONS("com.atlassian.jira.plugin.system.customfieldtypes:radiobuttons", "String"),
    READ_ONLY_FIELD("com.atlassian.jira.plugin.system.customfieldtypes:readonlyfield", "String"),
    SELECT("com.atlassian.jira.plugin.system.customfieldtypes:select", "String"),
    TEXT_AREA("com.atlassian.jira.plugin.system.customfieldtypes:textarea", "String"),
    TEXT_FIELD("com.atlassian.jira.plugin.system.customfieldtypes:textfield", "String"),
    URL("com.atlassian.jira.plugin.system.customfieldtypes:url", "String"),
    USER_PICKER("com.atlassian.jira.plugin.system.customfieldtypes:userpicker", "String"),
    VERSION("com.atlassian.jira.plugin.system.customfieldtypes:version", "String");

    private final String key;
    private final String clickhouseType;

    CustomFieldType(final String key, String clickhouseType) {
        this.key = key;
        this.clickhouseType = clickhouseType;
    }

    public static CustomFieldType getTypeByKey(String key) {
        for (CustomFieldType type : values()) {
            if (type.key.equals(key)) {
                return type;
            }
        }

        return null;
    }

    public String getClickhouseType() {
        return clickhouseType;
    }
}

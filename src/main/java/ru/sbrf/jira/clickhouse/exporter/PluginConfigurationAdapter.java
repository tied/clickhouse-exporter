package ru.sbrf.jira.clickhouse.exporter;

import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PluginConfigurationAdapter {
    private final PluginSettings settings;
    private static final String configPrefix = "ru.sbrf.jira.clickhouse.exporter.";

    @Autowired
    public PluginConfigurationAdapter(@ComponentImport PluginSettingsFactory pluginSettingsFactory) {
        settings = pluginSettingsFactory.createGlobalSettings();
    }

    public Object getValue(String key) {
        return settings.get(configPrefix + key);
    }
}

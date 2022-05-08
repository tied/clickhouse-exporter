package ru.sbrf.jira.clickhouse.exporter;

import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import ru.yandex.clickhouse.ClickHouseDataSource;
import ru.yandex.clickhouse.settings.ClickHouseProperties;


@Component
public class ClickhouseDataSourceConfiguration {
    private final PluginSettingsFactory pluginSettingsFactory;
    private static final String configPrefix = "ru.sbrf.jira.clickhouse.exporter";

    @Autowired
    public ClickhouseDataSourceConfiguration(@ComponentImport PluginSettingsFactory pluginSettingsFactory) {
        this.pluginSettingsFactory = pluginSettingsFactory;
    }

    @Bean
    public ClickHouseDataSource getDataSource() {
        PluginSettings settings = pluginSettingsFactory.createGlobalSettings();

        Object url = settings.get(configPrefix + ".jdbc_url");
        if (url == null) {
            url = "";
        }

        ClickHouseProperties properties = new ClickHouseProperties();
        properties.setCompress(false);

        return new ClickHouseDataSource((String) url, properties);
    }
}

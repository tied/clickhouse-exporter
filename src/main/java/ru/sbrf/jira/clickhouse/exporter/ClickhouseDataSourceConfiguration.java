package ru.sbrf.jira.clickhouse.exporter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import ru.yandex.clickhouse.ClickHouseDataSource;
import ru.yandex.clickhouse.settings.ClickHouseProperties;


@Component
public class ClickhouseDataSourceConfiguration {
    private final PluginConfigurationAdapter configuration;

    @Autowired
    public ClickhouseDataSourceConfiguration(PluginConfigurationAdapter configuration) {
        this.configuration = configuration;
    }

    @Bean
    public ClickHouseDataSource getDataSource() {
        Object url = configuration.getValue("jdbc_url");
        if (url == null) {
            url = "";
        }

        ClickHouseProperties properties = new ClickHouseProperties();
        properties.setCompress(false);

        return new ClickHouseDataSource((String) url, properties);
    }
}

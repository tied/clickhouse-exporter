package ru.sbrf.jira.clickhouse.exporter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import ru.sbrf.jira.clickhouse.configuration.PluginConfigurationRepository;
import ru.yandex.clickhouse.ClickHouseDataSource;
import ru.yandex.clickhouse.settings.ClickHouseProperties;


@Component
public class ClickhouseDataSourceConfiguration {
    private final PluginConfigurationRepository configurationRepository;

    @Autowired
    public ClickhouseDataSourceConfiguration(PluginConfigurationRepository configurationRepository) {
        this.configurationRepository = configurationRepository;
    }

    @Bean
    public ClickHouseDataSource getDataSource() {
        String url = configurationRepository.get().getDbUrl();
        if (url == null) {
            url = "";
        }

        return new ClickHouseDataSource(url);
    }
}

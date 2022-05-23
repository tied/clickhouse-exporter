package ru.sbrf.jira.clickhouse.configuration;

import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.transaction.TransactionTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.xml.bind.annotation.XmlElement;
import java.lang.reflect.Field;


/**
 * Класс для получения и сохранения настроек плагина
 */
@Component
public class PluginConfigurationRepository {
    private final PluginSettingsFactory pluginSettingsFactory;
    private final TransactionTemplate transactionTemplate;
    private static final String configPrefix = "ru.sbrf.jira.clickhouse.exporter";
    private static final Logger logger = LoggerFactory.getLogger(PluginConfigurationRepository.class);

    @Autowired
    public PluginConfigurationRepository(@ComponentImport PluginSettingsFactory pluginSettingsFactory, @ComponentImport TransactionTemplate transactionTemplate) {
        this.pluginSettingsFactory = pluginSettingsFactory;
        this.transactionTemplate = transactionTemplate;
    }

    public PluginConfiguration get() {
        return transactionTemplate.execute(() -> {
            PluginSettings settings = pluginSettingsFactory.createGlobalSettings();

            PluginConfiguration configuration = new PluginConfiguration();

            for (Field field : configuration.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                try {
                    field.set(configuration, settings.get(configPrefix + "." + field.getAnnotation(XmlElement.class).name()));
                } catch (IllegalAccessException e) {
                    logger.error("Configuration read error: {}", e.getMessage());
                }
            }

            return configuration;
        });
    }

    void save(PluginConfiguration configuration) {
        transactionTemplate.execute(() -> {
            PluginSettings settings = pluginSettingsFactory.createGlobalSettings();

            for (Field field : configuration.getClass().getDeclaredFields()) {
                field.setAccessible(true);

                try {
                    settings.put(configPrefix + "." + field.getAnnotation(XmlElement.class).name(), field.get(configuration));
                } catch (IllegalAccessException e) {
                    logger.error("Configuration write error: {}", e.getMessage());
                }
            }

            return null;
        });
    }
}

package ru.sbrf.jira.clickhouse.adminui;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;

import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.transaction.TransactionTemplate;
import com.atlassian.sal.api.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Path("/")
public class PluginConfiguration {
    private final UserManager userManager;
    private final PluginSettingsFactory pluginSettingsFactory;
    private final TransactionTemplate transactionTemplate;

    private static final String configPrefix = "ru.sbrf.jira.clickhouse.exporter";

    @Autowired
    public PluginConfiguration(@ComponentImport UserManager userManager, @ComponentImport PluginSettingsFactory pluginSettingsFactory,
                               @ComponentImport TransactionTemplate transactionTemplate) {
        this.userManager = userManager;
        this.pluginSettingsFactory = pluginSettingsFactory;
        this.transactionTemplate = transactionTemplate;
    }

    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    public static final class Config {
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

        public void setDbUrl(String dbUrl) {
            this.dbUrl = dbUrl;
        }

        public String getProjectCode() {
            return projectCode;
        }

        public void setProjectCode(String projectCode) {
            this.projectCode = projectCode;
        }

        public List<String> getIssueTypes() {
            return issueTypes;
        }

        public void setIssueTypes(List<String> issueTypes) {
            this.issueTypes = issueTypes;
        }

        public List<String> getIssueFields() {
            return issueFields;
        }

        public void setIssueFields(List<String> issueFields) {
            this.issueFields = issueFields;
        }

        public String getEventsTableName() {
            return eventsTableName;
        }

        public void setEventsTableName(String eventsTableName) {
            this.eventsTableName = eventsTableName;
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(@Context HttpServletRequest request) {
        String username = userManager.getRemoteUsername(request);
        if (username == null || !userManager.isSystemAdmin(username)) {
            return Response.status(Status.UNAUTHORIZED).build();
        }

        return Response.ok(transactionTemplate.execute(() -> {
            PluginSettings settings = pluginSettingsFactory.createGlobalSettings();
            Config config = new Config();

            config.setDbUrl((String) settings.get(configPrefix + ".jdbc_url"));
            config.setEventsTableName((String) settings.get(configPrefix + ".events_table"));
            config.setProjectCode((String) settings.get(configPrefix + ".project_code"));
            config.setIssueTypes((List) settings.get(configPrefix + ".issue_types"));
            config.setIssueFields((List) settings.get(configPrefix + ".issue_fields"));

            return config;
        })).build();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public Response put(final Config config, @Context HttpServletRequest request) {
        String username = userManager.getRemoteUsername(request);
        if (username == null || !userManager.isSystemAdmin(username)) {
            return Response.status(Status.UNAUTHORIZED).build();
        }

        transactionTemplate.execute(() -> {
            PluginSettings pluginSettings = pluginSettingsFactory.createGlobalSettings();
            pluginSettings.put(configPrefix + ".jdbc_url", config.getDbUrl());
            pluginSettings.put(configPrefix + ".events_table", config.getEventsTableName());
            pluginSettings.put(configPrefix + ".project_code", config.getProjectCode());
            pluginSettings.put(configPrefix + ".issue_types", config.getIssueTypes());
            pluginSettings.put(configPrefix + ".issue_fields", config.getIssueFields());
            return null;
        });

        return Response.noContent().build();
    }
}
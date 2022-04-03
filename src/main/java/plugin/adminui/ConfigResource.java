package plugin.adminui;

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

import javax.inject.Inject;

import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.transaction.TransactionCallback;
import com.atlassian.sal.api.transaction.TransactionTemplate;
import com.atlassian.sal.api.user.UserManager;

@Path("/")
public class ConfigResource {
    @ComponentImport
    private final UserManager userManager;
    @ComponentImport
    private final PluginSettingsFactory pluginSettingsFactory;
    @ComponentImport
    private final TransactionTemplate transactionTemplate;

    private static final String configPrefix = "ru.sbrf.jira.clickhouse.exporter";

    @Inject
    public ConfigResource(UserManager userManager, PluginSettingsFactory pluginSettingsFactory,
                          TransactionTemplate transactionTemplate) {
        this.userManager = userManager;
        this.pluginSettingsFactory = pluginSettingsFactory;
        this.transactionTemplate = transactionTemplate;
    }

    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    public static final class Config {
        @XmlElement(name="jdbc_url")
        private String dbUrl;
        @XmlElement(name="project_code")
        private String projectCode;

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
            config.setProjectCode((String) settings.get(configPrefix + ".project_code"));

            return config;
        })).build();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public Response put(final Config config, @Context HttpServletRequest request)
    {
        String username = userManager.getRemoteUsername(request);
        if (username == null || !userManager.isSystemAdmin(username))
        {
            return Response.status(Status.UNAUTHORIZED).build();
        }

        transactionTemplate.execute(() -> {
            PluginSettings pluginSettings = pluginSettingsFactory.createGlobalSettings();
            pluginSettings.put(configPrefix + ".jdbc_url", config.getDbUrl());
            pluginSettings.put(configPrefix + ".project_code", config.getProjectCode());
            return null;
        });

        return Response.noContent().build();
    }
}
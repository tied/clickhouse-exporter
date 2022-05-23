package ru.sbrf.jira.clickhouse.configuration;

import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.user.UserKey;
import com.atlassian.sal.api.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * REST сервис для получения и сохранения конфигурации плагина
 */
@Component
@Path("/")
public class PluginConfigurationRestService {
    private final UserManager userManager;
    private final PluginConfigurationRepository configurationRepository;

    @Autowired
    public PluginConfigurationRestService(@ComponentImport UserManager userManager, PluginConfigurationRepository configurationRepository) {
        this.userManager = userManager;
        this.configurationRepository = configurationRepository;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(@Context HttpServletRequest request) {
        UserKey userKey = userManager.getRemoteUserKey();
        if (userKey == null || !userManager.isSystemAdmin(userKey)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        return Response.ok(configurationRepository.get()).build();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public Response put(PluginConfiguration configuration, @Context HttpServletRequest request) {
        UserKey userKey = userManager.getRemoteUserKey();
        if (userKey == null || !userManager.isSystemAdmin(userKey)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        configurationRepository.save(configuration);

        return Response.noContent().build();
    }
}

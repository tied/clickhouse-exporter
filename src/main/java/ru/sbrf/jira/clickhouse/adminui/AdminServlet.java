package ru.sbrf.jira.clickhouse.adminui;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.templaterenderer.TemplateRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.sbrf.jira.clickhouse.jirautil.IssueEventFieldFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.util.*;

@Component
public class AdminServlet extends HttpServlet {
    private final UserManager userManager;
    private final LoginUriProvider loginUriProvider;
    private final TemplateRenderer renderer;
    private final ProjectManager projectManager;
    private final ConstantsManager constantsManager;
    private final IssueEventFieldFactory fieldFactory;

    @Autowired
    public AdminServlet(@ComponentImport UserManager userManager, @ComponentImport LoginUriProvider loginUriProvider, @ComponentImport TemplateRenderer renderer, @ComponentImport ProjectManager projectManager, @ComponentImport ConstantsManager constantsManager, IssueEventFieldFactory fieldFactory) {
        this.userManager = userManager;
        this.loginUriProvider = loginUriProvider;
        this.renderer = renderer;
        this.projectManager = projectManager;
        this.constantsManager = constantsManager;
        this.fieldFactory = fieldFactory;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String username = userManager.getRemoteUsername(request);
        if (username == null || !userManager.isSystemAdmin(username)) {
            redirectToLogin(request, response);
            return;
        }

        Map<String, Object> context = new HashMap<>();

        List<Project> projects = projectManager.getProjects();
        context.put("projects", projects);

        Collection<IssueType> issueTypes = constantsManager.getAllIssueTypeObjects();
        context.put("issue_types", issueTypes);
        context.put("issue_fields", fieldFactory.getFields());

        response.setContentType("text/html;charset=utf-8");
        renderer.render("admin.vm", context, response.getWriter());
    }

    private void redirectToLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.sendRedirect(loginUriProvider.getLoginUri(getUri(request)).toASCIIString());
    }

    private URI getUri(HttpServletRequest request) {
        StringBuffer builder = request.getRequestURL();
        if (request.getQueryString() != null) {
            builder.append("?");
            builder.append(request.getQueryString());
        }
        return URI.create(builder.toString());
    }
}

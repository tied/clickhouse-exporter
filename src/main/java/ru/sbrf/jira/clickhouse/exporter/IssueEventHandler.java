package ru.sbrf.jira.clickhouse.exporter;

import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.issue.Issue;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.sbrf.jira.clickhouse.configuration.PluginConfiguration;
import ru.sbrf.jira.clickhouse.configuration.PluginConfigurationRepository;

import java.util.Collection;
import java.util.List;


@Component
public class IssueEventHandler implements InitializingBean, DisposableBean {
    private final EventPublisher eventPublisher;
    private final IssueRepository issueRepository;
    private final PluginConfigurationRepository configurationRepository;

    @Autowired
    public IssueEventHandler(@ComponentImport EventPublisher eventPublisher, IssueRepository issueRepository, PluginConfigurationRepository configurationRepository) {
        this.eventPublisher = eventPublisher;
        this.issueRepository = issueRepository;
        this.configurationRepository = configurationRepository;
    }

    @Override
    public void afterPropertiesSet() {
        eventPublisher.register(this);
        issueRepository.prepareTable();
    }

    @Override
    public void destroy() {
        eventPublisher.unregister(this);
    }

    @EventListener
    public void onIssueEvent(IssueEvent issueEvent) {
        PluginConfiguration configuration = configurationRepository.get();
        List<String> allowedTypes = configuration.getIssueTypes();
        String allowedProject = configuration.getProjectCode();
        Issue issue = issueEvent.getIssue();

        if (!issue.getProjectId().toString().equals(allowedProject)) {
            return;
        }

        if (!allowedTypes.contains(issue.getIssueTypeId())) {
            return;
        }

        issueRepository.addEventData(issueEvent);
    }

}
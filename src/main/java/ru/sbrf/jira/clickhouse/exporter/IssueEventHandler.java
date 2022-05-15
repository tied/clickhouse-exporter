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

import java.util.Collection;


@Component
public class IssueEventHandler implements InitializingBean, DisposableBean {
    private final EventPublisher eventPublisher;
    private final IssueRepository issueRepository;
    private final PluginConfigurationAdapter configuration;

    @Autowired
    public IssueEventHandler(@ComponentImport EventPublisher eventPublisher, IssueRepository issueRepository, PluginConfigurationAdapter configuration) {
        this.eventPublisher = eventPublisher;
        this.issueRepository = issueRepository;
        this.configuration = configuration;
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
        Collection<String> allowedTypes = (Collection<String>) configuration.getValue("issue_types");
        Long eventTypeId = issueEvent.getEventTypeId();
        Issue issue = issueEvent.getIssue();

        if (!allowedTypes.contains(issue.getIssueTypeId())) {
            return;
        }

        issueRepository.addEventData(issueEvent.getTime(), issue);
    }

}
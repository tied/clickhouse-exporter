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

import java.util.ArrayList;
import java.util.List;

@Component
public class IssueEventHandler implements InitializingBean, DisposableBean {
    private final EventPublisher eventPublisher;
    private final IssueRepository issueRepository;

    @Autowired
    public IssueEventHandler(@ComponentImport EventPublisher eventPublisher, IssueRepository issueRepository) {
        this.eventPublisher = eventPublisher;
        this.issueRepository = issueRepository;
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
        Long eventTypeId = issueEvent.getEventTypeId();
        Issue issue = issueEvent.getIssue();

    }

}
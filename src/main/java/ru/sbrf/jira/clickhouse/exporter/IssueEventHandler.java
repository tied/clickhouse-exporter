package ru.sbrf.jira.clickhouse.exporter;

import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.issue.Issue;
import com.atlassian.plugin.spring.scanner.annotation.imports.JiraImport;
//import com.clickhouse.jdbc.ClickHouseDataSource;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class IssueEventHandler implements InitializingBean, DisposableBean {
    @JiraImport
    private final EventPublisher eventPublisher;

    @Autowired
    public IssueEventHandler(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void afterPropertiesSet() {
        eventPublisher.register(this);
    }

    @Override
    public void destroy() {
        eventPublisher.unregister(this);
    }

    @EventListener
    public void onIssueEvent(IssueEvent issueEvent) {
        Long eventTypeId = issueEvent.getEventTypeId();
        Issue issue = issueEvent.getIssue();


        /*String url = String.format("jdbc:clickhouse://%s:%d/default", System.getProperty("chHost", "localhost"),
                Integer.parseInt(System.getProperty("chPort", "8123")));

        ClickHouseProperties properties = new ClickHouseProperties();
        properties.setCompress(false);

        ClickHouseDataSource dataSource = new ClickHouseDataSource(url, properties);

        try (ClickHouseConnection conn = dataSource.getConnection();
             ClickHouseStatement stmt = conn.createStatement()) {
            stmt.execute(String.format("insert into jira_events values ('%s', '%s', '%s', '%s')", issueEvent.getTime().toString(), issueEvent.getUser().getName().toString(), issueEvent.getProject().getName().toString(), issue.getKey().toString()));
            System.out.println("wrote to db");
        } catch (SQLException e) {
            e.printStackTrace();
        }*/
    }

}
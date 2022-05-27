package ru.sbrf.jira.clickhouse.exporter;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.changehistory.ChangeHistory;
import com.atlassian.jira.issue.changehistory.ChangeHistoryManager;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.sbrf.jira.clickhouse.configuration.PluginConfiguration;
import ru.sbrf.jira.clickhouse.configuration.PluginConfigurationRepository;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.*;

@Component
public class OldEventsExporter implements InitializingBean {
    private final ChangeHistoryManager changeHistoryManager;
    private final IssueManager issueManager;
    private final PluginConfigurationRepository configurationRepository;
    private final IssueRepository issueRepository;
    private final EventDataConverter eventDataConverter;

    @Autowired
    public OldEventsExporter(@ComponentImport ChangeHistoryManager changeHistoryManager, PluginConfigurationRepository configurationRepository, @ComponentImport IssueManager issueManager, IssueRepository issueRepository, EventDataConverter eventDataConverter) {
        this.changeHistoryManager = changeHistoryManager;
        this.configurationRepository = configurationRepository;
        this.issueManager = issueManager;
        this.issueRepository = issueRepository;
        this.eventDataConverter = eventDataConverter;
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        exportOldEvents();
    }

    private void exportOldEvents() throws GenericEntityException {
        PluginConfiguration configuration = configurationRepository.get();
        Long projectCode = Long.valueOf(configuration.getProjectCode());
        for (Long issueId : issueManager.getIssueIdsForProject(projectCode)) {
            Issue issue = issueManager.getIssueObject(issueId);

            Date since = Date.valueOf(configurationRepository.get().getOldDate());
            Timestamp earliest = issueRepository.getEarliestEditDateForIssue(issue.getKey());
            if (earliest == null) {
                earliest = new Timestamp(System.currentTimeMillis());
            }

            SortedSet<ChangeHistory> appliedHistories = new TreeSet<>(Comparator.comparing(ChangeHistory::getTimePerformed).reversed());

            for (ChangeHistory changeHistory : changeHistoryManager.getChangeHistoriesSince(issue, since)) {
                Timestamp current = changeHistory.getTimePerformed();
                if (current.after(since)) {
                    appliedHistories.add(changeHistory);
                }
            }

            Map<String, Object> issueData = eventDataConverter.convert(issue);

            for (ChangeHistory changeHistory : appliedHistories) {
                Timestamp current = changeHistory.getTimePerformed();
                if (current.before(earliest)) {
                    issueData.put("event_id", UUID.randomUUID());
                    issueData.put("timestamp", current);
                    issueRepository.addEventData(issueData);
                }

                for (GenericValue changeItem : changeHistory.getChangeItems()) {
                    String field = (String) changeItem.get("field");
                    String oldString = (String) changeItem.get("oldstring");

                    issueData.put(field, oldString);
                }
            }

            Timestamp created = issue.getCreated();
            if (created.before(earliest)) {
                issueData.put("event_id", UUID.randomUUID());
                issueData.put("timestamp", created);
                issueRepository.addEventData(issueData);
            }
        }
    }
}

package ru.sbrf.jira.clickhouse.exporter;

import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.issue.Issue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.sbrf.jira.clickhouse.jirautil.IssueEventField;
import ru.sbrf.jira.clickhouse.jirautil.IssueEventFieldFactory;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class EventDataConverter {
    private final IssueEventFieldFactory fieldFactory;

    @Autowired
    public EventDataConverter(IssueEventFieldFactory fieldFactory) {
        this.fieldFactory = fieldFactory;
    }

    public Map<String, Object> convert(IssueEvent issueEvent) {
        Issue issue = issueEvent.getIssue();
        Map<String, Object> data = convert(issue);
        data.put("event_id", UUID.randomUUID());
        data.put("timestamp", new Timestamp(issueEvent.getTime().getTime()));
        return data;
    }

    public Map<String, Object> convert(Issue issue) {
        Map<String, Object> data = new HashMap<>();

        List<IssueEventField> fields = fieldFactory.getFields();

        for (IssueEventField field : fields) {
            data.put(field.getId(), field.getValueGetter().getValue(issue));
        }

        return data;
    }
}

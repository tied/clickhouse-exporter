package ru.sbrf.jira.clickhouse.exporter;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DataBaseInitializer implements InitializingBean {
    private final IssueRepository issueRepository;

    @Autowired
    public DataBaseInitializer(IssueRepository issueRepository) {
        this.issueRepository = issueRepository;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        issueRepository.prepareTable();
    }
}

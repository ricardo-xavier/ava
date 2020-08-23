package com.example.tutorial.plugins;

import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.issue.Issue;
import com.atlassian.plugin.spring.scanner.annotation.imports.JiraImport;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.worklog.Worklog;
import com.atlassian.jira.issue.worklog.WorklogManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.security.JiraAuthenticationContext;

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Date;
import java.util.List;

@Component
public class PluginAvanco implements InitializingBean, DisposableBean {

    @JiraImport
    private final EventPublisher eventPublisher;

    @Autowired
    public PluginAvanco(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    /**
     * Called when the plugin has been enabled.
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        eventPublisher.register(this);
    }

    /** hhj9677
     * Called when the plugin is being disabled or removed.
     * @throws Exception
     */
    @Override
    public void destroy() throws Exception {
        eventPublisher.unregister(this);
    }

    @EventListener
    public void onIssueEvent(IssueEvent issueEvent) {

		try {

		PrintStream stream = new PrintStream(new FileOutputStream("avanco.log", true));
		stream.println("====================evento recebido " + new Date());

        Long eventTypeId = issueEvent.getEventTypeId();
        stream.println("type: " + eventTypeId);

        Issue issue = issueEvent.getIssue();
        stream.println("getAssigneeId : " + issue.getAssigneeId());
        stream.println("getDescription: " + issue.getDescription());

		if (issue.getStatus() != null) {
        	stream.println("status: " + issue.getStatus().getName());
        	stream.println("status: " + issue.getStatus().getDescription());
        	stream.println("status: " + issue.getStatus().getSequence());
		}

		JiraAuthenticationContext jiraAuthenticationContext = ComponentAccessor.getJiraAuthenticationContext();
		com.atlassian.jira.user.ApplicationUser user = jiraAuthenticationContext.getUser();
        stream.println("user: " + user.getName());

		WorklogManager worklogManager = ComponentAccessor.getComponent(WorklogManager.class);
		List<Worklog> workLogs = worklogManager.getByIssue(issue);

		if (workLogs != null) {
        	stream.println("numlogs: " + workLogs.size());
			for (Worklog work : workLogs) {
        		stream.println("autor : " + work.getAuthor());
        		stream.println("inicio: " + work.getStartDate());
        		stream.println("tempo : " + work.getTimeSpent());
        		stream.println("tipo  : " + work.getClass().getName());
			}
			//worklogManager.create(user, work, (long) 1800, false);
		}

		/*
        if (eventTypeId.equals(EventType.ISSUE_CREATED_ID)) {
            log.info("Issue {} has been created at {}.", issue.getKey(), issue.getCreated());
        } else if (eventTypeId.equals(EventType.ISSUE_RESOLVED_ID)) {
            log.info("Issue {} has been resolved at {}.", issue.getKey(), issue.getResolutionDate());
        } else if (eventTypeId.equals(EventType.ISSUE_CLOSED_ID)) {
            log.info("Issue {} has been closed at {}.", issue.getKey(), issue.getUpdated());
        }
		*/

		stream.println();
		stream.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

    }

}

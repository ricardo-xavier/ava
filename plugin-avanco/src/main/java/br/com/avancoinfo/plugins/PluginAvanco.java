package com.example.tutorial.plugins;

import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.plugin.spring.scanner.annotation.imports.JiraImport;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.issue.worklog.Worklog;
import com.atlassian.jira.issue.worklog.WorklogManager;
import com.atlassian.jira.issue.worklog.WorklogImpl;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.security.JiraAuthenticationContext;
import org.ofbiz.core.entity.GenericValue;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintStream;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.atlassian.jira.bc.issue.worklog.WorklogService;
import com.atlassian.jira.bc.issue.worklog.WorklogResult;
import com.atlassian.jira.bc.issue.worklog.WorklogInputParametersImpl;
import com.atlassian.jira.bc.issue.worklog.WorklogNewEstimateInputParameters;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;

@Component
public class PluginAvanco implements InitializingBean, DisposableBean {

    @JiraImport
    private final EventPublisher eventPublisher;
    private String usuarios="*";
    private String listaStatus="*";

    @Autowired
    public PluginAvanco(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
        PrintStream log = null;
        try {
            log = new PrintStream(new FileOutputStream("avanco.log", true));
            log.println("====================PluginAvanco v1.0 " + new Date());
            log.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Called when the plugin has been enabled.
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {

        eventPublisher.register(this);
        PrintStream log = null;

        try {

            log = new PrintStream(new FileOutputStream("avanco.log", true));
            log.println("====================afterPropertiesSet " + new Date());

            // carrega a configuracao
            BufferedReader cfg = new BufferedReader(new FileReader("/u/atlassian/jira/plugin-avanco.cfg"));
            String linha;
            while ((linha = cfg.readLine()) != null) {
                if (linha.startsWith("usuarios=")) {
                    usuarios = linha.substring(9) + ":";
                }
                if (linha.startsWith("status=")) {
                    listaStatus = linha.substring(7) + ":";
                }
            }
            cfg.close();
            log.println("usuarios: " + usuarios);

        } catch (Exception e) {
            if (log != null) {
                e.printStackTrace(log);
            }
            e.printStackTrace();

        } finally {
            if (log != null) {
                log.close();
            }
        }
    }

    /** hhj9677
     * Called when the plugin is being disabled or removed.
     * @throws Exception
     */
    @Override
    public void destroy() throws Exception {
        eventPublisher.unregister(this);
        try {
            PrintStream log = new PrintStream(new FileOutputStream("avanco.log", true));
            log.println("====================destroy " + new Date());
            log.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

	private WorklogNewEstimateInputParameters createParams(MutableIssue issue, String timeSpent, Date StartDate) {
		return WorklogInputParametersImpl.issue(issue).startDate(StartDate).timeSpent(timeSpent)
				.comment("Tempo iniciado pelo plugin avanço: " + timeSpent + ".").buildNewEstimate();
	}

	private WorklogNewEstimateInputParameters updateParams(MutableIssue issue, String timeSpent, Date StartDate, Long worklogId) {
		return WorklogInputParametersImpl.issue(issue).worklogId(worklogId).startDate(StartDate).timeSpent(timeSpent)
				.comment("Tempo registrado pelo plugin avanço: " + timeSpent + ".").buildNewEstimate();
	}

    @EventListener
    public void onIssueEvent(IssueEvent issueEvent) {

        PrintStream log = null;

        try {

            log = new PrintStream(new FileOutputStream("avanco.log", true));
            log.println("====================evento recebido " + new Date());

            Long eventTypeId = issueEvent.getEventTypeId();
            log.println("type: " + eventTypeId);

            if ((eventTypeId == EventType.ISSUE_WORKLOGGED_ID)
                || (eventTypeId == EventType.ISSUE_WORKSTARTED_ID)
                || (eventTypeId == EventType.ISSUE_WORKSTOPPED_ID)
                || (eventTypeId == EventType.ISSUE_COMMENT_EDITED_ID)
                || (eventTypeId == EventType.ISSUE_WORKLOG_UPDATED_ID)
                || (eventTypeId == EventType.ISSUE_WORKLOG_DELETED_ID)
                || (eventTypeId == EventType.ISSUE_COMMENT_DELETED_ID)) {
                log.close();
                return;
            }

            Issue issue = issueEvent.getIssue();
            log.println("getId         : " + issue.getId());
            log.println("getKey        : " + issue.getKey());
            log.println("getNumber     : " + issue.getNumber());
            log.println("getAssigneeId : " + issue.getAssigneeId());

            Status status = issue.getStatusObject();
            if (status == null) {
                log.close();
                return;
            }
            log.println("status Id  : " + status.getId());
            log.println("status Name: " + status.getName());
            log.println("status Desc: " + status.getDescription());

            JiraAuthenticationContext jiraAuthenticationContext = ComponentAccessor.getJiraAuthenticationContext();
            com.atlassian.jira.user.ApplicationUser user = null;
            if (jiraAuthenticationContext != null) {
                user = jiraAuthenticationContext.getUser();
                if (user == null) {
                    log.close();
                    return;
                }
                if (!user.getName().equals(issue.getAssigneeId())) {
                    log.close();
                    return;
                }
            }

            IssueManager issueManager = ComponentAccessor.getComponent(IssueManager.class);
            if (issueManager == null) {
                log.close();
                return;
            }

            WorklogManager worklogManager = ComponentAccessor.getComponent(WorklogManager.class);
            if (worklogManager == null) {
                log.close();
                return;
            }

            //TODO testar tipo de evento

            Worklog alterar = null;
            List<Worklog> workLogs = worklogManager.getByIssue(issue);
            if (workLogs != null) {
                for (Worklog work : workLogs) {
                    if ((work.getComment() != null) && (work.getComment().startsWith("Tempo iniciado"))
                        && (work.getTimeSpent() == 60)) {
                        alterar = work;
                    }
                    log.println("workId: " + work.getId());
                    log.println("autor : " + work.getAuthor());
                    log.println("key   : " + work.getAuthorKey());
                    log.println("inicio: " + work.getStartDate());
                    log.println("tempo : " + work.getTimeSpent());
                    log.println("group : " + work.getGroupLevel());
                    log.println("level : " + work.getRoleLevelId());
                    log.println("tipo  : " + work.getClass().getName());
                }
            }
            
            if (!usuarios.equals("*") && !usuarios.contains(issue.getAssigneeId()+":")) {
                log.close();
                return;
            }

            WorklogService worklogService = (WorklogService) ComponentAccessor.getComponent(WorklogService.class);
            if (worklogService == null) {
                log.close();
                return;
            }

		    JiraServiceContext context = new JiraServiceContextImpl(user);

            if (alterar == null) {
		        WorklogNewEstimateInputParameters params = createParams((MutableIssue) issue, "1m", new Date());
                WorklogResult result = worklogService.validateCreate(context, params);
		        Worklog wl = worklogService.createAndAutoAdjustRemainingEstimate(context, result, true);
                worklogManager.create(user, wl, null, false);
            } else {
                Date inicio = alterar.getStartDate();
                long dif = new Date().getTime() - inicio.getTime();
                long difMin = dif / 1000 / 60;
		        WorklogNewEstimateInputParameters params = updateParams((MutableIssue) issue, difMin + "m", inicio, alterar.getId());
                WorklogResult result = worklogService.validateUpdate(context, params);
		        Worklog wl = worklogService.updateAndAutoAdjustRemainingEstimate(context, result, true);
                worklogManager.update(user, wl, null, false);
            }

            log.println();

        } catch (Exception e) {
            if (log != null) {
                e.printStackTrace(log);
            }
            e.printStackTrace();

        } finally {
            if (log != null) {
                log.close();
            }
        }
    }

}
/*
		    System.out.println(EventType.ISSUE_CREATED_ID); // 1 
		    System.out.println(EventType.ISSUE_UPDATED_ID); // 2
		    System.out.println(EventType.ISSUE_ASSIGNED_ID);  // 3
		    System.out.println(EventType.ISSUE_RESOLVED_ID); // 4
		    System.out.println(EventType.ISSUE_CLOSED_ID); // 5
		    System.out.println(EventType.ISSUE_COMMENTED_ID); // 6
		    System.out.println(EventType.ISSUE_REOPENED_ID); // 7
		    System.out.println(EventType.ISSUE_DELETED_ID); // 8
		    System.out.println(EventType.ISSUE_MOVED_ID); // 9
		    System.out.println(EventType.ISSUE_WORKLOGGED_ID); // 10
		    System.out.println(EventType.ISSUE_WORKSTARTED_ID); // 11
		    System.out.println(EventType.ISSUE_WORKSTOPPED_ID); // 12
		    System.out.println(EventType.ISSUE_GENERICEVENT_ID); // 13
		    System.out.println(EventType.ISSUE_COMMENT_EDITED_ID); // 14
		    System.out.println(EventType.ISSUE_WORKLOG_UPDATED_ID); // 15
		    System.out.println(EventType.ISSUE_WORKLOG_DELETED_ID); // 16
		    System.out.println(EventType.ISSUE_COMMENT_DELETED_ID); // 17

            1       Aberta
            3       Execução
            4       Detalhar Atividades
            10010   Done
            10011   In Progress
            10012   To Do
            10013   Backlog
            10014   Ready To Development
            10017   Aguardando Cliente
            10115   Testing
            10117   BLOCKED
            10213   Ready to Test
            10314   In Analysis
            10316   Bloqueado
            10318   Resolved
            10513   Suporte
            10613   Aguardando Liberar Versão
            11113   Análise
            11213   Code review
            11413   Comercial
            12013   A analisar
            12014   Planejamento Teste
            12016   A Testar
            12017   Em Teste
            12018   Em Desenvolvimento
            12019   A Fazer Desenvolvimento
            12113   Planejamento

*/

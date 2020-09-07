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
    private Configuracao cfg;

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

            cfg = new Configuracao();
            cfg.carrega(log);

        } catch (Exception e) {
            if (log != null) {
                e.printStackTrace(log);
            }

        } finally {
            if (log != null) {
                log.close();
            }
        }
    }

    /** 
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
				.comment(cfg.getMensagemInicio()).buildNewEstimate();
	}

	private WorklogNewEstimateInputParameters updateParams(MutableIssue issue, String timeSpent, Date StartDate, Long worklogId) {
		return WorklogInputParametersImpl.issue(issue).worklogId(worklogId).startDate(StartDate).timeSpent(timeSpent)
				.comment(cfg.getMensagemFim()).buildNewEstimate();
	}

    @EventListener
    public void onIssueEvent(IssueEvent issueEvent) {

        PrintStream log = null;

        try {

            log = new PrintStream(new FileOutputStream("avanco.log", true));
            log.println("====================evento recebido " + new Date());

            Long eventTypeId = issueEvent.getEventTypeId();

            Issue issue = issueEvent.getIssue();
            if (issue == null) {
                log.close();
                return;
            }

            Status status = issue.getStatusObject();
            if (status == null) {
                log.close();
                return;
            }

            log.printf("evento=%d %s issue=%s status=%s %s usuario=%s%n",
                eventTypeId, Evento.toString(eventTypeId), issue.getKey(),
                status.getId(), status.getName(), issue.getAssigneeId());
            
            // verifica se o usuario esta configurado para o plugin
            if (!cfg.getListaUsuarios().equals("*") && !cfg.getListaUsuarios().contains(issue.getAssigneeId()+":")) {
                log.println("Usuario nao configurado: " + issue.getAssigneeId());
                log.close();
                return;
            }

            // verifica se o usuario logado eh responsavel pela issue
            JiraAuthenticationContext jiraAuthenticationContext = ComponentAccessor.getJiraAuthenticationContext();
            com.atlassian.jira.user.ApplicationUser user = null;
            if (jiraAuthenticationContext != null) {
                user = jiraAuthenticationContext.getUser();
                if (user == null) {
                    log.println("Erro getUser");
                    log.close();
                    return;
                }
                if (!user.getName().equals(issue.getAssigneeId())) {
                    log.printf("Usuario logado %s diferente do responsavel %s%n", user.getName(), issue.getAssigneeId());
                    log.close();
                    return;
                }
            }

            IssueManager issueManager = ComponentAccessor.getComponent(IssueManager.class);
            if (issueManager == null) {
                log.println("Erro IssueManager");
                log.close();
                return;
            }

            WorklogManager worklogManager = ComponentAccessor.getComponent(WorklogManager.class);
            if (worklogManager == null) {
                log.println("Erro WorklogManager");
                log.close();
                return;
            }

            WorklogService worklogService = (WorklogService) ComponentAccessor.getComponent(WorklogService.class);
            if (worklogService == null) {
                log.println("Erro WorklogService");
                log.close();
                return;
            }

		    JiraServiceContext context = new JiraServiceContextImpl(user);
            if (context == null) {
                log.println("Erro JiraServiceContext");
                log.close();
                return;
            }

            // verifica se tem algum registro iniciado pelo plugin na issue
            Worklog registroIniciado = null;
            List<Worklog> workLogs = worklogManager.getByIssue(issue);
            if (workLogs != null) {
                for (Worklog work : workLogs) {
                    if ((work.getComment() != null) && (work.getComment().contains(cfg.getMensagemInicio()))) {
                        registroIniciado = work;
                        log.println("registro iniciado: " + registroIniciado.getStartDate());
                        break;
                    }
                }
            }

            // verifica se o evento pode iniciar um registro
            if (cfg.getListaEventosInicio().contains(String.valueOf(eventTypeId)+":")) {
                // verifica se o status pode iniciar um registro
                if (cfg.getListaStatusInicio().contains(status.getId()+":")) {
                    // verifica se ja tem um registro iniciado
                    if (registroIniciado != null) {
                        log.println("Ja existe um registro iniciado nessa issue");
                        log.close();
                        return;
                    }
                    // inicia o registro
                    log.println("Iniciando registro: " + new Date());
                    log.close();
		            WorklogNewEstimateInputParameters params = createParams((MutableIssue) issue, "1m", new Date());
                    WorklogResult result = worklogService.validateCreate(context, params);
		            Worklog wl = worklogService.createAndAutoAdjustRemainingEstimate(context, result, true);
                    worklogManager.create(user, wl, null, false);
                    return;
                }
            }

            // verifica se o evento pode finalizar um registro
            if (cfg.getListaEventosFim().contains(String.valueOf(eventTypeId)+":")) {
                // verifica se tem um registro iniciado
                if (registroIniciado == null) {
                    log.println("Nenhum registro iniciado nessa issue");
                    log.close();
                    return;
                }
                // finaliza o registro
                Date inicio = registroIniciado.getStartDate();
                long dif = new Date().getTime() - inicio.getTime();
                long difMin = dif / 1000 / 60;
                log.println("Finalizando registro:" + difMin + "m " + new Date());
                log.close();
		        WorklogNewEstimateInputParameters params = updateParams((MutableIssue) issue, difMin + "m", inicio, 
                    registroIniciado.getId());
                WorklogResult result = worklogService.validateUpdate(context, params);
		        Worklog wl = worklogService.updateAndAutoAdjustRemainingEstimate(context, result, true);
                worklogManager.update(user, wl, null, false);
                return;
            }

            log.println("Evento nao configurado: " + eventTypeId);
            log.close();
            return;

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

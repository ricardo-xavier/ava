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
import com.atlassian.jira.util.ErrorCollection;

@Component
public class PluginAvanco implements InitializingBean, DisposableBean {

    @JiraImport
    private final EventPublisher eventPublisher;
    private Configuracao cfg;
    private Boolean registrando = false;

    @Autowired
    public PluginAvanco(EventPublisher eventPublisher) {

        this.eventPublisher = eventPublisher;
        PrintStream log = null;
        try {
            log = new PrintStream(new FileOutputStream("avanco.log", true));
            log.println("====================PluginAvanco v1.17 " + new Date());
            log.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private String comment(String s) {
        return ((s == null) || (s.length() < 80)) ? s : s.substring(0, 80);
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

        if (registrando) {
            return;
        }

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

            // recupera o usuario logado
            JiraAuthenticationContext jiraAuthenticationContext = ComponentAccessor.getJiraAuthenticationContext();
            com.atlassian.jira.user.ApplicationUser user = null;
            if (jiraAuthenticationContext != null) {
                user = jiraAuthenticationContext.getUser();
                if (user == null) {
                    log.println("Erro getUser");
                    log.close();
                    return;
                }
            }

            log.printf("evento=%d %s issue=%s status=%s %s responsavel=%s usuario=%s%n",
                eventTypeId, Evento.toString(eventTypeId), issue.getKey(),
                status.getId(), status.getName(), issue.getAssigneeId(), user.getName());
            
            // verifica se o usuario esta configurado para o plugin
            if (!cfg.getListaUsuarios().equals("*") && !cfg.getListaUsuarios().contains(":"+user.getName()+"(")) {
                log.println("Usuario nao configurado: " + user.getName());
                log.close();
                return;
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
            int iniciados = 0;
            Worklog registroIniciado = null;
            List<Worklog> workLogs = worklogManager.getByIssue(issue);
            if (workLogs != null) {
                for (int i=0; i<workLogs.size(); i++) {
                    Worklog work = workLogs.get(i);
                    log.println("registro " + i + "/" + workLogs.size() + " " + work.getStartDate() + " " + work.getAuthorKey() + " " + comment(work.getComment()) + " id=" + work.getId());
                }
                for (Worklog work : workLogs) {
                    if ((work.getComment() != null) && (work.getComment().contains(cfg.getMensagemInicio()))) {
                        registroIniciado = work;
                        log.println("registro iniciado: " + registroIniciado.getStartDate() + " " + registroIniciado.getAuthorKey());
                        iniciados++;
                        //break;
                    }
                }
            }

            // finaliza os iniciados anteriores ao ultimo
            if (iniciados > 1) {
                for (Worklog work : workLogs) {
                    if (work == registroIniciado) {
                        break;
                    }
                    if ((work.getComment() != null) && work.getComment().contains(cfg.getMensagemInicio())) {
                        log.println("Corrigindo registro " + work.getStartDate() + " " + work.getId());
                        WorklogNewEstimateInputParameters params = updateParams((MutableIssue) issue, "1m", 
                            work.getStartDate(), work.getId());
                        WorklogResult result = worklogService.validateUpdate(context, params);
                        if (result == null) {
                            log.println("result null");
                            ErrorCollection errorCollection = context.getErrorCollection();
                            log.println(errorCollection.toString());
                        }
                        Worklog wl = worklogService.updateAndAutoAdjustRemainingEstimate(context, result, true);
                        if (wl != null) {
                            log.println("adjus: "  + comment(wl.getComment()) + " id=" + wl.getId());
                        }
                    }
                }
            }

            boolean configurado = false;             

            // verifica se o evento pode iniciar um registro
            if (cfg.getListaEventosInicio().contains(":"+String.valueOf(eventTypeId)+":")) {

                configurado = true;

                // verifica se o status pode iniciar um registro
                if (cfg.getListaStatusInicio().contains(":"+status.getId()+":")) {
                    // verifica se ja tem um registro iniciado
                    if (registroIniciado != null) {
                        log.println("Ja existe um registro iniciado nessa issue");
                        log.close();
                        return;
                    }

                    // verifica se o usuario logado eh responsavel pela issue
                    if (!user.getName().equals(issue.getAssigneeId())) {
                        log.printf("Usuario logado %s diferente do responsavel pela issue %s%n", user.getName(), issue.getAssigneeId());
                        log.close();
                        return;
                    }

                    // inicia o registro
                    log.println("Iniciando registro: " + new Date());
                    WorklogNewEstimateInputParameters params = createParams((MutableIssue) issue, "1m", new Date());
                    WorklogResult result = worklogService.validateCreate(context, params);
                    if (result != null) {
                        Worklog created = result.getWorklog();
                        if (created != null) {
                            log.println("validateCreate: " + created.getStartDate() + " " + created.getAuthorKey() + " " + comment(created.getComment()) + " id=" + created.getId());
                        }
                    } else {
                        log.println("result null");
                        ErrorCollection errorCollection = context.getErrorCollection();
                        log.println(errorCollection.toString());
                    }
                    Worklog wl = worklogService.createAndAutoAdjustRemainingEstimate(context, result, true);
                    if (wl != null) {
                        log.println("adjust: " + wl.getStartDate() + " " + wl.getAuthorKey() + " " + comment(wl.getComment()) + " id=" + wl.getId());
                    }
                    //worklogManager.create(com.atlassian.jira.user.ApplicationUsers.toDirectoryUser(user), wl, null, false);
                    log.println("Iniciando registro: " + new Date() + " id=" + wl.getId()
                                + " start=" + wl.getStartDate());
                    workLogs = worklogManager.getByIssue(issue);
                    if (workLogs != null) {
                        for (int i=0; i<workLogs.size(); i++) {
                            Worklog work = workLogs.get(i);
                            log.println("registro apos create " + i + "/" + workLogs.size() + " " + work.getStartDate() + " " + work.getAuthorKey() + " " + comment(work.getComment()) + " id=" + work.getId());
                        }
                    }
                    log.close();
                    return;
                }
            }

            // verifica se o evento pode finalizar um registro
            if (cfg.getListaEventosFim().contains(":"+String.valueOf(eventTypeId)+":")) {

                configurado = true;

                // verifica se tem um registro iniciado
                if (registroIniciado == null) {
                    log.println("Nenhum registro iniciado nessa issue");
                    log.close();
                    return;
                }

                // verifica se o usuario logado eh responsavel pelo worklog
                if (!user.getName().equals(registroIniciado.getAuthorKey())) {
                    log.printf("Usuario logado %s diferente do responsavel pelo worklog %s%n", user.getName(), 
                                registroIniciado.getAuthorKey());
                    log.close();
                    return;
                }

                // finaliza o registro

                synchronized (registrando) {

                    registrando = true;

                    cfg.carregaTurno(user.getName());
                    CalculoTempo calculo = new CalculoTempo();
                    long difMin = calculo.calculaTempo(log,
                        registroIniciado.getStartDate(), new Date(),
                        cfg.getIniManha(), cfg.getFimManha(),
                        cfg.getIniTarde(), cfg.getFimTarde(),
                        cfg.getFeriados(), user.getName());
                    log.println("        inicio:" + registroIniciado.getStartDate());
                    log.println("        fim   :" + new Date());
                    log.printf ("        turno : %s %s %s %s%n",
                        cfg.getIniManha(), cfg.getFimManha(),
                        cfg.getIniTarde(), cfg.getFimTarde());

                    List<Tempo> tempos = calculo.getTempos();
                    log.println("Tempo calculado : " + difMin);

                    int tempoJaRegistrado = 0;
                    for (int t=0; t<tempos.size(); t++) {

                        Tempo tempo = tempos.get(t);
                        log.printf("tempo %d/%d = %d - %d%n", t, tempos.size(), tempo.getMinutos(), tempoJaRegistrado);

                        if (t > 0) {
                            WorklogNewEstimateInputParameters params = createParams((MutableIssue) issue, "1m", new Date());
                            WorklogResult result = worklogService.validateCreate(context, params);
                            if (result != null) {
                                Worklog created = result.getWorklog();
                                if (created != null) {
                                    log.println("validateCreate: " + created.getStartDate() + " " + created.getAuthorKey() + " " + comment(created.getComment()) + " id=" + created.getId());
                                }
                            } else {
                                log.println("result null");
                                ErrorCollection errorCollection = context.getErrorCollection();
                                log.println(errorCollection.toString());
                            }
                            Worklog wl = worklogService.createAndAutoAdjustRemainingEstimate(context, result, true);
                            if (wl != null) {
                                log.println("adjust: " + wl.getStartDate() + " " + wl.getAuthorKey() + " " + comment(wl.getComment()) + " id=" + wl.getId());
                            }
                            registroIniciado = wl;
                            log.println("Iniciando registro: " + tempo.getInicio() + " id=" + registroIniciado.getId()
                                + " start=" + registroIniciado.getStartDate());
                        }

                        if (tempo.getMinutos() == 0) {
                            tempo.setMinutos(1);
                        }
                        // o tempo eh cumulativo
                        int tempoRegistrar = tempo.getMinutos() - tempoJaRegistrado;
                        tempoJaRegistrado += tempoRegistrar;
                        log.println("Finalizando registro:" + tempoRegistrar + "m " + tempo.getInicio() + " id=" + registroIniciado.getId()
                                + " start=" + registroIniciado.getStartDate());
                        WorklogNewEstimateInputParameters params = updateParams((MutableIssue) issue, tempoRegistrar + "m", 
                            tempo.getInicio(), registroIniciado.getId());
                        WorklogResult result = worklogService.validateUpdate(context, params);
                        if (result != null) {
                            Worklog created = result.getWorklog();
                            if (created != null) {
                                log.println("validateUpdate: " + created.getStartDate() + " " + created.getAuthorKey() + " " + comment(created.getComment()) + " id=" + created.getId());
                            }
                        } else {
                            log.println("result null");
                            ErrorCollection errorCollection = context.getErrorCollection();
                            log.println(errorCollection.toString());
                        }
                        Worklog wl = worklogService.updateAndAutoAdjustRemainingEstimate(context, result, true);
                        if (wl != null) {
                            log.println("adjust: " + wl.getStartDate() + " " + wl.getAuthorKey() + " " + comment(wl.getComment()) + " id=" + wl.getId());
                        }
                        //worklogManager.create(com.atlassian.jira.user.ApplicationUsers.toDirectoryUser(user), wl, null, false);

                    }

                    workLogs = worklogManager.getByIssue(issue);
                    if (workLogs != null) {
                        for (int i=0; i<workLogs.size(); i++) {
                            Worklog work = workLogs.get(i);
                            log.println("registro apos update " + i + "/" + workLogs.size() + " " + work.getStartDate() + " " + work.getAuthorKey() + " " + comment(work.getComment()) + " id=" + work.getId());
                        }
                    }

                    registrando = false;
                }
                log.close();
                return;
            }

            if (!configurado) {
                log.println("Evento nao configurado: " + eventTypeId);
            }
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

// 1.1 - 14/09 - finalizar o evento se o usuario com o worklog aberto for o usuario logado
// 1.2 - 19/09 - alteracao no calculo de tempo, considerando turnos e dias uteis
// 1.3 - 08/11 - criar um registro por dia quando fechar uma issue aberta a mais de um dia
// 1.4 - 12/11 - alterar o tempo para 1 minuto se for encerrado com 0
// 1.5 - 12/12 - alteracao da versao do jira-api de 7.7.1 para 6.2.4
// 1.6 - 03/01 - correcao no registro de tempo com mais de uma ocorrencia(retirada do acumulo)
// 1.7 - 06/01 - correcao no registro de tempo com mais de uma ocorrencia(id)
// 1.8 - 09/01 - estava registrando os tempos duas vezes
// 1.11- 27/01 - pegar o ultimo registro iniciado
// 1.12- 01/02 - ajuste quando h2 < iniManha
// 1.13- 13/02 - mais logs
// 1.17- 29/05 - correcao tempo < 0 ao iniciar depois do horario

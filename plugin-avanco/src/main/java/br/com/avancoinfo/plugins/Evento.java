package com.example.tutorial.plugins;

import com.atlassian.jira.event.type.EventType;

public class Evento {
	
	public static String toString(Long id) {
		
		if (id == null) {
			return "null";
		}
		
		if (id.equals(EventType.ISSUE_CREATED_ID)) { //1
			return "ISSUE_CREATED"; 
		}
		
		if (id.equals(EventType.ISSUE_UPDATED_ID)) { //2
			return "ISSUE_UPDATED";
		}
		
		if (id.equals(EventType.ISSUE_ASSIGNED_ID)) { //3
			return "ISSUE_ASSIGNED";
		}
		
		if (id.equals(EventType.ISSUE_RESOLVED_ID)) { //4
			return "ISSUE_RESOLVED";
		}
		
		if (id.equals(EventType.ISSUE_CLOSED_ID)) { //5
			return "ISSUE_CLOSED";
		}
		
		if (id.equals(EventType.ISSUE_COMMENTED_ID)) { //6
			return "ISSUE_COMMENTED";
		}
		
		if (id.equals(EventType.ISSUE_REOPENED_ID)) { //7
			return "ISSUE_REOPENED";
		}
		
		if (id.equals(EventType.ISSUE_DELETED_ID)) { //8
			return "ISSUE_DELETED";
		}
		
		if (id.equals(EventType.ISSUE_MOVED_ID)) { //9
			return "ISSUE_MOVED";
		}
		
		if (id.equals(EventType.ISSUE_WORKLOGGED_ID)) { //10
			return "ISSUE_WORKLOGGED";
		}
		
		if (id.equals(EventType.ISSUE_WORKSTARTED_ID)) { //11
			return "ISSUE_WORKSTARTED";
		}
		
		if (id.equals(EventType.ISSUE_WORKSTOPPED_ID)) { //12
			return "ISSUE_WORKSTOPPED";
		}
		
		if (id.equals(EventType.ISSUE_GENERICEVENT_ID)) { //13
			return "ISSUE_GENERICEVENT";
		}
		
		if (id.equals(EventType.ISSUE_COMMENT_EDITED_ID)) { //14
			return "ISSUE_COMMENT_EDITED";
		}
		
		if (id.equals(EventType.ISSUE_WORKLOG_UPDATED_ID)) { //15
			return "ISSUE_WORKLOG_UPDATED";
		}
		
		if (id.equals(EventType.ISSUE_WORKLOG_DELETED_ID)) { //16
			return "ISSUE_WORKLOG_DELETED";
		}
		
		if (id.equals(EventType.ISSUE_COMMENT_DELETED_ID)) { //17
			return "ISSUE_COMMENT_DELETED";
		}
		
		return "EVENTO DESCONHECIDO:" + id;
	}

}


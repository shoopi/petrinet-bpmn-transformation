package main.java.nl.tue.ieis.is.bpmGame.controller;

import java.util.List;

import main.java.nl.tue.ieis.is.bpmGame.activiti.ProcessDefinitionFunctions;

import org.activiti.engine.identity.User;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.SerializableEventListener;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.Row;
import org.zkoss.zul.Rows;
import org.zkoss.zul.Timer;

public class SidebarController extends SelectorComposer<Component>{

	private static final long serialVersionUID = -2986835198661573761L;
	@Wire	private Grid sidebarGrid;
	@Wire	private Timer sidebarTimer;
	private ProcessDefinitionFunctions defFunc = new ProcessDefinitionFunctions();

	
	@Override
	public void doAfterCompose(Component comp) throws Exception{
		super.doAfterCompose(comp);
		try {
			User user = (User) (Sessions.getCurrent()).getAttribute("user");
			if(user != null) {
				constructSidebarForUser(user.getId());
			} else {
				sidebarGrid.getRows().getChildren().clear();
			}
		} catch(Exception e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
	}

	private Row constructSidebarRow(final String label) {
		final Row row = new Row();
		try {
			Label lab = new Label(label);
			row.appendChild(lab);
			row.setSclass("sidebar-fn");
			EventListener<Event> actionListener = new SerializableEventListener<Event>() {
				private static final long serialVersionUID = 1L;
				public void onEvent(Event event) throws Exception {
					(Sessions.getCurrent()).setAttribute("selected" , label);
					Executions.sendRedirect("");
				}
			};
			row.addEventListener(Events.ON_CLICK, actionListener);
		} catch(Exception e) { 
			System.out.println(e.getMessage());
		}
		return row;
	}
	
	@Listen("onTimer = #sidebarTimer")
	public void update() {
		try {
			User user = (User) (Sessions.getCurrent()).getAttribute("user");
			if(user != null) {
				int prevItems = sidebarGrid.getRows().getVisibleItemCount();
				int currentItems = defFunc.getAllProcessModelsForUser(user.getId()).size();
				if(prevItems != currentItems) {
					sidebarGrid.getRows().getChildren().clear();
					constructSidebarForUser(user.getId());
				}
			} else {
				sidebarGrid.getRows().getChildren().clear();
			}
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private void constructSidebarForUser(String userId) {
		List<String> processSpecificaionIDs = defFunc.getAllProcessModelsForUser(userId);
		Rows rows = sidebarGrid.getRows();
		for(String id : processSpecificaionIDs) {
			Row row = constructSidebarRow(id);
			rows.appendChild(row);
			String specId = (String) (Sessions.getCurrent()).getAttribute("selected");
			if(specId != null && id.contentEquals(specId)) {
				row.setStyle("background-color: #FFFF99; font-weight: bold;");
			}
		}
	}
}


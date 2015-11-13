package main.java.nl.tue.ieis.is.bpmGame.activiti;

import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;


import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.FormService;
import org.activiti.engine.HistoryService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.form.FormProperty;
import org.activiti.engine.form.TaskFormData;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.pvm.PvmActivity;
import org.activiti.engine.impl.pvm.ReadOnlyProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.image.ProcessDiagramGenerator;
import org.activiti.image.impl.DefaultProcessDiagramGenerator;
import org.zkoss.zk.ui.Sessions;

public class TaskFunctions {
	
	private ProcessEngine processEngine = ActivitiConfig.processEngine;
	private TaskService taskService = processEngine.getTaskService();
	private HistoryService historyService = processEngine.getHistoryService();
	private RuntimeService runtimeService = processEngine.getRuntimeService();
	private RepositoryService repositoryService = processEngine.getRepositoryService();
	private FormService formService = processEngine.getFormService();
	
	public List<TaskObject> showAllTasksForProcessInstance(String instanceId, String userId) {
		
		List<TaskObject> tasks = new ArrayList<TaskObject>();
		try{
			ProcessInstance selectedProcessInstance = runtimeService.createProcessInstanceQuery().processInstanceId(instanceId).singleResult();
			String definitionID = selectedProcessInstance.getProcessDefinitionId();
			ReadOnlyProcessDefinition processDefinition = ((RepositoryServiceImpl)repositoryService).getDeployedProcessDefinition(definitionID);
			
			tasks.clear();
			if (processDefinition != null) {
				//TODO also add bpmn events
				for (PvmActivity activity : processDefinition.getActivities()) {
					if((String) activity.getProperty("type") == "userTask" || 
							(String) activity.getProperty("type") == "serviceTask" || 
							(String) activity.getProperty("type") == "receiveTask") {
						QueryAnnotation queryAnnotation = new QueryAnnotation();
						
						String xmlQueryString = (String) activity.getProperty("documentation");
						if(xmlQueryString != null && xmlQueryString.length() > 10) {
							try {
								StringReader reader = new StringReader(xmlQueryString);
								JAXBContext jaxbContext = JAXBContext.newInstance(QueryAnnotation.class);
						 
								Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
								queryAnnotation = (QueryAnnotation) jaxbUnmarshaller.unmarshal(reader);
							} catch(Exception e ) {
								System.out.println("System cannot parse the Query Annotation for task " + activity.getId() + ":" + (String)activity.getProperty("name") + " - (" + e.getMessage() + ")");
								e.getStackTrace();
							}
						}
						//TODO: add user name
						if(userId == null) userId = "unknown";
						tasks.add(new TaskObject(activity.getId(), (String)activity.getProperty("name"), 
								(String)activity.getProperty("type"), TaskStatus.Future, definitionID, instanceId, userId, queryAnnotation));
					}
	
				}
			}
			
			List<HistoricTaskInstance> historyTasks = historyService.createHistoricTaskInstanceQuery().processInstanceId(instanceId).list();
			
			if(historyTasks != null) {
				for(HistoricTaskInstance h : historyTasks) {
					for(TaskObject t : tasks) {
						if(h.getTaskDefinitionKey().contentEquals(t.getTaskId())) {
							t.setTaskStatus(TaskStatus.Finished);
							break;
						}
					}
				}
			}
			
			List<Task> enabledTask = taskService.createTaskQuery().processInstanceId(instanceId).list();
			for(Task e: enabledTask) {
				for(TaskObject t : tasks) {
					if(e.getTaskDefinitionKey().contentEquals(t.getTaskId())) {
						t.setTaskStatus(TaskStatus.Executing);
					}
				}
			}
		} catch (Exception e) {
			TransportOrderDataManagement tpdm = new TransportOrderDataManagement();
			String caseID = (String)(Sessions.getCurrent()).getAttribute("caseID");
			tpdm.updateTransportStatus(caseID, StatusCode.TRANSPORTATION_FINISHED.getValue());
			String assetId = tpdm.loadAssetIdByCaseId(caseID);
			if(assetId != null && assetId.length() > 0) {
				try {
					if(tpdm.loadAssetStatusById(assetId) == StatusCode.TRUCK_BUSY.getValue())
						tpdm.updateAssetStatus(assetId, StatusCode.TRUCK_FREE.getValue());

				} catch(Exception e2) {}
			}
			tpdm.setAssetForTransportationOrder(instanceId, "");
			System.out.println("Transportation case " + caseID + " has been finished.");
			(Sessions.getCurrent()).setAttribute("caseID" , "0");
		}
		return tasks;
	}

	public List<TaskObject> getExecutingTasks(String instanceId, String userId) {
		List<TaskObject> tasks = new ArrayList<TaskObject>();
		tasks = showAllTasksForProcessInstance(instanceId, userId);
		List<TaskObject> enabledTask = new ArrayList<TaskObject>();
		try {
			if(tasks != null) {
				for(TaskObject task : tasks) {
					if(task.getTaskStatus() == TaskStatus.Executing) {
						enabledTask.add(task);
					}
				}
			}
		} catch (Exception e) {
			//e.printStackTrace();
			System.err.println(e.getMessage() + " in loading all executing tasks for the case " + instanceId);
		}
		return enabledTask;
	}

	public List<FormProperty> showForm(String processInstanceId, String taskId) {
		String currentId = taskService.createTaskQuery().processInstanceId(processInstanceId).taskDefinitionKey(taskId).singleResult().getId();
		TaskFormData formData = (TaskFormData) formService.getTaskFormData(currentId);
		return formData.getFormProperties();
	}
	
	public boolean completeTask(String instanceId, String taskId, Map<String, String> variable) {
		boolean ok = false;
		try {
			Task task = taskService.createTaskQuery().processInstanceId(instanceId).taskDefinitionKey(taskId).singleResult();
			if(variable == null || variable.size() < 1) {
				taskService.complete(task.getId());
				ok = true;
			} else {
				formService.submitTaskFormData(task.getId(), variable);
				ok = true;
			}
		
		} catch (Exception e) {
			System.out.println("task " + taskId + " from " + instanceId + " cannot be executed.");
			System.err.println(e.getMessage() + " in submitting task " + taskId + " from case " + instanceId);
			ok = false;
			//e.printStackTrace();
		}
		return ok;
	}
	
	public InputStream getProcessModelImage(String instanceId) {
		try{
			ProcessInstance selectedProcessInstance = runtimeService.createProcessInstanceQuery().processInstanceId(instanceId).singleResult();
			String definitionID = selectedProcessInstance.getProcessDefinitionId();
			ProcessDiagramGenerator pdg = new DefaultProcessDiagramGenerator();
			BpmnModel model = repositoryService.getBpmnModel(definitionID);
			
			InputStream imageStream = pdg.generateDiagram(model, "png", runtimeService.getActiveActivityIds(instanceId), 0.75);
			return imageStream;
		} catch(Exception e) {
			//e.printStackTrace();
			//System.err.println(e.getMessage() + " in creating a picture of process model");
			return null;
		}
	}
	
}

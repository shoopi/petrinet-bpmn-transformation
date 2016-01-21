package nl.tue.ieis.is.bpmGame.activiti;


import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import nl.tue.ieis.is.bpmGame.controller.ErrorController;
import nl.tue.ieis.is.bpmGame.data.TableConfiguration;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.image.ProcessDiagramGenerator;
import org.activiti.image.impl.DefaultProcessDiagramGenerator;

public class ProcessDefinitionFunctions {
	
	private ProcessEngine processEngine = ActivitiConfig.processEngine;
	private RepositoryService repositoryService = processEngine.getRepositoryService();
	private TableConfiguration tc = new TableConfiguration();
	
	public String deployProcessDefinition(String userId, String fileName, InputStream file, int dupIndex) {
		try {
			if(tc.isSameFileName(userId, fileName)) {
				return "DUPLICATED_FILE_NAME";
			}
			String newFilename = fileName.substring(0,fileName.length()-dupIndex);
			String deploymentId = repositoryService.createDeployment()
					.name(userId)					
					.addInputStream(newFilename + "20.xml", file)
					.deploy()
					.getId();
			System.out.println(userId + " has uploaded " + repositoryService.createDeploymentQuery().deploymentName(userId).count() + " process definition(s).");
			tc.addDeploymentFilenameUser(deploymentId, fileName, userId);
			return deploymentId;
		} catch (Exception e) {
			ErrorController.errors.add(e.getMessage());
			return null;
		}
	}
	
	public String getProcessDefinitionId(String deploymentId) {
		 return repositoryService.createProcessDefinitionQuery().deploymentId(deploymentId).singleResult().getId();
	}
	
	public String getDeploymentId(String processDefinitionId) {
		 return repositoryService.createProcessDefinitionQuery().processDefinitionId(processDefinitionId).singleResult().getDeploymentId();
	}
	
	public InputStream generateProcessImageWithDeploymentId(String deploymentId) {
		try{
			String definitionId = getProcessDefinitionId(deploymentId);
			ProcessDiagramGenerator pdg = new DefaultProcessDiagramGenerator();
			BpmnModel model = repositoryService.getBpmnModel(definitionId);			
			InputStream imageStream = pdg.generatePngDiagram(model);
			return imageStream;
		} catch(Exception e) {
			return null;
		}
	}
	
	public InputStream generateProcessImageWithDefinitionId(String processDefinitionId) {
		try{
			ProcessDiagramGenerator pdg = new DefaultProcessDiagramGenerator();
			BpmnModel model = getBpmnModel(processDefinitionId);
			
			InputStream imageStream = pdg.generatePngDiagram(model);
			return imageStream;
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public Map<String,String> getAllUploadedProcessModelsForUser(String userId) {
		return tc.loadAllDeploymentFileForUser(userId);
		
	}
	
	public BpmnModel getBpmnModel (String processDefinitionId) {
		return repositoryService.getBpmnModel(processDefinitionId);
	}
	
	public void deleteProcessModelFromRepositoryByDeploymentId(String deploymentId) {
		repositoryService.deleteDeployment(deploymentId);
		tc.removeDeploymentFilenameUser(deploymentId);
	}
	
	public void deleteProcessModelFromRepositoryByDefinitionId(String definitionId) {
		String deploymentId = getDeploymentId(definitionId);
		deleteProcessModelFromRepositoryByDeploymentId(deploymentId);
	}
	
}

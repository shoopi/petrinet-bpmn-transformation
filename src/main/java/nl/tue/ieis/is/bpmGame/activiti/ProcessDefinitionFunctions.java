package main.java.nl.tue.ieis.is.bpmGame.activiti;


import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import main.java.nl.tue.ieis.is.bpmGame.controller.ErrorController;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Deployment;
import org.activiti.image.ProcessDiagramGenerator;
import org.activiti.image.impl.DefaultProcessDiagramGenerator;

public class ProcessDefinitionFunctions {
	
	private ProcessEngine processEngine = ActivitiConfig.processEngine;
	private RepositoryService repositoryService = processEngine.getRepositoryService();

	public String deployProcessDefinition(String userId, String fileName, InputStream file) {
		try {
			String deploymentId = repositoryService.createDeployment().name(userId).
					addInputStream(fileName + "20.xml", file)
					//.disableBpmnValidation()
					.deploy().getId();
			System.out.println(userId + " have deployed " + repositoryService.createDeploymentQuery().deploymentName(userId).count() + " process definition(s).");
			
			return deploymentId;
			
		} catch (Exception e) {
			ErrorController.errors.add(e.getMessage());
			//e.printStackTrace();
			return null;
		}
	}
	
	public InputStream generateProcessImageWithDeploymentId(String deploymentId) {
		try{
			int deploymnetSize = (int) repositoryService.createDeploymentQuery().deploymentId(deploymentId).count();
			String definitionId = repositoryService.createProcessDefinitionQuery().deploymentId(deploymentId).active().list().get(deploymnetSize-1).getId();
			ProcessDiagramGenerator pdg = new DefaultProcessDiagramGenerator();
			BpmnModel model = repositoryService.getBpmnModel(definitionId);
			//repositoryService.createDeploymentQuery().deploymentId(deploymentId).
			
			InputStream imageStream = pdg.generatePngDiagram(model);
			return imageStream;
		} catch(Exception e) {
			//e.printStackTrace();
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
	
	public List<String> getAllProcessModelsForUser(String userId) {
		List<String> specIDs = new ArrayList<String>();
		for(Deployment d : repositoryService.createDeploymentQuery().deploymentName(userId).list()) {
			String specId = repositoryService.createProcessDefinitionQuery().deploymentId(d.getId()).singleResult().getId();
			specIDs.add(specId);
		}
		return specIDs;
	}
	
	public BpmnModel getBpmnModel (String processDefinitionId) {
		return repositoryService.getBpmnModel(processDefinitionId);
	}
	
	
	
}

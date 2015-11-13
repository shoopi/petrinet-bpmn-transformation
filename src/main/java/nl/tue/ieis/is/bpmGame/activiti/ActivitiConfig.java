package main.java.nl.tue.ieis.is.bpmGame.activiti;

import java.io.File;
import java.util.Map;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;
import org.zkoss.io.Files;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.util.Initiator;

public class ActivitiConfig implements Initiator{

	public static ProcessEngine processEngine;
	public final static String projectPath = "C:\\Users\\spourmir\\Dropbox\\Standard\\BPM_Game\\";
	public final static String projectURL = "http://131.155.120.245:8080/BPM_Game/";
	private boolean cleanupRepository = true;
	
	@SuppressWarnings("rawtypes")
	public void doInit(Page page, Map args) throws Exception {
		
		if(processEngine == null) {
			
			processEngine = ProcessEngines.getDefaultProcessEngine();
			
			Group admins = processEngine.getIdentityService().newGroup("admin");
			processEngine.getIdentityService().saveGroup(admins);
			
			Group students = processEngine.getIdentityService().newGroup("student");
			processEngine.getIdentityService().saveGroup(students);
			
			User shaya = processEngine.getIdentityService().newUser("shaya");
			shaya.setPassword("shaya");
			shaya.setFirstName("Shaya");
			shaya.setLastName("Pourmirza");
			processEngine.getIdentityService().saveUser(shaya);
			processEngine.getIdentityService().createMembership("shaya", "admin");
			
			if(cleanupRepository) deleteAllProcessModel();
		}
	}
	
	private void deleteAllProcessModel() {
		File repositoryFolder = new File(ActivitiConfig.projectPath + "src\\main\\resources\\");
		Files.deleteAll(repositoryFolder);
		/*
		File[] processModels = repositoryFolder.listFiles();
		for (File pm : processModels) {
		    if (pm.isFile()) {
		    	Files.deleteAll(pm);
		        System.out.println(pm.getName() + " has been removed from the repository.");
		    }
		}
		*/
	}
	
}
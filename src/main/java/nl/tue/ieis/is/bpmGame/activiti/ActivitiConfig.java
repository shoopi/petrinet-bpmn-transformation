package main.java.nl.tue.ieis.is.bpmGame.activiti;

import java.util.List;
import java.util.Map;

import main.java.nl.tue.ieis.is.bpmGame.data.TableConfiguration;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;
import org.activiti.engine.repository.Deployment;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.util.Initiator;

public class ActivitiConfig implements Initiator{

	public static ProcessEngine processEngine;
	public final static String path = Sessions.getCurrent().getWebApp().getRealPath("/WEB-INF/");
    public final static String DriverClassName = "org.h2.Driver";
	public final static String DBSchema = "jdbc:h2:" + path + "/database/activiti";
	public final static String DBUsername = "sa";
	public final static String DBPassword = "123456";
	private boolean cleanupRepository = false;
	
	@SuppressWarnings("rawtypes")
	public void doInit(Page page, Map args) throws Exception {
		
		if(processEngine == null) {
			System.out.println(path);
			processEngine = ProcessEngineConfiguration.createStandaloneProcessEngineConfiguration()
					  .setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE)
					  .setJdbcDriver(DriverClassName)
					  .setJdbcUrl(DBSchema + ";DB_CLOSE_DELAY=-1")
					  .setJdbcUsername(DBUsername)
					  .setJdbcPassword(DBPassword)
					  .setAsyncExecutorEnabled(true)
					  .setAsyncExecutorActivate(false)
					  .buildProcessEngine();
			
			if(!processEngine.getIdentityService().checkPassword("shoopi@gmail.com", "shaya")) {
				Group admins = processEngine.getIdentityService().newGroup("admin");
				processEngine.getIdentityService().saveGroup(admins);
			
				Group students = processEngine.getIdentityService().newGroup("student");
				processEngine.getIdentityService().saveGroup(students);
			
				User shaya = processEngine.getIdentityService().newUser("shoopi@gmail.com");
				shaya.setPassword("shaya");
				shaya.setFirstName("Shaya");
				shaya.setLastName("Pourmirza");
				processEngine.getIdentityService().saveUser(shaya);
				processEngine.getIdentityService().createMembership("shoopi@gmail.com", "admin");
			}
			
			TableConfiguration tc = new TableConfiguration();

			if(cleanupRepository) {
				deleteAllProcessModel();
				tc.createTableDeploymentFilenameUser();
			}			
		}
	}
	
	private void deleteAllProcessModel() {
		if(processEngine.getRepositoryService().createDeploymentQuery() != null 
				&& processEngine.getRepositoryService().createDeploymentQuery().count() > 0) {
			List<Deployment> deployments = processEngine.getRepositoryService().createDeploymentQuery().list();
			for(Deployment d : deployments) {
				processEngine.getRepositoryService().deleteDeployment(d.getId());
			}
		}
		/*
		File repositoryFolder = new File(ActivitiConfig.projectPath + "\\resources\\");
		Files.deleteAll(repositoryFolder);
		*/

	}
	
}
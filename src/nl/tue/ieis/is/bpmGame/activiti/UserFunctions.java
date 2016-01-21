package nl.tue.ieis.is.bpmGame.activiti;

import java.io.InputStream;

import nl.tue.ieis.is.bpmGame.activiti.ActivitiConfig;

import org.activiti.engine.IdentityService;
import org.activiti.engine.identity.Picture;
import org.activiti.engine.identity.User;

public class UserFunctions {

	private IdentityService identityService = ActivitiConfig.processEngine.getIdentityService();
		
	public User loadUser(String username, String password) {
		if(identityService.checkPassword(username, password)) {
			return identityService.createUserQuery().userId(username).singleResult();
		}
		return null;
	}
	
	public InputStream loadUserProfilePic(String username) {
		Picture pic = identityService.getUserPicture(username);
		if(pic == null)
			return null;
		return pic.getInputStream();
	}
	
	public boolean createUser(String username, String password, String firstname, String lastname, String email, String role, byte[] imageByte) {
		User user = identityService.newUser(username);
		user.setPassword(password);
		user.setFirstName(firstname);
		user.setLastName(lastname);
		user.setEmail(email);
		try {
			identityService.saveUser(user);
			identityService.createMembership(username, role);
			if(imageByte != null) {
				Picture pic = new Picture(imageByte, "image/*");
				identityService.setUserPicture(username, pic);
			}
			return true;
		} catch(Exception e) {
			return false;
		}
	}
}

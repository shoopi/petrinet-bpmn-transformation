package nl.tue.ieis.is.bpmGame.controller;

import nl.tue.ieis.is.bpmGame.activiti.UserFunctions;

import org.activiti.engine.identity.User;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Image;
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;


public class UserController extends SelectorComposer<Component> {
	
	private static final long serialVersionUID = 8670883952725538263L;
	private static UserFunctions uf = new UserFunctions();

	@Wire	private 	Textbox 	email, password;
	@Wire	private 	Label 		loginMsgLabel, regMsgLabel;
	@Wire	private 	Button 		loginwBtn;
	@Wire	private 	Textbox 	regEmail, regPassword, regFirstname, regLastename;
	@Wire	private 	Combobox 	roleComobox;
	@Wire 	private 	Comboitem 	adminComboitem, studentComboitem;
	@Wire 	private 	Button 		picBtn, regBtn;
	@Wire	private 	Window 		loginWin, regWin;
	@Wire	private		Image		picPreview;
	private byte[] userImage;
	
	public void doAfterCompose(Component comp) throws Exception {
		super.doAfterCompose(comp);
	}
	
	@Listen("onOK = #loginWin")
	public void loginWindowOk() {
		login();
	}
	
	
	@Listen("onClick = #loginBtn")
	public void loginButtonClick() {
		login();
	}
	
	@Listen("onOK = #regWin")
	public void regWindowOk() {
		register();
	}
	
	@Listen("onClick = #regBtn")
	public void regButtonClick() {
		register();
	}
	
	private void login() {
		String inputEmail = email.getValue();
		String inputPassword = password.getValue();
		try{
			if(uf.loadUser(inputEmail, inputPassword) != null) {
				User user = uf.loadUser(inputEmail, inputPassword);
				(Sessions.getCurrent()).setAttribute("user", user);
				loginWin.setVisible(false);
				System.out.println("user " + inputEmail + " has logged in to the system.");
				Executions.sendRedirect("");
			} else loginMsgLabel.setValue("Please check your username and password and try again.");
		} catch(Exception e) {
			loginMsgLabel.setValue("Authentication Failed! Please try again.");
			e.printStackTrace();
		}
	}
	
	private void register() {
		String email = regEmail.getValue();
		String password = regPassword.getValue();
		String firstname = regFirstname.getValue();
		String lastname = regLastename.getValue();
		String role = "";
		if(roleComobox.getSelectedItem().equals(studentComboitem)) role = "student";
		else if (roleComobox.getSelectedItem().equals(adminComboitem)) role = "admin";
		
        try{
        	boolean done = uf.createUser(email, password, firstname, lastname, email, role, userImage);
			if(done) {
				System.out.println("user " + email + " has been created.");
				regWin.setVisible(false);
				Clients.showNotification("<p>Congratulations! </p> "
						+ "<p>User " + email + " has been registered. </p>");
				
			} else {
				regWin.setVisible(false);
				Clients.showNotification("User " + email + " has 'already' been registered. <br/>");
			}
    	} catch(Exception e) {
    		e.printStackTrace();
    	}
	}
	
	@Listen("onUpload = #picBtn")
	public void uploadUserImage(UploadEvent event) {
		org.zkoss.util.media.Media media = event.getMedia();
		try {
			if(media.getFormat().contentEquals("png") || media.getFormat().contentEquals("jpeg") || media.getFormat().contentEquals("jpg")) {
				userImage = media.getByteData();
				picPreview.setContent((org.zkoss.image.Image) media);
			} else {
				Messagebox.show("Please upload a file with '.png' or '.jpg' or '.jpeg' extension." , "Error", Messagebox.OK, Messagebox.ERROR);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
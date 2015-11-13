package main.java.nl.tue.ieis.is.bpmGame.controller;

import main.java.nl.tue.ieis.is.bpmGame.activiti.UserFunctions;

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
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;


public class UserController extends SelectorComposer<Component> {
	
	private static final long serialVersionUID = 8670883952725538263L;
	private static UserFunctions uf = new UserFunctions();

	@Wire	private Textbox username;
	@Wire	private Textbox password;
	@Wire	private Label loginMsgLabel;
	@Wire	private Button loginwBtn;
	@Wire	private Window loginWin;
	@Wire	private Textbox regUsername;
	@Wire	private Textbox regPassword;
	@Wire	private Textbox regFirstname;
	@Wire	private Textbox regLastename;
	@Wire	private Textbox regEmail;
	@Wire	private Combobox roleComobox;
	@Wire 	private Comboitem adminComboitem;
	@Wire	private	Comboitem studentComboitem;
	@Wire 	private Button picBtn;
	@Wire	private Button regBtn;
	@Wire	private Window regWin;
	@Wire	private Label regMsgLabel;
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
		String inputUsername = username.getValue();
		String inputPassword = password.getValue();
		try{
			if(uf.loadUser(inputUsername, inputPassword) != null) {
				User user = uf.loadUser(inputUsername, inputPassword);
				(Sessions.getCurrent()).setAttribute("user", user);
				loginWin.setVisible(false);
				System.out.println("user " + inputUsername + " has logged in to the system.");
				Executions.sendRedirect("");
			} else loginMsgLabel.setValue("Please check your username and password and try again.");
		} catch(Exception e) {
			loginMsgLabel.setValue("Authentication Failed! Please try again.");
			e.printStackTrace();
		}
	}
	
	private void register() {
		String username = regUsername.getValue();
		String password = regPassword.getValue();
		String firstname = regFirstname.getValue();
		String lastname = regLastename.getValue();
		String email = regEmail.getValue();
		String role = "";
		if(roleComobox.getSelectedItem().equals(studentComboitem)) role = "student";
		else if (roleComobox.getSelectedItem().equals(adminComboitem)) role = "admin";
		
        try{
        	boolean done = uf.createUser(username, password, firstname, lastname, email, role, userImage);
			if(done) {
				System.out.println("user " + username + " has been created.");
				regWin.setVisible(false);
				Clients.showNotification("<p>Congratulations! </p> "
						+ "<p>User " + username + " has been registered. </p>");
				
			} else {
				regWin.setVisible(false);
				Clients.showNotification("User " + username + " has 'already' been registered. <br/>");
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
			} else {
				Messagebox.show("Please upload a file with '.png' or '.jpg' or '.jpeg' extension." , "Error", Messagebox.OK, Messagebox.ERROR);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
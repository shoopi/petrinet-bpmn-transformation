package nl.tue.ieis.is.bpmGame.controller;


import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import nl.tue.ieis.is.bpmGame.activiti.UserFunctions;

import org.activiti.engine.identity.User;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Button;
import org.zkoss.zul.Div;
import org.zkoss.zul.Image;
import org.zkoss.zul.Label;
import org.zkoss.zul.Window;


public class BannerController extends SelectorComposer<Component> {

	private static final long serialVersionUID = -6360927128036523270L;
	
	@Wire	private 	Window 	loginWin, regWin;
	@Wire	private 	Button 	initLoginBtn, initRegBtn, logoutBtn;
	@Wire	private 	Div 	userInfo, bannerWin;
	@Wire	private 	Image 	userPic;
	@Wire 	private 	Label 	userLabel;
		
	public void doAfterCompose(Component comp) throws Exception {
		super.doAfterCompose(comp);
		setUserBanner();
	}
	
	@Listen("onClick = #initLoginBtn")
	public void showLoginWin() {
		try{
			bannerWin.appendChild(loginWin);
		} catch (Exception e) { System.out.println(e.getMessage());}
		if (!loginWin.isVisible())
			loginWin.setVisible(true);
		loginWin.doHighlighted();
	}

	@Listen("onClick = #logoutBtn")
	public void logout() {
		User user = (User)(Sessions.getCurrent()).getAttribute("user");
		(Sessions.getCurrent()).setAttribute("user", null);
		(Sessions.getCurrent()).setAttribute("selected", null);
		System.out.println("User " + user.getFirstName() + " has been logged out.");
		Executions.sendRedirect("");
	}
	
	@Listen("onClick = #initRegBtn")
	public void showRegWin() {
		try{
			bannerWin.appendChild(regWin);
		} catch (Exception e) { System.out.println(e.getMessage());}
		if (!regWin.isVisible())
			regWin.setVisible(true);
		regWin.doHighlighted();
	}
	
	@Listen("onOK = #loginWin")
	public void onOkLoginWindow(){
		setUserBanner();
	}
	
	private void setUserBanner() {
		User user = (User)(Sessions.getCurrent()).getAttribute("user");
		if(user != null) {
			userInfo.setVisible(true);
			UserFunctions uf = new UserFunctions();
			InputStream img = uf.loadUserProfilePic(user.getId());
			if(img != null) {
				BufferedImage img2;
				try {
					img2 = ImageIO.read(img);
					userPic.setContent(img2);
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				userPic.setSrc("/imgs/admin.png");
			}
			initLoginBtn.setVisible(false);
			initRegBtn.setVisible(false);
			userLabel.setValue(user.getFirstName() + " " + user.getLastName());
			logoutBtn.setVisible(true);
		} else {			
			userInfo.setVisible(false);
			initLoginBtn.setVisible(true);
			initRegBtn.setVisible(true);
			userLabel.setValue("No User");
			logoutBtn.setVisible(false);
			userPic.setSrc("/imgs/admin.png");
		}
	}
}

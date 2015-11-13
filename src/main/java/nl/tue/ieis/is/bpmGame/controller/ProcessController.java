package main.java.nl.tue.ieis.is.bpmGame.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import javax.imageio.ImageIO;

import main.java.nl.tue.ieis.is.bpmGame.activiti.ProcessDefinitionFunctions;
import main.java.nl.tue.ieis.is.bpmGame.bpmnParser.*;
import main.java.nl.tue.ieis.is.correlation.graph.GraphUtil;
import main.java.nl.tue.tm.is.ptnet.Node;
import main.java.nl.tue.tm.is.ptnet.PTNet;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.identity.User;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Button;
import org.zkoss.zul.Div;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Image;
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Vbox;
import org.zkoss.zul.Window;

public class ProcessController extends SelectorComposer<Component> {

	private static final long serialVersionUID = -9000079319525018613L;
	@Wire	private 	Button 	uploadBtn;
	@Wire	private 	Image 	processImage;
	@Wire	private 	Image 	petriNetImage;
	@Wire	private 	Vbox 	mainVbox;
	@Wire	private 	Window 	erroWin;
	@Wire	private 	Label 	errorLbl;
	@Wire	private 	Button 	showErrorBtn;
	@Wire 	private 	Div 	mainDiv;
	@Wire	private		Button 	downloadPnml;
	
	private ProcessDefinitionFunctions defFunc = new ProcessDefinitionFunctions();
	private PTNet generatedPetrinet = null;
	
	public void doAfterCompose(Component comp) throws Exception {
		super.doAfterCompose(comp);
		User user = (User) (Sessions.getCurrent()).getAttribute("user");
		if(user == null) { 
			uploadBtn.setDisabled(true); 
			showErrorBtn.setDisabled(true);
			downloadPnml.setDisabled(true);
		} else {
			uploadBtn.setDisabled(false);
			showErrorBtn.setDisabled(false); 
			String specId = (String) (Sessions.getCurrent()).getAttribute("selected");
			if(specId != null) {
				InputStream img = defFunc.generateProcessImageWithDefinitionId(specId);
				BufferedImage img2 = ImageIO.read(img);
				processImage.setContent(img2);
				
				PTNet output = BPMN2PetriNetsMapping.doMapping(specId);
				output.setFileName(specId);
				Set<Node> nodes = new HashSet<Node>();
				nodes.addAll(output.places());
				nodes.addAll(output.transitions());

				GraphUtil graphDrawer = new GraphUtil(nodes, output.arcs());
				graphDrawer.draw("Test Petri Net");
				BufferedImage img3 = graphDrawer.generatePicture("PICTURE.PNG");
				petriNetImage.setContent(img3);
				generatedPetrinet = output;
				if(generatedPetrinet != null) 
					downloadPnml.setDisabled(false);
				else 
					downloadPnml.setDisabled(true);
			} else {
				downloadPnml.setDisabled(true);
			}
		}
	}
	
	@Listen("onUpload = #uploadBtn")
	public void uploadSpecification(UploadEvent event) {
		org.zkoss.util.media.Media media = event.getMedia();
		try {
			String name = media.getName();
			if(!name.substring(name.length()-5, name.length()).contentEquals(".bpmn")) {
				Messagebox.show("Please upload a file with .bpmn extension." , "Error", Messagebox.OK, Messagebox.ERROR);
			} else {
				User user = (User) (Sessions.getCurrent()).getAttribute("user");
				String deploymentId = defFunc.deployProcessDefinition(user.getId(), name, media.getStreamData());
				try {
					if(deploymentId != null) {
						InputStream img = defFunc.generateProcessImageWithDeploymentId(deploymentId);
						BufferedImage img2 = ImageIO.read(img);
						processImage.setContent(img2);
					} else {
						showErrorWindow();
					}
				} catch (Exception e) {
					ErrorController.errors.add(e.getMessage());
					showErrorWindow();
				}
			}
			
		} catch (Exception e) {
			if(e instanceof ActivitiIllegalArgumentException)
				Clients.showNotification("Please try again...", "error", mainVbox, "middle_center", 5000, true);
			else
				e.printStackTrace();
		}
	}
	
	@Listen("onClick=#showErrorBtn")
	public void showErrorWindow() {
		try{
			mainDiv.appendChild(erroWin);
		} catch (Exception e1) { 
			e1.printStackTrace();
			System.out.println(e1.getMessage());
		}
		if (!erroWin.isVisible()) 
			erroWin.setVisible(true);
		erroWin.doOverlapped();
	}
	
	@Listen("onClick=#downloadPnml")
	public void downloadPnml() {
		if(generatedPetrinet != null) {
			try {
				File file = generatedPetrinet.exportToPNML(generatedPetrinet.getFileName() + ".pnml", PTNet.PNML_PNK);
				Filedownload.save(file, "application/xml");
			} catch (IOException e) {
				ErrorController.errors.add(e.getMessage());
			}
		} else {
			ErrorController.errors.add("No petri net has been selected.");
		}
	}
}

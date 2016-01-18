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
import main.java.nl.tue.ieis.is.bpmGame.data.TableConfiguration;
import main.java.nl.tue.ieis.is.correlation.graph.GraphUtil;
import main.java.nl.tue.tm.is.ptnet.Node;
import main.java.nl.tue.tm.is.ptnet.PTNet;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.identity.User;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.EventListener;
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
import org.zkoss.zul.Messagebox.ClickEvent;

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
	private TableConfiguration tc = new TableConfiguration();
	
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
			String deploymentId = (String) (Sessions.getCurrent()).getAttribute("selected");
			String filename = (String) (Sessions.getCurrent()).getAttribute("filename");
			if(deploymentId != null) {
				String specId = defFunc.getProcessDefinitionId(deploymentId);
				if(specId != null) {
					InputStream img = defFunc.generateProcessImageWithDefinitionId(specId);
					BufferedImage img2 = ImageIO.read(img);
					processImage.setContent(img2);
					drawPetrinet(specId, filename);
				}
			} else {
				downloadPnml.setDisabled(true);
			}
		}
	}
	
	private void drawPetrinet(String specId, String outputFilename) {
		PTNet output = BPMN2PetriNetsMapping.doMapping(specId, outputFilename);
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
	}
	
	@Listen("onUpload = #uploadBtn")
	public void uploadSpecification(UploadEvent event) {
		final org.zkoss.util.media.Media media = event.getMedia();
		try {
			final String Filename = media.getName();
			if(!Filename.substring(Filename.length()-5, Filename.length()).contentEquals(".bpmn")) {
				Messagebox.show("Please upload a file with .bpmn extension." , "Error", Messagebox.OK, Messagebox.ERROR);
			} else {
				//final String name = Filename.substring(0, Filename.length()-5);
				final String name = Filename;
				final User user = (User) (Sessions.getCurrent()).getAttribute("user");
				final String deploymentId = defFunc.deployProcessDefinition(user.getId(), name, media.getStreamData(), 0);
				try {
					if(deploymentId != null) {
						if(deploymentId.contentEquals("DUPLICATED_FILE_NAME")) {
							String message = "There is already a file with the same name in your repository. "
									+ "Press 'IGNORE' to REPLACE the existing file. "
									+ "Press 'RETRY' to keep both files with a duplication star."
									+ "Press 'CANCEL' to cancel the opertion.";
							Messagebox.Button[] buttons = { Messagebox.Button.IGNORE, Messagebox.Button.RETRY, Messagebox.Button.CANCEL}; 
							String icon = Messagebox.ERROR;
							EventListener<ClickEvent> eventListener2 = new EventListener<ClickEvent>() {
								public void onEvent(ClickEvent event) {
									if (Messagebox.ON_RETRY.equals(event.getName())) {
										int dupIndex = 1;
										String newName = name + "+";
										try {
											String newDeploymentId = defFunc.deployProcessDefinition(user.getId(), newName, media.getStreamData(), 1);
											while (newDeploymentId.contentEquals("DUPLICATED_FILE_NAME")) {
												newName = newName + "+";
												dupIndex = dupIndex + 1;
												newDeploymentId = defFunc.deployProcessDefinition(user.getId(), newName, media.getStreamData(), dupIndex);
											} 
											try {
												(Sessions.getCurrent()).setAttribute("filename" , newName);
												deployAndShow(newDeploymentId, newName);
											} catch (IOException e) {
												e.printStackTrace();
											}	
										} catch (Exception e) {
												e.printStackTrace();
										 }							
									} else if(Messagebox.ON_IGNORE.equals(event.getName())) { 
										String depId = tc.laodSingleDeploymentId(user.getId(), name);
										defFunc.deleteProcessModelFromRepositoryByDeploymentId(depId);
										String newDeploymentId = defFunc.deployProcessDefinition(user.getId(), name, media.getStreamData(), 0);
										(Sessions.getCurrent()).setAttribute("filename" , name);
										try {
											//deployAndShow(newDeploymentId);
											Executions.sendRedirect("");
										} catch (Exception e) {
											e.printStackTrace();
										}
									} else {
										return;
									}
								}
							 };
							 Messagebox.show(message, "WARNING - Duplicated Filename", buttons, icon, eventListener2);
						} else {
							//Show Uploaded BPMN File
							(Sessions.getCurrent()).setAttribute("filename" , name);
							deployAndShow(deploymentId, name);
						}
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
	
	private void deployAndShow(final String deploymentId, String outputFilename) throws IOException {
		InputStream img = defFunc.generateProcessImageWithDeploymentId(deploymentId);
		BufferedImage img2 = ImageIO.read(img);
		processImage.setContent(img2);
		String specId = defFunc.getProcessDefinitionId(deploymentId);
		drawPetrinet(specId, outputFilename);
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
				String filename = "";
				String tempFilenam = (String) (Sessions.getCurrent()).getAttribute("filename");
				if(tempFilenam != null) {
					filename = tempFilenam;
				} else {
					filename = generatedPetrinet.getFileName().replace(':', '_');
				}
				File file = generatedPetrinet.exportToPNML(filename + ".pnml", PTNet.PNML_PNK);
				Filedownload.save(file, "application/xml");
			} catch (IOException e) {
				ErrorController.errors.add(e.getMessage());
			}
		} else {
			ErrorController.errors.add("No petri net has been selected.");
		}
	}
}

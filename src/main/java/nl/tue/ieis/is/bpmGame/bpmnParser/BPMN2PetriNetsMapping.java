package main.java.nl.tue.ieis.is.bpmGame.bpmnParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.*;

import main.java.nl.tue.ieis.is.bpmGame.activiti.ProcessDefinitionFunctions;
import main.java.nl.tue.ieis.is.bpmGame.controller.ErrorController;
import main.java.nl.tue.tm.is.ptnet.Arc;
import main.java.nl.tue.tm.is.ptnet.Node;
import main.java.nl.tue.tm.is.ptnet.PTNet;
import main.java.nl.tue.tm.is.ptnet.Place;
import main.java.nl.tue.tm.is.ptnet.Transition;


public class BPMN2PetriNetsMapping {
	
	//private static Map<String, List<String>> extraPlaces = new HashMap<String, List<String>>();
	//private static Map<String, List<String>> extraTransitions = new HashMap<String, List<String>>();
	
	public static PTNet doMapping(String processDefinitionId, String outputFilename) {
		ProcessDefinitionFunctions defFunc = new ProcessDefinitionFunctions();
		PTNet output = new PTNet();
		
		BpmnModel bpmn = defFunc.getBpmnModel(processDefinitionId);
		for(Process process : bpmn.getProcesses()) {
			
			Map<String, List<String>> xorJoinInExtraTransitions = new HashMap<String, List<String>>();
			Map<String, List<String>> xorSplitInExtraTransitions = new HashMap<String, List<String>>();
			Map<String, List<String>> xorJoinOutExtraTransitions = new HashMap<String, List<String>>();
			Map<String, List<String>> xorSplitOutExtraTransitions = new HashMap<String, List<String>>();
			List<String> addedPlaces = new ArrayList<String>();
			List<SequenceFlow> allFlows = new ArrayList<SequenceFlow>();
			List<SequenceFlow> null2nullFlows = new ArrayList<SequenceFlow>();
			Map<String, Place> xorPlaceMap = new HashMap<String, Place>();
			
			for(FlowElement fe : process.getFlowElements()) {
				if (fe instanceof EndEvent) {
					mapEndEvent(output, fe);
				} 
				else if(fe instanceof StartEvent) {
					mapStartEvent(output, fe);
				} 
				else if (fe instanceof Activity || fe instanceof Event) {
					Transition t = new Transition(fe.getId(), fe.getName());
					output.addTransition(t);
				}
				else if (fe instanceof ParallelGateway) {
					mapParallelGateway(output, fe);
				}
				
				else if (fe instanceof ExclusiveGateway) {
					mapExclusiveGateway(output, xorJoinInExtraTransitions,
							xorSplitInExtraTransitions,
							xorJoinOutExtraTransitions,
							xorSplitOutExtraTransitions, fe);
					
				} else if (fe instanceof EventGateway) {
					mapEventGateway(output, addedPlaces, fe);
				} else if (fe instanceof SequenceFlow) {
					allFlows.add((SequenceFlow) fe);
				}
			}
			
			for(SequenceFlow fe : allFlows) {
				String sourceId = fe.getSourceRef();
				String targetId = fe.getTargetRef();
				
				Node n1 = output.findNode(sourceId);
				Node n2 = output.findNode(targetId);
				Transition t1 = null, t2 = null;
				Place p1 = null, p2 = null;
				
				if(n1 != null && n1 instanceof Transition)
					t1 = output.findTransition(sourceId);
				if(n2 != null && n2 instanceof Transition)
					t2 = output.findTransition(targetId);
				
				if(n1 != null && n1 instanceof Place)
					p1 = output.findPlace(sourceId);
				if(n2 != null && n2 instanceof Place)
					p2 = output.findPlace(targetId);
				
				if(p1 == null && p2 == null) {
					if(t1 != null && t2 != null)
						constructArcBetweenTwoTransitions(output, t1, t2);
					else if(t1 != null && t2 == null) {
						String id = targetId;
						if(xorSplitInExtraTransitions.containsKey(id)) {
							List<String> remainingTransitions = xorSplitInExtraTransitions.get(id);
							Place p = new Place(UUID.randomUUID().toString(), "");
							output.addPlace(p);
							output.addArc(new Arc(UUID.randomUUID().toString(), t1, p));
							while(remainingTransitions.size() > 0) {
								Transition t = output.findTransition(remainingTransitions.get(0));
								if(t != null) {
									output.addArc(new Arc(UUID.randomUUID().toString(), p, t));
									remainingTransitions.remove(t.getId());
								}
							}
							xorSplitInExtraTransitions.put(id, remainingTransitions);
							xorPlaceMap.put(id, p);
						} else if(xorJoinInExtraTransitions.containsKey(id)) {
							List<String> remainingTransitions = xorJoinInExtraTransitions.get(id);
							if(remainingTransitions.size() > 0) {
								Place p = new Place(UUID.randomUUID().toString(), "");
								output.addPlace(p);
								output.addArc(new Arc(UUID.randomUUID().toString(), t1, p));
								Transition t = output.findTransition(remainingTransitions.get(0));
								if(t != null) {
									output.addArc(new Arc(UUID.randomUUID().toString(), p, t));
									remainingTransitions.remove(t.getId());
									xorJoinInExtraTransitions.put(id, remainingTransitions);
								}
							}
						}
					}
					else if(t1 == null && t2 != null) {
						String id = sourceId;
						if(xorJoinOutExtraTransitions.containsKey(id)) {
							List<String> remainingTransitions = xorJoinOutExtraTransitions.get(id);
							Place p = new Place(UUID.randomUUID().toString(), "");
							output.addPlace(p);
							output.addArc(new Arc(UUID.randomUUID().toString(), p, t2));
							while(remainingTransitions.size() > 0) {
								Transition t = output.findTransition(remainingTransitions.get(0));
								if(t != null) {
									output.addArc(new Arc(UUID.randomUUID().toString(), t, p));
									remainingTransitions.remove(t.getId());
								}
							}
							xorPlaceMap.put(id, p);
							xorJoinOutExtraTransitions.put(id, remainingTransitions);
						} else if(xorSplitOutExtraTransitions.containsKey(id)) {
							List<String> remainingTransitions = xorSplitOutExtraTransitions.get(id);
							if(remainingTransitions.size() > 0) {
								Place p = new Place(UUID.randomUUID().toString(), "");
								output.addPlace(p);
								output.addArc(new Arc(UUID.randomUUID().toString(), p, t2));
								Transition t = output.findTransition(remainingTransitions.get(0));
								if(t != null) {
									output.addArc(new Arc(UUID.randomUUID().toString(), t, p));
									remainingTransitions.remove(t.getId());
									xorSplitOutExtraTransitions.put(id, remainingTransitions);
								}
							}
						}
					} else if (t1 == null && t2 == null) {
						null2nullFlows.add(fe);
					}
				} else {
					if(p1 != null && t2 != null) 
						output.addArc(new Arc(UUID.randomUUID().toString(), p1, t2));
					else if (t1 != null && p2 != null)
						output.addArc(new Arc(UUID.randomUUID().toString(), t1, p2));
				}
				
			}
				
			for(SequenceFlow fe : null2nullFlows) {
				if(xorJoinOutExtraTransitions.containsKey(fe.getSourceRef()) && xorSplitInExtraTransitions.containsKey(fe.getTargetRef())) {
					if(xorPlaceMap.containsKey(fe.getSourceRef()) && xorPlaceMap.containsKey(fe.getTargetRef())) {
						Place source = xorPlaceMap.get(fe.getSourceRef());
						Place target = xorPlaceMap.get(fe.getTargetRef());
						Transition t = new Transition(UUID.randomUUID().toString(), "[SILENT]");
						output.addTransition(t);
						output.addArc(new Arc(UUID.randomUUID().toString(), source, t));
						output.addArc(new Arc(UUID.randomUUID().toString(), t, target));
					} else {
						Place p1 = null, p2 = null;						
						List<String> remainingTransitions1 = xorJoinOutExtraTransitions.get(fe.getSourceRef());
						p1 = new Place(UUID.randomUUID().toString(), "");
						output.addPlace(p1);
						while(remainingTransitions1.size() > 0) {
							Transition t = output.findTransition(remainingTransitions1.get(0));
							if(t != null) {
								output.addArc(new Arc(UUID.randomUUID().toString(), t, p1));
								remainingTransitions1.remove(t.getId());
							}
						}
						xorPlaceMap.put(fe.getSourceRef(), p1);
						xorJoinOutExtraTransitions.put(fe.getSourceRef(), remainingTransitions1);
						
						List<String> remainingTransitions2 = xorSplitInExtraTransitions.get(fe.getTargetRef());
						p2 = new Place(UUID.randomUUID().toString(), "");
						output.addPlace(p2);
						while(remainingTransitions2.size() > 0) {
							Transition t = output.findTransition(remainingTransitions2.get(0));
							if(t != null) {
								output.addArc(new Arc(UUID.randomUUID().toString(), p2, t));
								remainingTransitions2.remove(t.getId());
							}
						}
						xorSplitInExtraTransitions.put(fe.getTargetRef(), remainingTransitions2);
						xorPlaceMap.put(fe.getTargetRef(), p2);
						
						if(p1 != null && p2 != null) {
							Transition t = new Transition(UUID.randomUUID().toString(), "[SILENT]");
							output.addTransition(t);
							output.addArc(new Arc(UUID.randomUUID().toString(), p1, t));
							output.addArc(new Arc(UUID.randomUUID().toString(), t, p2));
						}
					}
						
				} else if (xorSplitOutExtraTransitions.containsKey(fe.getSourceRef()) && xorJoinInExtraTransitions.containsKey(fe.getTargetRef())) {
					//TODO
					Transition t1 = null, t2 = null;
					List<String> remainingTransitions1 = xorSplitOutExtraTransitions.get(fe.getSourceRef());

					if(remainingTransitions1.size() > 0) {
						t1 = output.findTransition(remainingTransitions1.get(0));
					}
					
					List<String> remainingTransitions2 = xorJoinInExtraTransitions.get(fe.getTargetRef());
					if(remainingTransitions2.size() > 0) {
						t2 = output.findTransition(remainingTransitions2.get(0));
					}
					if(t1 != null && t2 != null) {
						remainingTransitions1.remove(t1.getId());
						remainingTransitions2.remove(t2.getId());
						
						xorSplitOutExtraTransitions.put(fe.getSourceRef(), remainingTransitions1);
						xorJoinInExtraTransitions.put(fe.getTargetRef(), remainingTransitions2);
						
						constructArcBetweenTwoTransitions(output, t1, t2);
					}
				}
			}
		}
		output.setFileName(outputFilename);
		return output;
	}

	private static void mapEventGateway(PTNet output, List<String> addedPlaces, FlowElement fe) {
		EventGateway pg = (EventGateway)fe;
		if(pg.getIncomingFlows().size() > 1 && pg.getOutgoingFlows().size() > 1) {
			ErrorController.errors.add("Model is not structrued. EventGateway [ID: " + pg.getId() + " || Name: " + pg.getName() + 
					"] has " + pg.getIncomingFlows().size() +  " incoming and " + pg.getOutgoingFlows().size() + " outgoing flows.");
			return;
		} else if (pg.getOutgoingFlows().size() > 1) {
			Place p = new Place(fe.getId(), "");
			output.addPlace(p);
			addedPlaces.add(p.getId());
			for(int i = 1; i <= pg.getOutgoingFlows().size(); i++) {}
		} else if (pg.getIncomingFlows().size() > 1) {
			for(int i = 1; i <= pg.getIncomingFlows().size(); i++) {}
		}
	}
	
	private static void mapExclusiveGateway(PTNet output,
			Map<String, List<String>> xorJoinInExtraTransitions,
			Map<String, List<String>> xorSplitInExtraTransitions,
			Map<String, List<String>> xorJoinOutExtraTransitions,
			Map<String, List<String>> xorSplitOutExtraTransitions,
			FlowElement fe) {
		ExclusiveGateway pg = (ExclusiveGateway)fe;
		List<String> incomingTransitions = new ArrayList<String>();
		List<String> outgoingTransitions = new ArrayList<String>();

		if(pg.getIncomingFlows().size() > 1 && pg.getOutgoingFlows().size() > 1) {
			ErrorController.errors.add("Model is not structrued. ExclusiveGateway [ID: " + pg.getId() + " || Name: " + pg.getName() + 
					"] has " + pg.getIncomingFlows().size() +  " incoming and " + pg.getOutgoingFlows().size() + " outgoing flows.");
			return;
		} else if (pg.getOutgoingFlows().size() > 1) {
			for(int i = 1; i <= pg.getOutgoingFlows().size(); i++) {
				String condition = pg.getOutgoingFlows().get(i-1).getConditionExpression();
				if(condition == null) condition = fe.getId();
				Transition t = new Transition(pg.getId() + i, "[CONDITION] " + condition);
				output.addTransition(t);
				incomingTransitions.add(t.getId());	
				outgoingTransitions.add(t.getId());	

			}
			xorSplitInExtraTransitions.put(fe.getId(), incomingTransitions);
			xorSplitOutExtraTransitions.put(fe.getId(), outgoingTransitions);
			
		} else if (pg.getIncomingFlows().size() > 1) {
			for(int i = 1; i <= pg.getIncomingFlows().size(); i++) {
				String name = fe.getName();
				if(name == null) name = fe.getId();
				Transition t = new Transition(pg.getId() + i, "[SILENT] " + name);
				output.addTransition(t);
				incomingTransitions.add(t.getId());
				outgoingTransitions.add(t.getId());	
			}
			xorJoinInExtraTransitions.put(fe.getId(), incomingTransitions);
			xorJoinOutExtraTransitions.put(fe.getId(), outgoingTransitions);
		}
	}

	private static void mapParallelGateway(PTNet output, FlowElement fe) {
		ParallelGateway pg = (ParallelGateway)fe;
		String name = fe.getName();
		if(pg.getIncomingFlows().size() > 1 && pg.getOutgoingFlows().size() > 1) {
			//model is not structured.
			ErrorController.errors.add("Model is not structrued. ParallelGateway [ID: " + pg.getId() + " || Name: " + pg.getName() + 
					"] has " + pg.getIncomingFlows().size() +  " incoming and " + pg.getOutgoingFlows().size() + " outgoing flows.");
			return;
			} else if (pg.getOutgoingFlows().size() > 1) {
				name = "[AND-split] " + name;
			} else if (pg.getIncomingFlows().size() > 1) {
				name = "[AND-join] " + name;
		}
		Transition t = new Transition(fe.getId(), name);
		output.addTransition(t);
	}

	private static void mapStartEvent(PTNet output, FlowElement fe) {
		Place p = new Place("p_" + fe.getId(), "");
		String name = "Start Event";
		if(fe.getName() != null) name = fe.getName();
		Transition t = new Transition(fe.getId(), name);
		output.addPlace(p);
		output.addTransition(t);
		output.addArc(new Arc(UUID.randomUUID().toString(), p, t));
	}

	private static void mapEndEvent(PTNet output, FlowElement fe) {
		Place p = new Place("p_" + fe.getId(), "");
		String name = "End Event";
		if(fe.getName() != null) name = fe.getName();
		Transition t = new Transition(fe.getId(), name);
		output.addPlace(p);
		output.addTransition(t);
		output.addArc(new Arc(UUID.randomUUID().toString(), t, p));
	}

	private static void constructArcBetweenTwoTransitions(PTNet output, Transition t1, Transition t2) {
		Place p = new Place(UUID.randomUUID().toString(), "");
		output.addPlace(p);
		output.addArc(new Arc(UUID.randomUUID().toString(), t1, p));
		output.addArc(new Arc(UUID.randomUUID().toString(), p, t2));
	}
}
	
	/*
	 public static PTNet doMapping(String processDefinitionId) {
		ProcessDefinitionFunctions defFunc = new ProcessDefinitionFunctions();
		PTNet output = new PTNet();
		BpmnModel bpmn = defFunc.getBpmnModel(processDefinitionId);
		for(Process process : bpmn.getProcesses()) {
				for(FlowElement fe : process.getFlowElements()) {
					if (fe instanceof EndEvent) {
						Place p = new Place("p_" + fe.getId(), "P: " + fe.getName());
						Transition t = new Transition("t_" + fe.getId(), "T: " + fe.getName());
						output.addPlace(p);
						output.addTransition(t);
						output.addArc(new Arc(UUID.randomUUID().toString(), p, t));
						Place p2 = new Place("p_" + UUID.randomUUID(), "P: End");
						output.addPlace(p2);
						output.addArc(new Arc(UUID.randomUUID().toString(), t, p2));
					} 
					else if(fe instanceof StartEvent || fe instanceof Activity || fe instanceof Event) {
						Place p = new Place("p_" + fe.getId(), "P: " + fe.getName());
						Transition t = new Transition("t_" + fe.getId(), "T: " + fe.getName());
						output.addPlace(p);
						output.addTransition(t);
						output.addArc(new Arc(UUID.randomUUID().toString(), p, t));
					} 
					
					else if (fe instanceof ParallelGateway) {
						ParallelGateway pg = (ParallelGateway)fe;
						if(pg.getIncomingFlows().size() > 1 && pg.getOutgoingFlows().size() > 1) {
							//model is not structured.
							ErrorController.errors.add("Model is not structrued. ParallelGateway [ID: " + pg.getId() + " || Name: " + pg.getName() + 
									"] has " + pg.getIncomingFlows().size() +  " incoming and " + pg.getOutgoingFlows().size() + " outgoing flows.");
							return null;
						} else if (pg.getOutgoingFlows().size() > 1) {
							//and-split
							Place p = new Place("p_" + fe.getId(), "P: " + fe.getName());
							Transition t = new Transition("t_" + fe.getId(), "[AND-split] T: " + fe.getName());
							output.addPlace(p);
							output.addTransition(t);
							output.addArc(new Arc(UUID.randomUUID().toString(), p, t));
							
						} else if (pg.getIncomingFlows().size() > 1) {
							//and-join
							Transition t = new Transition("t_" + fe.getId(), "[AND-join] T: " + fe.getName());
							output.addTransition(t);

							List<String> places = new ArrayList<String>();
							for(int i = 1; i <= pg.getIncomingFlows().size(); i++) {
								Place p = new Place("p_(" + i + ")" + fe.getId(), "P(" + i + "): " + fe.getName());
								places.add(p.getId());
								output.addPlace(p);
								output.addArc(new Arc(UUID.randomUUID().toString(), p, t));
							}
							extraPlaces.put("p_" + fe.getId(), places);
						}
						
					} 
					
					
					else if (fe instanceof ExclusiveGateway) {
						
						ExclusiveGateway pg = (ExclusiveGateway)fe;
						if(pg.getIncomingFlows().size() > 1 && pg.getOutgoingFlows().size() > 1) {
							//model is not structured.
							ErrorController.errors.add("Model is not structrued. ExclusiveGateway [ID: " + pg.getId() + " || Name: " + pg.getName() + 
									"] has " + pg.getIncomingFlows().size() +  " incoming and " + pg.getOutgoingFlows().size() + " outgoing flows.");
							return null;
						} else if (pg.getOutgoingFlows().size() > 1) {
							//XOR-split
							Place p = new Place("p_" + fe.getId(), "[XOR-split] P: " + fe.getName());
							output.addPlace(p);
							
							List<String> transitions = new ArrayList<String>();
							for(int i = 1; i <= pg.getOutgoingFlows().size(); i++) {
								Transition t = new Transition("t_(" + i + ")" + fe.getId(), "[Condition] T(" + i + "): " + fe.getName());
								transitions.add(t.getId());
								output.addTransition(t);
								output.addArc(new Arc(UUID.randomUUID().toString(), p, t));
							}
							extraTransitions.put("t_" + fe.getId(), transitions);
							
						} else if (pg.getIncomingFlows().size() > 1) {
							//XOR-join
							Place p_join = new Place("p_(" + (pg.getIncomingFlows().size() + 1) + ")" + fe.getId(), "[XOR-join] P: " + fe.getName());
							output.addPlace(p_join);
							Transition t_join = new Transition("t_(" + (pg.getIncomingFlows().size() + 1) + ")" + fe.getId(), "[SILENT] T: " + fe.getName());
							output.addTransition(t_join);
							output.addArc(new Arc(UUID.randomUUID().toString(), p_join, t_join));
							
							List<String> places = new ArrayList<String>();
							List<String> transitions = new ArrayList<String>();
							for(int i = 1; i <= pg.getIncomingFlows().size(); i++) {
								Place p = new Place("p_(" + i + ")" + fe.getId(), "P(" + i + "): " + fe.getName());
								Transition t = new Transition("t_(" + i + ")" + fe.getId(), "T(" + i + "): " + fe.getName());
								output.addPlace(p);
								output.addTransition(t);
								output.addArc(new Arc(UUID.randomUUID().toString(), p, t));
								places.add(p.getId());
								output.addArc(new Arc(UUID.randomUUID().toString(), t, p_join));
							}
							transitions.add(t_join.getId());
							extraPlaces.put("p_" + fe.getId(), places);
							extraTransitions.put("t_" + fe.getId(), transitions);
						}
					} 
					
				}
				
				for(FlowElement fe : process.getFlowElements()) {
					if (fe instanceof SequenceFlow) {
						String sourceId = ((SequenceFlow) fe).getSourceRef();
						String targetId = ((SequenceFlow) fe).getTargetRef();
						Transition t = output.findTransition("t_" + sourceId);
						if(t == null) {
							if(extraTransitions.containsKey("t_" + sourceId)) {
								List<String> remainingTransitions = extraTransitions.get("t_" + sourceId);
								if(remainingTransitions.size() > 0) {
									t = output.findTransition(remainingTransitions.get(0));
									if(t != null) {
										remainingTransitions.remove(t.getId());
										extraTransitions.put("t_" + sourceId, remainingTransitions);
									}
								}
							}
						}
						Place p = output.findPlace("p_" + targetId);
						if(p == null) {
							if(extraPlaces.containsKey("p_" + targetId)) {
								List<String> remainingPlaces = extraPlaces.get("p_" + targetId);
								if(remainingPlaces.size() > 0) {
									p = output.findPlace(remainingPlaces.get(0));
									if(p != null) {
										remainingPlaces.remove(p.getId());
										extraPlaces.put("p_" + targetId, remainingPlaces);
									}
								}
							}
						}
						output.addArc(new Arc(fe.getId(),t,p));
						
					}
				}
		}
		return output;
	}
	*/

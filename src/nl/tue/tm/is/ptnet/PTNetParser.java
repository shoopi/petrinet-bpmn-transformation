package nl.tue.tm.is.ptnet;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class PTNetParser extends DefaultHandler {

	private static final int STATE_NONE = 0;
	private static final int STATE_PLACE = 1;
	private static final int STATE_PLACE_NAME = 2;
	private static final int STATE_PLACE_NAME_VALUE = 3;
	private static final int STATE_PLACE_INITIALMARKING = 4;
	private static final int STATE_PLACE_INITIALMARKING_VALUE = 5;
	private static final int STATE_TRANSITION = 6;
	private static final int STATE_TRANSITION_NAME = 7;
	private static final int STATE_TRANSITION_NAME_VALUE = 8;
	private static final int STATE_ARC = 9;

	private Node currNode;
	private int currState;
	
	PTNet result;
	
	String suffix = "";
	
	public PTNetParser(PTNet result){
		this.result = result;
	}
	public PTNetParser(PTNet result, String suffix){
		this.result = result;
		this.suffix = suffix;
	}	
	
	public void characters(char[] arr, int start, int len) throws SAXException {
		switch (currState){
		case STATE_PLACE_NAME_VALUE:
			currNode.setName(currNode.getName().concat(new String(arr, start, len)));
			break;
		case STATE_PLACE_INITIALMARKING_VALUE:
			try {
					result.addMarking((Place) currNode, Integer.parseInt(new String(arr, start, len)));
				} catch (NumberFormatException e) {
					throw new SAXException("Cannot parse initial marking; wrong number format.");
				}
			break;
		case STATE_TRANSITION_NAME_VALUE:
			String name = currNode.getName().concat(new String(arr, start, len));
			currNode.setName(name);
			break;
		default:
			break;
		}
	}

	public void endElement(String namespace, String lname, String qname) throws SAXException {
		switch (currState){
		case STATE_NONE:
			break;
		case STATE_PLACE:
			if (qname.toLowerCase().equals("place")){
				currState = STATE_NONE;				
			}
			break;
		case STATE_PLACE_NAME:
			if (qname.toLowerCase().equals("name")){
				currState = STATE_PLACE;				
			}
			break;
		case STATE_PLACE_NAME_VALUE:
			if (qname.toLowerCase().equals("value") || qname.toLowerCase().equals("text")){
				currState = STATE_PLACE_NAME;				
			}
			break;
		case STATE_PLACE_INITIALMARKING:
			if (qname.toLowerCase().equals("initialmarking")){
				currState = STATE_PLACE;				
			}
			break;
		case STATE_PLACE_INITIALMARKING_VALUE:
			if (qname.toLowerCase().equals("value")){
				currState = STATE_PLACE_INITIALMARKING;				
			}
			break;
		case STATE_TRANSITION:
			if (qname.toLowerCase().equals("transition")){
				currState = STATE_NONE;				
			}
			break;
		case STATE_TRANSITION_NAME:
			if (qname.toLowerCase().equals("name")){
				currState = STATE_TRANSITION;				
			}
			break;
		case STATE_TRANSITION_NAME_VALUE:
			if (qname.toLowerCase().equals("value") || qname.toLowerCase().equals("text")){
				if (currNode.getName().length()==0){
					currNode.setName(Transition.SILENT_LABEL);
				}
				if (currNode.getName().startsWith(".\\n")){
					currNode.setName(currNode.getName().substring(3));
				}
				currState = STATE_TRANSITION_NAME;				
			}
			break;
		case STATE_ARC:
			if (qname.toLowerCase().equals("arc")){
				currState = STATE_NONE;				
			}
			break;
		default:
			break;
		}
	}

	public void startElement(String namespace, String lname, String qname, Attributes attrs) throws SAXException {
		if (qname.toLowerCase().equals("place")){
			currNode = new Place();
			for (int i = 0; i < attrs.getLength(); i++){
				if (attrs.getQName(i).toLowerCase().equals("id")){
					currNode.setId(attrs.getValue(i).concat(suffix));
				}
			}
			result.addPlace((Place)currNode);
			currState = STATE_PLACE;
		}else if (qname.toLowerCase().equals("transition")){
			currNode = new Transition();
			for (int i = 0; i < attrs.getLength(); i++){
				if (attrs.getQName(i).toLowerCase().equals("id")){
					currNode.setId(attrs.getValue(i).concat(suffix));
				}
			}
			result.addTransition((Transition)currNode);
			currState = STATE_TRANSITION;
		}else if (qname.toLowerCase().equals("arc")){
			Arc arc = new Arc();
			for (int i = 0; i < attrs.getLength(); i++){
				if (attrs.getQName(i).toLowerCase().equals("id")){
					arc.setId(attrs.getValue(i).concat(suffix));
				}else if (attrs.getQName(i).toLowerCase().equals("source")){
					arc.setSource(result.findNode(attrs.getValue(i).concat(suffix)));
				}else if (attrs.getQName(i).toLowerCase().equals("target")){
					arc.setTarget(result.findNode(attrs.getValue(i).concat(suffix)));
				}
			}
			result.addArc(arc);
			currState = STATE_ARC;
		}else if (qname.toLowerCase().equals("name")){
			if (currState == STATE_PLACE){
				currState = STATE_PLACE_NAME;
			}else if (currState == STATE_TRANSITION){
				currState = STATE_TRANSITION_NAME;
			}
		}else if (qname.toLowerCase().equals("initialmarking")){
			if (currState == STATE_PLACE){
				currState = STATE_PLACE_INITIALMARKING;
			}			
		}else if (qname.toLowerCase().equals("value") || qname.toLowerCase().equals("text")){
			if (currState == STATE_PLACE_NAME){
				currState = STATE_PLACE_NAME_VALUE;
			}else if (currState == STATE_PLACE_INITIALMARKING){
				currState = STATE_PLACE_INITIALMARKING_VALUE;
			}else if (currState == STATE_TRANSITION_NAME){
				currState = STATE_TRANSITION_NAME_VALUE;
			} 			
		}
	}

}

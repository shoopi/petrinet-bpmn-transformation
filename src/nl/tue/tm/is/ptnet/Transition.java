package nl.tue.tm.is.ptnet;

public class Transition extends Node{
	
	public static String SILENT_LABEL = "tau";

	public Transition(){
	}
	public Transition(String id){
		this.id = id;
	}
	public Transition(String id, String name){
		this.id = id;
		this.name = name;
	}	
	
	public String toString(){
		return id;
	}
}

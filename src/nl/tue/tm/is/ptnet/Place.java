package nl.tue.tm.is.ptnet;

public class Place extends Node{	

	public Place(){
	}
	public Place(String id){
		this.id = id;
	}
	public Place(String id, String name){
		this.id = id;
		this.name = name;
	}
	
	public String toString(){
		return id;
	}
}

package main.java.nl.tue.tm.is.ptnet;

public class Node {
	String id = "";
	String name = "";
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public boolean equals(Object arg0) {
		if (arg0.getClass().getSimpleName().equals(this.getClass().getSimpleName())){
			return id.equals(((Node)arg0).getId());
		}else{
			return false;
		}
	}
	public int hashCode() {
		return id.hashCode();
	}
}

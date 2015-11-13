package main.java.nl.tue.tm.is.ptnet;

public class Arc{
	String id;
	Node source;
	Node target;
	
	public Arc(){
	}
	public Arc(String id, Node source, Node target){
		this.id = id;
		this.source = source;
		this.target = target;
	}

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public Node getSource() {
		return source;
	}
	public void setSource(Node source) {
		this.source = source;
	}
	public Node getTarget() {
		return target;
	}
	public void setTarget(Node target) {
		this.target = target;
	}
	
	public String toString(){
		return "(" + source.toString() + "," + target.toString() + ")";
	}
	
	public boolean equals(Object arg0) {
		if (arg0 instanceof Arc){
			return id.equals(((Arc)arg0).getId());
		}else{
			return false;
		}
	}
	
	public int hashCode() {
		return id.hashCode();
	}
	
}

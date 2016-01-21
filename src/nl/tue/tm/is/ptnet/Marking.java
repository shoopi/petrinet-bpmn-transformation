package nl.tue.tm.is.ptnet;

import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;

public class Marking{
	
	Map<Place,Integer> marking;
	
	public Marking(){
		marking = new HashMap<Place,Integer>();
	}
	
	public void addMark(Place p, Integer m){
		marking.put(p,m);
	}
	public Integer getMark(Place p){
		return marking.get(p);
	}

	public String toString() {
		String result = "{";
		
		for (Iterator<Place> i = marking.keySet().iterator();i.hasNext();){
			Place p = i.next();
			result += p + "->" + marking.get(p);
			if (i.hasNext()){result += ",";}
		}
		
		result += "}";
		return result;
	}
	
	public Map<Place,Integer> getMap(){
		return marking;
	}
	
	public Set<Map.Entry<Place,Integer>> entrySet(){
		return marking.entrySet();
	}

	public boolean equals(Object arg0) {
		if (arg0 instanceof Marking){
			return marking.equals(((Marking)arg0).getMap());
		}
		return super.equals(arg0);
	}

	public int hashCode() {
		return marking.hashCode();
	}
		
}
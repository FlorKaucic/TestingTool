package core;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class ParsedFile {
	private String name;
	private ArrayList<ParsedClass> classes;
	
	public ParsedFile(String name) {
		this.name = name;
		this.classes = new ArrayList<ParsedClass>();
	}
	
	public String getName(){
		return this.name;
	}
	
	public String[] getClassList(){
		return this.classes
				.stream()
				.map(c->c.getName())
				.toArray(size -> new String[size]);
	}
	
	public ArrayList<ParsedClass> getClasses(){
		return this.classes;
	}

	public String[] getMethodsFrom(String className) {
		return this.classes
						.stream()
						.filter(x -> x.getName() == className)
						.limit(1)
						.collect(Collectors.toList())
						.get(0)
						.getMethodList();
	}

	public ParsedMethod getMethod(String className, String methodName) {
		return this.classes
				.stream()
				.filter(x -> x.getName() == className)
				.limit(1)
				.collect(Collectors.toList())
				.get(0)
				.getMethod(methodName);
	}
	
	public void addClass(ParsedClass item) {
		this.classes.add(item);		
	}

	public void addClass(ArrayList<ParsedClass> items) {
		this.classes.addAll(items);		
	}
}

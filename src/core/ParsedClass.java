package core;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class ParsedClass extends ParsedFile {
	private int start;
	private int end;
	private ArrayList<ParsedMethod> methods;
	
	public ParsedClass(String name, int start, int end){
		super(name);
		this.start = start;
		this.end = end;
		methods = new ArrayList<ParsedMethod>();
	}
	
	public int getStart() {
		return start;
	}

	public int getEnd() {
		return end;
	}	
	
	public void addMethod(ParsedMethod item){
		methods.add(item);
	}
	
	public void addMethod(ArrayList<ParsedMethod> items) {
		methods.addAll(items);
	}
	
	public String[] getMethodList(){
		return this.methods
				.stream()
				.map(m->m.getName())
				.toArray(size -> new String[size]);
	}

	public ParsedMethod getMethod(String methodName) {
		return this.methods
				.stream()
				.filter(x -> x.getName() == methodName)
				.limit(1)
				.collect(Collectors.toList())
				.get(0);
	}
	
	public ArrayList<ParsedMethod> getMethods(){
		return this.methods;
	}
}

package handler.parser;

import java.io.IOException;

import core.ParsedClass;
import core.ParsedFile;
import core.ParsedMethod;
import core.file.FileLoader;
import core.parser.Parser;

public class ParserHandler {
	private String source;
	private Parser parser;
	private ParsedFile file;
	private String className;
	private String methodName;

	public ParserHandler(){
		this.source = "";
		this.parser = new Parser();
		this.file = null;
		this.className = null;
		this.methodName = null;
	}
	
	public void loadSource(String path) throws IOException{
		this.source = FileLoader.readFile(path);
		this.file = new ParsedFile(path);
	}
	
	public String[] getClasses() {	
		return this.file.getClassList();
	}
	
	public String[] getMethods(){
		return (this.className!=null)?this.file.getMethodsFrom(this.className):null;
	}

	public ParsedMethod getMethod() {
		return (this.className!=null && this.methodName!=null)?this.file.getMethod(this.className, this.methodName):null;
	}

	public String getSource(int start, int end) {
		return source.substring(start, end);
	}

	public void parse() {
		if(source==null)
			return;
		this.file.addClass(parser.parseFile(source, this.file.getName()));	
		for(ParsedClass c : this.file.getClasses()){
			c.addMethod(parser.parseClass(source.substring(c.getStart(), c.getEnd()), c.getStart()));
			for(ParsedMethod m : c.getMethods()){
				parser.parseMethod(m, source.substring(m.getStart(), m.getEnd()));
			}
		}
	}
	
	public void setMethodName(String methodName){
		this.methodName = methodName;
	}
	
	public void setClassName(String className){
		this.className = className;
	}
}

package core.parser;

import java.util.ArrayList;
import java.util.regex.Pattern;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.WhileStatement;

import core.ParsedClass;
import core.ParsedMethod;

public class Parser {

	public ArrayList<ParsedClass> parseFile(String source) {
		// Metodo que separa el archivo en clases
		// (Obtiene nombre de la clase y donde empieza y termina)
		ASTParser parser = ASTParser.newParser(AST.JLS8);
		parser.setSource(source.toCharArray());
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setResolveBindings(true);

		final CompilationUnit cu = (CompilationUnit) parser.createAST(null);

		ArrayList<ParsedClass> classes = new ArrayList<ParsedClass>();

		// Se podria agregar clases anonimas tambien
		cu.accept(new ASTVisitor() {
			public boolean visit(TypeDeclaration node) {
				classes.add(new ParsedClass(node.getName().toString(), node.getStartPosition(),
						node.getStartPosition() + node.getLength()));
				return true;
			}
		});
		
		return classes;
	}

	public ArrayList<ParsedMethod> parseClass(String source, int classStart) {
		// Metodo que separa la clase en metodos
		// (Obtiene nombre del metodo y donde empieza y termina)
		ASTParser parser = ASTParser.newParser(AST.JLS8);
		parser.setSource(source.toCharArray());
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setResolveBindings(true);

		final CompilationUnit cu = (CompilationUnit) parser.createAST(null);

		ArrayList<ParsedMethod> methods = new ArrayList<ParsedMethod>();

		cu.accept(new ASTVisitor() {
			public boolean visit(MethodDeclaration node) {
				methods.add(new ParsedMethod(node.getName().toString(), classStart + node.getStartPosition(),
						classStart + node.getStartPosition() + node.getLength()));
				return true;
			}
		});
		return methods;
	}

	public void parseMethod(ParsedMethod method, String source){
		// Metodo que al pasarle un metodo, obtiene las metricas pedidas 
		ASTParser parser = ASTParser.newParser(AST.JLS8);
		parser.setSource(source.toCharArray());
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setResolveBindings(true);

		final CompilationUnit cu = (CompilationUnit) parser.createAST(null);
		
		method.setTotalComments(cu.getCommentList().size());
		// CANTIDAD DE LINEAS DE CODIGO
		// Es este valor o sin restarle 2 si no cuenta la declaracion del metodo y la llave de cierre
		method.setTotalLines(source.split("\\n").length-2); 
				
		parser = ASTParser.newParser(AST.JLS8);
		// Corto el source para tener el body nada mas
		source = source.split(Pattern.quote("{"),2)[1];
		parser.setSource(source.substring(0, source.length()-2).toCharArray());
		parser.setKind(ASTParser.K_STATEMENTS);
		parser.setResolveBindings(true);
		
		Block block = (Block) parser.createAST(null);
		
		// Analisis de la complejidad ciclomatica
		block.accept(new ASTVisitor() {
			public boolean visit(IfStatement node){
				method.incrementCyclomaticComplexity();
				int noc = node.getExpression().toString().split("\\&\\&|\\|\\|").length;
				method.incrementCyclomaticComplexity(noc - 1);
				return true;
			}
			
			public boolean visit(ConditionalExpression node){
				method.incrementCyclomaticComplexity();
				int noc = node.getExpression().toString().split("\\&\\&|\\|\\|").length;
				method.incrementCyclomaticComplexity(noc - 1);
				return true;
			}
			
			public boolean visit(ForStatement node){
				method.incrementCyclomaticComplexity();
				int noc = node.getExpression().toString().split("\\&\\&|\\|\\|").length;
				method.incrementCyclomaticComplexity(noc - 1);
				return true;
			}
			
			public boolean visit(WhileStatement node){
				method.incrementCyclomaticComplexity();
				int noc = node.getExpression().toString().split("\\&\\&|\\|\\|").length;
				method.incrementCyclomaticComplexity(noc - 1);
				return true;
			}
			
			public boolean visit(DoStatement node){
				method.incrementCyclomaticComplexity();
				int noc = node.getExpression().toString().split("\\&\\&|\\|\\|").length;
				method.incrementCyclomaticComplexity(noc - 1);
				return true;
			}
			
			public boolean visit(SwitchCase node){
				method.incrementCyclomaticComplexity();
				return true;
			}
			
			public boolean visit(CatchClause node){
				method.incrementCyclomaticComplexity();
				return true;
			}
		});
	}

}

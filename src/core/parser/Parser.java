package core.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.regex.Pattern;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Assignment.Operator;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.WhileStatement;

import core.ParsedClass;
import core.ParsedMethod;

public class Parser {

	HashSet<String> gvars = new HashSet<String>();
	LinkedList<String> mnames = new LinkedList<String>();

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

			public boolean visit(VariableDeclarationFragment node) {
				String name = node.getName().toString();
				gvars.add(name);
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

	public void parseMethod(ParsedMethod method, String source) {
		// Metodo que al pasarle un metodo, obtiene las metricas pedidas
		ASTParser parser = ASTParser.newParser(AST.JLS8);
		parser.setSource(source.toCharArray());
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setResolveBindings(true);

		final CompilationUnit cu = (CompilationUnit) parser.createAST(null);

		method.setTotalComments(cu.getCommentList().size());
		// CANTIDAD DE LINEAS DE CODIGO
		// Es este valor o sin restarle 2 si no cuenta la declaracion del metodo
		// y la llave de cierre
		method.setTotalLines(source.split("\\n").length - 2);

		HashSet<String> lvars = new HashSet<String>();

		cu.accept(new ASTVisitor() {
			@Override
			public boolean visit(final MethodDeclaration node) {
				for (Object parameter : node.parameters()) {
					VariableDeclaration variableDeclaration = (VariableDeclaration) parameter;
					lvars.add(variableDeclaration.getName().toString());
				}
				return true;
			}
			
			
		});

		parser = ASTParser.newParser(AST.JLS8);
		// Corto el source para tener el body nada mas
		String body = source.split(Pattern.quote("{"), 2)[1];
		parser.setSource(body.substring(0, body.length() - 2).toCharArray());
		parser.setKind(ASTParser.K_STATEMENTS);
		parser.setResolveBindings(true);

		Block block = (Block) parser.createAST(null);

		HashMap<String, Integer> operands = new HashMap<String, Integer>();
		HashMap<String, Integer> operators = new HashMap<String, Integer>();

		// Analisis de la complejidad ciclomatica
		block.accept(new ASTVisitor() {
			public boolean visit(VariableDeclarationFragment node){
				String name = "assign=";
				if (operators.containsKey(name))
					operators.put(name, operators.get(name) + 1);
				else
					operators.put(name, 1);
				return true;
			}
			
			public boolean visit(SimpleName node) {
				String name = node.toString();
				if (gvars.contains(name) || lvars.contains(name)) {
					if (operands.containsKey(name))
						operands.put(name, operands.get(name) + 1);
					else
						operands.put(name, 1);
				}
				return true;
			}

			public boolean visit(IfStatement node) {
				if (operators.containsKey("if"))
					operators.put("if", operators.get("if") + 1);
				else
					operators.put("if", 1);
				method.incrementCyclomaticComplexity();
				int noc = node.getExpression().toString().split("\\&\\&|\\|\\|").length;
				method.incrementCyclomaticComplexity(noc - 1);
				return true;
			}

			public boolean visit(ConditionalExpression node) {
				if (operators.containsKey("cond"))
					operators.put("cond", operators.get("cond") + 1);
				else
					operators.put("cond", 1);
				method.incrementCyclomaticComplexity();
				int noc = node.getExpression().toString().split("\\&\\&|\\|\\|").length;
				method.incrementCyclomaticComplexity(noc - 1);
				return true;
			}

			public boolean visit(ForStatement node) {
				if (operators.containsKey("for"))
					operators.put("for", operators.get("for") + 1);
				else
					operators.put("for", 1);
				method.incrementCyclomaticComplexity();
				int noc = node.getExpression().toString().split("\\&\\&|\\|\\|").length;
				method.incrementCyclomaticComplexity(noc - 1);
				return true;
			}

			public boolean visit(WhileStatement node) {
				if (operators.containsKey("while"))
					operators.put("while", operators.get("while") + 1);
				else
					operators.put("while", 1);
				method.incrementCyclomaticComplexity();
				int noc = node.getExpression().toString().split("\\&\\&|\\|\\|").length;
				method.incrementCyclomaticComplexity(noc - 1);
				return true;
			}

			public boolean visit(DoStatement node) {
				if (operators.containsKey("do"))
					operators.put("do", operators.get("do") + 1);
				else
					operators.put("do", 1);
				method.incrementCyclomaticComplexity();
				int noc = node.getExpression().toString().split("\\&\\&|\\|\\|").length;
				method.incrementCyclomaticComplexity(noc - 1);
				return true;
			}

			public boolean visit(SwitchCase node) {
				if (operators.containsKey("switch"))
					operators.put("switch", operators.get("switch") + 1);
				else
					operators.put("switch", 1);
				method.incrementCyclomaticComplexity();
				return true;
			}

			public boolean visit(CatchClause node) {
				if (operators.containsKey("catch"))
					operators.put("catch", operators.get("catch") + 1);
				else
					operators.put("catch", 1);
				method.incrementCyclomaticComplexity();
				return true;
			}

			// Si la forma en la que maneje los operandos no funciona, estos
			// metodos devuelven los operandos involucrados

			public boolean visit(PrefixExpression node) {
				String name = "pre" + node.getOperator();
				if (operators.containsKey(name))
					operators.put(name, operators.get(name) + 1);
				else
					operators.put(name, 1);
				return true;
			}

			public boolean visit(InfixExpression node) {
				String name = "in" + node.getOperator();
				if (operators.containsKey(name))
					operators.put(name, operators.get(name) + 1);
				else
					operators.put(name, 1);
				return true;
			}

			public boolean visit(PostfixExpression node) {
				String name = "post" + node.getOperator();
				if (operators.containsKey(name))
					operators.put(name, operators.get(name) + 1);
				else
					operators.put(name, 1);
				return true;
			}

			public boolean visit(Assignment node) {
				String name = "assign" + node.getOperator();
				if (operators.containsKey(name))
					operators.put(name, operators.get(name) + 1);
				else
					operators.put(name, 1);
				return true;
			}
		});

		int aux = 0;
		// No se le resta 1 porque hay que contar los que envuelven al metodo
		if ((aux = body.split(Pattern.quote("}")).length) > 0)
			operators.put("{}", aux);

		// Se le resta 1 porque sino divide por cada uno en dos partes, y se
		// pasa de la cantidad
		aux = body.split(Pattern.quote(")")).length - 1 - (operators.containsKey("for") ? operators.get("for") : 0)
				- (operators.containsKey("while") ? operators.get("while") : 0) - (operators.containsKey("do") ? operators.get("do") : 0)
				- (operators.containsKey("catch") ? operators.get("catch") : 0)
				- (operators.containsKey("switch") ? operators.get("for") : 0)
				- (operators.containsKey("cond") ? operators.get("cond") : 0);
		// Tambien se restan las condiciones que usan si o si parentesis

		if (aux > 0)
			operators.put("()", aux);

		if ((aux = body.split(Pattern.quote("]")).length - 1) > 0)
			operators.put("[]", aux);

		int smallN1=0, smallN2=0, bigN1=0, bigN2=0;
		
		// Calculo la cantidad de operadores totales
		for (String k : operators.keySet()) {
			bigN1 += operators.get(k);
		}
		// Calculo la cantidad de operadores diferentes
		smallN1 = operators.size();

		

		// Calculo la cantidad de operandos totales
		for (String k : operands.keySet()) {
			bigN2 += operands.get(k);
		}
		// Calculo la cantidad de operadores diferentes
		smallN2 = operands.size();
		

		
		// La longitud es N = N1 + N2
		method.setHalsteadLength(bigN1 + bigN2);
		// El vocabulario es n = n1 + n2
		// El volumen se calcula como N * Log2 (n)
		method.setHalsteadVolume((bigN1 + bigN2) * (Math.log10(smallN1 + smallN2) / Math.log10(2)));
		// Guardo el valor directamente para hacer la cuenta una sola vez por
		// metodo
	}

}

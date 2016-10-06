package core.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimplePropertyDescriptor;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;

import com.sun.corba.se.impl.orbutil.graph.Node;

import core.ParsedClass;
import core.ParsedMethod;

public class Parser {

	HashSet<String> gvars = new HashSet<String>();

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

		HashMap<String, Integer> smallN = new HashMap<String, Integer>();
		HashMap<String, Integer> bigN = new HashMap<String, Integer>();

		// Analisis de la complejidad ciclomatica
		block.accept(new ASTVisitor() {
			public boolean visit(SimpleName node) {
				String name = node.toString();
				if (gvars.contains(name) || lvars.contains(name)) {
					if (smallN.containsKey(name))
						smallN.put(name, smallN.get(name) + 1);
					else
						smallN.put(name, 1);
				}
				return true;
			}

			public boolean visit(IfStatement node) {
				if (bigN.containsKey("if"))
					bigN.put("if", bigN.get("if") + 1);
				else
					bigN.put("if", 1);
				method.incrementCyclomaticComplexity();
				int noc = node.getExpression().toString().split("\\&\\&|\\|\\|").length;
				method.incrementCyclomaticComplexity(noc - 1);
				return true;
			}

			public boolean visit(ConditionalExpression node) {
				if (bigN.containsKey("cond"))
					bigN.put("cond", bigN.get("cond") + 1);
				else
					bigN.put("cond", 1);
				method.incrementCyclomaticComplexity();
				int noc = node.getExpression().toString().split("\\&\\&|\\|\\|").length;
				method.incrementCyclomaticComplexity(noc - 1);
				return true;
			}

			public boolean visit(ForStatement node) {
				if (bigN.containsKey("for"))
					bigN.put("for", bigN.get("for") + 1);
				else
					bigN.put("for", 1);
				method.incrementCyclomaticComplexity();
				int noc = node.getExpression().toString().split("\\&\\&|\\|\\|").length;
				method.incrementCyclomaticComplexity(noc - 1);
				return true;
			}

			public boolean visit(WhileStatement node) {
				if (bigN.containsKey("while"))
					bigN.put("while", bigN.get("while") + 1);
				else
					bigN.put("while", 1);
				method.incrementCyclomaticComplexity();
				int noc = node.getExpression().toString().split("\\&\\&|\\|\\|").length;
				method.incrementCyclomaticComplexity(noc - 1);
				return true;
			}

			public boolean visit(DoStatement node) {
				if (bigN.containsKey("do"))
					bigN.put("do", bigN.get("do") + 1);
				else
					bigN.put("do", 1);
				method.incrementCyclomaticComplexity();
				int noc = node.getExpression().toString().split("\\&\\&|\\|\\|").length;
				method.incrementCyclomaticComplexity(noc - 1);
				return true;
			}

			public boolean visit(SwitchCase node) {
				if (bigN.containsKey("switch"))
					bigN.put("switch", bigN.get("switch") + 1);
				else
					bigN.put("switch", 1);
				method.incrementCyclomaticComplexity();
				return true;
			}

			public boolean visit(CatchClause node) {
				if (bigN.containsKey("catch"))
					bigN.put("catch", bigN.get("catch") + 1);
				else
					bigN.put("catch", 1);
				method.incrementCyclomaticComplexity();
				return true;
			}

			// Si la forma en la que maneje los operandos no funciona, estos
			// metodos devuelven los operandos involucrados

			public boolean visit(PrefixExpression node) {
				String name = "pre" + node.getOperator();
				if (bigN.containsKey(name))
					bigN.put(name, bigN.get(name) + 1);
				else
					bigN.put(name, 1);
				return true;
			}

			public boolean visit(InfixExpression node) {
				String name = "in" + node.getOperator();
				if (bigN.containsKey(name))
					bigN.put(name, bigN.get(name) + 1);
				else
					bigN.put(name, 1);
				return true;
			}

			public boolean visit(PostfixExpression node) {
				String name = "post" + node.getOperator();
				if (bigN.containsKey(name))
					bigN.put(name, bigN.get(name) + 1);
				else
					bigN.put(name, 1);
				return true;
			}

			public boolean visit(Assignment node) {
				String name = "assign" + node.getOperator();
				if (bigN.containsKey(name))
					bigN.put(name, bigN.get(name) + 1);
				else
					bigN.put(name, 1);
				return true;
			}
		});

		// No se le resta 1 porque hay que contar los que envuelven al metodo
		bigN.put("{}", body.split(Pattern.quote("}")).length);

		// Se le resta 1 porque sino divide por cada uno en dos partes, y se
		// pasa de la cantidad
		bigN.put("()", body.split(Pattern.quote(")")).length - 1 
				- (bigN.containsKey("for")?bigN.get("for"):0)
				- (bigN.containsKey("while")?bigN.get("while"):0)
				- (bigN.containsKey("do")?bigN.get("do"):0)
				- (bigN.containsKey("catch")?bigN.get("catch"):0)
				- (bigN.containsKey("switch")?bigN.get("for"):0)
				- (bigN.containsKey("cond")?bigN.get("cond"):0));
		// Tambien se restan las condiciones que usan si o si parentesis

		bigN.put("[]", body.split(Pattern.quote("]")).length - 1);

		int length = 0;
		for (String k : bigN.keySet()) {
			length += bigN.get(k);
		}

		length += bigN.size();

		method.setHalsteadLength(length);

		// Contiene n1 (total de operandos usados)
		int sum = 0;
		for (String k : smallN.keySet()) {
			sum += smallN.get(k);
		}

		method.setHalsteadVolume(length * (Math.log10(sum + smallN.size()) / Math.log10(2)));

	}

}

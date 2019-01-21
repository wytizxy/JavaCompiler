package cop5556fa18;

import static cop5556fa18.PLPScanner.Kind.OP_PLUS;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import cop5556fa18.PLPParser.SyntaxException;
import cop5556fa18.PLPScanner;
import cop5556fa18.PLPScanner.LexicalException;
import cop5556fa18.PLPAST.Block;
import cop5556fa18.PLPAST.Declaration;
import cop5556fa18.PLPAST.Expression;
import cop5556fa18.PLPAST.ExpressionBinary;
import cop5556fa18.PLPAST.ExpressionCharLiteral;
import cop5556fa18.PLPAST.ExpressionConditional;
import cop5556fa18.PLPAST.ExpressionFloatLiteral;
import cop5556fa18.PLPAST.ExpressionIdentifier;
import cop5556fa18.PLPAST.ExpressionIntegerLiteral;
import cop5556fa18.PLPAST.ExpressionUnary;
import cop5556fa18.PLPAST.FunctionWithArg;
import cop5556fa18.PLPAST.PLPASTNode;
import cop5556fa18.PLPAST.Program;
import cop5556fa18.PLPAST.VariableDeclaration;
import cop5556fa18.PLPAST.VariableListDeclaration;
import cop5556fa18.PLPScanner.Kind;

public class PLPParserTest {
	
	//set Junit to be able to catch exceptions
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	
	//To make it easy to print objects and turn this output on and off
	static final boolean doPrint = true;
	private void show(Object input) {
		if (doPrint) {
			System.out.println(input.toString());
		}
	}


	//creates and returns a parser for the given input.
	private PLPParser makeParser(String input) throws LexicalException {
		show(input);        //Display the input 
		PLPScanner scanner = new PLPScanner(input).scan();  //Create a Scanner and initialize it
		show(scanner);   //Display the Scanner
		PLPParser parser = new PLPParser(scanner);
		return parser;
	}
	
	/**
	 * Test case with an empty program.  This throws an exception 
	 * because it lacks an identifier and a block
	 *   
	 * @throws LexicalException
	 * @throws SyntaxException 
	 */
	@Test
	public void testEmpty() throws LexicalException, SyntaxException {
		String input = "";  //The input is the empty string.  
		thrown.expect(SyntaxException.class);
		PLPParser parser = makeParser(input);
		@SuppressWarnings("unused")
		Program p = parser.parse();
	}
	
	/**
	 * Smallest legal program.
	 *   
	 * @throws LexicalException
	 * @throws SyntaxException 
	 */
	@Test
	public void testSmallest() throws LexicalException, SyntaxException {
		String input = "b{}";  
		PLPParser parser = makeParser(input);
		Program p = parser.parse();
		show(p);
		assertEquals("b", p.name);
		assertEquals(0, p.block.declarationsAndStatements.size());
	}	
	
	
	/**
	 * Utility method to check if an element of a block at an index is a declaration with a given type and name.
	 * 
	 * @param block
	 * @param index
	 * @param type
	 * @param name
	 * @return
	 */
	Declaration checkDec(Block block, int index, Kind type, String name) {
		PLPASTNode node = block.declarationsAndStatements(index);
		assertEquals(VariableDeclaration.class, node.getClass());
		VariableDeclaration dec = (VariableDeclaration) node;
		assertEquals(type, dec.type);
		assertEquals(name, dec.name);
		return dec;
	}	
	
	@Test
	public void testDec0() throws LexicalException, SyntaxException {
		String input = "b{int i; char c;}";
		PLPParser parser = makeParser(input);
		Program p = parser.parse();
		show(p);	
		checkDec(p.block, 0, Kind.KW_int, "i");
		checkDec(p.block, 1, Kind.KW_char, "c");
	}
	
	
	/** 
	 * Test a specific grammar element by calling a corresponding parser method rather than parse.
	 * This requires that the methods are visible (not private). 
	 * 
	 * @throws LexicalException
	 * @throws SyntaxException
	 */
	
	@Test
	public void testExpression() throws LexicalException, SyntaxException {
		String input = "x + 2";
		PLPParser parser = makeParser(input);
		Expression e = parser.expression();  //call expression here instead of parse
		show(e);	
		assertEquals(ExpressionBinary.class, e.getClass());
		ExpressionBinary b = (ExpressionBinary)e;
		assertEquals(ExpressionIdentifier.class, b.leftExpression.getClass());
		ExpressionIdentifier left = (ExpressionIdentifier)b.leftExpression;
		assertEquals("x", left.name);
		assertEquals(ExpressionIntegerLiteral.class, b.rightExpression.getClass());
		ExpressionIntegerLiteral right = (ExpressionIntegerLiteral)b.rightExpression;
		assertEquals(2, right.value);
		assertEquals(OP_PLUS, b.op);
	}

	
	/**
	 * Utility method to check if an element of a block at an index is a declaration with a given type and several names.
	 * 
	 * @param block
	 * @param index
	 * @param type
	 * @param ArrayList<String> name
	 * @return
	 */
	Declaration checkDecList(Block block, int index, Kind type, ArrayList<String> names) {
		PLPASTNode node = block.declarationsAndStatements(index);
		assertEquals(VariableListDeclaration.class, node.getClass());
		VariableListDeclaration dec = (VariableListDeclaration) node;
		for (int i = 0; i < dec.names.size(); ++i) {
			assertEquals(type, dec.type);
			assertEquals(names.get(i), dec.names.get(i));
		}
		return dec;
	}
	
	/**
	 * Declaration Test
	 */
	@Test
	public void DecTest1() throws LexicalException, SyntaxException {
		System.out.println("-------------------DecTest1-------------------");
		String input = "testDec {\n"
				+ "    int a; \n"
				+ "    float b;\n"
				+ "    char c;\n"
				+ "    boolean d;\n"
				+ "    string e;\n"
				+ "}";
		PLPParser parser = makeParser(input);
		Program p = parser.parse();
		show(p);	
		checkDec(p.block, 0, Kind.KW_int, "a");
		checkDec(p.block, 1, Kind.KW_float, "b");
		checkDec(p.block, 2, Kind.KW_char, "c");
		checkDec(p.block, 3, Kind.KW_boolean, "d");
		checkDec(p.block, 4, Kind.KW_string, "e");
	}
	
	@Test
	public void DecTest2() throws LexicalException, SyntaxException {
		System.out.println("-------------------DecTest2-------------------");
		String input = "testDec {\n"
				+ "    int a1, a2, a3, a4; \n"
				+ "    float b1, b2, b3, b4, b5, b6;\n"
				+ "    char c1, c2, c3;\n"
				+ "    boolean d1, d2;\n"
				+ "    string e1, e2, e3, e4, e5;\n"
				+ "}";
		PLPParser parser = makeParser(input);
		Program p = parser.parse();
		show(p);
		
		ArrayList<String> names0 = new ArrayList<String>(Arrays.asList("a1", "a2", "a3", "a4"));
		ArrayList<String> names1 = new ArrayList<String>(Arrays.asList("b1", "b2", "b3", "b4", "b5", "b6"));
		ArrayList<String> names2 = new ArrayList<String>(Arrays.asList("c1", "c2", "c3"));
		ArrayList<String> names3 = new ArrayList<String>(Arrays.asList("d1", "d2"));
		ArrayList<String> names4 = new ArrayList<String>(Arrays.asList("e1", "e2", "e3", "e4", "e5"));
	
		checkDecList(p.block, 0, Kind.KW_int, names0);
		checkDecList(p.block, 1, Kind.KW_float, names1);
		checkDecList(p.block, 2, Kind.KW_char, names2);
		checkDecList(p.block, 3, Kind.KW_boolean, names3);
		checkDecList(p.block, 4, Kind.KW_string, names4);
	}
	
	@Test
	public void DecTest3() throws LexicalException, SyntaxException {
		System.out.println("-------------------DecTest3-------------------");
		String input = "testDec {\n"
				+ "    int a1, a2, a3, a4; \n"
				+ "    float b1;\n"
				+ "    char c1, c2, c3;\n"
				+ "    boolean d1;\n"
				+ "    string e1, e2, e3, e4, e5;\n"
				+ "}";
		PLPParser parser = makeParser(input);
		Program p = parser.parse();
		show(p);
		
		ArrayList<String> names0 = new ArrayList<String>(Arrays.asList("a1", "a2", "a3", "a4"));
		ArrayList<String> names2 = new ArrayList<String>(Arrays.asList("c1", "c2", "c3"));
		ArrayList<String> names4 = new ArrayList<String>(Arrays.asList("e1", "e2", "e3", "e4", "e5"));
		
		checkDecList(p.block, 0, Kind.KW_int, names0);
		checkDec(p.block, 1, Kind.KW_float, "b1");
		checkDecList(p.block, 2, Kind.KW_char, names2);
		checkDec(p.block, 3, Kind.KW_boolean, "d1");
		checkDecList(p.block, 4, Kind.KW_string, names4);
	}
	
	@Test
	public void ExprTest1() throws LexicalException, SyntaxException {
		System.out.println("-------------------ExprTest1-------------------");
		String input = "abc - 0.01 + '2'";
		PLPParser parser = makeParser(input);
		Expression e = parser.expression();  //call expression here instead of parse
		show(e);	
		assertEquals(ExpressionBinary.class, e.getClass());
		
		ExpressionBinary b = (ExpressionBinary)e;
		assertEquals(ExpressionBinary.class, b.getClass());
		
		ExpressionBinary c = (ExpressionBinary)b.leftExpression;
		assertEquals(ExpressionBinary.class, c.getClass());
		
		assertEquals(ExpressionIdentifier.class, c.leftExpression.getClass());
		ExpressionIdentifier left = (ExpressionIdentifier)c.leftExpression;
		assertEquals(left.name, "abc");
		assertEquals(c.op, Kind.OP_MINUS);
		
		assertEquals(ExpressionFloatLiteral.class, c.rightExpression.getClass());
		ExpressionFloatLiteral right1 = (ExpressionFloatLiteral)c.rightExpression;
		assertEquals(right1.value, 0.01, 0.00001);
		
		
		assertEquals(b.op, OP_PLUS);
		
		assertEquals(ExpressionCharLiteral.class, b.rightExpression.getClass());
		ExpressionCharLiteral right2 = (ExpressionCharLiteral)b.rightExpression;
		assertEquals(right2.text, '2');
		
	}
	
	@Test
	public void ExprTest2() throws Exception, SyntaxException {
		System.out.println("-------------------ExprTest2-------------------");
		String input = "sin(1) + cos(2) * atan(3) / abs(4) - log(5) / int(5.5) * float(10)";
		PLPParser parser = makeParser(input);
		Expression e = parser.expression();  //call expression here instead of parse
		show(e);	
		assertEquals(ExpressionBinary.class, e.getClass());
		
		ExpressionBinary b = (ExpressionBinary)e;
		assertEquals(ExpressionBinary.class, b.getClass());
		
		ExpressionBinary c = (ExpressionBinary)b.leftExpression;
		assertEquals(ExpressionBinary.class, c.getClass());
		
		/*
		 * sin(1)
		 */
		assertEquals(FunctionWithArg.class, c.leftExpression.getClass());
		FunctionWithArg d = (FunctionWithArg) c.leftExpression;
		assertEquals(d.functionName, Kind.KW_sin);
		
		assertEquals(ExpressionIntegerLiteral.class, d.expression.getClass());
		ExpressionIntegerLiteral Expr1 = (ExpressionIntegerLiteral) d.expression;
		assertEquals(Expr1.value, 1);
		
		assertEquals(c.op, Kind.OP_PLUS);
		
		/*
		 * cos(2) * atan(3) / abs(4)
		 */
		assertEquals(ExpressionBinary.class, c.rightExpression.getClass());
		ExpressionBinary f = (ExpressionBinary) c.rightExpression;
		
		assertEquals(ExpressionBinary.class, f.leftExpression.getClass());
		ExpressionBinary g = (ExpressionBinary) f.leftExpression;
		
		assertEquals(FunctionWithArg.class, g.leftExpression.getClass());
		d = (FunctionWithArg) g.leftExpression;
		assertEquals(d.functionName, Kind.KW_cos);
		
		assertEquals(ExpressionIntegerLiteral.class, d.expression.getClass());
		Expr1 = (ExpressionIntegerLiteral) d.expression;
		assertEquals(Expr1.value, 2);
		
		assertEquals(g.op, Kind.OP_TIMES);
		
		assertEquals(FunctionWithArg.class, g.rightExpression.getClass());
		d = (FunctionWithArg) g.rightExpression;
		assertEquals(d.functionName, Kind.KW_atan);
		
		assertEquals(ExpressionIntegerLiteral.class, d.expression.getClass());
		Expr1 = (ExpressionIntegerLiteral) d.expression;
		assertEquals(Expr1.value, 3);
		
		assertEquals(f.op, Kind.OP_DIV);
		
		assertEquals(FunctionWithArg.class, f.rightExpression.getClass());
		d = (FunctionWithArg) f.rightExpression;
		assertEquals(d.functionName, Kind.KW_abs);
		
		assertEquals(ExpressionIntegerLiteral.class, d.expression.getClass());
		Expr1 = (ExpressionIntegerLiteral) d.expression;
		assertEquals(Expr1.value, 4);
		
		assertEquals(b.op, Kind.OP_MINUS);
		
		/*
		 * log(5) / int(5.5) * float(10)
		 */
		assertEquals(ExpressionBinary.class, b.rightExpression.getClass());
		f = (ExpressionBinary) b.rightExpression;
		
		assertEquals(ExpressionBinary.class, f.leftExpression.getClass());
		g = (ExpressionBinary) f.leftExpression;
		
		assertEquals(FunctionWithArg.class, g.leftExpression.getClass());
		d = (FunctionWithArg) g.leftExpression;
		assertEquals(d.functionName, Kind.KW_log);
		
		assertEquals(ExpressionIntegerLiteral.class, d.expression.getClass());
		Expr1 = (ExpressionIntegerLiteral) d.expression;
		assertEquals(Expr1.value, 5);
		
		assertEquals(g.op, Kind.OP_DIV);
		
		assertEquals(FunctionWithArg.class, g.rightExpression.getClass());
		d = (FunctionWithArg) g.rightExpression;
		assertEquals(d.functionName, Kind.KW_int);
		
		assertEquals(ExpressionFloatLiteral.class, d.expression.getClass());
		ExpressionFloatLiteral Expr2 = (ExpressionFloatLiteral) d.expression;
		assertEquals(Expr2.value, 5.5, 0.00001);
		
		assertEquals(f.op, Kind.OP_TIMES);
		
		assertEquals(FunctionWithArg.class, f.rightExpression.getClass());
		d = (FunctionWithArg) f.rightExpression;
		assertEquals(d.functionName, Kind.KW_float);
		
		assertEquals(ExpressionIntegerLiteral.class, d.expression.getClass());
		Expr1 = (ExpressionIntegerLiteral) d.expression;
		assertEquals(Expr1.value, 10);
	}
	
	@Test
	public void ExprTest3() throws Exception, SyntaxException {
		System.out.println("-------------------ExprTest3-------------------");
		String input = "(5 & 2 | 3) ** (10)";
		PLPParser parser = makeParser(input);
		Expression e = parser.expression();  //call expression here instead of parse
		show(e);
		assertEquals(ExpressionBinary.class, e.getClass());
		
		ExpressionBinary b = (ExpressionBinary)e;
		assertEquals(ExpressionBinary.class, b.getClass());
		
		assertEquals(ExpressionBinary.class, b.leftExpression.getClass());
		ExpressionBinary c = (ExpressionBinary) b.leftExpression;
		
		assertEquals(ExpressionBinary.class, c.leftExpression.getClass());
		ExpressionBinary d = (ExpressionBinary) c.leftExpression;
		
		assertEquals(ExpressionIntegerLiteral.class, d.leftExpression.getClass());
		ExpressionIntegerLiteral expr = (ExpressionIntegerLiteral) d.leftExpression;
		assertEquals(expr.value, 5);
		
		assertEquals(d.op, Kind.OP_AND);
		
		assertEquals(ExpressionIntegerLiteral.class, d.rightExpression.getClass());
		expr = (ExpressionIntegerLiteral) d.rightExpression;
		assertEquals(expr.value, 2);
		
		assertEquals(c.op, Kind.OP_OR);
		
		assertEquals(ExpressionIntegerLiteral.class, c.rightExpression.getClass());
		expr = (ExpressionIntegerLiteral) c.rightExpression;
		assertEquals(expr.value, 3);
		
		assertEquals(b.op, Kind.OP_POWER);
		
		assertEquals(ExpressionIntegerLiteral.class, b.rightExpression.getClass());
		expr = (ExpressionIntegerLiteral) b.rightExpression;
		assertEquals(expr.value, 10);
	}
	
	@Test
	public void ExprTest4() throws Exception, SyntaxException {
		System.out.println("-------------------ExprTest4-------------------");
		String input = "(5 + 2) % (4 - 1)";
		PLPParser parser = makeParser(input);
		Expression e = parser.expression();  //call expression here instead of parse
		show(e);
		assertEquals(ExpressionBinary.class, e.getClass());
		
		ExpressionBinary b = (ExpressionBinary)e;
		assertEquals(ExpressionBinary.class, b.getClass());
		
		/*
		 * (5 + 2)
		 */
		assertEquals(ExpressionBinary.class, b.leftExpression.getClass());
		ExpressionBinary c = (ExpressionBinary) b.leftExpression;
		
		assertEquals(ExpressionIntegerLiteral.class, c.leftExpression.getClass());
		ExpressionIntegerLiteral d = (ExpressionIntegerLiteral) c.leftExpression;
		assertEquals(d.value, 5);
		
		assertEquals(c.op, Kind.OP_PLUS);
		
		assertEquals(ExpressionIntegerLiteral.class, c.rightExpression.getClass());
		d = (ExpressionIntegerLiteral) c.rightExpression;
		assertEquals(d.value, 2);
		
		assertEquals(b.op, Kind.OP_MOD);
		
		/*
		 * (4 - 1)
		 */
		assertEquals(ExpressionBinary.class, b.rightExpression.getClass());
		c = (ExpressionBinary) b.rightExpression;
		
		assertEquals(ExpressionIntegerLiteral.class, c.leftExpression.getClass());
		d = (ExpressionIntegerLiteral) c.leftExpression;
		assertEquals(d.value, 4);
		
		assertEquals(c.op, Kind.OP_MINUS);
		
		assertEquals(ExpressionIntegerLiteral.class, c.rightExpression.getClass());
		d = (ExpressionIntegerLiteral) c.rightExpression;
		assertEquals(d.value, 1);
	}
	
	@Test
	public void ExprTest5() throws Exception, SyntaxException {
		System.out.println("-------------------ExprTest5-------------------");
		String input = "!2 <= 5 < 10";
		PLPParser parser = makeParser(input);
		Expression e = parser.expression();  //call expression here instead of parse
		show(e);
		assertEquals(ExpressionBinary.class, e.getClass());
		
		ExpressionBinary b = (ExpressionBinary)e;
		assertEquals(ExpressionBinary.class, b.getClass());
		
		assertEquals(ExpressionBinary.class, b.leftExpression.getClass());
		ExpressionBinary c = (ExpressionBinary) b.leftExpression;
		
		assertEquals(ExpressionUnary.class, c.leftExpression.getClass());
		ExpressionUnary d = (ExpressionUnary) c.leftExpression;
		
		assertEquals(d.op, Kind.OP_EXCLAMATION);
		
		assertEquals(ExpressionIntegerLiteral.class, d.expression.getClass());
		ExpressionIntegerLiteral f = (ExpressionIntegerLiteral) d.expression;
		assertEquals(f.value, 2);
		
		assertEquals(c.op, Kind.OP_LE);
		
		assertEquals(ExpressionIntegerLiteral.class, c.rightExpression.getClass());
		f = (ExpressionIntegerLiteral) c.rightExpression;
		assertEquals(f.value, 5);
		
		assertEquals(b.op, Kind.OP_LT);
		
		assertEquals(ExpressionIntegerLiteral.class, b.rightExpression.getClass());
		f = (ExpressionIntegerLiteral) b.rightExpression;
		assertEquals(f.value, 10);
	}
	
	@Test
	public void ExprTest6() throws Exception, SyntaxException {
		System.out.println("-------------------ExprTest6-------------------");
		String input = "a == 5 + 3 ? 8 * 2 : (7 - 4)";
		PLPParser parser = makeParser(input);
		Expression e = parser.expression();  //call expression here instead of parse
		show(e);
		assertEquals(ExpressionConditional.class, e.getClass());
		
		ExpressionConditional b = (ExpressionConditional)e;
		assertEquals(ExpressionConditional.class, b.getClass());
		
		assertEquals(ExpressionBinary.class, b.condition.getClass());
		ExpressionBinary condition = (ExpressionBinary) b.condition;
		
		assertEquals(ExpressionBinary.class, b.trueExpression.getClass());
		ExpressionBinary trueExpression = (ExpressionBinary) b.trueExpression;
		
		assertEquals(ExpressionBinary.class, b.falseExpression.getClass());
		ExpressionBinary falseExpression = (ExpressionBinary) b.falseExpression;
		
		assertEquals(ExpressionIdentifier.class, condition.leftExpression.getClass());
		ExpressionIdentifier id = (ExpressionIdentifier) condition.leftExpression;
		assertEquals(id.name, "a");
		
		assertEquals(condition.op, Kind.OP_EQ);
		
		assertEquals(ExpressionBinary.class, condition.rightExpression.getClass());
		ExpressionBinary c = (ExpressionBinary) condition.rightExpression;
		
		assertEquals(ExpressionIntegerLiteral.class, c.leftExpression.getClass());
		ExpressionIntegerLiteral integer = (ExpressionIntegerLiteral) c.leftExpression;
		assertEquals(integer.value, 5);
		
		assertEquals(c.op, Kind.OP_PLUS);
		
		assertEquals(ExpressionIntegerLiteral.class, c.rightExpression.getClass());
		integer = (ExpressionIntegerLiteral) c.rightExpression;
		assertEquals(integer.value, 3);
		
		assertEquals(ExpressionIntegerLiteral.class, trueExpression.leftExpression.getClass());
		integer = (ExpressionIntegerLiteral) trueExpression.leftExpression;
		assertEquals(integer.value, 8);
		
		assertEquals(trueExpression.op, Kind.OP_TIMES);
		
		assertEquals(ExpressionIntegerLiteral.class, trueExpression.rightExpression.getClass());
		integer = (ExpressionIntegerLiteral) trueExpression.rightExpression;
		assertEquals(integer.value, 2);
		
		assertEquals(ExpressionIntegerLiteral.class, falseExpression.leftExpression.getClass());
		integer = (ExpressionIntegerLiteral) falseExpression.leftExpression;
		assertEquals(integer.value, 7);
		
		assertEquals(falseExpression.op, Kind.OP_MINUS);
		
		assertEquals(ExpressionIntegerLiteral.class, falseExpression.rightExpression.getClass());
		integer = (ExpressionIntegerLiteral) falseExpression.rightExpression;
		assertEquals(integer.value, 4);
	}
}

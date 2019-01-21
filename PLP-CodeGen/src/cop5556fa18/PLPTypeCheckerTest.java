//name: Xiyuan Zheng assignment number: p5 date due: Nov. 5th

package cop5556fa18;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import cop5556fa18.PLPScanner;
import cop5556fa18.PLPTypeChecker.SemanticException;
import cop5556fa18.PLPAST.PLPASTVisitor;
import cop5556fa18.PLPAST.Program;

public class PLPTypeCheckerTest {
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	/**
	 * Prints objects in a way that is easy to turn on and off
	 */
	static final boolean doPrint = true;

	private void show(Object input) {
		if (doPrint) {
			System.out.println(input.toString());
		}
	}

	/**
	 * Scan, parse, and type check an input string
	 * 
	 * @param input
	 * @throws Exception
	 */
	void typeCheck(String input) throws Exception {
		show(input);
		// instantiate a Scanner and scan input
		PLPScanner scanner = new PLPScanner(input).scan();
		show(scanner);
		// instantiate a Parser and parse input to obtain and AST
		Program ast = new PLPParser(scanner).parse();
		show(ast);
		// instantiate a TypeChecker and visit the ast to perform type checking and
		// decorate the AST.
		PLPASTVisitor v = new PLPTypeChecker();
		ast.visit(v, null);
	}
	
	
	@Test
	public void emptyProg() throws Exception {
		String input = "emptyProg{}";
		typeCheck(input);
	}
	/*
	@Test
	public void expression1() throws Exception {
		String input = "prog {show 1+2;}";
		typeCheck(input);
	}

	@Test
	public void expression2_fail() throws Exception {
		String input = "prog { show true+4; }"; //should throw an error due to incompatible types in binary expression
		thrown.expect(SemanticException.class);
		try {
			typeCheck(input);
		} catch (SemanticException e) {
			show(e);
			throw e;
		}
	}
	*/
	@Test
	public void testScope0() throws Exception {
	   String input = "testProgram {\n" +
	         "\tint a, b, c;\n" +
	         "\tif (true) {\n" +
	         "\t\tint a = 3;\n" +
	         "\t\tif (true) {\n" +
	         "\t\t\tb = c;\n" +
	         "\t\t};\n" +
	         "\t};\n" +
	         "\twhile (true) {\n" +
	         "\t\tfloat a, b ,c;\n" +
	         "\t\twhile (true) {\n" +
	         "\t\t\ta = 1.0;\n" +
	         "\t\t};\n" +
	         "\t};\n" +
	         "}";
	   try {
	      typeCheck(input);
	   }
	   catch (SemanticException e) {
	      show(e);
	   }
	}
	
	
	@Test
	public void testScope1() throws Exception {
	   String input = "testProgram {\n" +
	         "\tint a, b;\n" +
	         "\tif (abs(a) == a % b) {\n" +
	         "\t\tfloat a = -1.2;\n" +
	         "\t\tfloat c = a;\n" +
	         "\t\tint d = b;\n" +
	         "\t\tb = a == c ? !d : abs(-20);\n" +
	         "\t\tsleep b;\n" +
	         "\t};\n" +
	         "}";
	   try {
	      typeCheck(input);
	   }
	   catch (SemanticException e) {
	      show(e);
	   }
	}
	
	
	@Test
	public void testScope2() throws Exception {
	   String input = "testProgram {\n" +
	         "\tfloat a, b;\n" +
	         "\tif (true) {\n" +
	         "\t\tint a = 3;\n" +
	         "\t\tif (true) {\n" +
	         "\t\t\tsleep a;\n" +
	         "\t\t\tfloat c = log(b) + a;\n" +
	         "\t\t\t\tif (true) {\n" +
	         "\t\t\t\t\tboolean b;\n" +
	         "\t\t\t\t\tif (b) {\n" +
	         "\t\t\t\t\t\tfloat d = sin(float(a));\n" +
	         "\t\t\t\t\t\ta = d != 0.01 ? 1:2;\n" +
	         "\t\t\t\t\t};\n" +
	         "\t\t\t\t};\n" +
	         "\t\t};\n" +
	         "\t};\n" +
	         "}";
	   try {
	      typeCheck(input);
	   }
	   catch (SemanticException e) {
	      show(e);
	   }
	}
	
	@Test
	public void LegalCases1() throws Exception {
		System.out.println("----------Legal case 1----------");
		
		String input = "main {\n"
				+ "    int a = 5 + (3 % 7 / 8) - 5 ** 2 + (5 | 2 - 2 & 3);\n"
				+ "    float b = 5.5 ** 2.5 + 1 ** 2 + 4;\n"
				+ "    float g = sin(1.0) + cos(2.0) * atan(3.0) / abs(4.0) - log(5.0) / int(5.5) * float(10.0);\n"
				+ "    boolean c = true & false | true;\n"
				+ "    boolean f = (5.5 ** 2.5) >= (5.03);\n"
				+ "    char d = 'c';\n"
				+ "    string e = \"hello, world!\" + \" I love cs!\";\n"
				+ "}";
		
		try {
			typeCheck(input);
		} 
		catch (SemanticException e) {
			show(e);
		}
	}

	@Test
	public void LegalCases2() throws Exception {
		System.out.println("----------Legal case 2----------");
		
		String input = "main {\n"
				+ "    int a, b, c;\n"
				+ "    float d = 5.5;\n"
				+ "    boolean e, f;\n"
				+ "    char g = 'c';\n"
				+ "    string h, i, j;\n"
				+ "}";
		
		try {
			typeCheck(input);
		} 
		catch (SemanticException e) {
			show(e);
		}
	}
	
	@Test
	public void LegalCases3() throws Exception {
		System.out.println("----------Legal case 3----------");
		
		String input = "main {\n"
				+ "    int a, b, c;\n"
				+ "    float d = 5.5;\n"
				+ "    boolean e, f;\n"
				+ "    char g = 'c';\n"
				+ "    string h, i, j;\n"
				+ "    if (5 >= 3) {\n"
				+ "        d = d + 1;\n"
				+ "    };\n"
				+ "}";
		
		try {
			typeCheck(input);
		} 
		catch (SemanticException e) {
			show(e);
		}
	}
	
	@Test
	public void FailedCases1() throws Exception {
		System.out.println("----------Failed case 1----------");
		
		String input = "main {\n"
				+ "    float a = 5.5 ** 2.5 + 1 | 2 & 4;\n"
				+ "}";
		
		try {
			typeCheck(input);
		} 
		catch (SemanticException e) {
			show(e);
		}
	}
	
	@Test
	public void FailedCases2() throws Exception {
		System.out.println("----------Failed case 2----------");
		
		String input = "main {\n"
				+ "    float a;\n"
				+ "    int a, b;\n"
				+ "}";
		
		try {
			typeCheck(input);
		} 
		catch (SemanticException e) {
			show(e);
		}
	}
	
	@Test
	public void FailedCases3() throws Exception {
		System.out.println("----------Failed case 3----------");
		
		String input = "main {\n"
				+ "    int a = 5 ? 5 : 3;\n"
				+ "}";
		
		try {
			typeCheck(input);
		} 
		catch (SemanticException e) {
			show(e);
		}
	}
	
	@Test
	public void FailedCases4() throws Exception {
		System.out.println("----------Failed case 4----------");
		
		String input = "main {\n"
				+ "    int a = 5 >= 3 ? \"hello\" : 5;\n"
				+ "}";
		
		try {
			typeCheck(input);
		} 
		catch (SemanticException e) {
			show(e);
		}
	}
	
	@Test
	public void FailedCases5() throws Exception {
		System.out.println("----------Failed case 5----------");
		
		String input = "main {\n"
				+ "    int a;\n"
				+ "    a = b + 5;\n"
				+ "}";
		
		try {
			typeCheck(input);
		} 
		catch (SemanticException e) {
			show(e);
		}
	}
	
	@Test
	public void FailedCases6() throws Exception {
		System.out.println("----------Failed case 6----------");
		
		String input = "main {\n"
				+ "    int a = 0;\n"
				+ "    a = 'c';\n"
				+ "}";
		
		try {
			typeCheck(input);
		} 
		catch (SemanticException e) {
			show(e);
		}
	}
	
	@Test
	public void FailedCases7() throws Exception {
		System.out.println("----------Failed case 7----------");
		
		String input = "main {\n"
				+ "    if (5) { };\n"
				+ "}";
		
		try {
			typeCheck(input);
		} 
		catch (SemanticException e) {
			show(e);
		}
	}
	
	@Test
	public void FailedCases8() throws Exception {
		System.out.println("----------Failed case 8----------");
		
		String input = "main {\n"
				+ "    while (5) { };\n"
				+ "}";
		
		try {
			typeCheck(input);
		} 
		catch (SemanticException e) {
			show(e);
		}
	}
	
	@Test
	public void FailedCases9() throws Exception {
		System.out.println("----------Failed case 9----------");
		
		String input = "main {\n"
				+ "    sleep 5.5;\n"
				+ "}";
		
		try {
			typeCheck(input);
		} 
		catch (SemanticException e) {
			show(e);
		}
	}
	
	@Test
	public void FailedCases10() throws Exception {
		System.out.println("----------Failed case 10----------");
		
		String input = "main {\n"
				+ "    int a;\n"
				+ "    a = 1 + !\"hello\";\n"
				+ "}";
		
		try {
			typeCheck(input);
		} 
		catch (SemanticException e) {
			show(e);
		}
	}
	
	@Test
	public void FailedCases11() throws Exception {
		System.out.println("----------Failed case 11----------");
		
		String input = "main {\n"
				+ "    int a;\n"
				+ "    a = -\"hello\";\n"
				+ "}";
		
		try {
			typeCheck(input);
		} 
		catch (SemanticException e) {
			show(e);
		}
	}
	
	@Test
	public void FailedCases12() throws Exception {
		System.out.println("----------Failed case 12----------");
		
		String input = "main {\n"
				+ "    a = b + 1;\n"
				+ "}";
		
		try {
			typeCheck(input);
		} 
		catch (SemanticException e) {
			show(e);
		}
	}
	
	@Test
	public void FailedCases13() throws Exception {
		System.out.println("----------Failed case 13----------");
		
		String input = "main {\n"
				+ "    int a;\n"
				+ "    a = abs('c');\n"
				+ "}";
		
		try {
			typeCheck(input);
		} 
		catch (SemanticException e) {
			show(e);
		}
	}
	
	@Test
	public void FailedCases14() throws Exception {
		System.out.println("----------Failed case 14----------");
		
		String input = "main {\n"
				+ "    int a;\n"
				+ "    a = 1 + 'c';\n"
				+ "}";
		
		try {
			typeCheck(input);
		} 
		catch (SemanticException e) {
			show(e);
		}
	}
}

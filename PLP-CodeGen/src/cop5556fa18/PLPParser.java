//name: Xiyuan Zheng assignment number: p5 date due: Nov. 5th

package cop5556fa18;

import cop5556fa18.PLPScanner.Token;
import cop5556fa18.PLPScanner.Kind;

import java.util.ArrayList;
import java.util.List;

import cop5556fa18.PLPAST.Block;
import cop5556fa18.PLPAST.Declaration;
import cop5556fa18.PLPAST.Expression;
import cop5556fa18.PLPAST.ExpressionBinary;
import cop5556fa18.PLPAST.ExpressionUnary;
import cop5556fa18.PLPAST.ExpressionConditional;
import cop5556fa18.PLPAST.ExpressionIdentifier;
import cop5556fa18.PLPAST.ExpressionIntegerLiteral;
import cop5556fa18.PLPAST.ExpressionBooleanLiteral;
import cop5556fa18.PLPAST.ExpressionFloatLiteral;
import cop5556fa18.PLPAST.ExpressionCharLiteral;
import cop5556fa18.PLPAST.ExpressionStringLiteral;
import cop5556fa18.PLPAST.FunctionWithArg;
import cop5556fa18.PLPAST.PLPASTNode;
import cop5556fa18.PLPAST.Program;
import cop5556fa18.PLPAST.Statement;
import cop5556fa18.PLPAST.VariableDeclaration;
import cop5556fa18.PLPAST.VariableListDeclaration;
import cop5556fa18.PLPAST.IfStatement;
import cop5556fa18.PLPAST.LHS;
import cop5556fa18.PLPAST.PrintStatement;
import cop5556fa18.PLPAST.WhileStatement;
import cop5556fa18.PLPAST.SleepStatement;
import cop5556fa18.PLPAST.AssignmentStatement;

public class PLPParser {
	
	@SuppressWarnings("serial")
	public static class SyntaxException extends Exception {
		Token t;

		public SyntaxException(Token t, String message) {
			super(message);
			this.t = t;
		}

	}
	
	PLPScanner scanner;
	Token t;

	PLPParser(PLPScanner scanner) {
		this.scanner = scanner;
		t = scanner.nextToken();
	}
	
	public Program parse() throws SyntaxException {
		Program p0;
		p0 = program();
		matchEOF();
		return p0;
	}
	
	/*
	 * Program -> Identifier Block
	 */
	Kind[] firstDec = { Kind.KW_int, Kind.KW_boolean, Kind.KW_float, Kind.KW_char,
			Kind.KW_string /* Complete this */ };
	Kind[] firstStatement = {Kind.KW_if, Kind.KW_while, Kind.IDENTIFIER, Kind.KW_sleep, 
			Kind.KW_print/* Complete this */  };
	
	public Program program() throws SyntaxException {
		String name = t.pureString();
		Token firsttoken = t;
		match(Kind.IDENTIFIER);
		Block block = block();
		Program p0 = new Program(firsttoken, name, block);
		return p0;
	}
	
	/*
	 * Block ->  { (  (Declaration | Statement) ; )* }
	 */
	public Block block() throws SyntaxException {
		List<PLPASTNode> declarationsAndStatements = new ArrayList<PLPASTNode>();
		Token firsttoken = t;
		match(Kind.LBRACE);
		while (checkKind(firstDec) | checkKind(firstStatement)) {
			if (checkKind(firstDec)) {
				declarationsAndStatements.add(declaration());
			} 
			else if (checkKind(firstStatement)) {
				declarationsAndStatements.add(statement());
			}
			match(Kind.SEMI);
		}
		Block block = new Block(firsttoken, declarationsAndStatements);
		match(Kind.RBRACE);
		return block;
	}
	
	public Declaration declaration() throws SyntaxException {
		//TODO
		Declaration p0;
		Token firsttoken = t;
		Kind type = type();
		String name;
		name = t.pureString();
		match(Kind.IDENTIFIER);
		if (checkKind(Kind.COMMA)) {
			List<String> names= new ArrayList<String>();
			names.add(name);
			while(checkKind(Kind.COMMA)) {
				match(Kind.COMMA);
				name = t.pureString();
				names.add(name);
				match(Kind.IDENTIFIER);
			}
			p0 = new VariableListDeclaration(firsttoken, type, names);	//t could be wrong? 
			return p0;
		}
		else {
			Expression expression;
			if (checkKind(Kind.OP_ASSIGN)) {
				match(Kind.OP_ASSIGN);
				expression = expression();
				p0 = new VariableDeclaration(firsttoken, type, name, expression);
				return p0;
			}
			else {
				expression = null;
				p0 = new VariableDeclaration(firsttoken, type, name, expression);
				return p0;
			}
		}
	}
	
	public void identifierlist() throws SyntaxException {
		while (checkKind(Kind.COMMA)) {
			match(Kind.COMMA);
			match(Kind.IDENTIFIER);
		}
	}
	
	public Expression expression() throws SyntaxException {
		Expression condition, trueExpression, falseExpression, e0;
		condition = orexpression();
		Token firsttoken = t;
		if (checkKind(Kind.OP_QUESTION)) {
			match(Kind.OP_QUESTION);
			trueExpression = expression();
			match(Kind.OP_COLON);
			falseExpression = expression();
			e0 = new ExpressionConditional(firsttoken, condition, trueExpression, falseExpression);
			return e0;
		}
		return condition;
	}
	public Expression orexpression() throws SyntaxException {
		Expression leftExpression, rightExpression;
		Token firsttoken = t;
		Kind op = Kind.OP_OR;
		leftExpression = andexpression();
		while (checkKind(Kind.OP_OR)) {
			match(Kind.OP_OR);
			rightExpression = andexpression();
			leftExpression = new ExpressionBinary(firsttoken, leftExpression, op, rightExpression);
		}
		return leftExpression;
	}
	public Expression andexpression() throws SyntaxException {
		Expression leftExpression, rightExpression;
		Token firsttoken = t;
		Kind op = Kind.OP_AND;
		leftExpression = eqexpression();
		while (checkKind(Kind.OP_AND)) {
			match(Kind.OP_AND);
			rightExpression = eqexpression();
			leftExpression = new ExpressionBinary(firsttoken, leftExpression, op, rightExpression);
		}
		return leftExpression;
	}
	public Expression eqexpression() throws SyntaxException {
		Expression leftExpression, rightExpression;
		leftExpression = relexpression();
		Token firsttoken = t;
		while (checkKind(Kind.OP_EQ) | checkKind(Kind.OP_NEQ)) {
			Kind op = Kind.OP_EQ;
			if (checkKind(Kind.OP_EQ)) {
				match(Kind.OP_EQ);
				op = Kind.OP_EQ;
			}
			else if(checkKind(Kind.OP_NEQ)) {
				match(Kind.OP_NEQ);
				op = Kind.OP_NEQ;
			}
			rightExpression = relexpression();
			leftExpression = new ExpressionBinary(firsttoken, leftExpression, op, rightExpression);
		}
		return leftExpression;
	}
	public Expression relexpression() throws SyntaxException {
		Expression leftExpression, rightExpression;
		Token firsttoken = t;
		leftExpression = addexpression();
		while (checkKind(Kind.OP_GE) | checkKind(Kind.OP_LE) | checkKind(Kind.OP_GT) | checkKind(Kind.OP_LT)) {
			Kind op = Kind.OP_GE;
			if (checkKind(Kind.OP_GE)) {
				match(Kind.OP_GE);
				op = Kind.OP_GE;
			}
			else if(checkKind(Kind.OP_LE)) {
				match(Kind.OP_LE);
				op = Kind.OP_LE;
			}
			else if (checkKind(Kind.OP_GT)) {
				match(Kind.OP_GT);
				op = Kind.OP_GT;
			}
			else if(checkKind(Kind.OP_LT)) {
				match(Kind.OP_LT);
				op = Kind.OP_LT;
			}
			rightExpression = addexpression();
			leftExpression = new ExpressionBinary(firsttoken, leftExpression, op, rightExpression);
		}
		return leftExpression;
	}
	public Expression addexpression() throws SyntaxException {
		Expression leftExpression, rightExpression;
		Token firsttoken = t;
		leftExpression = multexpression();
		while (checkKind(Kind.OP_PLUS) | checkKind(Kind.OP_MINUS)) {
			Kind op = Kind.OP_PLUS;
			if (checkKind(Kind.OP_PLUS)) {
				match(Kind.OP_PLUS);
				op = Kind.OP_PLUS;
			}
			else if(checkKind(Kind.OP_MINUS)) {
				match(Kind.OP_MINUS);
				op = Kind.OP_MINUS;
			}
			rightExpression = multexpression();
			leftExpression = new ExpressionBinary(firsttoken, leftExpression, op, rightExpression);
		}
		return leftExpression;
	}
	public Expression multexpression() throws SyntaxException {
		Expression leftExpression, rightExpression;
		Token firsttoken = t;
		leftExpression = powerexpression();
		while (checkKind(Kind.OP_TIMES) | checkKind(Kind.OP_DIV) | checkKind(Kind.OP_MOD)) {
			Kind op = Kind.OP_TIMES;
			if (checkKind(Kind.OP_TIMES)) {
				match(Kind.OP_TIMES);
				op = Kind.OP_TIMES;
			}
			else if(checkKind(Kind.OP_DIV)) {
				match(Kind.OP_DIV);
				op = Kind.OP_DIV;
			}
			else if (checkKind(Kind.OP_MOD)) {
				match(Kind.OP_MOD);
				op = Kind.OP_MOD;
			}
			rightExpression = powerexpression();
			leftExpression = new ExpressionBinary(firsttoken, leftExpression, op, rightExpression);
		}
		return leftExpression;
	}
	public Expression powerexpression() throws SyntaxException {
		Expression leftExpression, rightExpression, e0;
		Token firsttoken = t;
		leftExpression = unaryexpression();
		if (checkKind(Kind.OP_POWER)) {
			Kind op = Kind.OP_POWER;
			match(Kind.OP_POWER);
			rightExpression = powerexpression();
			e0 = new ExpressionBinary(firsttoken, leftExpression, op, rightExpression);
			return e0;
		}
		return leftExpression;
	}
	public Expression unaryexpression() throws SyntaxException {
		Expression expression, e0;
		Kind op = null;
		Token firsttoken = t;
		if(checkKind(Kind.OP_PLUS)) {
			match(Kind.OP_PLUS);
			op = Kind.OP_PLUS;
			expression = unaryexpression();
			e0 = new ExpressionUnary(firsttoken, op, expression);
			return e0;
		}
		else if (checkKind(Kind.OP_MINUS)) {
			match(Kind.OP_MINUS);
			op = Kind.OP_MINUS;
			expression = unaryexpression();
			e0 = new ExpressionUnary(firsttoken, op, expression);
			return e0;
		}
		else if (checkKind(Kind.OP_EXCLAMATION)) {
			match(Kind.OP_EXCLAMATION);
			op = Kind.OP_EXCLAMATION;
			expression = unaryexpression();
			e0 = new ExpressionUnary(firsttoken, op, expression);
			return e0;
		}
		else {
			expression = primary();
			return expression;
		}
		
	}
	public Expression primary() throws SyntaxException {
		Expression e0;
		Token firsttoken = t;
		if (checkKind(Kind.INTEGER_LITERAL)) {
			int value = Integer.parseInt(t.pureString());
			match(Kind.INTEGER_LITERAL);
			e0 = new ExpressionIntegerLiteral(firsttoken, value);
		}
		else if (checkKind(Kind.BOOLEAN_LITERAL)) {
			boolean value = false;
			if(t.pureString().equals("true"))
			{
				value = true;
			}
			else if(t.pureString().equals("false"))
			{
				value = false;
			}
			match(Kind.BOOLEAN_LITERAL);
			e0 = new ExpressionBooleanLiteral(firsttoken, value);
		}
		else if (checkKind(Kind.FLOAT_LITERAL)) {
			float value = Float.parseFloat(t.pureString());
			match(Kind.FLOAT_LITERAL);
			e0 = new ExpressionFloatLiteral(firsttoken, value);
		}
		else if (checkKind(Kind.CHAR_LITERAL)) {
			char text = t.pureString().charAt(1);		//could be wrong?  [0]' [1]char [2]'
			match(Kind.CHAR_LITERAL);
			e0 = new ExpressionCharLiteral(firsttoken, text);
		}
		else if (checkKind(Kind.STRING_LITERAL)) {
			String text = String.copyValueOf(scanner.chars, t.pos+1, t.length-2);
			match(Kind.STRING_LITERAL);
			e0 = new ExpressionStringLiteral(firsttoken, text);
		}
		else if (checkKind(Kind.LPAREN)) {
			match(Kind.LPAREN);
			e0 = expression();
			match(Kind.RPAREN);
		}
		else if (checkKind(Kind.IDENTIFIER)) {
			String name = t.pureString();
			match(Kind.IDENTIFIER);
			e0 = new ExpressionIdentifier(firsttoken, name);
		}
		else {
			e0 = function();
		}
		return e0;
	}
	public Expression function() throws SyntaxException {
		Expression e0, e1;
		Token firsttoken = t;
		Kind funcName;
		funcName = functionname();
		match(Kind.LPAREN);
		e1 = expression();
		match(Kind.RPAREN);
		e0 = new FunctionWithArg(firsttoken, funcName, e1);
		return e0;
	}
	public Kind functionname() throws SyntaxException {
		if (checkKind(Kind.KW_sin)) {
			match(Kind.KW_sin);
			return Kind.KW_sin;
		}
		else if (checkKind(Kind.KW_cos)) {
			match(Kind.KW_cos);
			return Kind.KW_cos;
		}
		else if (checkKind(Kind.KW_atan)) {
			match(Kind.KW_atan);
			return Kind.KW_atan;
		}
		else if (checkKind(Kind.KW_abs)) {
			match(Kind.KW_abs);
			return Kind.KW_abs;
		}
		else if (checkKind(Kind.KW_log)) {
			match(Kind.KW_log);
			return Kind.KW_log;
		}
		else if (checkKind(Kind.KW_int)) {
			match(Kind.KW_int);
			return Kind.KW_int;
		}
		else if (checkKind(Kind.KW_float)) {
			match(Kind.KW_float);
			return Kind.KW_float;
		}
		else {
			throw new SyntaxException(t,"function unmatched");
		}
	}
	public Statement statement() throws SyntaxException {
		Statement s0;
		if (checkKind(Kind.KW_if)) {
			s0 = ifstatement();
			return s0;
		}
		else if (checkKind(Kind.IDENTIFIER)) {
			String identifier = t.pureString();
			Token firsttoken = t;
			LHS lhs = new LHS(t, identifier);
			match(Kind.IDENTIFIER);
			if(checkKind(Kind.OP_ASSIGN)) {
				match(Kind.OP_ASSIGN);
				Expression expression = expression();
				s0 = new AssignmentStatement(firsttoken, lhs, expression);
				return s0;
			}
			else {
				throw new SyntaxException(t,"AssignmentStatement unmatched");
			}
		}
		else if (checkKind(Kind.KW_sleep)) {
			s0 = sleepstatement();
			return s0;
		}
		else if (checkKind(Kind.KW_print)) {
			s0 = printstatement();
			return s0;
		}
		else if (checkKind(Kind.KW_while)) {
			s0 = whilestatement();
			return s0;
		}
		else {
			throw new SyntaxException(t,"statement unmatched");
		}
	}
	public Statement ifstatement() throws SyntaxException {
		Statement s0;
		Token firsttoken = t;
		match(Kind.KW_if);
		match(Kind.LPAREN);
		Expression condition = expression();
		match(Kind.RPAREN);
		Block block = block();
		s0 = new IfStatement(firsttoken, condition, block);
		return s0;
	}
	/*public Statement assignmentstatement() throws SyntaxException {
		Statement s0;
		AssignmentStatement(Token firstToken, ExpressionIdentifier identifier, Expression expression)
		expression();
	}*/
	public Statement sleepstatement() throws SyntaxException {
		Statement s0;
		Token firsttoken = t;
		match(Kind.KW_sleep);
		Expression time = expression();
		s0 = new SleepStatement(firsttoken, time);
		return s0;
	}
	public Statement printstatement() throws SyntaxException {
		Statement s0;
		Token firsttoken = t;
		match(Kind.KW_print);
		Expression expression = expression();
		s0 = new PrintStatement(firsttoken, expression);
		return s0;
	}
	public Statement whilestatement() throws SyntaxException {
		Statement s0;
		Token firsttoken = t;
		match(Kind.KW_while);
		match(Kind.LPAREN);
		Expression condition = expression();
		match(Kind.RPAREN);
		Block b = block();
		s0 = new WhileStatement(firsttoken, condition, b);
		return s0;
	}
	
	public Kind type() throws SyntaxException {
		if (checkKind(Kind.KW_int)) {
			match(Kind.KW_int);
			return Kind.KW_int;
		}
		else if (checkKind(Kind.KW_float)) {
			match(Kind.KW_float);
			return Kind.KW_float;
		}
		else if (checkKind(Kind.KW_boolean)) {
			match(Kind.KW_boolean);
			return Kind.KW_boolean;
		}
		else if (checkKind(Kind.KW_char)) {
			match(Kind.KW_char);
			return Kind.KW_char;
		}
		else if (checkKind(Kind.KW_string)) {
			match(Kind.KW_string);
			return Kind.KW_string;
		}
		else {
			throw new SyntaxException(t,"Type unfound");
		}
			
	}
	//TODO Complete all other productions

	protected boolean checkKind(Kind kind) {
		return t.kind == kind;
	}

	protected boolean checkKind(Kind... kinds) {
		for (Kind k : kinds) {
			if (k == t.kind)
				return true;
		}
		return false;
	}
	
	private Token matchEOF() throws SyntaxException {
		if (checkKind(Kind.EOF)) {
			return t;
		}
		throw new SyntaxException(t,"Something wrong with End of file"); //TODO  give a better error message!
	}
	
	/**
	 * @param kind
	 * @return 
	 * @return 
	 * @return
	 * @throws SyntaxException
	 */
	private void match(Kind kind) throws SyntaxException {
		if (kind == Kind.EOF) {
			System.out.println("End of file"); //return t;
		}
		else if (checkKind(kind)) {
			t = scanner.nextToken();
		}
		else {
			//TODO  give a better error message!
			if (kind == Kind.SEMI) {
				throw new SyntaxException(t,"Missing Semi");
			}
			else if (kind == Kind.IDENTIFIER) {
				throw new SyntaxException(t,"Missing Identifier");
			}
			else if (kind == Kind.LBRACE) {
				throw new SyntaxException(t,"Missing LBRACE");
			}
			else if (kind == Kind.RBRACE) {
				throw new SyntaxException(t,"Missing RBRACE");
			}
			else if (kind == Kind.LPAREN) {
				throw new SyntaxException(t,"Missing LPAREN");
			}
			else if (kind == Kind.RPAREN) {
				throw new SyntaxException(t,"Missing RPAREN");
			}
			else if (kind == Kind.COMMA) {
				throw new SyntaxException(t,"Missing COMMA");
			}
			else if (kind == Kind.OP_ASSIGN) {
				throw new SyntaxException(t,"Missing OP_ASSIGN");
			}
			else if (kind == Kind.OP_COLON) {
				throw new SyntaxException(t,"Missing COLON");
			}
			else {
				throw new SyntaxException(t,"Syntax Error");
			}
		}
		//TODO  give a better error message!
	}

}

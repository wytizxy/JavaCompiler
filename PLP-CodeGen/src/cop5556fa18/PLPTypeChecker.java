//name: Xiyuan Zheng assignment number: p5 date due: Nov. 5th

package cop5556fa18;

import cop5556fa18.PLPAST.AssignmentStatement;
import cop5556fa18.PLPAST.Block;
import cop5556fa18.PLPAST.ExpressionBinary;
import cop5556fa18.PLPAST.ExpressionBooleanLiteral;
import cop5556fa18.PLPAST.ExpressionCharLiteral;
import cop5556fa18.PLPAST.ExpressionConditional;
import cop5556fa18.PLPAST.ExpressionFloatLiteral;
import cop5556fa18.PLPAST.ExpressionIdentifier;
import cop5556fa18.PLPAST.ExpressionIntegerLiteral;
import cop5556fa18.PLPAST.ExpressionStringLiteral;
import cop5556fa18.PLPAST.ExpressionUnary;
import cop5556fa18.PLPAST.FunctionWithArg;
import cop5556fa18.PLPAST.IfStatement;
import cop5556fa18.PLPAST.LHS;
import cop5556fa18.PLPAST.PLPASTNode;
import cop5556fa18.PLPAST.PLPASTVisitor;
import cop5556fa18.PLPAST.PrintStatement;
import cop5556fa18.PLPAST.Program;
import cop5556fa18.PLPAST.SleepStatement;
import cop5556fa18.PLPAST.VariableDeclaration;
import cop5556fa18.PLPAST.VariableListDeclaration;
import cop5556fa18.PLPAST.WhileStatement;
import cop5556fa18.PLPScanner.Kind;
import cop5556fa18.PLPScanner.Token;

public class PLPTypeChecker implements PLPASTVisitor {
	
	PLPTypeChecker() {
	}
	
	@SuppressWarnings("serial")
	public static class SemanticException extends Exception {
		Token t;

		public SemanticException(Token t, String message) {
			super(message);
			this.t = t;
		}
	}
	PLPSymbolTable symbolTable = new PLPSymbolTable();
	// Name is only used for naming the output file. 
		// Visit the child block to type check program.
		@Override
		public Object visitProgram(Program program, Object arg) throws Exception {
			program.block.visit(this, arg);
			return null;
		}
		
	@Override
	public Object visitBlock(Block block, Object arg) throws Exception {
		symbolTable.enterScope();
		for (PLPASTNode node : block.declarationsAndStatements) {
			node.visit(this, arg);
		}
		symbolTable.leaveScope();
		return null;
	}

	@Override
	public Object visitVariableDeclaration(VariableDeclaration declaration, Object arg) throws Exception {
		if(symbolTable.existInScope(declaration.name)) {
			throw new SemanticException(declaration.firstToken, declaration.name + "is existed.");
		}
		if(declaration.expression != null) {
			declaration.expression.visit(this, arg);
			if(declaration.expression.type != PLPTypes.getType(declaration.type)) {
				throw new SemanticException(declaration.firstToken, "Expression type is wrong.");
			}	
		}
		symbolTable.insert(declaration.name, declaration);
		return null;
	}

	@Override
	public Object visitVariableListDeclaration(VariableListDeclaration declaration, Object arg) throws Exception {
		for(int t = 0; t < declaration.names.size(); t++) {
			if(symbolTable.existInScope(declaration.names.get(t))) {
				throw new SemanticException(declaration.firstToken, declaration.names.get(t) + "is existed.");
			}
			symbolTable.insert(declaration.names(t), declaration);
		}
		return null;
	}

	@Override
	public Object visitExpressionBooleanLiteral(ExpressionBooleanLiteral expressionBooleanLiteral, Object arg) throws Exception {
		expressionBooleanLiteral.type = PLPTypes.Type.BOOLEAN;
		return null;
	}

	@Override
	public Object visitExpressionBinary(ExpressionBinary expressionBinary, Object arg) throws Exception {
		expressionBinary.leftExpression.visit(this, arg);
		expressionBinary.rightExpression.visit(this, arg);
		expressionBinary.type = inferredType(expressionBinary, expressionBinary.leftExpression.type,
				expressionBinary.rightExpression.type, expressionBinary.op);
		return null;
	}

	@Override
	public Object visitExpressionConditional(ExpressionConditional expressionConditional, Object arg) throws Exception {
		expressionConditional.condition.visit(this, arg);
		if(expressionConditional.condition.type != PLPTypes.Type.BOOLEAN) {
			throw new SemanticException(expressionConditional.firstToken, "condition type is wrong.");
		}
		expressionConditional.trueExpression.visit(this, arg);
		expressionConditional.falseExpression.visit(this, arg);
		if(expressionConditional.trueExpression.type != expressionConditional.falseExpression.type) {
			throw new SemanticException(expressionConditional.firstToken, "Types of Expr1 & Expr2 are different.");
		}
		expressionConditional.type = expressionConditional.trueExpression.type;
		return null;
		
	}

	@Override
	public Object visitExpressionFloatLiteral(ExpressionFloatLiteral expressionFloatLiteral, Object arg) throws Exception {
		expressionFloatLiteral.type = PLPTypes.Type.FLOAT;
		return null;
	}

	@Override
	public Object visitFunctionWithArg(FunctionWithArg FunctionWithArg, Object arg) throws Exception {
		FunctionWithArg.expression.visit(this, arg);
		FunctionWithArg.type = inferredTypeFunctionWithArg(FunctionWithArg, FunctionWithArg.functionName, FunctionWithArg.expression.type);
		return null;
	}

	@Override
	public Object visitExpressionIdent(ExpressionIdentifier expressionIdent, Object arg) throws Exception {
		expressionIdent.dec = symbolTable.lookup(expressionIdent.name);
		if(expressionIdent.dec == null)
		{
			throw new SemanticException(expressionIdent.firstToken, "expressionIdent.dec is null.");
		}
		if(expressionIdent.dec.getClass() == VariableDeclaration.class) {
			VariableDeclaration ptr = (VariableDeclaration) expressionIdent.dec;
			expressionIdent.type = PLPTypes.getType(ptr.type);
		}
		else if(expressionIdent.dec.getClass() == VariableListDeclaration.class) {
			VariableListDeclaration ptr = (VariableListDeclaration) expressionIdent.dec;
			expressionIdent.type = PLPTypes.getType(ptr.type);
		}
		return null;
	}

	@Override
	public Object visitExpressionIntegerLiteral(ExpressionIntegerLiteral expressionIntegerLiteral, Object arg) throws Exception {
		expressionIntegerLiteral.type = PLPTypes.Type.INTEGER;
		return null;
	}

	@Override
	public Object visitExpressionStringLiteral(ExpressionStringLiteral expressionStringLiteral, Object arg) throws Exception {
		expressionStringLiteral.type = PLPTypes.Type.STRING;
		return null;
	}

	@Override
	public Object visitExpressionCharLiteral(ExpressionCharLiteral expressionCharLiteral, Object arg) throws Exception {
		expressionCharLiteral.type = PLPTypes.Type.CHAR;
		return null;
	}

	@Override
	public Object visitAssignmentStatement(AssignmentStatement statementAssign, Object arg) throws Exception {
		statementAssign.expression.visit(this, arg);
		statementAssign.lhs.visit(this, arg);
		if(statementAssign.lhs.type != statementAssign.expression.type) {
			throw new SemanticException(statementAssign.firstToken, "lhs.type != expression.type.");
		}
		return null;
	}

	@Override
	public Object visitIfStatement(IfStatement ifStatement, Object arg) throws Exception {
		ifStatement.condition.visit(this, arg);
		if(ifStatement.condition.type != PLPTypes.Type.BOOLEAN) {
			throw new SemanticException(ifStatement.firstToken, "if condition type is not BOOLEAN.");
		}
		ifStatement.block.visit(this, arg);
		return null;
	}

	@Override
	public Object visitWhileStatement(WhileStatement whileStatement, Object arg) throws Exception {
		whileStatement.condition.visit(this, arg);
		if(whileStatement.condition.type != PLPTypes.Type.BOOLEAN) {
			throw new SemanticException(whileStatement.firstToken, "while condition type is not BOOLEAN.");
		}
		whileStatement.b.visit(this, arg);
		return null;
	}

	@Override
	public Object visitPrintStatement(PrintStatement printStatement, Object arg) throws Exception {
		printStatement.expression.visit(this, arg);
		if(printStatement.expression.type != PLPTypes.Type.BOOLEAN && printStatement.expression.type != PLPTypes.Type.INTEGER &&
				printStatement.expression.type != PLPTypes.Type.CHAR &&printStatement.expression.type != PLPTypes.Type.FLOAT &&
				printStatement.expression.type != PLPTypes.Type.STRING) {
			throw new SemanticException(printStatement.firstToken, "expression type is not correct.");
		}
		return null;
	}

	@Override
	public Object visitSleepStatement(SleepStatement sleepStatement, Object arg) throws Exception {
		sleepStatement.time.visit(this, arg);
		if(sleepStatement.time.type != PLPTypes.Type.INTEGER) {
			throw new SemanticException(sleepStatement.firstToken, "SleepStatement time is not an integer.");
		}
		return null;
	}

	@Override
	public Object visitExpressionUnary(ExpressionUnary expressionUnary, Object arg) throws Exception {
		expressionUnary.expression.visit(this, arg);
		expressionUnary.type = expressionUnary.expression.type;
		if(expressionUnary.op == Kind.OP_EXCLAMATION) {
			if(expressionUnary.type != PLPTypes.Type.INTEGER && expressionUnary.type != PLPTypes.Type.BOOLEAN) {
				throw new SemanticException(expressionUnary.firstToken, "Type should be boolean or integer.");
			}
		}
		else if(expressionUnary.op == Kind.OP_PLUS || expressionUnary.op == Kind.OP_MINUS) {
			if(expressionUnary.type != PLPTypes.Type.INTEGER && expressionUnary.type != PLPTypes.Type.FLOAT) {
				throw new SemanticException(expressionUnary.firstToken, "Type should be float or integer.");
			}
		}
		return null;
	}

	@Override
	public Object visitLHS(LHS lhs, Object arg) throws Exception {
		lhs.dec = symbolTable.lookup(lhs.identifier);
		if(lhs.dec == null) {
			throw new SemanticException(lhs.firstToken, "lhs.dec == null.");
		}
		if(lhs.dec.getClass() == VariableDeclaration.class) {
			VariableDeclaration ptr = (VariableDeclaration) lhs.dec;
			lhs.type = PLPTypes.getType(ptr.type);
		}
		else if(lhs.dec.getClass() == VariableListDeclaration.class) {
			VariableListDeclaration ptr = (VariableListDeclaration) lhs.dec;
			lhs.type = PLPTypes.getType(ptr.type);
		}
		return null;
	}
	
	public PLPTypes.Type inferredType(ExpressionBinary expressionBinary, PLPTypes.Type type0, PLPTypes.Type type1, Kind op) throws Exception {
		if(type0 == PLPTypes.Type.INTEGER && type1 == PLPTypes.Type.INTEGER &&
				(op == Kind.OP_PLUS || op == Kind.OP_MINUS || op == Kind.OP_TIMES || 
				op == Kind.OP_DIV || op == Kind.OP_POWER || op == Kind.OP_MOD ||
				op == Kind.OP_AND ||op == Kind.OP_OR)) {
			return PLPTypes.Type.INTEGER;
		}
		else if(type0 == PLPTypes.Type.FLOAT && type1 == PLPTypes.Type.FLOAT &&
				(op == Kind.OP_PLUS || op == Kind.OP_MINUS || op == Kind.OP_TIMES || 
				op == Kind.OP_DIV || op == Kind.OP_POWER)) {
			return  PLPTypes.Type.FLOAT;
		}
		else if(type0 == PLPTypes.Type.FLOAT && type1 == PLPTypes.Type.INTEGER &&
				(op == Kind.OP_PLUS || op == Kind.OP_MINUS || op == Kind.OP_TIMES || 
				op == Kind.OP_DIV || op == Kind.OP_POWER)) {
			return  PLPTypes.Type.FLOAT;
		}
		else if(type0 == PLPTypes.Type.INTEGER && type1 == PLPTypes.Type.FLOAT &&
				(op == Kind.OP_PLUS || op == Kind.OP_MINUS || op == Kind.OP_TIMES || 
				op == Kind.OP_DIV || op == Kind.OP_POWER)) {
			return  PLPTypes.Type.FLOAT;
		}
		else if(type0 == PLPTypes.Type.STRING && type1 == PLPTypes.Type.STRING &&
				op == Kind.OP_PLUS) {
			return PLPTypes.Type.STRING;
		}
		else if(type0 == PLPTypes.Type.BOOLEAN && type1 == PLPTypes.Type.BOOLEAN &&
				(op == Kind.OP_AND || op == Kind.OP_OR)) {
			return PLPTypes.Type.BOOLEAN;
		}
		else if(type0 == PLPTypes.Type.INTEGER && type1 == PLPTypes.Type.INTEGER &&
				(op == Kind.OP_AND || op == Kind.OP_OR)) {
			return PLPTypes.Type.INTEGER;
		}
		else if(type0 == PLPTypes.Type.INTEGER && type1 == PLPTypes.Type.INTEGER &&
				(op == Kind.OP_EQ || op == Kind.OP_NEQ ||op == Kind.OP_GT ||
				op == Kind.OP_GE ||op == Kind.OP_LE ||op == Kind.OP_LT)) {
			return PLPTypes.Type.BOOLEAN;
		}
		else if(type0 == PLPTypes.Type.FLOAT && type1 == PLPTypes.Type.FLOAT &&
				(op == Kind.OP_EQ || op == Kind.OP_NEQ ||op == Kind.OP_GT ||
				op == Kind.OP_GE ||op == Kind.OP_LE ||op == Kind.OP_LT)) {
			return PLPTypes.Type.BOOLEAN;
		}
		else if(type0 == PLPTypes.Type.BOOLEAN && type1 == PLPTypes.Type.BOOLEAN &&
				(op == Kind.OP_EQ || op == Kind.OP_NEQ ||op == Kind.OP_GT ||
				op == Kind.OP_GE ||op == Kind.OP_LE ||op == Kind.OP_LT)) {
			return PLPTypes.Type.BOOLEAN;
		}
		else {
			throw new SemanticException(expressionBinary.firstToken, "Something wrong with inferredType");
		}
	}
	
	public PLPTypes.Type inferredTypeFunctionWithArg(FunctionWithArg FunctionWithArg, Kind func, PLPTypes.Type type) throws Exception {
		if(type == PLPTypes.Type.INTEGER && func == Kind.KW_abs) {
			return PLPTypes.Type.INTEGER;
		}
		else if (type == PLPTypes.Type.FLOAT && (func == Kind.KW_abs || func == Kind.KW_sin ||
				func == Kind.KW_cos || func == Kind.KW_atan || func ==Kind.KW_log)) {
			return PLPTypes.Type.FLOAT;
		}
		else if (type == PLPTypes.Type.INTEGER && func == Kind.KW_float) {
			return PLPTypes.Type.FLOAT;
		}
		else if (type == PLPTypes.Type.FLOAT && func == Kind.KW_float) {
			return PLPTypes.Type.FLOAT;
		}
		else if (type == PLPTypes.Type.FLOAT && func == Kind.KW_int) {
			return PLPTypes.Type.INTEGER;
		}
		else if (type == PLPTypes.Type.INTEGER && func == Kind.KW_int) {
			return PLPTypes.Type.INTEGER;
		}
		else {
			throw new SemanticException(FunctionWithArg.firstToken, "Something wrong with inferredTypeFunctionWithArg");
		}
	}
	
}

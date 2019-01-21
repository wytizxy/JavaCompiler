//name: Xiyuan Zheng assignment number: p6 date due: Nov. 20th

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
import cop5556fa18.PLPTypes.Type;
import cop5556fa18.PLPScanner.Kind;


import org.objectweb.asm.Opcodes;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

//import java.util.List;

public class PLPCodeGen implements PLPASTVisitor, Opcodes {
	
	ClassWriter cw;
	String className;
	String classDesc;
	String sourceFileName;
	
	
	
	MethodVisitor mv; // visitor of method currently under construction
	int slotNum = 5;

	/** Indicates whether genPrint and genPrintTOS should generate code. */
	final boolean DEVEL;
	final boolean GRADE;
	

	public PLPCodeGen(String sourceFileName, boolean dEVEL, boolean gRADE) {
		super();
		this.sourceFileName = sourceFileName;
		DEVEL = dEVEL;
		GRADE = gRADE;
	}

	@Override
	public Object visitBlock(Block block, Object arg) throws Exception {
		// TODO refactor and extend as necessary
		for (PLPASTNode node : block.declarationsAndStatements) {
			if (node.getClass() == VariableDeclaration.class) {
				VariableDeclaration temp = (VariableDeclaration) node;
				temp.setSlot(slotNum++);
			}
			else if (node.getClass() == VariableListDeclaration.class) {
				VariableListDeclaration temp = (VariableListDeclaration) node;
				for (String name : temp.names) {
					temp.setSlot(name, slotNum++);
				}
			}
		}
		
		Label start = new Label();
		Label end   = new Label();
		mv.visitLabel(start);
		for (PLPASTNode node : block.declarationsAndStatements) {
			node.visit(this, null);
		}
		mv.visitLabel(end);
		for (PLPASTNode node : block.declarationsAndStatements) {
			if (node.getClass() == VariableDeclaration.class) {
				VariableDeclaration temp = (VariableDeclaration) node;
				Type type = PLPTypes.getType(temp.type);
				int index = temp.getSlot();
				visitVar(type, temp.name, start, end, mv, index);
			}
			else if (node.getClass() == VariableListDeclaration.class) {
				VariableListDeclaration temp = (VariableListDeclaration) node;
				for (String name : temp.names) {
					Type type = PLPTypes.getType(temp.type);
					int index = temp.getSlot(name);
					visitVar(type, name, start, end, mv, index);
				}
			}
		}
		return null;
	}

	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		// TODO refactor and extend as necessary
		cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		// cw = new ClassWriter(0); 
		// If the call to mv.visitMaxs(1, 1) crashes, it is sometimes helpful 
		// to temporarily run it without COMPUTE_FRAMES. You probably won't 
		// get a completely correct classfile, but you will be able to see the 
		// code that was generated.
		
		className = program.name;
		classDesc = "L" + className + ";";
		String sourceFileName = (String) arg;
		cw.visit(52, ACC_PUBLIC + ACC_SUPER, className, null, "java/lang/Object", null);
		cw.visitSource(sourceFileName, null);
		
		// create main method
		mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
		// initialize
		mv.visitCode();
		
		// add label before first instruction
		Label mainStart = new Label();
		mv.visitLabel(mainStart);

		PLPCodeGenUtils.genLog(DEVEL, mv, "entering main");

		program.block.visit(this, arg);

		// generates code to add string to log
		PLPCodeGenUtils.genLog(DEVEL, mv, "leaving main");
		
		// adds the required (by the JVM) return statement to main
		mv.visitInsn(RETURN);

		// adds label at end of code
		Label mainEnd = new Label();
		mv.visitLabel(mainEnd);
		mv.visitLocalVariable("args", "[Ljava/lang/String;", null, mainStart, mainEnd, 0);
		
		// Because we use ClassWriter.COMPUTE_FRAMES as a parameter in the
		// constructor, asm will calculate this itself and the parameters are ignored.
		// If you have trouble with failures in this routine, it may be useful
		// to temporarily change the parameter in the ClassWriter constructor
		// from COMPUTE_FRAMES to 0.
		// The generated classfile will not be correct, but you will at least be
		// able to see what is in it.
		mv.visitMaxs(0, 0);

		// terminate construction of main method
		mv.visitEnd();

		// terminate class construction
		cw.visitEnd();

		// generate classfile as byte array and return
		return cw.toByteArray();			
	}

	@Override
	public Object visitVariableDeclaration(VariableDeclaration declaration, Object arg) throws Exception {
		if(declaration.expression != null) {
			declaration.expression.visit(this, arg);
			Type type = declaration.expression.getType();
			int decType_slot = declaration.getSlot();
			switch (type) {				// leave the expression value on top of the stack.
				case INTEGER: {
					mv.visitVarInsn(ISTORE, decType_slot);
				}
					break;
				case BOOLEAN: {
					mv.visitVarInsn(ISTORE, decType_slot);
				}
					break;
				case FLOAT: {
					mv.visitVarInsn(FSTORE, decType_slot);
				}
					break;
				case STRING: {
					mv.visitVarInsn(ASTORE, decType_slot);
				}
					break;
				case CHAR: {
					mv.visitVarInsn(ISTORE, decType_slot);
				}
					break;
				default: {
					throw new Exception("CodeGen declaration.type wrong.");
				}
			}
		}
		return null;
	}

	@Override
	public Object visitVariableListDeclaration(VariableListDeclaration declaration, Object arg) throws Exception {
		return null;
	}

	@Override
	public Object visitExpressionBooleanLiteral(ExpressionBooleanLiteral expressionBooleanLiteral, Object arg)
			throws Exception {
		mv.visitLdcInsn(expressionBooleanLiteral.value);
		return null;
	}

	@Override
	public Object visitExpressionBinary(ExpressionBinary expressionBinary, Object arg) throws Exception {
		Label set_true = new Label();
		Label l = new Label();
		Type t1 = expressionBinary.leftExpression.getType();
		Type t2 = expressionBinary.rightExpression.getType();
		expressionBinary.leftExpression.visit(this, arg);
		expressionBinary.rightExpression.visit(this, arg);
		if(expressionBinary.op.equals(Kind.OP_PLUS))
		{
			if(t1.equals(Type.INTEGER) && t2.equals(Type.INTEGER))
			{
				mv.visitInsn(IADD);
			}
			else if(t1.equals(Type.FLOAT) && t2.equals(Type.FLOAT))
			{
				mv.visitInsn(FADD);
			}
			else if(t1.equals(Type.INTEGER) && t2.equals(Type.FLOAT))
			{
				mv.visitInsn(SWAP);
				mv.visitInsn(I2F);
				mv.visitInsn(FADD);
			}
			else if(t1.equals(Type.FLOAT) && t2.equals(Type.INTEGER))
			{
				mv.visitInsn(I2F);
				mv.visitInsn(FADD);
			}
			else if (t1.equals(Type.STRING) && t2.equals(Type.STRING)) {					
				Label start = new Label();
				mv.visitLabel(start);
				mv.visitVarInsn(ASTORE, 0);
				mv.visitVarInsn(ASTORE, 1);
				mv.visitTypeInsn(NEW, "java/lang/StringBuilder");
				mv.visitInsn(DUP);
				mv.visitVarInsn(ALOAD, 1);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/String", "valueOf", "(Ljava/lang/Object;)Ljava/lang/String;", false);
				mv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "(Ljava/lang/String;)V", false);
				mv.visitVarInsn(ALOAD, 0);
				mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
				mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
			}
		}
		else if(expressionBinary.op.equals(Kind.OP_MINUS))
		{
			if(t1.equals(Type.INTEGER) && t2.equals(Type.INTEGER))
			{
				mv.visitInsn(ISUB);
			}
			else if(t1.equals(Type.FLOAT) && t2.equals(Type.FLOAT))
			{
				mv.visitInsn(FSUB);
			}
			else if(t1.equals(Type.INTEGER) && t2.equals(Type.FLOAT))
			{
				mv.visitInsn(SWAP);
				mv.visitInsn(I2F);
				mv.visitInsn(SWAP);
				mv.visitInsn(FSUB);
			}
			else if(t1.equals(Type.FLOAT) && t2.equals(Type.INTEGER))
			{
				mv.visitInsn(I2F);
				mv.visitInsn(FSUB);
			}
		}
		else if(expressionBinary.op.equals(Kind.OP_TIMES))
		{
			if(t1.equals(Type.INTEGER) && t2.equals(Type.INTEGER))
			{
				mv.visitInsn(IMUL);
			}
			else if(t1.equals(Type.FLOAT) && t2.equals(Type.FLOAT))
			{
				mv.visitInsn(FMUL);
			}
			else if(t1.equals(Type.INTEGER) && t2.equals(Type.FLOAT))
			{
				mv.visitInsn(SWAP);
				mv.visitInsn(I2F);
				mv.visitInsn(FMUL);
			}
			else if(t1.equals(Type.FLOAT) && t2.equals(Type.INTEGER))
			{
				mv.visitInsn(I2F);
				mv.visitInsn(FMUL);
			}
		}
		else if(expressionBinary.op.equals(Kind.OP_DIV))
		{
			if(t1.equals(Type.INTEGER) && t2.equals(Type.INTEGER))
			{
				mv.visitInsn(IDIV);
			}
			else if(t1.equals(Type.FLOAT) && t2.equals(Type.FLOAT))
			{
				mv.visitInsn(FDIV);
			}
			else if(t1.equals(Type.INTEGER) && t2.equals(Type.FLOAT))
			{
				mv.visitInsn(SWAP);
				mv.visitInsn(I2F);
				mv.visitInsn(SWAP);
				mv.visitInsn(FDIV);
			}
			else if(t1.equals(Type.FLOAT) && t2.equals(Type.INTEGER))
			{
				mv.visitInsn(I2F);
				mv.visitInsn(FDIV);
			}
		}
		else if(expressionBinary.op.equals(Kind.OP_POWER))
		{
			if(t1.equals(Type.FLOAT) && t2.equals(Type.FLOAT))
			{
				mv.visitInsn(F2D);
				mv.visitVarInsn(DSTORE, 0);
				mv.visitInsn(F2D);
				mv.visitVarInsn(DLOAD, 0);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "pow", "(DD)D", false);
				mv.visitInsn(D2F);
			}
			if(t1.equals(Type.INTEGER) && t2.equals(Type.INTEGER))
			{
				mv.visitInsn(I2D);
				mv.visitVarInsn(DSTORE, 0);
				mv.visitInsn(I2D);
				mv.visitVarInsn(DLOAD, 0);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "pow", "(DD)D", false);
				mv.visitInsn(D2I);
			}
			if(t1.equals(Type.INTEGER) && t2.equals(Type.FLOAT))
			{
				mv.visitInsn(F2D);
				mv.visitVarInsn(DSTORE, 0);
				mv.visitInsn(I2D);
				mv.visitVarInsn(DLOAD, 0);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "pow", "(DD)D", false);
				mv.visitInsn(D2F);
			}
			if(t1.equals(Type.FLOAT) && t2.equals(Type.INTEGER))
			{
				mv.visitInsn(I2D);
				mv.visitVarInsn(DSTORE, 0);
				mv.visitInsn(F2D);
				mv.visitVarInsn(DLOAD, 0);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "pow", "(DD)D", false);
				mv.visitInsn(D2F);
			}
		}
		else if(expressionBinary.op.equals(Kind.OP_MOD))
		{
			mv.visitInsn(IREM);
		}
		else if(expressionBinary.op.equals(Kind.OP_AND))
		{
			mv.visitInsn(IAND);
		}
		else if(expressionBinary.op.equals(Kind.OP_OR))
		{
			mv.visitInsn(IOR);
		}
		else if(expressionBinary.op.equals(Kind.OP_EQ))
		{
			if(t1.equals(Type.INTEGER) || t2.equals(Type.BOOLEAN))
			{
				mv.visitJumpInsn(IF_ICMPEQ, set_true);
			}
			else
			{
				mv.visitInsn(FCMPL);
				mv.visitJumpInsn(IFEQ, set_true);
			}
			mv.visitLdcInsn(false);
		}
		else if(expressionBinary.op.equals(Kind.OP_NEQ))
		{
			if(t1.equals(Type.INTEGER) || t2.equals(Type.BOOLEAN))
			{
				mv.visitJumpInsn(IF_ICMPNE, set_true);
			}
			else
			{
				mv.visitInsn(FCMPL);
				mv.visitJumpInsn(IFNE, set_true);
			}
			mv.visitLdcInsn(false);
		}
		else if(expressionBinary.op.equals(Kind.OP_GT))
		{
			if(t1.equals(Type.INTEGER) || t2.equals(Type.BOOLEAN))
			{
				mv.visitJumpInsn(IF_ICMPGT, set_true);
			}
			else
			{
				mv.visitInsn(FCMPL);
				mv.visitJumpInsn(IFGT, set_true);
			}
			mv.visitLdcInsn(false);
		}
		else if(expressionBinary.op.equals(Kind.OP_GE))
		{
			if(t1.equals(Type.INTEGER) || t2.equals(Type.BOOLEAN))
			{
				mv.visitJumpInsn(IF_ICMPGE, set_true);
			}
			else
			{
				mv.visitInsn(FCMPL);
				mv.visitJumpInsn(IFGE, set_true);
			}
			mv.visitLdcInsn(false);
		}
		else if(expressionBinary.op.equals(Kind.OP_LT))
		{
			if(t1.equals(Type.INTEGER) || t2.equals(Type.BOOLEAN))
			{
				mv.visitJumpInsn(IF_ICMPLT, set_true);
			}
			else
			{
				mv.visitInsn(FCMPL);
				mv.visitJumpInsn(IFLT, set_true);
			}
			mv.visitLdcInsn(false);
		}
		else if(expressionBinary.op.equals(Kind.OP_LE))
		{
			if(t1.equals(Type.INTEGER) || t2.equals(Type.BOOLEAN))
			{
				mv.visitJumpInsn(IF_ICMPLE, set_true);
			}
			else
			{
				mv.visitInsn(FCMPL);
				mv.visitJumpInsn(IFLE, set_true);
			}
			mv.visitLdcInsn(false);
		}
		mv.visitJumpInsn(GOTO, l);
		mv.visitLabel(set_true);
		mv.visitLdcInsn(true);
		mv.visitLabel(l);				
		return null;
	}

	@Override
	public Object visitExpressionConditional(ExpressionConditional expressionConditional, Object arg) throws Exception {
		expressionConditional.condition.visit(this, arg);
		Label set_true = new Label();
		Label l = new Label();
		mv.visitJumpInsn(IFNE, set_true);
		expressionConditional.falseExpression.visit(this, arg);
		mv.visitJumpInsn(GOTO, l);
		mv.visitLabel(set_true);
		expressionConditional.trueExpression.visit(this, arg);
		mv.visitLabel(l);
		return null;
	}

	@Override
	public Object visitExpressionFloatLiteral(ExpressionFloatLiteral expressionFloatLiteral, Object arg)
			throws Exception {
		mv.visitLdcInsn(expressionFloatLiteral.value);
		return null;
	}

	@Override
	public Object visitFunctionWithArg(FunctionWithArg FunctionWithArg, Object arg) throws Exception {
		FunctionWithArg.expression.visit(this, arg);
		Type t = FunctionWithArg.expression.getType();
		if(FunctionWithArg.functionName.equals(Kind.KW_sin))
		{
			mv.visitInsn(F2D);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "sin", "(D)D", false);
			mv.visitInsn(D2F);
		}
		else if(FunctionWithArg.functionName.equals(Kind.KW_cos))
		{
			mv.visitInsn(F2D);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "cos", "(D)D", false);
			mv.visitInsn(D2F);
		}
		else if(FunctionWithArg.functionName.equals(Kind.KW_atan))
		{
			mv.visitInsn(F2D);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "atan", "(D)D", false);
			mv.visitInsn(D2F);
		}
		else if(FunctionWithArg.functionName.equals(Kind.KW_log))
		{
			mv.visitInsn(F2D);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "log", "(D)D", false);
			mv.visitInsn(D2F);
		}
		else if(FunctionWithArg.functionName.equals(Kind.KW_abs))
		{
			if(t.equals(Type.FLOAT))
			{
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "abs", "(F)F", false);
			}
			else if(t.equals(Type.INTEGER))
			{
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "abs", "(I)I", false);
			}
		}
		else if(FunctionWithArg.functionName.equals(Kind.KW_int))
		{
			if(t.equals(Type.FLOAT))
			{
				mv.visitInsn(F2I);
			}
		}
		else if(FunctionWithArg.functionName.equals(Kind.KW_float))
		{
			if(t.equals(Type.INTEGER))
			{
				mv.visitInsn(I2F);
			}
		}
		
		return null;
	}

	@Override
	public Object visitExpressionIdent(ExpressionIdentifier expressionIdent, Object arg) throws Exception {
		int []opcode = {ILOAD, FLOAD, ALOAD};
		
		if (expressionIdent.dec.getClass() == VariableDeclaration.class) {
			VariableDeclaration dec = (VariableDeclaration) expressionIdent.dec;
			Type type = PLPTypes.getType(dec.type);
			VisitVar(opcode, type, mv, dec.getSlot());
		}
		
		else if (expressionIdent.dec.getClass() == VariableListDeclaration.class) {
			VariableListDeclaration dec = (VariableListDeclaration) expressionIdent.dec;
			Type type = PLPTypes.getType(dec.type);
			VisitVar(opcode, type, mv, dec.getSlot(expressionIdent.name));
		}
		
		return null;
	}

	@Override
	public Object visitExpressionIntegerLiteral(ExpressionIntegerLiteral expressionIntegerLiteral, Object arg)
			throws Exception {
		mv.visitLdcInsn(expressionIntegerLiteral.value);
		return null;
	}

	@Override
	public Object visitExpressionStringLiteral(ExpressionStringLiteral expressionStringLiteral, Object arg)
			throws Exception {
		mv.visitLdcInsn(expressionStringLiteral.text);
		return null;
	}

	@Override
	public Object visitExpressionCharLiteral(ExpressionCharLiteral expressionCharLiteral, Object arg) throws Exception {
		mv.visitLdcInsn(expressionCharLiteral.text);
		return null;
	}

	@Override
	public Object visitAssignmentStatement(AssignmentStatement statementAssign, Object arg) throws Exception {
		statementAssign.lhs.visit(this, arg);
		statementAssign.expression.visit(this, arg);
		
		int []opcode = {ISTORE, FSTORE, ASTORE};
		
		if (statementAssign.lhs.dec.getClass() == VariableDeclaration.class) {
			VariableDeclaration temp = (VariableDeclaration)statementAssign.lhs.dec;
			Type type = PLPTypes.getType(temp.type);
			VisitVar(opcode, type, mv, temp.getSlot());
		}
		else if (statementAssign.lhs.dec.getClass() == VariableListDeclaration.class) {
			VariableListDeclaration temp = (VariableListDeclaration)statementAssign.lhs.dec;
			Type type = PLPTypes.getType(temp.type);
			VisitVar(opcode, type, mv, temp.getSlot(statementAssign.lhs.identifier));
		}
		
		return null;
	}

	@Override
	public Object visitLHS(LHS lhs, Object arg) throws Exception {
//		lhs.visit(this, arg);
//		mv.visitVarInsn(ISTORE, 1);					//???????
		return null;
	}

	@Override
	public Object visitIfStatement(IfStatement ifStatement, Object arg) throws Exception {
		ifStatement.condition.visit(this, arg);
		Label lb = new Label();
		mv.visitJumpInsn(IFEQ,lb);			//if condition == 0 jump to Label lb
		ifStatement.block.visit(this, arg);
		mv.visitLabel(lb);
		return null;
	}

	@Override
	public Object visitWhileStatement(WhileStatement whileStatement, Object arg) throws Exception {
		Label condition = new Label(); // whether meet the requirement of while loop.
		Label startWhile = new Label(); // start while loop.
		mv.visitJumpInsn(GOTO, condition);
		mv.visitLabel(startWhile);
		whileStatement.b.visit(this, arg);
		mv.visitLabel(condition);
		whileStatement.condition.visit(this, arg);
		mv.visitJumpInsn(IFNE, startWhile);
		return null;
	}

	@Override
	public Object visitPrintStatement(PrintStatement printStatement, Object arg) throws Exception {
		/**
		 * TODO refactor and complete implementation.
		 * 
		 * In all cases, invoke CodeGenUtils.genLogTOS(GRADE, mv, type); before
		 * consuming top of stack.
		 */
		printStatement.expression.visit(this, arg);
		Type type = printStatement.expression.getType();
		switch (type) {
			case INTEGER : {
				PLPCodeGenUtils.genLogTOS(GRADE, mv, type);
				mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out",
						"Ljava/io/PrintStream;");
				mv.visitInsn(Opcodes.SWAP);
				mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream",
						"println", "(I)V", false);
			}
			break;
			case BOOLEAN : {
				PLPCodeGenUtils.genLogTOS(GRADE, mv, type);
				mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out",
						"Ljava/io/PrintStream;");
				mv.visitInsn(Opcodes.SWAP);
				mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream",
						"println", "(Z)V", false);					
			}
			break; //commented out because currently unreachable. You will need
			// it.
			case FLOAT : {
				PLPCodeGenUtils.genLogTOS(GRADE, mv, type);
				mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out",
						"Ljava/io/PrintStream;");
				mv.visitInsn(Opcodes.SWAP);
				mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream",
						"println", "(F)V", false);
			}
			break; //commented out because currently unreachable. You will need
			// it.
			case CHAR : {
				PLPCodeGenUtils.genLogTOS(GRADE, mv, type);
				mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out",
						"Ljava/io/PrintStream;");
				mv.visitInsn(Opcodes.SWAP);
				mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream",
						"println", "(C)V", false);						
			}
			break; //commented out because currently unreachable. You will need
			// it.
			case STRING : {
				PLPCodeGenUtils.genLogTOS(GRADE, mv, type);
				mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out",
						"Ljava/io/PrintStream;");
				mv.visitInsn(Opcodes.SWAP);
				mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream",
						"println", "(Ljava/lang/String;)V", false);
			}
			break;
			default:
				break;
		}
		return null;
		
	}

	@Override
	public Object visitSleepStatement(SleepStatement sleepStatement, Object arg) throws Exception {
		sleepStatement.time.visit(this, arg);
		mv.visitInsn(I2L);
		mv.visitMethodInsn(INVOKESTATIC, "java/lang/Thread", "sleep", "(J)V", false);
		return null;
	}

	@Override
	public Object visitExpressionUnary(ExpressionUnary expressionUnary, Object arg) throws Exception {
		expressionUnary.expression.visit(this, arg);
		Type type = expressionUnary.expression.getType();
		
		switch (expressionUnary.op) {
		case OP_EXCLAMATION: { // !
			switch (type) {
			case INTEGER: {
				mv.visitLdcInsn(-1);
				mv.visitInsn(IXOR);
			}
			break;
			
			case BOOLEAN: { // true or false
				Label t = new Label(); // Should be set to true.
				Label end = new Label(); // Should be set to false.
				
				mv.visitJumpInsn(IFEQ, t);
				mv.visitLdcInsn(false);
				mv.visitJumpInsn(GOTO, end);
				
				mv.visitLabel(t);
				mv.visitLdcInsn(true);
				mv.visitLabel(end);
			}
			break;
			
			default:
				break;
			}
		}
		break;
		case OP_MINUS: { // -
			switch (type) {
			case INTEGER:
				mv.visitInsn(INEG);
				break;
			case FLOAT:
				mv.visitInsn(FNEG);
				break;
			default:
				break;
			}
		}
		break;
		default:
			break;
		}
		return null;
	}
	
	private void VisitVar(int []opcode, Type type, MethodVisitor mv, int index) throws Exception {
		switch (type) {
		case INTEGER:
			mv.visitVarInsn(opcode[0], index); 
			break;
		case BOOLEAN:
			mv.visitVarInsn(opcode[0], index);
			break;
		case FLOAT:
			mv.visitVarInsn(opcode[1], index);
			break;
		case CHAR:
			mv.visitVarInsn(opcode[0], index);
			break;
		case STRING:
			mv.visitVarInsn(opcode[2], index);
			break;
		default:
			throw new Exception("Type Error!");
		}
		return;
	}
	
	private void visitVar(Type type, String name, Label start, Label end, MethodVisitor mv, int index) 
			throws Exception {
		switch (type) {
		case INTEGER: 
			mv.visitLocalVariable(name, "I", null, start, end, index);
			break;
		case BOOLEAN:
			mv.visitLocalVariable(name, "Z", null, start, end, index);
			break;
		case FLOAT:
			mv.visitLocalVariable(name, "F", null, start, end, index);
			break;
		case CHAR:
			mv.visitLocalVariable(name, "C", null, start, end, index);
			break;
		case STRING:
			mv.visitLocalVariable(name, "Ljava/lang/String;", null, start, end, index);
			break;
		default:
			throw new Exception("Type Error!");
		}
		return;
	}

}

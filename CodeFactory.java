
//change variables list to be symbol list with each type so that data can be properly declared.
//this checks types of variables so that proper read/write can be used. 
//also change symbol table to include types.
class CodeFactory {
	private static int tempCount;
	public static SymbolTable variablesList;
	private static int labelCount = 0;
	private static boolean firstWrite = true;

	public CodeFactory() {
		tempCount = 0;
		variablesList = new SymbolTable();
	}

	//sets variables list equal to symbol table each time a 
	//new symbol is added
	void generateDeclaration() {
		variablesList = Parser.symbolTable;
	}

	Expression generateArithExpr(Expression left, Expression right, Operation op) {
		Expression tempExpr = new Expression(Expression.TEMPEXPR, createIntTempName());
		if (right.expressionType == Expression.LITERALEXPR) {
			System.out.println("\tMOVL " + "$" + right.expressionName + ", %ebx");
		} else {
			System.out.println("\tMOVL " + right.expressionName + ", %ebx");
		}
		if (left.expressionType == Expression.LITERALEXPR) {
			System.out.println("\tMOVL " + "$" + left.expressionName + ", %eax");
		} else {
			System.out.println("\tMOVL " + left.expressionName + ", %eax");
		}
		if (op.opType == Token.PLUS) {
			System.out.println("\tADD %ebx, %eax");
			
		} else if (op.opType == Token.MINUS) {
			System.out.println("\tSUB %ebx, %eax");
		}
		else if(op.opType == Token.MULT){
			System.out.println("\timull %ebx");
		}
		else if(op.opType == Token.DIV){
			System.out.println("\txorl %edx, %edx"); //clears edx
			System.out.println("\tidiv %ebx");
		}
		else if (op.opType == Token.MOD){
			System.out.println("\txorl %edx, %edx"); //clears edx
			System.out.println("\tidiv %ebx");
			System.out.println("\tmovl %edx, " + tempExpr.expressionName);
			return tempExpr;
		}
		System.out.println("\tMOVL " + "%eax, " + tempExpr.expressionName);
		return tempExpr;
	}

	void generateWrite(Expression expr) {
		switch (expr.expressionType) {
		case Expression.IDEXPR:
		case Expression.TEMPEXPR: {
			generateAssemblyCodeForWriting(expr.expressionName);
			break;
		}
		case Expression.LITERALEXPR: {
			generateAssemblyCodeForWriting("$" + expr.expressionName);
		}
		}
	}

	private void generateAssemblyCodeForWriting(String idName) {
		if (!firstWrite) {
			
			System.out.println("\tmovl " + idName + ",%eax");
			System.out.println("\tpushl %eax");
			System.out.println("\tcall __reversePrint    /* The return address is at top of stack! */");
			System.out.println("\tpopl  %eax    /* Remove value pushed onto the stack */");
			
		} else
		// String reverseLoopLabel = generateLabel("reverseLoop");
		{
			firstWrite = false;
			
			System.out.println("\tmovl " + idName + ",%eax");
			System.out.println("\tpushl %eax");
			System.out.println("\tcall __reversePrint    /* The return address is at top of stack! */");
			System.out.println("\tpopl  %eax    /* Remove value pushed onto the stack */");
			System.out.println("\tjmp __writeExit");  /* Needed to jump over the reversePrint code since it was called */

			System.out.println("__reversePrint: ");
			System.out.println("\t/* Save registers this method modifies */");
			System.out.println("\tpushl %eax");
			System.out.println("\tpushl %edx");
			System.out.println("\tpushl %ecx");
			System.out.println("\tpushl %ebx");

			System.out.println("\tcmpw $0, 20(%esp)");
			System.out.println("\tjge __positive");
			System.out.println("\t/* Display minus on console */");
			System.out.println("\tmovl $4, %eax       /* The system call for write (sys_write) */");
			System.out.println("\tmovl $1, %ebx       /* File descriptor 1 - standard output */");
			System.out.println("\tmovl $1, %edx     /* Place number of characters to display */");
			System.out.println("\tmovl $__minus, %ecx   /* Put effective address of stack into ecx */");
			System.out.println("\tint $0x80	    /* Call to the Linux OS */");
			
			System.out.println("\t__positive:");
			System.out.println("\txorl %eax, %eax       /* eax = 0 */");
			System.out.println("\txorl %ecx, %ecx       /* ecx = 0, to track characters printed */");

			System.out.println("\t/** Skip 16-bytes of register data stored on stack and 4 bytes");
			System.out.println("\tof return address to get to first parameter on stack ");
			System.out.println("\t*/   ");
			System.out.println("\tmovw 20(%esp), %ax     /* ax = parameter on stack */");

			System.out.println("\tcmpw $0, %ax");
			System.out.println("\tjge __reverseLoop");
			System.out.println("\tmulw __negOne\n");
			
			System.out.println("__reverseLoop:");

			System.out.println("\tcmpw $0, %ax");
			System.out.println("\tje   __reverseExit");
			System.out.println("\t/* Do div and mod operations */");
			System.out.println("\tmovl $10, %ebx         /* ebx = 10 as divisor  */");
			System.out.println("\txorl %edx, %edx        /* edx = 0 to get remainder */");
			System.out.println("\tidivl %ebx             /* edx = eax % 10, eax /= 10 */");
			System.out.println("\taddb $'0', %dl         /* convert 0..9 to '0'..'9'  */");

			System.out.println("\tdecl %esp              /* use stack to store digit  */");
			System.out.println("\tmovb %dl, (%esp)       /* Save character on stack.  */");
			System.out.println("\tincl %ecx              /* track number of digits.   */");

			System.out.println("\tjmp __reverseLoop");

			System.out.println("__reverseExit:");

			System.out.println("__printReverse:");

			System.out.println("\t/* Display characters on _stack_ on console */");

			System.out.println("\tmovl $4, %eax       /* The system call for write (sys_write) */");
			System.out.println("\tmovl $1, %ebx       /* File descriptor 1 - standard output */");
			System.out.println("\tmovl %ecx, %edx     /* Place number of characters to display */");
			System.out.println("\tleal (%esp), %ecx   /* Put effective address of stack into ecx */");
			System.out.println("\tint $0x80	    /* Call to the Linux OS */");

			System.out.println("\t /* Clean up data and registers on the stack */");
			System.out.println("\taddl %edx, %esp");
			System.out.println("\tpopl %ebx");
			System.out.println("\tpopl %ecx");
			System.out.println("\tpopl %edx");
			System.out.println("\t popl %eax");

			System.out.println("\tret");
			System.out.println("__writeExit:");
		}
	}

	void generateRead(Expression expr) {
		switch (expr.expressionType) {
		case Expression.IDEXPR:
		case Expression.TEMPEXPR: {
			generateAssemblyCodeForReading(expr.expressionName);
			break;
		}
		case Expression.LITERALEXPR: {
			// not possible since you cannot read into a literal. An error
			// should be generated
		}
		}
	}

	private void generateAssemblyCodeForReading(String idName) {
		
		String readLoopLabel = generateLabel("__readLoop");
		String readLoopEndLabel = generateLabel("__readLoopEnd");
		String readEndLabel = generateLabel("__readEnd");
		String readPositiveLabel = generateLabel("__readPositive");
		
		System.out.println("\tmovl $0, " + idName);
		
		System.out.println("\tmovl %esp, %ebp");
		System.out.println("\t/* read first character to check for negative */");
		System.out.println("\tmovl $3, %eax        /* The system call for read (sys_read) */");
		System.out.println("\tmovl $0, %ebx        /* File descriptor 0 - standard input */");
		System.out.println("\tlea 4(%ebp), %ecx      /* Put the address of character in a buffer */");
		System.out.println("\tmovl $1, %edx        /* Place number of characters to read in edx */");
		System.out.println("\tint $0x80	     /* Call to the Linux OS */ ");
		System.out.println("\tmovb 4(%ebp), %al");
		System.out.println("\tcmpb $'\\n', %al      /* Is the newline character? */");
		System.out.println("\tje  " + readEndLabel);
		System.out.println("\tcmpb $'-', %al		/* Is the character '-'? */");
		System.out.println("\tjne " + readPositiveLabel);
		
		System.out.println("\tmovb $'-', __negFlag	");
		System.out.println("\tjmp " + readLoopLabel);
		
		
		System.out.println(readPositiveLabel + ":");
		System.out.println("\tcmpb $'+', %al");
		System.out.println("\tje " + readLoopLabel);
		System.out.println("\t/*Process the first digit that is not a minnus or newline.*/");
		System.out.println("\tsubb $'0', 4(%ebp)      /* Convert '0'..'9' to 0..9 */ \n");

		System.out.println("\t/* result  = (result * 10) + (idName  - '0') */");
		System.out.println("\tmovl $10, %eax");
		System.out.println("\txorl %edx, %edx");
		System.out.println("\tmull " + idName + "        /* result  *= 10 */");
		System.out.println("\txorl %ebx, %ebx    /* ebx = (int) idName */");
		System.out.println("\tmovb 4(%ebp), %bl");
		System.out.println("\taddl %ebx, %eax    /* eax += idName */");
		System.out.println("\tmovl %eax, " + idName);
		
		
		System.out.println(readLoopLabel + ":");
		System.out.println("\tmovl $3, %eax        /* The system call for read (sys_read) */");
		System.out.println("\tmovl $0, %ebx        /* File descriptor 0 - standard input */");
		System.out.println("\tlea 4(%ebp), %ecx      /* Put the address of character in a buffer */");
		System.out.println("\tmovl $1, %edx        /* Place number of characters to read in edx */");
		System.out.println("\tint $0x80	     /* Call to the Linux OS */ \n");

		System.out.println("\tmovb 4(%ebp), %al");
		System.out.println("\tcmpb $'\\n', %al      /* Is the character '\\n'? */");

		
		System.out.println("\tje  " + readLoopEndLabel);
		System.out.println("\tsubb $'0', 4(%ebp)      /* Convert '0'..'9' to 0..9 */ \n");

		System.out.println("\t/* result  = (result * 10) + (idName  - '0') */");
		System.out.println("\tmovl $10, %eax");
		System.out.println("\txorl %edx, %edx");
		System.out.println("\tmull " + idName + "        /* result  *= 10 */");
		System.out.println("\txorl %ebx, %ebx    /* ebx = (int) idName */");
		System.out.println("\tmovb 4(%ebp), %bl");
		System.out.println("\taddl %ebx, %eax    /* eax += idName */");
		System.out.println("\tmovl %eax, " + idName);
		System.out.println("\t/* Read the next character */");
		System.out.println("\tjmp " + readLoopLabel);
		System.out.println(readLoopEndLabel + ":\n");
		System.out.println("\tcmpb $'-', __negFlag");
		System.out.println("\tjne " + readEndLabel);
		System.out.println("\tmovl a, %eax");
		System.out.println("\tmull __negOne");
		System.out.println("\tmovl %eax, a");
		System.out.println("\tmovb $'+', __negFlag");
		System.out.println(readEndLabel + ":\n");

	}

	private String generateLabel(String start) {
		String label = start + labelCount++;
		return label;
	}

	void generateStart() {
		System.out.println(".text\n.global _start\n\n_start:\n");
	}

	void generateExit() {
		System.out.println("exit:");
		System.out.println("\tmov $1, %eax");
		System.out.println("\tmov $1, %ebx");
		System.out.println("\tint $0x80");
	}

	public void generateData() {
		System.out.println("\n\n.data");
		int i = 0;
		while(i < variablesList.getSize()){
			if(variablesList.getType(i).equals("string")){
				if(variablesList.getValue(variablesList.getItem(i)).equals("")){
					System.out.println(variablesList.getItem(i) + ": .zero 256");
				}
				else {
					System.out.println(variablesList.getItem(i) + ":\t ." + variablesList.getType(i) + " \"" + variablesList.getValue(variablesList.getItem(i)) + "\"");
					System.out.println(".equ " + variablesList.getItem(i) + "Len, . - " + variablesList.getItem(i) + "\n");
				}
			}
			else {
				System.out.println(variablesList.getItem(i) + ":\t ." + "int" + " " + variablesList.getValue(variablesList.getItem(i)));
			}
			i++;
		}
		System.out.println("__minus:  .byte '-'");
		System.out.println("__negOne: .int -1");
		System.out.println("__negFlag: .byte '+'");
	}

	//creating temp name for ints
	public String createIntTempName() {
		Token tempVar = new Token("__temp" + tempCount++, 18);
		variablesList.addIntItem(tempVar);
		return tempVar.getId();
	}
	
	//creating temp name for strings
	public String createStringTempName() {
		Token tempVar = new Token("__temp" + tempCount++, 18);
		variablesList.addStringItem(tempVar);
		return tempVar.getId();
	}

	//creating temp name for strings including value.
	public String createStringTempName(String value) {
		Token tempVar = new Token("__temp" + tempCount++, 18);
		variablesList.addStringItem(tempVar);
		variablesList.addValue(tempVar.getId(), value);
		return tempVar.getId();
	}
	
	//generates assembly code for writing strings to console
	public void generateStringWrite(StringExpression expr) {
		System.out.println("\tmov $4, %eax");
		System.out.println("\tmov $1, %ebx");
		System.out.println("\tmov $" + expr.stringExpressionName + ", %ecx");
		if(expr.stringValue.length() != 0)
			System.out.println("\tmov $" + expr.stringExpressionName + "Len, %edx");
		else 
			System.out.println("\tmov $6, %edx");
		System.out.println("\tint $0x80");

	}

	//string concatenation assembly code. Is not entirely correct, but 
	//we believe that it is close.
	public StringExpression generateStringExpression(
			StringExpression leftString, StringExpression rightString,
			Operation op) {
		StringExpression temp = new StringExpression(StringExpression.TEMPEXPR, createStringTempName(),  leftString.stringValue);
		System.out.println("\tmovl $0, %eax");
		System.out.println("\tmovl $0, %ebx");
		
		System.out.println("firstString: ");
		System.out.println("\tcmpl $0, " + leftString.stringExpressionName + "(%eax)");
		System.out.println("\tje firstDone");
		System.out.println("\tmovb " + leftString.stringExpressionName + "(%eax), %cl");
		System.out.println("\tmovb %cl, " + temp.stringExpressionName + "(%ebx)");
		System.out.println("\tincl %eax");
		System.out.println("\tincl %ebx");
		System.out.println("\tjmp firstString\n");
		
		System.out.println("firstDone: ");
		System.out.println("\tmovl $0, %eax");
		System.out.println("\tmovl $0, %ebx");
		
		System.out.println("secondString: ");
		System.out.println("\tcmpl $0, " + rightString.stringExpressionName + "(%eax)");
		System.out.println("\tje secondDone");
		System.out.println("\tmovb " + rightString.stringExpressionName + "(%eax), %cl");
		System.out.println("\tmovb %cl, " + temp.stringExpressionName + "(%ebx)");
		System.out.println("\tincl %eax");
		System.out.println("\tincl %ebx");
		System.out.println("\tjmp secondString\n");
		
		System.out.println("secondDone: ");
		return temp;
	}

	void generateIntegerAssignment(Expression lValue, Expression expr) {
		if (expr.expressionType == Expression.LITERALEXPR) {
			System.out.println("\tMOVL " + "$" + expr.expressionIntValue + ", %eax");
			System.out.println("\tMOVL %eax, " + lValue.expressionName);
		} else {
			System.out.println("\tMOVL " + expr.expressionName + ", %eax");
			System.out.println("\tMOVL %eax, " + lValue.expressionName);
		}
	}
	
	public void generateBoolAssignment(Expression boolLeftVal, Expression expr) {
		if (expr.expressionType == Expression.LITERALEXPR) {
			System.out.println("\tMOVL " + "$" + expr.expressionIntValue + ", %eax");
			System.out.println("\tMOVL %eax, " + boolLeftVal.expressionName);
		} else {
			System.out.println("\tMOVL " + expr.expressionName + ", %eax");
			System.out.println("\tMOVL %eax, " + boolLeftVal.expressionName);
		}
	}

	public Expression generateBoolExpr(Expression leftOperand,
			Expression rightOperand, Operation op) {
		String falseFunc = generateAssemFuncName("__false");
		String trueFunc = generateAssemFuncName("__true");
		String continueFunc = generateAssemFuncName("__continue");
		Expression tempExpr = new Expression(Expression.TEMPEXPR, createIntTempName());
		if(rightOperand.expressionType == Expression.LITERALEXPR){
			System.out.println("\tmovl $" + rightOperand.expressionName + ", %ebx");
		}
		else {
			System.out.println("\tmovl " + rightOperand.expressionName + ", %ebx");
		}
		if(leftOperand.expressionType == Expression.LITERALEXPR){
			System.out.println("\tmovl " + leftOperand.expressionName + ", %eax");
		}
		else {
			System.out.println("\tmovl " + leftOperand.expressionName + ", %eax");
		}
		
		if(op.opType == Token.AND){
			System.out.println("\tcmpl $0, %eax");
			System.out.println("\tje " + falseFunc);
			System.out.println("\tcmpl $0, %ebx");
			System.out.println("\tje " + falseFunc);
			System.out.println("\tjne " + trueFunc + "\n");
			System.out.println(falseFunc + ": ");
			System.out.println("\tmovl $0, " + tempExpr.expressionName);
			System.out.println("\tjmp" + continueFunc + "\n");
			System.out.println(trueFunc + ": ");
			System.out.println("\tmovl $1, " + tempExpr.expressionName + "\n");
			System.out.println(continueFunc + ": ");
		}
		else if(op.opType == Token.OR){
			System.out.println("\tcmpl $0, %eax");
			System.out.println("\tjne " + trueFunc);
			System.out.println("\tcmpl $0, %ebx");
			System.out.println("\tjne " + trueFunc);
			System.out.println("\tje " + falseFunc + "\n");
			System.out.println(trueFunc + ": ");
			System.out.println("\tmovl $1, " + tempExpr.expressionName);
			System.out.println("\tjmp " + continueFunc + "\n");
			System.out.println(falseFunc + ": ");
			System.out.println("\tmovl $0, " + tempExpr.expressionName + "\n");
			System.out.println(continueFunc + ": ");
		}
		return tempExpr;
	}

	public Expression generateNotExpr(Expression rightOperand, Operation op) {
		Expression tempExpr = new Expression(Expression.TEMPEXPR, createIntTempName());
		if(rightOperand.expressionType == Expression.LITERALEXPR){
			System.out.println("\tmovl $" + rightOperand.expressionName + ", %eax");
		}
		else {
			System.out.println("\tmovl " + rightOperand.expressionName + ", %eax");
		}
		String falseFunc = generateAssemFuncName("__false");
		String trueFunc = generateAssemFuncName("__true");
		String continueFunc = generateAssemFuncName("__continue");
		System.out.println("\tcmpl $0, %eax");
		System.out.println("\tje " + falseFunc);
		System.out.println("\tcmpl $1, %eax");
		System.out.println("\tje " + trueFunc + "\n");
		System.out.println(falseFunc + ": ");
		System.out.println("\tmovl $0, " + tempExpr.expressionName);
		System.out.println("\tjmp " + continueFunc + "\n");
		System.out.println(trueFunc + ": ");
		System.out.println("\tmovl $1, " + tempExpr.expressionName);
		System.out.println("\tjmp " + continueFunc + "\n");
		System.out.println(continueFunc + ": ");
		return tempExpr;
	}
	private String generateAssemFuncName(String func){
		String name = func + labelCount++;
		return name;
	}

	//unsure what goes in this method.
	public void generateStringAssignment(StringExpression stringLeftVal,
			StringExpression stringExpr) {
		
	}
}

import java.util.*;
/*
 * 
 * Micro grammar
<program>          -> #Start BEGIN <statement list> END
<statement list>  -> <statement> {<statement>}
<statement>       -> int <ident>; | int <ident> := <expression>;
<statement>       -> bool <ident>; | bool <ident> := <boolExpression>;
<statement>       -> String <ident>; | String <ident> := <stringExpression>;
<statement>       -> <ident> := <expression> | <ident> := <stringExpression> | <ident> := <boolExpression>; #Assign;
<statement>       -> READ (<id list>);
<statement>       -> WRITE (<expr list>);
<statement>       -> WRITE(<boolExpr list>);
<statement>       -> WRITE(<string list>);
<statement>       -> <if> | <while>
<id list>              -> <ident> #ReadId {, <ident> #ReadId}
<expr list>      -> <expression> #WriteExpr {, <expression> #WriteExpr}
<string list>         -> <string> {, <string> }
<expression>      -> <factor> <addOp> <expression> | <factor>
<factor>              -> <primary> <multOp> <factor> | <primary>
<primary>           -> ( <expression> ) | <ident> 
<primary>           -> IntLiteral
<if>                      -> IF(<relativeExpr>) <statement list> ENDIF <else>
<else>                 -> ELSE <statement list> ENDELSE | $ (blank) //no else statement
<while>               -> WHILE(<relativeExpr>) <statement list> ENDWHILE
<boolExpression> -> <andExpr> ? <boolExpression> | <andExpr>
<andExpr>           -> <notExpr> & <andExpr> | <notExpr>
<notExpr>            -> ! <boolPrimary> | <boolPrimary>
<boolPrimary>    -> ( <boolExpression>) | <ident>
<boolPrimary>    -> IntLiteral
<stringExpression>     ->  <stringPrimary> {+ <stringPrimary> #GenInFix};
<stringPrimary>  ->  <ident>
<stringPrimary>  -> StringLiteral
<relativeExpr>    -> <expr> <relOp> <expr>
<addOp>            -> + #ProcessOp | - #ProcessOp
<multOp>           -> * | / | %
<relOp>              -> == | != | > | < | >= | <=
<ident>              ->  a-z {a-z | 0-9 | specialCharacters} #ProcessId
<system goal>   -> <program> EofSym #Finish

 */


public class Parser
{
    private static Scanner scanner;
    public static SymbolTable symbolTable;
    private static CodeFactory codeFactory;
    private Token currentToken;
    private Token previousToken;
    private static boolean signSet = false;
    private static String signFlag = "+";
    public static ArrayList<SymbolTable> scopes;
    public static int scopeNum;
    public static int returnNum; 
    public Parser()
    {
        
    }

    static public void main (String args[])
    {
        Parser parser = new Parser();
      //  scanner = new Scanner( args[0]);
        scanner = new Scanner("test.txt");
        symbolTable = new SymbolTable();
        codeFactory = new CodeFactory();
        scopes = new ArrayList<SymbolTable>();
        scopes.add(0, symbolTable);
        scopeNum = 0;
        parser.parse();
    }
    
    public void parse()
    {
        currentToken = scanner.findNextToken();
        systemGoal();
    }
    
    private void systemGoal()
    {
        program();
        codeFactory.generateData();
    }
    
    private void program()
    {
        match( Token.BEGIN );
        codeFactory.generateStart();
        statementList();
        match( Token.END );
        codeFactory.generateExit();
    }
    
    private void statementList()
    {
    	//if it hits one of these then it ends the current statement list.
    
    	while(currentToken.getType() != Token.END && currentToken.getType() != Token.ENDIF &&
    			currentToken.getType() != Token.ENDELSE && currentToken.getType() != Token.ENDWHILE){
//    		if(previousToken.getType() == Token.ENDPROC || previousToken.getType() == Token.ENDIF || previousToken.getType() == Token.ENDWHILE || currentToken.getType() == Token.ENDIF){
//        	}
//    		if(currentToken.getType() == Token.PROC || currentToken.getType() == Token.IF || currentToken.getType() == Token.WHILE){
//        		scopeNum++;
//        		System.out.println(scopeNum);
//
//        	}
            statement();
        }
    }
    
    private void statement()
    {
        Expression lValue;
        Expression expr;
        StringExpression stringLeftVal;
        StringExpression stringExpr;
        Expression boolLeftVal;
        
        switch ( currentToken.getType() )
        {
        case Token.READ :
        {
            match( Token.READ );
            match( Token.LPAREN );
            idList();
            match( Token.RPAREN );
            match( Token.SEMICOLON );
            break;
        }
        //checks if command is to write string list or expression list
        case Token.WRITE :
        {
            match( Token.WRITE );
            match( Token.LPAREN );
            if(currentToken.getType() == Token.ID && scopes.get(scopeNum).checkSTforItem(currentToken.getId() + scopeNum ) == true &&
            		scopes.get(scopeNum).getType(symbolTable.getSpot(currentToken.getId() + scopeNum )).equals("string")){
            	stringList();
            }
            else if(currentToken.getType() == Token.ID && symbolTable.checkSTforItem(currentToken.getId() + scopeNum) == true &&
            		scopes.get(scopeNum).getType(symbolTable.getSpot(currentToken.getId() + scopeNum )).equals("int")){
            	expressionList();
            }
            match( Token.RPAREN );
            match( Token.SEMICOLON );
            break;
        }
        //finds if word "int" is present before a variable.
        	case Token.INTTYPE:
        	{
        		match(Token.INTTYPE);
        		lValue = identifier();
        		if(scopes.get(scopeNum).checkSTforItem(lValue.expressionName)){
        			alreadyDeclaredError(previousToken.getId());
        		}
        		if(currentToken.getType() == Token.ASSIGNOP){
        			match(Token.ASSIGNOP);
        			expr = expression();
        			scopes.get(scopeNum).addIntItem(lValue, Integer.toString(expr.expressionIntValue));
        			codeFactory.generateIntegerAssignment(lValue, expr);
        			match(Token.SEMICOLON);
        			break;
        		}
        		else if(currentToken.getType() == Token.SEMICOLON){
        			expr = new Expression(Expression.IDEXPR, "0");
        			scopes.get(scopeNum).addIntItem(lValue, "0");
        			match(Token.SEMICOLON);
        			break;
        		}
        	}
        	//declares and assigns bools as needed.
        	case Token.BOOL : {
        		match(Token.BOOL);
        		boolLeftVal = boolIdentifier();
        		if(scopes.get(scopeNum).checkSTforItem(boolLeftVal.expressionName)){
        			alreadyDeclaredError(previousToken.getId());
        		}
        		if(currentToken.getType() == Token.ASSIGNOP){
        			match(Token.ASSIGNOP);
        			expr = logicalExpressionBegin();
        			scopes.get(scopeNum).addBoolItem(boolLeftVal, Integer.toString(expr.expressionIntValue));
        			codeFactory.generateBoolAssignment(boolLeftVal, expr);
        			match(Token.SEMICOLON);
        			break;
        		}
        		else if (currentToken.getType() == Token.SEMICOLON){
        			expr = new Expression(Expression.IDEXPR, previousToken.getId() + scopeNum);
        			scopes.get(scopeNum).addBoolItem(boolLeftVal, "0");
        			match(Token.SEMICOLON);
        			break;
        		}
        	}
        	case Token.IF : {
        		match(Token.IF);
        		match(Token.LPAREN);
        		Expression result = relationalExpression();
        		match(Token.RPAREN);
        		scopeNum++;
        		if(result.expressionIntValue != 0){
        			statementList();
        			match(Token.ENDIF);
        			scopes.remove(scopeNum);
            		scopeNum--;
        			elseStatement(false);
        			break;
        		}
        		else {
        			while(currentToken.getType() != Token.ENDIF){
        				match(currentToken.getType()); //moves scanner to else part
        			}
        			match(Token.ENDIF);
        			scopes.remove(scopeNum);
            		scopeNum--;
        			elseStatement(true);
        			break;
        		}
        	}
        	case Token.WHILE : {
        		match(Token.WHILE);
        		match(Token.LPAREN);
        		ArrayList<String> whileNameForAssem = new ArrayList<String>();
        		whileNameForAssem = relationalExpForWhile();
        		match(Token.RPAREN);
        		scopeNum++;
       			statementList();
           		codeFactory.generateWhileEnd(whileNameForAssem.get(0), whileNameForAssem.get(1));
        		match(Token.ENDWHILE);
        		scopes.remove(scopeNum);
        		scopeNum--;
        		break;
        	}
        	//declares and assigns strings as needed. also checks for errors.
        	//finds if the word "String" is present before a variable
        	case Token.STRINGTYPE: 
        	{
        		match(Token.STRINGTYPE);
        		stringLeftVal = stringIdentifier();
        		if(scopes.get(scopeNum).checkSTforItem(stringLeftVal.stringExpressionName  + scopeNum)){
        			alreadyDeclaredError(stringLeftVal.stringExpressionName);
        		}
        		if(currentToken.getType() == Token.ASSIGNOP){
        			match(Token.ASSIGNOP);
        			if(currentToken.getType() == Token.ID){
        				if(!scopes.get(scopeNum).checkSTforItem(currentToken.getId()  + scopeNum)){
        					declarationError(currentToken.getId());
        				}
        				if(scopes.get(scopeNum).getValue(currentToken.getId()).equals("")){
            				initializationError(currentToken.getId());
            			}
        				stringExpr = stringExpression();
        				scopes.get(scopeNum).addStringItem(stringLeftVal, stringExpr.stringValue);
        				codeFactory.generateStringAssignment(stringLeftVal, stringExpr);
        				match(Token.SEMICOLON);
        			}
        			else {
	        			stringExpr = stringExpression();
	        			scopes.get(scopeNum).addStringItem(stringLeftVal, stringExpr.stringValue);
	        			codeFactory.generateStringAssignment(stringLeftVal, stringExpr);
	        			match(Token.SEMICOLON);
        			}
        		}
        		else if(currentToken.getType() == Token.SEMICOLON){
        			stringExpr = new StringExpression(StringExpression.LITERALEXPR, "");
        			scopes.get(scopeNum).addStringItem(stringLeftVal, "");
        			codeFactory.generateStringAssignment(stringLeftVal, stringExpr);
        			match(Token.SEMICOLON);
        		}
        		break;
        	}
        	//checks if ID in symbol table and what type it is, and assigns it
        	//as it is needed. If it is not in the symbol table and 
        	//this code is accessed, then a declaration error is thrown.
            case Token.ID:
            {
            	if(scopes.get(scopeNum).checkSTforItem(currentToken.getId()  + scopeNum)){
            		if(scopes.get(scopeNum).getType(currentToken.getId()  + scopeNum).equals("int")){
            			lValue = identifier();
			            match( Token.ASSIGNOP );
			            expr = expression();
		               	codeFactory.generateIntegerAssignment( lValue, expr );
		               	scopes.get(scopeNum).addValue(lValue.expressionName, Integer.toString(expr.expressionIntValue));
		                match( Token.SEMICOLON );
            		}
            		else if(scopes.get(scopeNum).getType(currentToken.getId() + scopeNum).equals("bool")){
            			boolLeftVal = boolIdentifier();
            			match(Token.ASSIGNOP);
            			expr = logicalExpressionBegin();
            			codeFactory.generateBoolAssignment(boolLeftVal, expr);
            			scopes.get(scopeNum).addValue(boolLeftVal.expressionName, Integer.toString(expr.expressionIntValue));
            			match(Token.SEMICOLON);
            		}
            		else {
            			stringLeftVal = stringIdentifier();
            			match( Token.ASSIGNOP );
            			stringExpr = stringExpression();
		               	codeFactory.generateStringAssignment(stringLeftVal, stringExpr);
		               	scopes.get(scopeNum).addValue(stringLeftVal.stringExpressionName, stringExpr.stringValue);
		                match( Token.SEMICOLON );
            		}
	            }
            	else {
            		declarationError(currentToken.getId());
            	}
               break;
            }
            default: error(currentToken);
        }
    }
    
	private ArrayList<String> relationalExpForWhile() {
		ArrayList<String> names = new ArrayList<String>();
		Expression result;
		Expression leftOp;
		Expression rightOp;
		Operation op;
		
		result = factor();
		if(currentToken.getType() == Token.EQUAL || currentToken.getType() == Token.NOTEQUAL ||
				currentToken.getType() == Token.LESS || currentToken.getType() == Token.GREAT ||
				currentToken.getType() == Token.LESSOREQ || currentToken.getType() == Token.GREATOREQ){
			leftOp = result;
			op = relOperation();
			rightOp = expression();
			names = codeFactory.generateWhile(leftOp, rightOp, op);
		}
		return names;
	}

	private void elseStatement(boolean elseIsRun) {
		if(currentToken.getType() == Token.ELSE){
			if(elseIsRun){
				match(Token.ELSE);
				statementList();
				match(Token.ENDELSE);
			}
			else {
				while(currentToken.getType() != Token.ENDELSE){
					match(currentToken.getType());
				} //moves past else statement so nothing gets run
				  //if it isnt supposed to be.
				match(Token.ENDELSE);
			}
		}
		else { //no else statement
			return;
		}
	}

	private Expression relationalExpression() {
		Expression result;
		Expression leftOp;
		Expression rightOp;
		Operation op;
		
		result = logicalExpressionBegin();
		if(currentToken.getType() == Token.EQUAL || currentToken.getType() == Token.NOTEQUAL ||
				currentToken.getType() == Token.GREAT || currentToken.getType() == Token.GREATOREQ || 
				currentToken.getType() == Token.LESS || currentToken.getType() == Token.LESSOREQ){
    		leftOp = result;
    		op = relOperation();
    		rightOp = logicalExpressionBegin();
    		result = codeFactory.generateArithExpr(leftOp, rightOp, op);
		}
		//makes sure no chained operations
		
		if(currentToken.getType() == Token.EQUAL || currentToken.getType() == Token.NOTEQUAL || currentToken.getType() == Token.GREAT || 
			currentToken.getType() == Token.GREATOREQ || currentToken.getType() == Token.LESS || currentToken.getType() == Token.LESSOREQ){
			chainedOpsError();
		}
		return result;
	}

	private Operation relOperation() {
		Operation op = new Operation();
		if(currentToken.getType() == Token.EQUAL){
			match(Token.EQUAL);
			op = processOperation();
		}
		else if(currentToken.getType() == Token.NOTEQUAL){
			match(Token.NOTEQUAL);
			op = processOperation();
		}
		else if(currentToken.getType() == Token.GREAT){
			match(Token.GREAT);
			op = processOperation();
		}
		else if(currentToken.getType() == Token.GREATOREQ){
			match(Token.GREATOREQ);
			op = processOperation();
		}
		else if(currentToken.getType() == Token.LESS){
			match(Token.LESS);
			op = processOperation();
		}
		else if(currentToken.getType() == Token.LESSOREQ){
			match(Token.LESSOREQ);
			op = processOperation();
		}
		else {
			error(currentToken);
		}
		return op;
	}

	private Expression expression()
    {
        Expression result;
        Expression leftOperand;
        Expression rightOperand;
        Operation op;
        
        result = factor();
        while ( currentToken.getType() == Token.PLUS || currentToken.getType() == Token.MINUS )
        {
            leftOperand = result;
            op = addOperation();
            rightOperand = expression();
            result = codeFactory.generateArithExpr( leftOperand, rightOperand, op );
        }
        return result;
    }
    
    private Expression boolIdentifier() {
    	Expression expr;
    	match(Token.ID);
    	expr = processIdentifier();
    	return expr;
	}

    //
	private Expression logicalExpressionBegin() {
    	Expression result;
    	Expression leftOperand;
    	Expression rightOperand;
    	Operation op;
    	
    	result = boolAndExpression();
    	while(currentToken.getType() == Token.AND){
    		leftOperand = result; 
    		op = andOp();
    		rightOperand = logicalExpressionBegin();
    		result = codeFactory.generateBoolExpr(leftOperand, rightOperand, op);
    	}
    	
    	return result;
    }

	//method for mult operations.
    private Expression factor(){
    	Expression result;
    	Expression leftOperand;
    	Expression rightOperand;
    	Operation op;
    	
    	result = primary();
    	while(currentToken.getType() == Token.MULT || 
    			currentToken.getType() == Token.MOD || currentToken.getType() == Token.DIV){
    		leftOperand = result;
    		op = multOp();
    		rightOperand = factor();
    		if(op.opType == Token.DIV && rightOperand.expressionIntValue == 0){
    			divideByZeroError(currentToken);
    			return result;
    		}
    		result = codeFactory.generateArithExpr(leftOperand, rightOperand, op);
    	}
    	return result;
    }
    
    //method similar to addop for multiplication operations.
	private Operation multOp() {
		Operation op = new Operation();
		int type = currentToken.getType();
		if(type == Token.MULT){
			match(Token.MULT);
			op = processOperation();
		}
		else if(type == Token.DIV){
			match(Token.DIV);
			op = processOperation();
		}
		else if(type == Token.MOD){
			match(Token.MOD);
			op = processOperation();
		}
		else {
			error(currentToken);
		}
		return op;
	}
	
	//specific level of precedence for logical operations. 
	//works similarly to expression and string expression.
	private Expression boolAndExpression() {
		Expression result;
		Expression leftOperand;
		Expression rightOperand;
		Operation op;
		
		result = notExpr();
		while(currentToken.getType() == Token.OR){
			leftOperand = result;
			op = orOp();
			rightOperand = boolAndExpression();
			result = codeFactory.generateBoolExpr(leftOperand, rightOperand, op);
		}
		int type = currentToken.getType();
		if(type == Token.PLUS || type == Token.MINUS || type == Token.DIV ||
			type == Token.MULT || type == Token.MOD){
			typeMixingError(currentToken);
		}
		return result;
	}
	
	
	//this method outputs a boolean expression either with or without a not.
	//boolean expressions can contain any numbers, and any logical operators
	//and 0 is false while any other number is true.
	private Expression notExpr() {
		Expression result;
		if(currentToken.getType() == Token.NOT){
			Operation op = notOperation();
			result = primary(); 
			Expression leftOperand = result;
			result = codeFactory.generateNotExpr(leftOperand, op);
		}
		else {
			result = primary(); 
		}
		return result;
	}

	//next 3 methods correspond to logical operation 
	//matching and processing.
	private Operation notOperation() {
		Operation op = new Operation();
		match(Token.NOT);
		op = processOperation();
		return op;
	}

	private Operation orOp() {
		Operation op = new Operation();
		match(Token.OR);
		op = processOperation();
		return op;
	}


	private Operation andOp() {
		Operation op = new Operation();
		match(Token.AND);
		op = processOperation();
		return op;
	}
	
	private void idList()
    {
        Expression idExpr;
        idExpr = identifier();
        codeFactory.generateRead(idExpr);
        while ( currentToken.getType() == Token.COMMA )
        {
            match(Token.COMMA);
            idExpr = identifier();
            codeFactory.generateRead(idExpr);
        }
    }
    
    private void expressionList()
    {
        Expression expr;
        expr = expression();
        codeFactory.generateWrite(expr);
        while ( currentToken.getType() == Token.COMMA)
        {
            match( Token.COMMA );
            expr = expression();
            codeFactory.generateWrite(expr);
        }
       
    }
    
    //strings list that functions for writing lists of strings to console.
    //handles both previously initialized strings and string literals.
    //WRITE(a, "abc"); is a valid statement if a has been initialized.
    //will write assembly that prints value of a and abc to console.
    private void stringList() {
    	StringExpression expr;
    	if(currentToken.getType() == Token.ID){
    		match(Token.ID);
    		expr = new StringExpression(StringExpression.IDEXPR, previousToken.getId(), scopes.get(scopeNum).getValue(previousToken.getId() + scopeNum));
    		codeFactory.generateStringWrite(expr);
    	}
    	else if(currentToken.getType() == Token.STRING) {
    		match(Token.STRING);
    		expr = new StringExpression(StringExpression.LITERALEXPR, previousToken.getId() + scopeNum);
    		codeFactory.generateStringWrite(expr);
    	}
    	while(currentToken.getType() == Token.COMMA){
    		match(Token.COMMA);
    		if(currentToken.getType() == Token.ID){
        		match(Token.ID);
        		expr = new StringExpression(StringExpression.IDEXPR, previousToken.getId(), scopes.get(scopeNum).getValue(previousToken.getId() + scopeNum));
        		codeFactory.generateStringWrite(expr);
        	}
        	else if(currentToken.getType() == Token.STRING) {
        		match(Token.STRING);
        		expr = new StringExpression(StringExpression.TEMPEXPR, codeFactory.createStringTempName(previousToken.getId() + scopeNum));
        		codeFactory.generateStringWrite(expr);
        	}
    	}
    }
    
    //string expression based off of expression method.
    private StringExpression stringExpression() {
		StringExpression result;
		StringExpression leftString;
		StringExpression rightString;
		Operation op;
		
		result = stringPrimary();
		if(currentToken.getType() == Token.MINUS){
			error(currentToken);
		}
		else {
			while(currentToken.getType() == Token.PLUS){
				leftString = result;
				op = addOperation();
				rightString = stringPrimary();
				result = codeFactory.generateStringExpression(leftString, rightString, op);
			}
		}
		return result;
	}

    private Expression primary()
    {
        Expression result = new Expression();
        switch ( currentToken.getType() )
        {
            case Token.LPAREN :
            {
                match( Token.LPAREN );
                result = expression();
                match( Token.RPAREN );
                break;
            }
            case Token.ID:
            {
                result = identifier();
                break;
            }
            case Token.INTLITERAL:
            {
                match(Token.INTLITERAL);
                result = processLiteral();
                break;
            }
            case Token.MINUS:
            {
                match(Token.MINUS);
                processSign();
                match(Token.INTLITERAL);
                result = processLiteral();
                break;
            }
            case Token.PLUS:
            {
                processSign();
                match(Token.INTLITERAL);
                result = processLiteral();
                break;
            }
            case Token.MOD: 
            {
            	processSign();
            	match(Token.INTLITERAL);
            	result = processLiteral();
            	break;
            }
            case Token.MULT: 
            {
            	processSign();
            	match(Token.INTLITERAL);
            	result = processLiteral();
            	break;
            }
            case Token.DIV : {
            	processSign();
            	match(Token.INTLITERAL);
            	result = processLiteral();
            	break;
            }
            case Token.STRING : {
            	typesMixingError(currentToken);
            }
            default: error( currentToken );
        }
        return result;
    }
    
    //string primary based off of primary. Leads to different
    //types of string formations depending on the token. 
    private StringExpression stringPrimary(){
    	StringExpression result = new StringExpression();
    	if(currentToken.getType() == Token.STRING){
    		match(Token.STRING);
			result = processStringLiteral();
    	}
    	else if(currentToken.getType() == Token.ID){
    		result = stringIdentifier();
    	}
    	else if(previousToken.getType() == Token.LPAREN){
    		result = stringIdentifier();
    	}
    	else if(currentToken.getType() == Token.PLUS){
    		match(Token.PLUS);
    		match(Token.STRING);
    		result = processStringLiteral();
    	}
    	else {
    		error(currentToken);
    	}
    	return result;
    }
    
    private Operation addOperation()
    {
        Operation op = new Operation();
        switch ( currentToken.getType() )
        {
            case Token.PLUS:
            {
                match( Token.PLUS ); 
                op = processOperation();
                break;
            }
            case Token.MINUS:
            {
                match( Token.MINUS ); 
                op = processOperation();
                break;
            }
            default: error( currentToken );
        }
        return op;
    }
    
    
    private Expression identifier()
    {
        Expression expr;
        match( Token.ID );
        
        expr = processIdentifier();
        return expr;
    }
    
    //string identifier based off of int identifier
    private StringExpression stringIdentifier(){
    	StringExpression expr;
    	match(Token.ID);
    	expr = processStringIdentifier();
    	return expr;
    }
    
    private void match( int tokenType)
    {
        previousToken = currentToken;
        if(currentToken.getType() == Token.END){
        	return;
        }
        if ( currentToken.getType() == tokenType )
            currentToken = scanner.findNextToken();
        else 
        {
            error( currentToken );
            currentToken = scanner.findNextToken();
        }
    }

    //new operations added.
    private void processSign()
    {
    	Parser.signSet = true;
    	if ( previousToken.getType() == Token.PLUS ) 
    	{
    		Parser.signFlag = "+";
    	} 
    	else if(previousToken.getType() == Token.MULT){
    		Parser.signFlag = "*";
    	}
    	else if(previousToken.getType() == Token.DIV){
    		Parser.signFlag = "/";
    	}
    	else if(previousToken.getType() == Token.MOD){
    		Parser.signFlag = "%";
    	}
    	else
    	{
    		Parser.signFlag = "-";
    	}
    }
    private Expression processLiteral()
    {
    	Expression expr;
        int value = ( new Integer( previousToken.getId() )).intValue();
        if (Parser.signSet && Parser.signFlag.equals("-"))
        {
        	 expr = new Expression( Expression.LITERALEXPR, "-"+previousToken.getId() , value*-1 );
        } else
        {
        	 expr = new Expression( Expression.LITERALEXPR, previousToken.getId(), value ); 
        }
        Parser.signSet = false;
        return expr;
    }
    
    //creates string expressions for string literals
    private StringExpression processStringLiteral(){
    	StringExpression expr = new StringExpression(StringExpression.LITERALEXPR, previousToken);
    	return expr;
    }
    private Operation processOperation()
    {
        Operation op = new Operation();
        if ( previousToken.getType() == Token.PLUS ) op.opType = Token.PLUS;
        else if ( previousToken.getType() == Token.MINUS ) op.opType = Token.MINUS;
        else if (previousToken.getType() == Token.MULT) op.opType = Token.MULT;
        else if (previousToken.getType() == Token.DIV) op.opType = Token.DIV;
        else if (previousToken.getType() == Token.MOD) op.opType = Token.MOD;
        else if ( previousToken.getType() == Token.NOT) op.opType = Token.NOT;
        else if ( previousToken.getType() == Token.AND) op.opType = Token.AND;
        else if ( previousToken.getType() == Token.OR) op.opType = Token.OR;
        else if ( previousToken.getType() == Token.EQUAL) op.opType = Token.EQUAL;
        else if ( previousToken.getType() == Token.NOTEQUAL) op.opType = Token.NOTEQUAL;
        else if ( previousToken.getType() == Token.GREAT) op.opType = Token.GREAT;
        else if ( previousToken.getType() == Token.GREATOREQ) op.opType = Token.GREATOREQ;
        else if ( previousToken.getType() == Token.LESS) op.opType = Token.LESS;
        else if ( previousToken.getType() == Token.LESSOREQ) op.opType = Token.LESSOREQ;
        else error( previousToken );
        return op;
    }
    
    //makes expression equal to symbol table entry if it already exists.
    private Expression processIdentifier()
    {
        Expression expr = new Expression( Expression.IDEXPR, previousToken.getId()  + scopeNum );
         if (!scopes.get(scopeNum).checkSTforItem( previousToken.getId()  + scopeNum) )
        {
            codeFactory.generateDeclaration();
            scopes.add(scopeNum, symbolTable);
        }
         else {
        	 expr = new Expression(Expression.IDEXPR, scopes.get(scopeNum).getItem(symbolTable.getSpot(previousToken.getId()  + scopeNum)), 
        			 Integer.parseInt(scopes.get(scopeNum).getValue(previousToken.getId()  + scopeNum)));
         }
        return expr;
    }
    
    //checks if string is in symbol table and if not, it sets the code factory variables list 
    //equal to the symbol table. If it is, then the new string expression's value is set equal
    //to the old symbol's value
	private StringExpression processStringIdentifier(){
    	StringExpression expr = new StringExpression(StringExpression.IDEXPR, previousToken.getId()  + scopeNum);
    	if(!scopes.get(scopeNum).checkSTforItem(previousToken.getId() + scopeNum)){
    		codeFactory.generateDeclaration();
    		scopes.add(scopeNum, symbolTable);
    		//symbol table added to elsewhere
    	}
    	else {
    		expr = new StringExpression(StringExpression.IDEXPR, scopes.get(scopeNum).getItem(symbolTable.getSpot(previousToken.getId()  + scopeNum)),
    				scopes.get(scopeNum).getValue(previousToken.getId() + scopeNum));
    	}
    	return expr;
    }
	
	//error for not declaring variables properly.
    //exits so assembly code does not print after an error.
    private void declarationError(String id) {
		System.out.println("Error! Variable " + id + " has not been declared.");
		System.exit(0);
	}
    
    //error for not initializing variable before use.
    //also exits.
    private void initializationError(String id){
    	System.out.println("Error! Variable " + id + " has not been initialized.");
    	System.exit(0);
    }
    
    private void alreadyDeclaredError(String id) {
    	System.out.println("Error! Variable " + id + " has already been declared");
    	System.exit(0);
	}
    
    //error if types are mixed.
    private void typeMixingError(Token currentToken) {
		System.out.println("Error! Operation types are mixed.");
		System.exit(0);
    }
    
    private void typesMixingError(Token currentToken) {
		System.out.println("Error! Data types are mixed. Cannot use a " + currentToken.toString().toLowerCase() + " here.");
		System.exit(0);
    }
    
	//added exit so no more assembly code gets printed after an error.
    private void error( Token token )
    {
        System.out.println( "Syntax error! Parsing token " + token.getId() + " at line number " + 
                scanner.getLineNumber() );
        if (token.getType() == Token.ID )
            System.out.println( "ID name: " + token.getId() );
        System.exit(0);
    }
    
    //error if division by zero occurs.
    private void divideByZeroError(Token currentToken) {
		System.out.println("Divide by 0 error!");
		System.exit(0);
	}
    
    private void chainedOpsError() {
		System.out.println("Error! Relational operators cannot be chained.");
		System.exit(0);
	}
    
}
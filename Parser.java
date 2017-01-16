/*
 * 
 * Micro grammar
<program>    -> #Start BEGIN <statement list> END
<statement list> -> <statement> {<statement>}
<statement>    -> int <ident>; | int <ident> := <expression>  | String <ident>; | String <ident> := <stringExpression>
<statement>    -> <ident> := <expression> | <ident> := <stringExpression> #Assign;
<statement>    -> READ (<id list>);
<statement>    -> WRITE (<expr list>);
<statement>    -> WRITE(<string list>);
<id list>    -> <ident> #ReadId {, <ident> #ReadId}
<expr list>    -> <expression> #WriteExpr {, <expression> #WriteExpr}
<string list>      -> <stringExpression> {, <stringExpression> }
<expression>    -> <primary> {<add op> <primary> #GenInfix};
<stringExpression>     ->  <stringPrimary> {+ <stringPrimary> #GenInFix};
<primary>    -> ( <expression> )
<primary>    -> <ident>
<stringPrimary>->  <ident>
<primary>    -> IntLiteral
<stringPrimary>-> StringLiteral
<add op>    -> + #ProcessOp | - #ProcessOp
<ident>    ->  a-z {a-z | 0-9 | specialCharacters} #ProcessId
<system goal> -> <program> EofSym #Finish

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
        while ( currentToken.getType() == Token.ID || currentToken.getType() == Token.READ || 
                    currentToken.getType() == Token.WRITE || currentToken.getType() == Token.STRINGTYPE ||
                    currentToken.getType() == Token.INTTYPE || currentToken.getType() == Token.BOOL)
        {
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
            if(currentToken.getType() == Token.ID && symbolTable.checkSTforItem(currentToken.getId()) == true &&
            		symbolTable.getType(symbolTable.getSpot(currentToken.getId())).equals("string")){
            	stringList();
            }
            else if(currentToken.getType() == Token.ID && symbolTable.checkSTforItem(currentToken.getId()) == true &&
            		symbolTable.getType(symbolTable.getSpot(currentToken.getId())).equals("int")){
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
        		if(currentToken.getType() == Token.ASSIGNOP){
        			match(Token.ASSIGNOP);
        			expr = expression();
        			symbolTable.addIntItem(lValue, Integer.toString(expr.expressionIntValue));
        			codeFactory.generateIntegerAssignment(lValue, expr);
        			match(Token.SEMICOLON);
        			break;
        		}
        		else if(currentToken.getType() == Token.SEMICOLON){
        			expr = new Expression(Expression.IDEXPR, "0");
        			symbolTable.addIntItem(lValue, "0");
        			match(Token.SEMICOLON);
        			break;
        		}
        	}
        	case Token.BOOL : {
        		match(Token.BOOL);
        		boolLeftVal = boolIdentifier();
        		if(currentToken.getType() == Token.ASSIGNOP){
        			match(Token.ASSIGNOP);
        			expr = boolOrExpression();
        			symbolTable.addBoolItem(boolLeftVal, Integer.toString(expr.expressionIntValue));
        			codeFactory.generateBoolAssignment(boolLeftVal, expr);
        			match(Token.SEMICOLON);
        			break;
        		}
        		else if (currentToken.getType() == Token.SEMICOLON){
        			expr = new Expression(Expression.IDEXPR, "0");
        			symbolTable.addBoolItem(boolLeftVal, "0");
        			match(Token.SEMICOLON);
        			break;
        		}
        	}
        	//declares and assigns strings as needed. also checks for errors.
        	//finds if the word "String" is present before a variable
        	case Token.STRINGTYPE: 
        	{
        		match(Token.STRINGTYPE);
        		stringLeftVal = stringIdentifier();
        		if(currentToken.getType() == Token.ASSIGNOP){
        			match(Token.ASSIGNOP);
        			if(currentToken.getType() == Token.ID){
        				if(!symbolTable.checkSTforItem(currentToken.getId())){
        					declarationError(currentToken.getId());
        				}
        				if(symbolTable.getValue(currentToken.getId()).equals("")){
            				initializationError(currentToken.getId());
            			}
        				stringExpr = stringExpression();
        				symbolTable.addStringItem(stringLeftVal, stringExpr.stringValue);
        				codeFactory.generateStringAssignment(stringLeftVal, stringExpr);
        				match(Token.SEMICOLON);
        			}
        			else {
	        			stringExpr = stringExpression();
	        			symbolTable.addStringItem(stringLeftVal, stringExpr.stringValue);
	        			codeFactory.generateStringAssignment(stringLeftVal, stringExpr);
	        			match(Token.SEMICOLON);
        			}
        		}
        		else if(currentToken.getType() == Token.SEMICOLON){
        			stringExpr = new StringExpression(StringExpression.LITERALEXPR, "");
        			symbolTable.addStringItem(stringLeftVal, "");
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
            	if(symbolTable.checkSTforItem(currentToken.getId())){
            		if(symbolTable.getType(currentToken.getId()).equals("int")){
            			lValue = identifier();
			            match( Token.ASSIGNOP );
			            expr = expression();
		               	codeFactory.generateIntegerAssignment( lValue, expr );
		               	symbolTable.addValue(lValue.expressionName, Integer.toString(expr.expressionIntValue));
		                match( Token.SEMICOLON );
            		}
            		else if(symbolTable.getType(currentToken.getId()).equals("bool")){
            			boolLeftVal = boolIdentifier();
            			match(Token.ASSIGNOP);
            			expr = boolOrExpression();
            			codeFactory.generateBoolAssignment(boolLeftVal, expr);
            			symbolTable.addValue(boolLeftVal.expressionName, Integer.toString(expr.expressionIntValue));
            			match(Token.SEMICOLON);
            		}
            		else {
            			stringLeftVal = stringIdentifier();
            			match( Token.ASSIGNOP );
            			stringExpr = stringExpression();
		               	codeFactory.generateStringAssignment(stringLeftVal, stringExpr);
		               	symbolTable.addValue(stringLeftVal.stringExpressionName, stringExpr.stringValue);
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

	private Expression boolOrExpression() {
    	Expression result;
    	Expression leftOperand;
    	Expression rightOperand;
    	Operation op;
    	
    	result = boolAndExpression();
    	while(currentToken.getType() == Token.AND){
    		leftOperand = result; 
    		op = andOp();
    		rightOperand = boolOrExpression();
    		result = codeFactory.generateBoolExpr(leftOperand, rightOperand, op);
    	}
    	return result;
    }

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
    		expr = new StringExpression(StringExpression.IDEXPR, previousToken.getId(), symbolTable.getValue(previousToken.getId()));
    		codeFactory.generateStringWrite(expr);
    	}
    	else if(currentToken.getType() == Token.STRING) {
    		match(Token.STRING);
    		expr = new StringExpression(StringExpression.LITERALEXPR, previousToken.getId());
    		codeFactory.generateStringWrite(expr);
    	}
    	while(currentToken.getType() == Token.COMMA){
    		match(Token.COMMA);
    		if(currentToken.getType() == Token.ID){
        		match(Token.ID);
        		expr = new StringExpression(StringExpression.IDEXPR, previousToken.getId(), symbolTable.getValue(previousToken.getId()));
        		codeFactory.generateStringWrite(expr);
        	}
        	else if(currentToken.getType() == Token.STRING) {
        		match(Token.STRING);
        		expr = new StringExpression(StringExpression.TEMPEXPR, codeFactory.createStringTempName(previousToken.getId()));
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
        	 expr = new Expression( Expression.LITERALEXPR, "-"+previousToken.getId(), value*-1 );
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
        else error( previousToken );
        return op;
    }
    
    private Expression processIdentifier()
    {
        Expression expr = new Expression( Expression.IDEXPR, previousToken.getId());
         if ( ! symbolTable.checkSTforItem( previousToken.getId() ) )
        {
            codeFactory.generateDeclaration();
        }
        return expr;
    }
    
    //checks if string is in symbol table and if not, it sets the code factory variables list 
    //equal to the symbol table. If it is, then the new string expression's value is set equal
    //to the old symbol's value
	private StringExpression processStringIdentifier(){
    	StringExpression expr = new StringExpression(StringExpression.IDEXPR, previousToken.getId());
    	if(!symbolTable.checkSTforItem(previousToken.getId())){
    		codeFactory.generateDeclaration();
    		//symbol table added to elsewhere
    	}
    	else {
    		expr = new StringExpression(StringExpression.IDEXPR, symbolTable.getItem(symbolTable.getSpot(previousToken.getId())),
    				symbolTable.getValue(previousToken.getId()));
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
    
    private void typeMixingError(Token currentToken) {
		System.out.println("Error! Operation types are mixed.");
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
    
    private void divideByZeroError(Token currentToken) {
		System.out.println("Divide by 0 error!");
		System.exit(0);
	}
}
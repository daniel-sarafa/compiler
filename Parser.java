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
<string list>      -> <string> {, <string> }
<expression>    -> <primary> {<add op> <primary> #GenInfix};
<stringExpression>     ->  <stringPrimary> {+ <stringPrimary> #GenInFix};
<primary>    -> ( <expression> )
<primary>    -> <ident>
<stringPrimary>->  <ident>
<primary>    -> IntLiteral
<stringPrimary>->StringLiteral
<add op>    -> + #ProcessOp | - #ProcessOp
<ident>    ->  a-z {a-z | 0-9 | specialCharacters} #ProcessId
<system goal> -> <program> EofSym #Finish

 */


public class Parser
{
    private static Scanner scanner;
    private static SymbolTable symbolTable;
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
        codeFactory = new CodeFactory();
        symbolTable = new SymbolTable();
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
                    currentToken.getType() == Token.WRITE)
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
        
        switch ( currentToken.getType() )
        {
        	case Token.INTTYPE:
        	{
        		match(Token.INTTYPE);
        		lValue = identifier();
        		if(currentToken.getType() == Token.ASSIGNOP){
        			match(Token.ASSIGNOP);
        			expr = expression();
        			codeFactory.generateIntegerAssignment(lValue, expr);
        		}
        		match(Token.SEMICOLON);
        		break;
        	}
        	case Token.STRINGTYPE: 
        	{
        		match(Token.STRINGTYPE);
        		stringLeftVal = stringIdentifier();
        		if(currentToken.getType() == Token.ASSIGNOP){
        			match(Token.ASSIGNOP);
        			match(Token.STRING);
        			stringExpr = stringExpression();
        			codeFactory.generateStringAssignment(stringLeftVal, stringExpr);
        		}
        		match(Token.SEMICOLON);
        		break;
        	}
            case Token.ID:
            {
                lValue = identifier();
                stringLeftVal = stringIdentifier();
                if(currentToken.getType() == Token.ASSIGNOP){
	                match( Token.ASSIGNOP );
	                if(currentToken.getType() == Token.STRING){
	                	stringExpr = stringExpression();
	                	codeFactory.generateStringAssignment(stringLeftVal, stringExpr);
	                }
	                else {
	                	expr = expression();
	                	codeFactory.generateIntegerAssignment( lValue, expr );
	                }
                }
                
                match( Token.SEMICOLON );
                break;
            }
            case Token.READ :
            {
                match( Token.READ );
                match( Token.LPAREN );
                idList();
                match( Token.RPAREN );
                match( Token.SEMICOLON );
                break;
            }
            case Token.WRITE :
            {
                match( Token.WRITE );
                match( Token.LPAREN );
                if(currentToken.getType() == Token.STRING){
                	stringList();
                }
                else {
                	expressionList();
                }
                match( Token.RPAREN );
                match( Token.SEMICOLON );
                break;
            }
            default: error(currentToken);
        }
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
        while ( currentToken.getType() == Token.COMMA )
        {
            match( Token.COMMA );
            expr = expression();
            codeFactory.generateWrite(expr);
        }
    }
    
    private void stringList() {
    	StringExpression expr;
    	expr = stringExpression();
    	codeFactory.generateStringWrite(expr);
    	while(currentToken.getType() == Token.COMMA){
    		match(Token.COMMA);
    		expr = stringExpression();
    		codeFactory.generateStringWrite(expr);
    	}
    }
    
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

	private Expression expression()
    {
        Expression result;
        Expression leftOperand;
        Expression rightOperand;
        Operation op;
        
        result = primary();
        while ( currentToken.getType() == Token.PLUS || currentToken.getType() == Token.MINUS )
        {
            leftOperand = result;
            op = addOperation();
            rightOperand = primary();
            result = codeFactory.generateArithExpr( leftOperand, rightOperand, op );
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
                match(Token.PLUS);
                processSign();
                match(Token.INTLITERAL);
                result = processLiteral();
                break;
            }
            default: error( currentToken );
        }
        return result;
    }
    
    private StringExpression stringPrimary(){
    	StringExpression result = new StringExpression();
    	switch(currentToken.getType()){
    		case Token.STRING: {
    			match(Token.STRING);
    			result = stringExpression();
    			break;
    		}
    		case Token.ID : {
    			result = stringIdentifier();
    			break;
    		}
    		default: error(currentToken);
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
            error( tokenType );
            currentToken = scanner.findNextToken();
        }
    }

    private void processSign()
    {
    	Parser.signSet = true;
    	if ( previousToken.getType() == Token.PLUS ) 
    	{
    		Parser.signFlag = "+";
    	} else
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
    
    
    private Operation processOperation()
    {
        Operation op = new Operation();
        if ( previousToken.getType() == Token.PLUS ) op.opType = Token.PLUS;
        else if ( previousToken.getType() == Token.MINUS ) op.opType = Token.MINUS;
        else error( previousToken );
        return op;
    }
    
    private Expression processIdentifier()
    {
        Expression expr = new Expression( Expression.IDEXPR, previousToken.getId());
        
        if ( ! symbolTable.checkSTforItem( previousToken.getId() ) )
        {
            symbolTable.addItem( previousToken );
            codeFactory.generateDeclaration( previousToken );
        }
        return expr;
    }
    
    private StringExpression processStringIdentifier(){
    	StringExpression expr = new StringExpression(StringExpression.IDEXPR, previousToken.getId());
    	if(!symbolTable.checkSTforItem(previousToken.getId())){
    		symbolTable.addItem(previousToken);
    		codeFactory.generateDeclaration(previousToken);
    	}
    	return expr;
    }
    private void error( Token token )
    {
        System.out.println( "Syntax error! Parsing token type " + token.toString() + " at line number " + 
                scanner.getLineNumber() );
        if (token.getType() == Token.ID )
            System.out.println( "ID name: " + token.getId() );
    }
    private void error( int tokenType )
    {
        System.out.println( "Syntax error! Parsing token type " +tokenType + " at line number " + 
                scanner.getLineNumber() );
    }
}
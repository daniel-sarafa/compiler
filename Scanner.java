import java.io.FileInputStream;          
import java.io.IOException;         
import java.io.FileReader;       
import java.io.BufferedReader;

public class Scanner
{    
    public static FileReader fileIn;
    public static BufferedReader bufReader;
    public String fileName;
    
    private int currentLineNumber;
    private String currentLine;
    private String nextLine;
  //  private boolean done;
    private int currentLocation;
    
    public Scanner(String fname)
    {
        currentLineNumber = 1;
        fileName = fname;
        try
        {
            fileIn = new FileReader(fileName);
            bufReader = new BufferedReader(fileIn);
            
            currentLine = bufReader.readLine();
            currentLocation = 0;
            if (currentLine == null )
            {
   //             done = true;
                nextLine = null;
            } else
            {
   //             done = false;
                nextLine = bufReader.readLine();
            }
        }
        catch (IOException e)
        {
            System.out.println(e);
            return;
        }
    }        
    
    public int getLineNumber()
    {
        return currentLineNumber;
    }
    
    public Token findNextToken()
    {
        int len = currentLine.length();
        String tokenStr = new String();
        int tokenType;
        if ( currentLocation >= len && nextLine == null) 
        {
            Token token = new Token( "", Token.EOF );
            return token;
        }
        if ( currentLocation >= len ) // all characters of currentLine used
        {
            currentLine = nextLine;
            currentLineNumber++;
            try
            {
                nextLine = bufReader.readLine();
            }
            catch (IOException e)
            {
                System.out.println(e);
                Token token = new Token( "", Token.EOF );
                return token;
            } 
            currentLocation = 0;
        }
        while ( Character.isWhitespace( currentLine.charAt(currentLocation)))
            currentLocation++;
        int i = currentLocation;
        if (currentLine.charAt(i) == ';')
        {
            tokenStr = ";";
            tokenType = Token.SEMICOLON;
            i++;
        } else if (currentLine.charAt(i) == '(')
        {
            tokenStr = "(";
            tokenType = Token.LPAREN;
            i++;
        } else if (currentLine.charAt(i) == ')')
        {
            tokenStr = ")";
            tokenType = Token.RPAREN;
            i++;
        } else if(currentLine.charAt(i) == '+')
        {
            tokenStr = "+";
            tokenType = Token.PLUS;
            i++;
        } else if(currentLine.charAt(i) == '-')
        {
            tokenStr = "-";
            tokenType = Token.MINUS;
            i++;
        } else if(currentLine.charAt(i) == ',')
        {
            tokenStr = ",";
            tokenType = Token.COMMA;
            i++;
        } 
        else if (currentLine.charAt(i) == '*'){
        	tokenStr = "*";
        	tokenType = Token.MULT;
        	i++;
        }
        else if (currentLine.charAt(i) == '/'){
        	tokenStr = "/";
        	tokenType = Token.DIV;
        	i++;
        }
        else if (currentLine.charAt(i) == '%'){
        	tokenStr = "%";
        	tokenType = Token.MOD;
        	i++;
        }
        else if(currentLine.charAt(i) == '&'){
        	tokenStr = "&";
        	tokenType = Token.AND;
        	i++;
        }
        else if(currentLine.charAt(i) == '?'){
        	tokenStr = "?";
        	tokenType = Token.OR;
        	i++;
        }
        else if(currentLine.charAt(i) == '!' && i+1 < len && currentLine.charAt(i+1) != '='){
        	tokenStr = "!";
        	tokenType = Token.NOT;
        	i++;
        }
        //added for finding "int" types
        else if(i <= 3 && currentLine.length() > 3 && currentLine.substring(0, 3).equals("int")){
        	tokenStr = "int";
        	tokenType = Token.INTTYPE;
        	i += 3;
        }
        //added for finding "String" types
        else if(i <= 6 && currentLine.length() > 6 && currentLine.substring(0, 6).equals("String")){
        	tokenStr = "String";
        	tokenType = Token.STRINGTYPE;
        	i += 6;
        }
        else if(currentLine.charAt(i) == '<' && i+1 < len && currentLine.charAt(i+1) != '='){
        	tokenStr = "<";
        	tokenType = Token.LESS;
        	i++;
        }
        else if(currentLine.charAt(i) == '>' && i+1 < len && currentLine.charAt(i+1) != '='){
        	tokenStr = ">";
        	tokenType = Token.GREAT;
        	i++;
        }
        else if(currentLine.charAt(i) == '<' && i+1 < len && currentLine.charAt(i+1) == '='){
        	tokenStr = "<=";
        	tokenType = Token.LESSOREQ;
        	i+=2;
        }
        else if(currentLine.charAt(i) == '>' && i+1 < len && currentLine.charAt(i+1) == '='){
        	tokenStr = ">=";
        	tokenType = Token.GREATOREQ;
        	i+=2;
        }
        else if(currentLine.charAt(i) == '=' && i+1 < len && currentLine.charAt(i+1) == '='){
        	tokenStr = "==";
        	tokenType = Token.EQUAL;
        	i+=2;
        }
        else if(currentLine.charAt(i) == '=' && i+1 < len && currentLine.charAt(i+1) != '='){
        	tokenStr = "=";
        	tokenType = Token.LexERROR;
        	i++;
        }
        else if(currentLine.charAt(i) == '!' && i+1 < len && currentLine.charAt(i+1) == '='){
        	tokenStr = "!=";
        	tokenType = Token.NOTEQUAL;
        	i+=2;
        }
        else if(i <= 4 && currentLine.length() > 4 && currentLine.substring(0, 4).equals("bool")){
        	tokenStr = "bool";
        	tokenType = Token.BOOL;
        	i += 4;
        } 
        else if(currentLine.charAt(i) == 'I' && i + 1 < len && currentLine.charAt(i+1) == 'F'){
        	tokenStr = "IF";
        	tokenType = Token.IF;
        	i += 2;
        }
        else if(i <= 5 && currentLine.length() > 5 && currentLine.substring(0, 5).equals("WHILE")){
        	tokenStr = "WHILE";
        	tokenType = Token.WHILE;
        	i += 5;
        } 
        else if(i <= 5 && currentLine.length() > 5 && currentLine.substring(0, 5).equals("ENDIF")){
        	tokenStr = "ENDIF";
        	tokenType = Token.ENDIF;
        	i += 5;
        } 
        else if( i <= 8 &&currentLine.length() > 8 && currentLine.substring(0, 8).equals("ENDWHILE")){
        	tokenStr = "ENDWHILE";
        	tokenType = Token.ENDWHILE;
        	i += 8;
        } 
        else if( i <= 4 && currentLine.length() > 4 && currentLine.substring(0, 4).equals("ELSE")){
        	tokenStr = "ELSE";
        	tokenType = Token.ELSE;
        	i += 4;
        } 
        else if( i <= 7 && currentLine.length() > 7 && currentLine.substring(0, 7).equals("ENDELSE")){
        	tokenStr = "ENDELSE";
        	tokenType = Token.ENDELSE;
        	i += 7;
        } 
        else if( i <= 9 && currentLine.length() > 9 && currentLine.substring(0, 9).equals("BEGINPROC")){
        	tokenStr = "BEGINPROC";
        	tokenType = Token.PROC;
        	i += 9;
        } 
        else if( i <= 7 && currentLine.length() > 7 && currentLine.substring(0, 7).equals("ENDPROC")){
        	tokenStr = "ENDPROC";
        	tokenType = Token.ENDPROC;
        	i += 7;
        } 
        else if(i <= 4 && currentLine.length() > 4 && currentLine.substring(0, 4).equals("CALL")){
        	tokenStr = "CALL";
        	tokenType = Token.CALL;
        	i += 9;
        } 
        else if (currentLine.charAt(i) == ':'  && i+1 < len && currentLine.charAt(i+1) == '=')
        {
            tokenStr = ":=";
            tokenType = Token.ASSIGNOP;
            i+=2;
        } else  if ( Character.isDigit((currentLine.charAt(i))) )// find literals
        {
            while ( i < len && Character.isDigit(currentLine.charAt(i)) )
            {
                i++;
            }
            tokenStr = currentLine.substring(currentLocation, i);
            tokenType = Token.INTLITERAL;
        } 
        //added for finding string literals
        else if(currentLine.charAt(i) == '"'){
        	i++;
        	while ( i < len && currentLine.charAt(i) != '"'){
        		i++;
        	}
        	i++;
        	tokenStr = currentLine.substring(currentLocation+1, i-1);
        	tokenType = Token.STRING;
        }
        else // find identifiers and reserved words
        {
            while ( i < len && ! isReservedSymbol(currentLine.charAt(i)) )
            {
                i++;
            }
            tokenStr = currentLine.substring(currentLocation, i);
            tokenType = Token.ID;
        }
       
        currentLocation = i;
        Token token = new Token(tokenStr, tokenType);
        if ( i == len )// characters on currentLine used up
        {
            currentLine = nextLine;
            currentLineNumber++;
            try
            {
                nextLine = bufReader.readLine();
            }
            catch (IOException e)
            {
                System.out.println(e);
                return null;
            }
            currentLocation = 0;
        }
//        if (currentLine == null) done = true;  // reached EOF
        return token;
    }
 
    boolean isReservedSymbol( char ch)
    {
        return( ch == ' ' || ch == '\n' || ch == '\t' || ch == ';' | ch == '+' ||
                ch == '-' || ch == '(' || ch == ')' || ch == ','  || ch == ':' ||
                ch == '*' || ch == '/' || ch == '&' || ch == '?' || ch == '%' ||
                ch == '!' || ch == '>' || ch == '<' || ch == '=');
    }
}
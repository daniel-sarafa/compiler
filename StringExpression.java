
public class StringExpression extends Expression{

    public final static int IDEXPR = 0;
    public final static int LITERALEXPR = 1;
    public final static int TEMPEXPR = 2;
    
    public int expressionType;
    public String expressionName;
    public int expressionLength;
        
    public StringExpression( )
    {
        expressionType = 0;
        expressionName = "";
    }
        
    public StringExpression( int type, int length)
    {
        expressionType = type;
        expressionLength = length;
    }

    public StringExpression( int type, String name)
    {
        expressionType = type;
        expressionName = name;
    }
    
    public StringExpression( int type, String name, int length)
    {
        expressionType = type;
        expressionName = name;
        expressionLength = length;
    }
}

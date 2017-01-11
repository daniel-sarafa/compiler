
public class StringExpression {

    public final static int IDEXPR = 0;
    public final static int LITERALEXPR = 1;
    public final static int TEMPEXPR = 2;
    
    public int stringExpressionType;
    public String stringExpressionName;
    public int stringExpressionLength;
    public String stringValue;
        
    public StringExpression()
    {
        stringExpressionType = 0;
        stringExpressionName = "";
    }
    public StringExpression(Token value)
    {
        stringExpressionType = 0;
        stringExpressionName = "";
        stringValue = value.getId();
    }   
    
    public StringExpression(int type, int length)
    {
        stringExpressionType = type;
        stringExpressionLength = length;
    }

    public StringExpression(int type, String name, Token value)
    {
        stringExpressionType = type;
        stringExpressionName = name;
        stringValue = value.toString();
    }
    public StringExpression(int type, String name)
    {
        stringExpressionType = type;
        stringExpressionName = name;
    }
    
    public StringExpression(int type, String name, int length)
    {
        stringExpressionType = type;
        stringExpressionName = name;
        stringExpressionLength = length;
    }
}

import java.util.Vector; 

//added types and values table
//types(i) and values(i) correspond to 
//st(i) so that the correct type and 
//value is kept for each symbol.
//methods were added to access each symbol's
//various qualities.
class SymbolTable
{
        
    private Vector<String> st;
    private Vector<String> types;
    private Vector<String> values;
    
    public SymbolTable()
    {
        st = new Vector<String>();
        types = new Vector<String>();
        values = new Vector<String>();
    }
    
    public void addIntItem( Token token )
    {
        st.add( token.getId() );
        types.add("int");
        values.add("0");
    }
    public void addIntItem(Expression exp, String value)
    {
        st.add( exp.expressionName );
        types.add("int");
        values.add(value);
    }
    
    public void addBoolItem(Expression exp, String value){
    	st.add(exp.expressionName);
    	types.add("bool");
    	values.add(value);
    }
    public void addStringItem( Token token )
    {
        st.add( token.getId() );
        types.add("string");
        values.add("");
    }
    
    public void addStringItem(StringExpression exp, String value)
    {
        st.add( exp.stringExpressionName );
        types.add("string");
        values.add(value);
    }
    
    public boolean checkSTforItem( String id )
    {
       return st.contains( id );
    }
    
    public String getItem(int i){
    	return st.get(i);
    }
    
    public int getSpot(String id){
    	int spot = 0;
    	if(checkSTforItem(id) == true){
    		for(int i = 0; i < st.size(); i++){
    			if(st.get(i).equals(id)){
    				spot = i;
    				return spot;
    			}
    		}
    	}
    	return -1;
    }
    
    public void addValue(String id, String value){
    	int spot = getSpot(id);
    	values.add(spot, value);
    }
    
    public String getValue(String id){
    	int spot = getSpot(id);
    	return values.get(spot);
    }
    
    public String getValue(int spot){
    	return values.get(spot);
    }
    public String getType(int i){
    	return types.get(i).toString();
    }
    
    public String getType(String id){
    	int i = getSpot(id);
    	return types.get(i).toString();
    }
    
    
    public int getSize(){
    	return st.size();
    }

}
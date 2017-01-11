import java.util.Vector; 

class SymbolTable
{
        
    private Vector<String> st;
    private Vector<String> types;
    
    public SymbolTable()
    {
        st = new Vector<String>();
        types = new Vector<String>();
    }
    
    public void addIntItem( Token token )
    {
        st.add( token.getId() );
        types.add("int");
    }
    public void addStringItem( Token token )
    {
        st.add( token.getId() );
        types.add("string");
    }
    
    public boolean checkSTforItem( String id )
    {
       return st.contains( id );
    }
    
    public String getItem(int i){
    	return st.get(i);
    }
    
    public String getType(int i){
    	return types.get(i).toString();
    }
    
    public int getSize(){
    	return st.size();
    }

}
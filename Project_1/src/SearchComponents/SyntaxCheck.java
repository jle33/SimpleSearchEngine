package SearchComponents;

/**
 * Created by dzung on 10/9/2014.
 */
public class SyntaxCheck {


   	
	private static int countPlus(String s) {

        int num = 0;
        String newS = s;
        while(newS.length() > 0){
            if (newS.contains("+")){
                newS = newS.substring(newS.indexOf("+")+ 1,newS.length());
                num++;
            }
            else break;
        }
        return num;
    }
    
    public static String QSyntaxCheck (String strQuery) {
    	
    	String strStatus = "Ok";
    	int countParen = 0 , countQuote = 0 ;
    	    		
    	   for(int i = 0; i< strQuery.length() ; i ++) {
    			 
    		     if( strQuery.charAt(i) == '"' ) { 
    	             if (countQuote == 0)
    		    	      countQuote ++;
    	             else countQuote --;
    		     }
    		     else if( strQuery.charAt(i) == '(' ) 
    				 countParen ++;
    			 else if (strQuery.charAt(i) == ')' ) {
    				 
    				 if (countParen == 0) {
    					 strStatus = "Error: cannot have a ')' without a '(' ";
    		    	     break;
    				 }
    				 else countParen --;
    			 }			 
      	    }			 
    	  
    	     if (countParen > 0)   // count cannot be > 0,  
    			  strStatus = "Error: cannot have a '(' without a ')' ";
    	     else if (countQuote > 0) 	  
    	    	  strStatus = "Error: missing a '\"' ";
    	     			 
      	  	return (strStatus); 
  }
    	
    
    public static String QuerySyntaxCheck (String strQuery) {
        
    	String delims, strStatus = "OK";
        String[] tokens;
        int count;
    
        if (strQuery.isEmpty()) {
        	strStatus = "Error: Query is empty";
            return strStatus;  
        }
        
        // * first check for + 
        
        if (strQuery.contains("+")) {

            if (strQuery.startsWith("+"))
                strStatus = "Error: cannot start with '+'";
            else if (strQuery.endsWith("+"))
                strStatus = "Error: cannot end with '+'";
            else {

                delims = "[ + ]+";
                tokens = strQuery.split(delims);
                count = countPlus(strQuery);
                if (tokens.length <= count)  
                     strStatus = "Error: query must be Q1 + Q2";
            }
                   
        }
        
        // final check for other syntactical errors ie "'" or "("
        if(!strStatus.startsWith("Error"))
             strStatus = QSyntaxCheck(strQuery);       
        
        // return strStatus with value = "OK" or "Error"
        return(strStatus);
        
    }

}

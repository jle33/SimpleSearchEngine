import java.util.regex.*;

public class PorterStemmer {

   // a single consonant
   private static final String c = "[^aeiou]";
   // a single vowel
   private static final String v = "[aeiouy]";

   // a sequence of consonants; the second/third/etc consonant cannot be 'y'
   private static final String C = c + "[^aeiouy]*";
   // a sequence of vowels; the second/third/etc cannot be 'y'
   private static final String V = v + "[aeiou]*";

   // this regex pattern tests if the token has measure > 0 [at least one VC].
   private static final Pattern mGr0 = Pattern.compile("^(" + C + ")?" + V + C);
   //final Pattern mGr0 = Pattern.compile("^(" + C + ")?" + V + C);
   // add more Pattern variables for the following patterns:
   // m equals 1: token has measure == 1
   private static final Pattern mEq1 = Pattern.compile("^(" + C + ")?" + "(" + V + C + "){1}" + "(" + V + ")?");
   // m greater than 1: token has measure > 1
   private static final Pattern mGr1 = Pattern.compile("^(" + C + ")?" + "(" + V + C + V + C +")+");
   // vowel: token has a vowel after the first (optional) C
   private static final Pattern vowel = Pattern.compile("^(" + C + ")?" + V);
   // double consonant: token ends in two consonants that are the same, 
   //			unless they are L, S, or Z. (look up "backreferencing" to help 
   //			with this)
   private static final Pattern double_C = Pattern.compile("([^aeioulsz])\\1$");
   // m equals 1, Cvc: token is in Cvc form, where the last c is not w, x, 
   //			or y.
   private static final Pattern mEq1_Cvc = Pattern.compile("^" + C+v+"[^aeiouwxy]$");
   
   //*d - the stems ends with a double constant allowing L, S, Z for step 5b
   private static final Pattern double_C_withLSZ = Pattern.compile("([^aeiou])\\1$");
   
   //*v* -any char one or more times_vowel_any char one or more times for use with step 1c
   private static final Pattern star_v_star = Pattern.compile("^(.*" + v + ".*)y$");
   //step2pairs
   private static final String[][] step2pairs = new String[][]{
		   	 {"ational", "ate"},
			 {"tional", "tion"},
			 {"enci", "ence"},
			 {"anci","ance"},
			 {"izer","ize"},
			 {"abli","able"},
			 {"alli","al"},
			 {"entli","ent"},
			 {"eli","e"},
			 {"ousli","ous"},
			 {"ization","ize"},
			 {"ation","ate"},
			 {"ator","ate"},
			 {"alism","al"},
			 {"iveness","ive"},
			 {"fulness","ful"},
			 {"ousness","ous"},
			 {"aliti","al"},
			 {"iviti","ive"},
			 {"biliti","ble"}};
   
   //step3pairs
   private static final String[][] step3pairs = new String[][]{
		   	 {"icate", "ic"},
			 {"ative", ""},
			 {"alize", "al"},
			 {"iciti","ice"},
			 {"ical","ic"},
			 {"ful",""},
			 {"ness",""}};
      
   //step4suffixes
   private static final String[] step4suffixes = new String[]{
	   	 "al",
	   	 "ance",
	   	 "ence",
	   	 "er",
	   	 "ic",
	   	 "able",
	   	 "ible",
	   	 "ant",
	   	 "ement",
	   	 "ment",
	   	 "ent",
	   	 "ion",
	   	 "ou",
	   	 "ism",
	   	 "ate",
	   	 "iti",
	   	 "ous",
	   	 "ive",
	   	 "ize"};
   
   //Helper method for step 2 and 3
   private static String step2_3(String token, String [][] step2_3pairs){
	   boolean nextPair = true;
	   int curRow = 0;
	   while((nextPair == true) && (curRow < step2_3pairs.length)){
		   if(token.endsWith(step2_3pairs[curRow][0])){
			   String stem = token.substring(0, token.length() - step2_3pairs[curRow][0].length());
			   if(mGr0.matcher(stem).find()){
				   token = stem + step2_3pairs[curRow][1];
			   }
			   /*if one suffix matches, do not try any others even if the 
			   stem does not have measure > 0*/
			   nextPair = false;
		   }
		   curRow++;
	   }
	   return token;
   }
   public static String processToken(String token) {
      if (token.length() < 3) {
         return token; // token must be at least 3 chars
      }
      // step 1a
      if (token.endsWith("sses")) {
         token = token.substring(0, token.length() - 2);
      }
      else if(token.endsWith("ies")){
    	  token = token.substring(0, token.length()-2);
      }
      else if(token.endsWith("ss")){
    	  //SS -> SS
      }
      else if(token.endsWith("s") && !token.endsWith("ss")){
    	  token = token.substring(0, token.length()-1);
      }
      else{
    	  //default to token = token;
      }
      // program the other steps in 1a. 
      // note that Step 1a.3 implies that there is only a single 's' as the 
      //	suffix; ss does not count. you may need a regex pattern here for 
      // "not s followed by s".

      // step 1b
      boolean doStep1bb = false;
      //		step 1b
      if (token.endsWith("eed")) { // 1b.1
         // token.substring(0, token.length() - 3) is the stem prior to "eed".
         // if that has m>0, then remove the "d".
         String stem = token.substring(0, token.length() - 3);
         if (mGr0.matcher(stem).find()) { // if the pattern matches the stem
            token = stem + "ee";
         }
      }
      else if(token.endsWith("ed")){
    	  String stem = token.substring(0, token.length() - 2);
    	  if(vowel.matcher(stem).find()){
    		  token = stem;
    		  doStep1bb = true;
    	  }
      }
      else if(token.endsWith("ing")){
    	  String stem = token.substring(0, token.length() - 3);
    	  if(vowel.matcher(stem).find()){
    		  token = stem;
    		  doStep1bb = true;
    	  }
      }
      else{
    	  //default to token = token;
      }
      // program the rest of 1b. set the boolean doStep1bb to true if Step 1b* 
      // should be performed.

      // step 1b*, only if the 1b.2 or 1b.3 were performed.
      if (doStep1bb) {
         if (token.endsWith("at") || token.endsWith("bl")
          || token.endsWith("iz")) {

            token = token + "e";
         }
         else if(double_C.matcher(token).find()){
        	 token = token.substring(0, token.length() - 1);
         }
         else if(mEq1_Cvc.matcher(token).find()){
        	 token = token + "e";
         }
         // use the regex patterns you wrote for 1b*.4 and 1b*.5
      }
      
      // step 1c
      // program this step. test the suffix of 'y' first, then test the 
      // condition *v*.
      if(token.endsWith("y")){
    	  if(star_v_star.matcher(token).find()){
    		  token = token.substring(0, token.length() - 1) + "i";
    	  }
      }
      
      // step 2
      // program this step. for each suffix, see if the token ends in the 
      // suffix. 
      //		* if it does, extract the stem, and do NOT test any other suffix.
      //    * take the stem and make sure it has m > 0.
      //			* if it does, complete the step. if it does not, do not 
      //				attempt any other suffix.
      // you may want to write a helper method for this. a matrix of 
      // "suffix"/"replacement" pairs might be helpful. It could look like
      // string[][] step2pairs = {  new string[] {"ational", "ate"}, 
      //										new string[] {"tional", "tion"}, ....
      token = step2_3(token, step2pairs);
      
      // step 3
      // program this step. the rules are identical to step 2 and you can use
      // the same helper method. you may also want a matrix here.
      token = step2_3(token,step3pairs);



      // step 4
      // program this step similar to step 2/3, except now the stem must have
      // measure > 1.
      // note that ION should only be removed if the suffix is SION or TION, 
      // which would leave the S or T.
      // as before, if one suffix matches, do not try any others even if the 
      // stem does not have measure > 1.
      boolean nextPair = true;
      int curRow = 0;
      while((nextPair == true) && (curRow < step4suffixes.length)){
    	  if(token.endsWith(step4suffixes[curRow])){
    		  String stem = token.substring(0, token.length() - step4suffixes[curRow].length());
    		  if(mGr1.matcher(stem).find()){
    			  //Since at step4suffiexes[11] = "ion", process sion and tion only
    			  if(step4suffixes[curRow].equals("ion")){
    				  if(token.endsWith("sion") || token.endsWith("tion")){
    					  token = token.substring(0, token.length() - 3);
    				  }//else do not process token
    			  }
    			  else{
    				  token = stem;
    			  }

    		  }
    		  //if any suffixes in this step, continue to next step
    		  nextPair = false;
    	  }
    	  curRow++;
      }



      // step 5
      // program this step. you have a regex for m=1 and for "Cvc", which
      // you can use to see if m=1 and NOT Cvc.
      // all your code should change the variable token, which represents
      // the stemmed term for the token.

      //step 5a
      if(token.endsWith("e")){
    	  String stem = token.substring(0, token.length() - 1);
    	  if(mGr1.matcher(stem).find()){
    		  token = stem;
    	  }
    	  else if(mEq1.matcher(stem).find() && !mEq1_Cvc.matcher(stem).find()){
    		  token = stem;
    	  }
      }
      
      //step5b
      if(token.endsWith("ll")){
    	  if(mGr1.matcher(token).find() && double_C_withLSZ.matcher(token).find()){
    		  token = token.substring(0, token.length() - 1);
    	  }
      }
      return token;
   }
}

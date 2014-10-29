package SearchComponents;
import java.io.*;
import java.util.*;


/**
 * Reads tokens from the input stream one at a time. It will remove all non-alphanumeric characters from the beginning and end. 
 * This will remove the hyphens from the token and then create a modified token of the original hyphen. 
 */
public class AdvancedTokenStream implements TokenStream {
	private Scanner mReader;
	
	/**
	 * Constructs a AdvancedTokenStream to read from the specified file.
	 */
	public AdvancedTokenStream(File fileToOpen) throws FileNotFoundException{
		mReader = new Scanner(new FileReader(fileToOpen));
	}

	/**
	 * Constructs a AdvancedTokenStream to read from the specified text.
	 */
	public AdvancedTokenStream(String text){
		mReader = new Scanner(text);
	}
	
	/**
	 * Will check if it is a hyphenated token
	 */
	@Override
	public boolean isHyphenatedToken(){
		//Assuming the hyphen needs to be between two word chars to be a hyphenated token
		if(mReader.hasNext(".*\\w+-+\\w+.*")){
			return true;
		}
		return false;
	}
	
	/**
	 * Returns the next token from the stream for hyphenanted tokens
	 * or null if there is no token.
	 */
	@Override
	public String nextHyphenToken(){
		if (!hasNextToken()){
			return null;
		}
		String next = mReader.next().replaceAll("^\\W+", "").replaceAll("\\W+$", "").replaceAll("-"," ").toLowerCase();
		return next.length() > 0 ? next : 
			hasNextToken() ? nextToken() : null;
	}
	/**
	 * Returns the next token from the stream, or null if there is no token
	 *available.
	 */
	@Override
	public String nextToken() {
		if (!hasNextToken()){
			return null;
		}
		//String next = mReader.next().replaceAll("^\\W+", "").replaceAll("\\W+$", "").toLowerCase();
		String next = mReader.next().replaceAll("\\W", "").toLowerCase();
		return next.length() > 0 ? next : 
			hasNextToken() ? nextToken() : null;
	}

	/**
	 * Will check if there is a next token available.
	 */
	@Override
	public boolean hasNextToken() {
		return mReader.hasNext();
	}


}

package SearchComponents;

public class termPostingList {
	private int docID = -1;
	private int termFreq = -1;
	public int[] positions = null;
	
	/**
	 * Sets the current document ID.
	 * 
	 */
	public int setDocID(int docID){
		return this.docID = docID;
	}
	
	/**
	 * Gets the current document ID.
	 * @return docID
	 */
	public int getDocID(){
		return docID;
	}
	
	/**
	 * 
	 * Sets the term frequency Tf_td based on the length of the current array
	 * 
	 */
	public void setTermFreq(){
		if(positions == null){
			termFreq = 0;
		} else {
			termFreq = positions.length;
		}
	}
	
	/**
	 * Returns the term frequency Tf_td
	 * @return termFreq
	 */
	public int getTermFreq(){
		return termFreq;
	}
	
}

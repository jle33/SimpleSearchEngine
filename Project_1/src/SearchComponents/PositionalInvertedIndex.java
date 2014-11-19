package SearchComponents;


import java.util.*;

public class PositionalInvertedIndex {
	private HashMap<String, HashMap<Integer, List<Integer>>> mIndex;
	private List<String> typeIndex;	// only for storing the types
	private int numTypes;
	private int numTerms;
	private int numPosts;
	private int numDocs;
	private double avgPosts;
	private double[] termFreq;			// stores up to 10 of the highest probability terms in directory
	private int totalIndexSize;

	public PositionalInvertedIndex() {
		mIndex = new HashMap<String, HashMap<Integer, List<Integer>>>();
		typeIndex = new ArrayList<String>();
		numTypes = 0;
		numTerms = 0;
		numPosts = 0;
		avgPosts = 0.0;
		termFreq = new double[10];
		totalIndexSize = 0;
	}

	// adds the type to a separate index for statistics
	public void addType(String term) {

		if(!typeIndex.contains(term)){
			// create new list with current document ID to track documents containing the term
			typeIndex.add(term);
		}
	}

	// adds the term, document, and/or position to the index
	public void addTerm(String term, int documentID, int positionID, HashMap<String, Integer> termFreq) {
		// add the term to the index hashtable. If the table does not have
		// an entry for the term, initialize a new ArrayList<Integer>, add the 
		// docID to the list, and put it into the map. Otherwise add the docID
		// to the list that already exists in the map, but ONLY IF the list does
		// not already contain the docID.

		// knows it is a new document if it is recording the term in position 0 of the document
		if(positionID == 0){
			numDocs++;
			//System.out.println("Number of documents : " + documentID);
		}

		if(!mIndex.containsKey(term)){							// if do not have the term,
			// create new list with current document ID to track documents containing the term
			HashMap<Integer, List<Integer>> docList = new HashMap<Integer, List<Integer>>();
			ArrayList<Integer> posList = new ArrayList<Integer>();
			posList.add(positionID);
			docList.put(documentID, posList);

			// add new document list mapped to the term 
			mIndex.put(term, docList);
			numPosts++;
		}
		else if(!mIndex.get(term).containsKey(documentID)){		// current document not associated with the term 
			HashMap<Integer, List<Integer>> docList = mIndex.get(term);
			ArrayList<Integer> posList = new ArrayList<Integer>();
			posList.add(positionID);
			docList.put(documentID, posList);
			numPosts++;
		}
		else{													// every term gets its position recorded
			mIndex.get(term).get(documentID).add(positionID);
		}
		
		// cannot check with the mIndex since it persists between docs while mTFtd does not
		if(!termFreq.containsKey(term)){						// if term not in TFtd table
			termFreq.put(term, 1);
		}
		else{
			termFreq.put(term, termFreq.get(term) + 1);			// increment term count
		}
	}

	// return the postings list for the given term from the index map.
	public List<Integer> getPostings(String term) {
		if(!mIndex.containsKey(term)){
			return null;
		}
		else{	// get documents Map, create an Integer buffer list, add the set of keys to that buffer, return the list 
			HashMap<Integer, List<Integer>> docMap = mIndex.get(term);
			ArrayList<Integer> docList = new ArrayList<Integer>();
			docList.addAll(docMap.keySet());

			return docList;
		}
	}

	// return the number of terms in the index.
	public int getTermCount() {
		return mIndex.size();
	}

	// return a sorted string array of all terms in the index
	public String[] getDictionary() {
		// create set to hold set of all terms
		Set<String> dictionarySet;
		dictionarySet = mIndex.keySet();

		// convert to array, then sort
		String[] termArray;
		termArray = dictionarySet.toArray(new String[0]);
		Arrays.sort(termArray);		

		return termArray;
	}

	// returns a list of the positions of the given term in the given document
	public List<Integer> getTermPositions(String givenTerm, int document) {
		if((!mIndex.containsKey(givenTerm)) || (!mIndex.get(givenTerm).containsKey(document))){
			return null;
		}

		else{
			List<Integer> positionsList = mIndex.get(givenTerm).get(document);

			return positionsList;
		}
	}

	// get the number of types in index
	public int getNumTypes(){		
		return numTypes;
	}

	// get number of terms in index
	public int getNumTerms(){
		return numTerms;
	}

	// get number of documents in index
	public int getNumDocs(){
		return numDocs;
	}

	// get the average number of posts per document
	public double getAvgPosts(){
		return avgPosts;
	}

	// get an array of the proportion of appearance for the 10 most frequent terms
	public double[] getTopTermFreq(){	
		return termFreq;
	}

	// get the approximate byte size of the index
	public int getTotalIndexSize(){
		return totalIndexSize;
	}

	// calculate the statistic for the Positional Inverted Index
	public void finalize(){
		numTypes = typeIndex.size();
		numTerms = mIndex.size();
		avgPosts = (double)numPosts/mIndex.size();

		// calculate the double array for the top terms proportions
		PriorityQueue<Integer> setOfFreq = new PriorityQueue<Integer>();	// keeps size of top 10 postings
		String[] listOfTerms = getDictionary();								// list of terms

		// for each term, get its size and add to the TreeSet of largest postings
		for(int i = 0; i < listOfTerms.length; i++){
			int postingSize = getPostings(listOfTerms[i]).size();

			// check if the current posting is in the top 10
			if(setOfFreq.size() >= 10){
				if(postingSize > setOfFreq.peek()){
					setOfFreq.poll();
					setOfFreq.add(postingSize);
				}
			}
			else
				setOfFreq.add(postingSize);
		}

		// convert to array and get proportions
		for(int i = setOfFreq.size() - 1; setOfFreq.size() > 0 && i >= 0; i--){	// while the tree set since has values; should not be more than 10
			double number = setOfFreq.poll();
			termFreq[i] = number/numDocs;
		}

		// approximating total memory requirements
		totalIndexSize = totalIndexSize + 24 + (36 * mIndex.size()); 	// hashmap memory size						
		for(int j = 0; j < listOfTerms.length; j++){
			totalIndexSize = totalIndexSize + 40 + (2 * listOfTerms[j].length());		// terms memory size

			List<Integer> posts = getPostings(listOfTerms[j]);
			totalIndexSize = totalIndexSize + 24 + (8 * posts.size());			// postings list memory size

			for(int k = 0; k < posts.size(); k++){
				List<Integer> positions = getTermPositions(listOfTerms[j], posts.get(k));	// individual posting memory size
				totalIndexSize = totalIndexSize + 48 + (4 * positions.size());
			}
		}
	}
}

package SearchComponents;

import java.util.*;

public class NaiveInvertedIndex {
	private HashMap<String, HashMap<Integer, List<Integer>>> mIndex;
	private HashMap<String, List<Integer>> typeIndex;
	private int numTypes;
	private int numTerms;
	private int numPosts;
	private int numDocs;
	private double avgPosts;
	private double[] termFreq;
	private int totalIndexSize;

	public NaiveInvertedIndex() {
		mIndex = new HashMap<String, HashMap<Integer, List<Integer>>>();
		typeIndex = new HashMap<String, List<Integer>>();
		numTypes = 0;
		numTerms = 0;
		numPosts = 0;
		avgPosts = 0.0;
		termFreq = new double[10];
		totalIndexSize = 0;
	}

	public void addType(String term, int documentID) {

		if(!typeIndex.containsKey(term)){
			// create new list with current document ID to track documents containing the term
			ArrayList<Integer> docList = new ArrayList<Integer>();
			docList.add(documentID);

			// add new document list mapped to the term 
			typeIndex.put(term, docList);
		}
		else{
			if(!typeIndex.get(term).contains(documentID)){
				typeIndex.get(term).add(documentID);
			}
		}
	}

	public void addTerm(String term, int documentID, int positionID) {
		// TO-DO: add the term to the index hashtable. If the table does not have
		// an entry for the term, initialize a new ArrayList<Integer>, add the 
		// docID to the list, and put it into the map. Otherwise add the docID
		// to the list that already exists in the map, but ONLY IF the list does
		// not already contain the docID.
		
		if(positionID == 1){
			numDocs++;
		}

		if(!mIndex.containsKey(term)){							// do not have the term
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
	}

	public List<Integer> getPostings(String term) {
		// TO-DO: return the postings list for the given term from the index map.
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

	public int getTermCount() {
		// TO-DO: return the number of terms in the index.

		return mIndex.size();
	}

	public String[] getDictionary() {
		// TO-DO: fill an array of Strings with all the keys from the hashtable.
		// Sort the array and return it.

		// create set to hold set of all terms
		Set<String> dictionarySet;
		dictionarySet = mIndex.keySet();

		// convert to array, then sort
		String[] termArray;
		termArray = dictionarySet.toArray(new String[0]);
		Arrays.sort(termArray);		

		return termArray;
	}

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

	public void finalize(){
		numTypes = typeIndex.size();
		numTerms = mIndex.size();
		avgPosts = (double)numPosts/mIndex.size();

		// calculate the double array for the top terms proportions
		PriorityQueue<Integer> setOfFreq = new PriorityQueue<Integer>();	// keeps size of top 10 postings
		String[] listOfTerms = getDictionary();								// list of terms
		//int tenthTerm = 0;													// keeps track of the tenth largest value

		// for each term, get its size and add to the TreeSet of largest postings
		for(int i = 0; i < listOfTerms.length; i++){
			int postingSize = getPostings(listOfTerms[i]).size();
			System.out.println("Current posting size: " + postingSize);
			System.out.println("Current set of freq size: " + setOfFreq.size());

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

		/*
		totalIndexSize = totalIndexSize + 24 + (36 * mIndex.size()); 	// hashmap memory size						
		for(int x = 0; x < listOfTerms.length; x++){
			totalIndexSize = totalIndexSize + 40 + (2 * listOfTerms[x].length());		// terms memory size

			List<Integer> posts = getPostings(listOfTerms[x]);
			totalIndexSize = totalIndexSize + 24 + (8 * posts.size());			// postings list memory size

			for(int y = 0; y < posts.size(); y++){
				List<Integer> positions = getTermPositions(listOfTerms[x], posts.get(y));	// individual posting memory size
				totalIndexSize = totalIndexSize + 48 + (4 * positions.size());
			}
		}
		 */

	}

}

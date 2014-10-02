
import java.util.*;

public class NaiveInvertedIndex {
	private HashMap<String, HashMap<Integer, List<Integer>>> mIndex;

	public NaiveInvertedIndex() {
		mIndex = new HashMap<String, HashMap<Integer, List<Integer>>>();
	}

	public void addTerm(String term, int documentID, int positionID) {
		// TO-DO: add the term to the index hashtable. If the table does not have
		// an entry for the term, initialize a new ArrayList<Integer>, add the 
		// docID to the list, and put it into the map. Otherwise add the docID
		// to the list that already exists in the map, but ONLY IF the list does
		// not already contain the docID.

		if(!mIndex.containsKey(term)){							// do not have the term
			// create new list with current document ID to track documents containing the term
			HashMap<Integer, List<Integer>> docList = new HashMap<Integer, List<Integer>>();
			ArrayList<Integer> posList = new ArrayList<Integer>();
			posList.add(positionID);
			docList.put(documentID, posList);

			// add new document list mapped to the term 
			mIndex.put(term, docList);
		}
		else if(!mIndex.get(term).containsKey(documentID)){		// current document not associated with the term 
			HashMap<Integer, List<Integer>> docList = mIndex.get(term);
			ArrayList<Integer> posList = new ArrayList<Integer>();
			posList.add(positionID);
			docList.put(documentID, posList);
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
}

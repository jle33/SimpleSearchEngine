package SearchComponents;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProcessQuery {


	public static List<Integer> processQuery(NaiveInvertedIndex index, List<String> fileNames, String query) {
		query = query.trim();
		String delims = "";
		String[] tokens;

		//1. a single token
		String posLit1 = "";
		//2. a sequence of tokens that are within double quotes, phase query
		List<String> posLit2 = new ArrayList<String>();

		//take care of + (OR) when it sees + store in new queryLiteral
		List<List<Integer>> OrList = new ArrayList<List<Integer>>();

		HashMap<String, Boolean> queryLiteral = new HashMap<String,Boolean>();

		delims = "[ ]+";
		tokens = query.split(delims);

		boolean isPhrase = false;
		boolean endPhrase = true;
		boolean isOr = false;
		for (int i = 0; i < tokens.length; i++) {
			if(!tokens[i].equals("+")){
				//find first "
				if(tokens[i].substring(0, 1).equals("\"")){
					isPhrase = true;
					endPhrase = false;
				}

				if(tokens[i].substring(tokens[i].length()-1, tokens[i].length()).equals("\"")){
					endPhrase = true;
				}

				if (isPhrase){
					if (endPhrase){
						isPhrase = false;
						posLit2.add(tokens[i]);
						String phrase = "";
						for(String str : posLit2){
							phrase = phrase + " " + str;
						}
						queryLiteral.put(phrase, true);
						posLit2 = new ArrayList<String>();
					}
					else {
						posLit2.add(tokens[i]);
					}

				}
				else {
					posLit1 = tokens[i];
					queryLiteral.put(posLit1, false);
				}

			}
			else {
				isOr = true;
				//store the Q
				List<Integer> Q = processQueryLiteral(index, queryLiteral, fileNames);
				queryLiteral.clear();
				OrList.add(Q);
			}
		}

		List<Integer> result = null;
		if(isOr == true){
			//only last Q is not added into the OrList
			result = processQueryLiteral(index, queryLiteral, fileNames);
			//Process any Q's in the OrList
			for(int i = 0; i < OrList.size(); i++){
				List<Integer> Q = OrList.get(i);
				result = OrMerge(result, Q);
			}
		}
		else{
			result = processQueryLiteral(index, queryLiteral, fileNames);
		}
		return result;
	}

	private static List<Integer> processQueryLiteral(NaiveInvertedIndex index, HashMap<String, Boolean> queryLiteral, List<String> fileNames){
		List<Integer> Q = new ArrayList<Integer>();
		java.util.Iterator<String> it = queryLiteral.keySet().iterator();
		while(it.hasNext()){
			String str = it.next();
			if(queryLiteral.get(str) == true){
				List<Integer> docList = processPhrase(index, str);
				if(docList == null){
					return null;
				}
				if(Q.isEmpty()){
					Q = docList;
				}else {
					Q = AndMerge(Q, docList);
				}
			}
			else if(queryLiteral.get(str) == false){
				List<Integer> docList = processToken(index, str);
				if (docList == null){
					return null;
				}
				if (Q.isEmpty()){
					Q = docList;
				}
				else {
					Q = AndMerge(Q, docList);
				}
			}
		}

		return Q;
	}

	private static List<Integer> processPhrase(NaiveInvertedIndex index, String posLit2){
		List<Integer> docIDs = new ArrayList<Integer>();
		AdvancedTokenStream readText;
		String token;
		Map<Integer, List<Integer>> mergeList = new HashMap<Integer, List<Integer>>();
		readText = new AdvancedTokenStream(posLit2);
		token = readText.nextToken();
		token = PorterStemmer.processToken(token.toLowerCase());
		List<Integer> postings_1 = index.getPostings(token);
		if(postings_1 == null){
			return docIDs;
		}
		for(int j = 0; j < postings_1.size(); j++){
			int docIndex = postings_1.get(j);
			List<Integer> positions = index.getTermPositions(token, docIndex);
			if(positions != null){
				mergeList.put(docIndex, positions);
			}else {
				return docIDs;
			}
		}


		Map<Integer, List<Integer>> p2 = null;
		while(readText.hasNextToken()) {
			token = readText.nextToken();
			token = PorterStemmer.processToken(token.toLowerCase());
			List<Integer> postings = index.getPostings(token);
			if(postings == null){
				return docIDs;
			}
			p2 = new HashMap<Integer, List<Integer>>();
			for(int j = 0; j < postings.size(); j++){
				Integer docIndex = postings.get(j);
				List<Integer> positions = index.getTermPositions(token, docIndex);
				if(positions != null){
					p2.put(docIndex, positions);
				}else {
					return docIDs;
				}
			}
			mergeList = mergePhrase(mergeList, p2);
		}

		for(int docId : mergeList.keySet()){
			docIDs.add(docId);
		}

		//return docIDs of merged phrase postings
		return docIDs;
	}

	private static Map<Integer, List<Integer>> mergePhrase(Map<Integer, List<Integer>> p1, Map<Integer, List<Integer>> p2){
		List<Integer> l1 = new ArrayList<Integer>(p1.keySet());
		List<Integer> l2 = new ArrayList<Integer>(p2.keySet());
		Collections.sort(l1);
		Collections.sort(l2);

		java.util.Iterator<Integer> it1 = l1.iterator();
		java.util.Iterator<Integer> it2 = l2.iterator();

		Map<Integer, List<Integer>> mergedList = new HashMap<Integer, List<Integer>>();

		int docID_1 = -1;
		int docID_2 = -1;

		do{
			// set up the documents to compare
			if(docID_1 == -1){		// required for initial assignment 
				if(it1.hasNext()){				
					docID_1 = it1.next();
				}
				if(it2.hasNext()){
					docID_2 = it2.next();
				}
			}
			else if(docID_1 < docID_2){
				if(it1.hasNext()){
					docID_1 = it1.next();
				}
			}
			else if(docID_1 > docID_2){
				if(it2.hasNext()){
					docID_2 = it2.next();
				}
			}
			else if((docID_1 != docID_2) && (!it1.hasNext() || !it2.hasNext()) ){
				return mergedList; 
			}

			// if pointing at the same document, check for proper ordering
			if(docID_1 == docID_2){
				//match the two
				int i = 0;
				int j = 0;
				List<Integer> pp1 = p1.get(docID_1);
				List<Integer> pp2 = p2.get(docID_2);
				while(i < pp1.size() && j < pp2.size()){
					int pos1 = pp1.get(i);
					int pos2 = pp2.get(j);
					//check if pos1 is less than pos2
					if(pos1 < pos2){
						//check for offset of 1
						if(Math.abs(pos1 - pos2) == 1){
							//store docID and append pos to list;
							if(mergedList.containsKey(docID_1)){
								mergedList.get(docID_1).add(pos1);
								mergedList.get(docID_1).add(pos2);
							}else {
								//want to just store docID and positions with the phrase
								mergedList.put(docID_1, new ArrayList<Integer>());
								mergedList.get(docID_1).add(pos1);
								mergedList.get(docID_1).add(pos2);
							}
							i++;
							j++;
						}
						else {
							i++;
						}
					}else {
						j++;
					}
				}

				if(it1.hasNext() && it2.hasNext()){
					docID_1 = it1.next();
					docID_2 = it2.next();
				}
			}
		}while(it1.hasNext() || it2.hasNext());

		//return empty list if empty
		return mergedList;
	}

	private static List<Integer> processToken(NaiveInvertedIndex index, String posLit1){
		String token = posLit1.toLowerCase();
		token = PorterStemmer.processToken(token);
		List<Integer> postings = index.getPostings(token);

		return postings;
	}

	private static List<Integer> AndMerge(List<Integer> p1, List<Integer> p2){
		List<Integer> mergedP = new ArrayList<Integer>();
		Collections.sort(p1);
		Collections.sort(p2);
		int i = 0;
		int j = 0;
		int doc1 = -1;
		int doc2 = -1;

		while (true){
			if (i >= p1.size()){
				break;
			}
			else if ( j>= p2.size()){
				break;
			}
			doc1 = p1.get(i);
			doc2 = p2.get(j);
			if (doc1 == doc2){
				mergedP.add(doc1);
				i++;
				j++;
			}
			else if (doc1 > doc2){
				j++;
			}
			else {
				i++;
			}
		}
		return mergedP;
	}

	private static List<Integer> OrMerge(List<Integer> p1, List<Integer> p2){
		List<Integer> mergedP = new ArrayList<Integer>();
		Collections.sort(p1);
		Collections.sort(p2);

		if (p1 == null){
			return p2;
		}
		else if(p2 == null){
			return p1;
		}
		
		int i = 0;
		int j = 0;
		int doc1 = -1;
		int doc2 = -1;
		
		while(true){
			if(i < p1.size() && j < p2.size()){
				doc1 = p1.get(i);
				doc2 = p2.get(j);
			}
			if(i >= p1.size()){
				while(j < p2.size()){
					mergedP.add(doc2);
					j++;
				}
				break;
			}
			else if(j >= p2.size()){
				while(i < p1.size()){
					mergedP.add(doc1);
					i++;
				}
				break;
			}
			else{

				if(doc1 == doc2){
					mergedP.add(doc1);
					i++;
					j++;
				}
				else if(doc1 > doc2){
					mergedP.add(doc2);
					j++;
				}
				else {
					mergedP.add(doc1);
					i++;
				}
			}
		}
		return mergedP;
	}
}



























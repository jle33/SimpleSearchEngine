package SearchComponents;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProcessQuery {


	public static List<Integer> processQuery(PositionalInvertedIndex index, List<String> fileNames, String query) {
		query = query.trim();
		String delims = "";
		String[] tokens;

		//1. a single token
		String posLit1 = "";
		//2. a sequence of tokens that are within double quotes, phase query
		List<String> posLit2 = new ArrayList<String>();
		String[] posLiteral2; 
		String[][] myOrList;
		//take care of + (OR) when it sees + store in new queryLiteral
		List<List<Integer>> OrList = new ArrayList<List<Integer>>();

		HashMap<String, Boolean> queryLiteral = new HashMap<String,Boolean>();

		delims = "[ ]+";
		tokens = query.split(delims);

		boolean isPhrase = false;
		boolean endPhrase = true;
		boolean isOr = false;
		for (int i = 0; i < tokens.length; i++) {
			String tokenStr = tokens[i];
			if(!tokenStr.equals("+")){
				//find first "
				if(tokenStr.substring(0, 1).equals("\"") || tokenStr.substring(1, 2).equals("\"")){
					isPhrase = true;
					endPhrase = false;
				}

				//find 2nd "
				if(tokenStr.substring(tokenStr.length()-1, tokenStr.length()).equals("\"")){
					endPhrase = true;
				}


				if (isPhrase){
					if (endPhrase){
						isPhrase = false;
						posLit2.add(tokenStr);
						String phrase = "";
						for(String str : posLit2){
							phrase = phrase + " " + str;
						}
						queryLiteral.put(phrase.trim(), true);
						posLit2 = new ArrayList<String>();
					}
					else {
						posLit2.add(tokenStr);
					}

				}
				else {
					posLit1 = tokenStr;
					queryLiteral.put(posLit1.trim(), false);


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

	private static List<Integer> processQueryLiteral(PositionalInvertedIndex index, HashMap<String, Boolean> queryLiteral, List<String> fileNames){
		List<Integer> Q = new ArrayList<Integer>();
		java.util.Iterator<String> it = queryLiteral.keySet().iterator();
		String str = null;
		List<Integer> docList;
		List<List<Integer>> notList = new ArrayList<List<Integer>>();
		
		while(it.hasNext()){
			str = it.next();
			if(str.startsWith("-")){
				if(queryLiteral.get(str) == true){
					docList = processPhrase(index, str);
				} else {
					docList = processToken(index, str);
				}
				notList.add(docList);
			} else {
				break;
			}
		}
		
		if(queryLiteral.get(str) == true){
			 docList = processPhrase(index, str);
			 Q = docList;
		}else{
			docList = processToken(index, str);
			Q = docList;
		}
		while(it.hasNext()){
			str = it.next();
			if(queryLiteral.get(str) == true){
				docList = processPhrase(index, str);
				if(docList == null){
					return null;
				}
				if(str.startsWith("-")){
					notList.add(docList);
				} else {
					Q = AndMerge(Q, docList);
				}
			}
			else if(queryLiteral.get(str) == false){
				docList = processToken(index, str);
				if (docList == null){
					return null;
				}
				if(str.startsWith("-")){
					notList.add(docList);
				} else {
					Q = AndMerge(Q, docList);
				}
			}
		}
		if(!notList.isEmpty()){
			for(int i = 0; i < notList.size(); i++){
				Q = NotMerge(Q, notList.get(i));
			}
		}

		return Q;
	}

	private static List<Integer> processToken(PositionalInvertedIndex index, String posLit1){
		String token = posLit1.toLowerCase();
		token = PorterStemmer.processToken(token);
		List<Integer> postings = index.getPostings(token);

		return postings;
	}


	private static List<Integer> processPhrase(PositionalInvertedIndex index, String posLit2){
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
			//System.out.println("Looping");
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
				}else {
					return mergedList;
				}
			}
			else if(docID_1 > docID_2){
				if(it2.hasNext()){
					docID_2 = it2.next();
				}else {
					return mergedList;
				}
			}
			else if((!it1.hasNext() || !it2.hasNext()) ){
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

	//TO-DO change to take in arrays only need to Build the Index First
	private static List<Integer> AndMerge(List<Integer> p11, List<Integer> p22){
		Integer[] mergedP;
		Collections.sort(p11);
		Collections.sort(p22);
		Integer[] p1 = p11.toArray(new Integer[p11.size()]);
		Integer[] p2 = p22.toArray(new Integer[p22.size()]);

		int size = 0;
		if(p1.length < p2.length){
			size = p1.length;
		}else {
			size = p2.length;
		}
		mergedP = new Integer[size];

		int i = 0;
		int j = 0;
		int mergedCounter = 0;
		int doc1 = -1;
		int doc2 = -1;
		
		while (i < p1.length && j < p2.length){
			doc1 = p1[i];
			doc2 = p2[j];
			if (doc1 == doc2){
				mergedP[mergedCounter++] = doc1;
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
		if(mergedCounter < mergedP.length){
			mergedP[mergedCounter] = -1;
		}
		//Temp conversion -DELETE ME
		List<Integer> temp = new ArrayList<Integer>();
		for(int i1 = 0; i1 < mergedP.length; i1++){
			if(mergedP[i1] != -1){
				temp.add(mergedP[i1]);
			}else {
				break;
			}
		}

		return temp;
	}

	//TO-DO change to take in arrays only need to Build the Index First
	private static List<Integer> OrMerge(List<Integer> p11, List<Integer> p22){
		Integer[] mergedP;
		if(p11 == null){
			return p22;
		}else if(p22 == null){
			return p11;
		}
		Collections.sort(p11);
		Collections.sort(p22);
		Integer[] p1 = p11.toArray(new Integer[p11.size()]);
		Integer[] p2 = p22.toArray(new Integer[p22.size()]);

		int size = p1.length + p2.length;

		mergedP = new Integer[size];

		int i = 0;
		int j = 0;
		int mergedCounter = 0;
		int doc1 = -1;
		int doc2 = -1;
		while(true){
			if(i < p1.length && j < p2.length){
				doc1 = p1[i];
				doc2 = p2[j];
			}
			
			if(i >= p1.length){
				while(j < p2.length){
					mergedP[mergedCounter++] = p2[j];
					j++;
				}
				break;
			}
			else if(j >= p2.length){
				while(i < p1.length){
					mergedP[mergedCounter++] = p1[i];
					i++;
				}
				break;
			}
			else{
				if(doc1 == doc2){
					mergedP[mergedCounter++] = doc1;
					i++;
					j++;
				}
				else if(doc1 > doc2){
					mergedP[mergedCounter++] = doc2;
					j++;
				}
				else {
					mergedP[mergedCounter++] = doc1;
					i++;
				}
			}
		}

		if(mergedCounter < mergedP.length){
			mergedP[mergedCounter] = -1;
		}
		//Temp conversion -DELETE ME
		List<Integer> temp = new ArrayList<Integer>();
		for(int i1 = 0; i1 < mergedP.length; i1++){
			if(mergedP[i1] != -1){
				temp.add(mergedP[i1]);
			}else{
				break;
			}
		}
		return temp;
	}
	
	private static List<Integer> NotMerge(List<Integer> p1, List<Integer> p2){
		List<Integer> mergedP = new ArrayList<Integer>();
		if(p1 == null){
			return p2;
		}else if(p2 == null){
			return p1;
		}
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
			else if ( j >= p2.size()){
			 
			    for (int k = i; k < p1.size(); k++) {
				   mergedP.add(p1.get(k));  // continue to add all the rest
	            }
				break;
			}
			
			doc1 = p1.get(i);
			doc2 = p2.get(j);
		
			
			if (doc1 == doc2){
				
				i++;
				j++;
			}
			else if (doc1 > doc2){
				j++;
			}
			else {   // doc1 < doc2
					mergedP.add(doc1);
				i++;
			}
		}
		
		
		return mergedP;
	}




}






















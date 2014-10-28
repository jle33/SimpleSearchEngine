package SearchComponents;

import java.util.ArrayList;
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
			System.out.println(tokens[i]);
			if(!tokens[i].equals("+")){
				System.out.println(tokens[i].substring(0, 1));
				System.out.println(tokens[i].substring(0, 1).equals("\""));
				//find first "
				if(tokens[i].substring(0, 1).equals("\"")){
					System.out.println("Found : start " + tokens[i].substring(0, 1));
					isPhrase = true;
					endPhrase = false;
				}

				if(tokens[i].substring(tokens[i].length()-1, tokens[i].length()).equals("\"")){
					System.out.println("Found : end " + tokens[i].substring(tokens[i].length()-1, tokens[i].length()));
					endPhrase = true;
				}

				System.out.println("isPhrase: " + isPhrase);
				System.out.println("endPhrase: " + endPhrase);

				if(isPhrase){
					if(endPhrase){
						isPhrase = false;
						posLit2.add(tokens[i]);
						String phrase = "";
						for(String str : posLit2){
							phrase = phrase + " " + str;
						}
						System.out.println("Combine" + phrase);
						queryLiteral.put(phrase, true);
						posLit2 = new ArrayList<String>();
					}else {
						posLit2.add(tokens[i]);
					}

				}else {
					posLit1 = tokens[i];
					queryLiteral.put(posLit1, false);
				}

			}else {
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
		}else{
			result = processQueryLiteral(index, queryLiteral, fileNames);
		}
		return result;
	}

	private static List<Integer> processQueryLiteral(NaiveInvertedIndex index, HashMap<String, Boolean> queryLiteral, List<String> fileNames){
		List<Integer> Q = new ArrayList<Integer>();
		java.util.Iterator<String> it = queryLiteral.keySet().iterator();
		while(it.hasNext()){
			String str = it.next();
			System.out.println("In Q_k is : " + str + " with boolean : " + queryLiteral.get(str));
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
			}else if(queryLiteral.get(str) == false){
				List<Integer> docList = processToken(index, str);
				if(docList == null){
					return null;
				}
				if(Q.isEmpty()){
					Q = docList;
				}else {
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
		System.out.println("First token ever: " + token);
		for(int j = 0; j < postings_1.size(); j++){
			int docIndex = postings_1.get(j);
			//System.out.print("DocIDs for : " + token + " with docID : "+ docIndex + " with run: " + j);
			//System.out.println();
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
				System.out.print("DocIDs for : " + token + " with docID : "+ docIndex);
				System.out.println();
				List<Integer> positions = index.getTermPositions(token, docIndex);
				if(positions != null){
					p2.put(docIndex, positions);
				}else {
					return docIDs;
				}
			}
			System.out.println("Merge positions for " + token);
			mergeList = mergePhrase(mergeList, p2);
		}

		for(int docId : mergeList.keySet()){
			docIDs.add(docId);
		}

		//return docIDs of merged phrase postings
		return docIDs;
	}

	private static Map<Integer, List<Integer>> mergePhrase(Map<Integer, List<Integer>> p1, Map<Integer, List<Integer>> p2){
		java.util.Iterator<Integer> it1 = p1.keySet().iterator();
		java.util.Iterator<Integer> it2 = p2.keySet().iterator();
		Map<Integer, List<Integer>> mergedList = new HashMap<Integer, List<Integer>>();
		System.out.println("Merge Phrases");

		int docID_1 = it1.next();
		int docID_2 = it2.next();

		do{
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
					System.out.println("Doc " + docID_1 + " Pos: " + pos1 + " Doc " + docID_2 +" Pos: " + pos2);
					if(pos1 < pos2){
						//check for offset of 1
						if(Math.abs(pos1 - pos2) == 1){
							System.out.println("-------------------------Adding mergedList");
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
			else if(docID_1 < docID_2){
				if(it1.hasNext()){
					docID_1 = it1.next();
				}
			}else {
				if(it2.hasNext()){
					docID_2 = it2.next();
				}
			}
		}while(it1.hasNext() && it2.hasNext());
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
		int i = 0;
		int j = 0;
		while(true){
			if(i >= p1.size()){
				break;
			}else if( j>= p2.size()){
				break;
			}
			if(p1.get(i) == p2.get(j)){
				mergedP.add(p1.get(i));
				i++;
				j++;
			}else if(p1.get(i) > p2.get(j)){
				j++;
			}else {
				i++;
			}
		}
		return mergedP;
	}

	private static List<Integer> OrMerge(List<Integer> p1, List<Integer> p2){
		List<Integer> mergedP = new ArrayList<Integer>();
		if(p1 == null){
			return p2;
		}else if(p2 == null){
			return p1;
		}
		int i = 0;
		int j = 0;
		while(true){
			if(i >= p1.size()){
				while(j < p2.size()){
					mergedP.add(p2.get(j));
					j++;
				}
				break;
			}else if(j >= p2.size()){
				while(i < p1.size()){
					mergedP.add(p1.get(i));
					i++;
				}
				break;
			}else{

				if(p1.get(i) == p2.get(j)){
					mergedP.add(p1.get(i));
					i++;
					j++;
				}else if(p1.get(i) > p2.get(j)){
					mergedP.add(p2.get(j));
					j++;
				}else {
					mergedP.add(p1.get(i));
					i++;
				}
			}
		}


		return mergedP;
	}
}



























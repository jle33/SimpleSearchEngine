package SearchComponents;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class ProcessQuery {


	public static int[] processQuery(DiskPositionalIndex index, List<String> fileNames, String query) {
		query = query.trim();
		String delims = "";
		String[] tokens;

		//1. a single token
		String posLit1 = "";
		//2. a sequence of tokens that are within double quotes, phase query
		String posLit2 = "";

		String[][] myOrList;
		//take care of + (OR) when it sees + store in new queryLiteral
		List<int[]> OrList = new ArrayList<int[]>();
		HashMap<String, Boolean> queryLiteral = new HashMap<String,Boolean>();

		delims = "[ ]+";
		tokens = query.split(delims);

		boolean isPhrase = false;
		boolean endPhrase = true;
		boolean isOr = false;
		for (int i = 0; i < tokens.length; i++) {
			if(!tokens[i].equals("+")){
				//find first quotation mark "
				if(tokens[i].substring(0, 1).equals("\"")){
					isPhrase = true;
					endPhrase = false;
				}

				//find 2nd quotation mark "
				if(tokens[i].substring(tokens[i].length()-1, tokens[i].length()).equals("\"")){
					endPhrase = true;
				}

				if (isPhrase){
					if (endPhrase){
						isPhrase = false;
						//Add individual tokens to the 
						posLit2 = posLit2 + tokens[i] + " ";
						queryLiteral.put(posLit2, true);
						posLit2 = "";
					}
					else {
						posLit2 = posLit2 + tokens[i] + " ";
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
				int[] Q = processQueryLiteral(index, queryLiteral, fileNames);
				queryLiteral.clear();
				OrList.add(Q);
			}
		}

		int[] result = null;
		if(isOr == true){
			//only last Q is not added into the OrList
			result = processQueryLiteral(index, queryLiteral, fileNames);
			//Process any Q's in the OrList
			for(int i = 0; i < OrList.size(); i++){
				int[] Q = OrList.get(i);
				result = OrMerge(result, Q);
			}
		}
		else{
			result = processQueryLiteral(index, queryLiteral, fileNames);
		}
		return result;
	}

	private static int[] processQueryLiteral(DiskPositionalIndex index, HashMap<String, Boolean> queryLiteral, List<String> fileNames){
		int[] Q;
		int[] docList;
		java.util.Iterator<String> it = queryLiteral.keySet().iterator();
		String str = null;

		if(it.hasNext()){
			str = it.next();
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
				Q = AndMerge(Q, docList);

			}
			else if(queryLiteral.get(str) == false){
				docList = processToken(index, str);
				if (docList == null){
					return null;
				}
				Q = AndMerge(Q, docList);
			}
		}

		return Q;
	}

	private static int[] processToken(DiskPositionalIndex index, String posLit1){
		String token = posLit1.toLowerCase();
		token = PorterStemmer.processToken(token);
		int[] postings = index.GetPostings(token);

		return postings;
	}


	private static int[] processPhrase(DiskPositionalIndex index, String posLit2){
		int[] docIDs = null;
		AdvancedTokenStream readText;
		String token;
		termPostingList[] postings;

		//Get postings list with position for first token in phrase
		readText = new AdvancedTokenStream(posLit2);
		token = readText.nextToken();
		token = PorterStemmer.processToken(token.toLowerCase());
		postings = index.GetPostingsPositions(token);

		if(postings == null){
			System.out.println("ERROR - Process Query Line 149");
			System.out.println("For Token: " + token);
			return docIDs;
		}

		//Get postings list with position for the second token in phrase and its following
		while(readText.hasNextToken()){
			token = readText.nextToken();
			token = PorterStemmer.processToken(token.toLowerCase());
			termPostingList[] postings_2 = index.GetPostingsPositions(token);
			if(postings_2 == null){
				System.out.println("ERROR - Process Query Line 159");
				System.out.println("For Token: " + token);
				return docIDs;
			}

			postings = mergePhrase(postings, postings_2, 1);

		}


		/*
		 * Bad implementation? Extra Work done, might have to change to a better alternative.
		 * After merging the phrases, now we just need the docIDs to merge the other literals together
		 * so copy all docIDs into a separate array and return it
		 */

		docIDs = new int[postings.length];
		for(int i = 0; i < postings.length; i++){
			docIDs[i] = postings[i].getDocID();
		}
		//return docIDs of merged phrase postings
		return docIDs;
	}

	private static termPostingList[] mergePhrase(termPostingList[] p1, termPostingList[] p2, int k){
		termPostingList[] mergedList;
		int size = 0;
		//At most n operations of the smaller of the two positional lists
		if(p1.length < p2.length){
			size = p1.length;
		}else {
			size = p2.length;
		}

		mergedList = new termPostingList[size];
		int i = 0, j = 0, nextDocID = -1;
		int docID_1 = -1, docID_2 = -1;
		while(i < p1.length && j < p2.length){
			docID_1 = p1[i].getDocID();
			docID_2 = p2[j].getDocID();
			if(docID_1 == docID_2){
				int c = 0, l = 0;

				int[] pp1 = p1[i].positions;
				int[] pp2 = p2[j].positions;
				//At most n operations of the smaller of the two position lists
				//int[] positions = new int[getPositionListSize(pp1, pp2)];
				Vector<Integer> positions = new Vector<Integer>();
				//check if positions are within k
				boolean isNear = false;
				//Keep track of the current positions;
				int posCount = 0;
				while(c < pp1.length && l < pp2.length){
					int pos1 = pp1[c]; //posting's position for term 1
					int pos2 = pp2[l]; //posting's position for term 2
					//check if pos1 is less than pos2
					if(pos1 < pos2){
						//check for offset of k
						if(Math.abs(pos1 - pos2) <= k){
							//positions[posCount++] = pos1; //store pos1 at cur posCount and increment
							//positions[posCount++] = pos2; //store pos2 at cur posCount and increment
							positions.addElement(pos1);
							positions.addElement(pos2);
							isNear = true;
							c++;
							l++;
						}
						else {
							c++; //increment pos2
						}
					}else {
						l++; //increment pos1
					}
				}
				if(isNear == true){
					nextDocID++;
					mergedList[nextDocID] = new termPostingList();
					mergedList[nextDocID].setDocID(docID_1);
					mergedList[nextDocID].positions = copyArray(positions);
					//Below would now be the amount of times that phrase appeared//does not really mean termFreq anymore in this case
					mergedList[nextDocID].setTermFreq(); //termFreq based on how many times
					
				}

				i++; //increment doc1
				j++; //increment doc2
				isNear = false;
			} else if(docID_1 < docID_2) {
				i++;
			} else if(docID_1 > docID_2){
				j++;
			}
		}


		//What happens when the merged postings don't actually take up the full size of the array
		//For now have a -1 to indicate no more reading.
		if(nextDocID == -1){
			return null;
		}

		if(nextDocID >= mergedList.length){
			return mergedList;
		}
		//Downsize the array;
		termPostingList[] finalList = new termPostingList[nextDocID + 1];
		for(int i2 = 0; i2 < nextDocID + 1; i2++){
			int docid = mergedList[i2].getDocID();
			finalList[i2] = new termPostingList();
			finalList[i2].setDocID(docid);
			finalList[i2].positions = mergedList[i2].positions;
			finalList[i2].setTermFreq();
		}
		//return empty list if empty
		return finalList;
	}

	private static int[] AndMerge(int[] p1, int[] p2){
		int[] mergedP;
		int size = 0;
		if(p1.length < p2.length){
			size = p1.length;
		}else {
			size = p2.length;
		}
		mergedP = new int[size];

		int i = 0;
		int j = 0;
		int mergedCounter = 0;
		int doc1 = -1;
		int doc2 = -1;

		while (i < p1.length && j < p2.length){
			doc1 = p1[i];
			doc2 = p2[j];

			//DELETE ME?? 
			if(doc1 == -1 || doc2 == -1){
				break;
			}

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

		//What happens when the merged postings don't actually take up the full size of the array
		//For now have a -1 to indicate no more reading.
		if(mergedCounter < mergedP.length){
			mergedP[mergedCounter] = -1;
		}

		return mergedP;
	}


	private static int[] OrMerge(int[] p1, int[] p2){
		int[] mergedP;
		if(p1 == null){
			return p2;
		}else if(p2 == null){
			return p1;
		}

		int size = p1.length + p2.length;

		mergedP = new int[size];

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
					if(p2[j] == -1){
						break;
					}
					mergedP[mergedCounter++] = p2[j];
					j++;
				}
				break;
			}
			else if(j >= p2.length){
				while(i < p1.length){
					if(p1[j] == -1){
						break;
					}
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
		//What happens when the merged postings don't actually take up the full size of the array
		//For now have a -1 to indicate no more reading.
		if(mergedCounter < mergedP.length){
			mergedP[mergedCounter] = -1;
		}

		return mergedP;
	}

	private static int getPositionListSize(int[] pp1, int[] pp2){
		int size = 0;
		if(pp1.length < pp2.length){
			size = pp1.length;
		}else {
			size = pp2.length;
		}
		return size;
	}

	private static int[] copyArray(Vector<Integer> fromList){
		int[] a = new int[fromList.size()];
		for(int i = 0; i < a.length; i++){
			a[i] = fromList.get(i);
		}
		return a;
	}
}

























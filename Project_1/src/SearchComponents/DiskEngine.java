package SearchComponents;

import java.util.ArrayList;
import java.util.List;

public class DiskEngine {

	private static List<String> fileNames;
	private static DiskPositionalIndex index;
	private static int docCount;
	private static boolean rankMode = false;
	private static Accumulator[] rankedDocs;
	private static float[] docWeights;

	public static List<String> processUserQuery(String userQuery){
		List<String> docResults = new ArrayList<String>(); //FileNames
		int[] docIDs;
		docWeights = new float[1]; //Temp Fix for A_d display
		docWeights[0] = -1; //Temp Fix for A_d display
		if(rankMode){
			rankedDocs = ProcessQuery.rankQuery(index, fileNames, userQuery);
			docIDs = new int[rankedDocs.length];
			docWeights = new float[rankedDocs.length];

			for(int j = 0; j < rankedDocs.length; j++){
				docIDs[j] = rankedDocs[j].getDocID();
			}

			for(int k = 0; k < rankedDocs.length; k++){
				docWeights[k] = rankedDocs[k].getValue();
				System.out.println("Doc" + rankedDocs[k].getDocID() + " = " + rankedDocs[k].getValue());
			}

		} else {
			docIDs =  ProcessQuery.processQuery(index, fileNames, userQuery);
		}


		if(docIDs != null){
			docCount = docIDs.length;
			for(int curDocID : docIDs){
				if(curDocID == -1){
					break;
				}
				docResults.add(index.getFileNames().get(curDocID)); //Add found filename to list
			}
		}
		return docResults;
	}
	public static int getDocCount() {
		return docCount;
	}

	public static void setDiskPositionalIndex(String indexName){
		DiskEngine.index = new DiskPositionalIndex(indexName);
	}

	public static void setBooleanMode(){
		rankMode = false;
	}

	public static void setRankMode(){
		rankMode = true;
	}
	
	public static float[] getAd(){
		return docWeights;
	}

}

package SearchComponents;

import java.util.ArrayList;
import java.util.List;

public class DiskEngine {
	
	private static List<String> fileNames;
	private static DiskPositionalIndex index;
	private static int docCount;
	
	public static List<String> processUserQuery(String userQuery){
		List<String> docResults = new ArrayList<String>(); //FileNames
		
		int[] docIDs = ProcessQuery.processQuery(index, fileNames, userQuery);
		
		if(docIDs != null){
			docCount = docIDs.length;
			for(int curDocID : docIDs){
				docResults.add(fileNames.get(curDocID)); //Add found filename to list
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
	
}

package SearchComponents;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.text.DecimalFormat;

public class Statistics {
	private String overallStatistics;
	
	public Statistics(String path){
		overallStatistics = readStatistics(path);
	}
	private static String readStatistics(String indexName){
		String stats = new String();
		int numTypes;
		int numTerms;
		double avgPosts;
		int memReq;
		int numFreq;
		double topFreq;
		
		try{
			DecimalFormat numForm = new DecimalFormat("#.00");
			RandomAccessFile statsFile = new RandomAccessFile(
					new File(indexName, "stats.bin"), 
					"r");
			
			// read stats to memory
			byte[] byteBuffer = new byte[8];					
			statsFile.read(byteBuffer, 0, byteBuffer.length);	//number of types
			numTypes = ByteBuffer.wrap(byteBuffer).getInt();
			statsFile.read(byteBuffer, 0, byteBuffer.length);	//number of terms
			numTerms = ByteBuffer.wrap(byteBuffer).getInt();
			statsFile.read(byteBuffer, 0, byteBuffer.length);	//avg docs per posting
			avgPosts = ByteBuffer.wrap(byteBuffer).getDouble();
			statsFile.read(byteBuffer, 0, byteBuffer.length);	//memory required
			memReq = ByteBuffer.wrap(byteBuffer).getInt();
			statsFile.read(byteBuffer, 0, byteBuffer.length);	//number of frequencies recorded
			numFreq = ByteBuffer.wrap(byteBuffer).getInt();
			
			// build up the statistics String
			stats = "Number of Types: " + numTypes + "\n";
			stats = stats + "Number of Terms: " + numTerms + "\n";
			stats = stats + "Average Number of Documents per Posting: " + avgPosts + "\n";
			stats = stats + "The document frequencies of the top 10 terms are: ";
			for(int x = 0; x < numFreq; x++){
				statsFile.read(byteBuffer, 0, byteBuffer.length);	//number of frequencies recorded
				topFreq = ByteBuffer.wrap(byteBuffer).getDouble();
				if(topFreq > 0){
					stats = stats + numForm.format(topFreq * 100) + "% ";
				}
			}
			stats = stats + "\n";
			stats = stats + "The approximate total memory requirements is: " + memReq + " bytes" + "\n\n";
			
		}
		
		catch (FileNotFoundException ex) {
			System.out.println(ex.toString());
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println(stats);
		
		return stats;
	}
	
	public String getStats(){
		return overallStatistics;
	}
}

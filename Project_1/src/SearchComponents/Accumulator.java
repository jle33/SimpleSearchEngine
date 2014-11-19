package SearchComponents;

public class Accumulator {
	private int docID = -1;
	private float value = 0;
	
	public Accumulator(int id, int val){
		setDocID(id);
		setValue(val);
	}
	
	private int setDocID(int docID){
		return this.docID = docID;
	}
	
	public int getDocID(){
		return docID;
	}
	
	public float setValue(float start){
		return value = start;
	}
	
	public float incrementValue(float gain){
		return value = value + gain;
	}
	
	public float getValue(){
		return value;
	}
}

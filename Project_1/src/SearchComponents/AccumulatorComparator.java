package SearchComponents;
import java.util.Comparator;

public class AccumulatorComparator implements Comparator<Accumulator>{
	@Override
	public int compare(Accumulator x, Accumulator y)
    {
        // Orders the Accumulator with the higher value first
        if (x.getValue() < y.getValue())
        {
            return 1;
        }
        if (x.getValue() > y.getValue())
        {
            return -1;
        }
        return 0;
    }
}

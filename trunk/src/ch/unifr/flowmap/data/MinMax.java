package ch.unifr.flowmap.data;

public class MinMax {
	public final double min;
	public final double max;

	public final double minLog;
    public final double maxLog;

    public MinMax(double minValue, double maxValue) {
        this.min = minValue;
        this.max = maxValue;
        this.minLog = Math.log(minValue);
        this.maxLog = Math.log(maxValue);
    }
    
    @Override
    public String toString() {
    	return
    	 "[" +
    	 	"min = " + min + ", " +
    	 	"max = " + max + ", " +
    	 	"minLog = " + minLog + ", " +
    	 	"maxLog = " + maxLog + 
    	 "]"
    	;
    }
}
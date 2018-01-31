package jp.megachips.frizzservice;

import java.io.Serializable;

public class FrizzEvent implements Serializable {
	
	private static final long serialVersionUID = 5415786969637871274L;
	
	public final float[] values;
	public long stepCount;
	public long timestamp;
	public long prevTimestamp;
	public Frizz sensor;
	
	FrizzEvent(int valueSize) {
		values = new float[valueSize];
		stepCount = 0;
	}
}
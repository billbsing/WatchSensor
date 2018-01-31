package jp.megachips.frizzservice;

import java.util.EventListener;

public interface FrizzListener extends EventListener {
	public void onFrizzChanged(FrizzEvent sensorEvent);
}
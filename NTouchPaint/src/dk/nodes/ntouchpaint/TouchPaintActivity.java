package dk.nodes.ntouchpaint;

import android.app.Activity;
import android.os.Bundle;
import dk.nodes.ntouchpaint.ntouchpaint.TouchPaint;

public class TouchPaintActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		TouchPaint tp = new TouchPaint(getBaseContext());
		
		setContentView(tp.getView());

	}
}

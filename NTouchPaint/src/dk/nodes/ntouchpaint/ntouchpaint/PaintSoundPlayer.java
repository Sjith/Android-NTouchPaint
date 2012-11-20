package dk.nodes.ntouchpaint.ntouchpaint;

import android.content.Context;
import android.media.MediaPlayer;
import dk.nodes.ntouchpaint.R;

public class PaintSoundPlayer  {
	MediaPlayer mp;
	Context mContext;

	public PaintSoundPlayer(Context mContext){
		this.mContext=mContext;
	}


	public void stopAllSound(){
		if(mp!=null)
			mp.stop();
	}

	public void startBrush(){
		if(mp!=null){		
			mp = MediaPlayer.create(mContext,R.raw.brush);
			mp.setLooping(true);
			mp.start();		
		}
	}
	public void startEraser(){	
		if(mp!=null){	
			mp = MediaPlayer.create(mContext,R.raw.eraser);			
			mp.start();		
		}
	}
	public void startCamarea(){	
		if(mp!=null){	
			mp = MediaPlayer.create(mContext,R.raw.camera);
			mp.start();	
		}
	}
}

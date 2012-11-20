package dk.nodes.ntouchpaint.ntouchpaint;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;


public class TouchPaint {

	private NTouchPaintView mNTouchPaintView;
	Context mContext;
	private NTouchPointUndoRedoListener mUndoRedoCallback;
	private NTouchPointSoundListener mNTouchPointSoundListener;
	private NTouchPaintFloodFillProgressListener mNTouchPaintFloodFillProgressListener;
	private int currentColor = Color.BLACK;
	private int width=14;

	public TouchPaint(Context  mContext){
		this.mContext = mContext;  
		mNTouchPaintView = new NTouchPaintView(mContext);
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inTempStorage = new byte[16*1024];
	}
	public void setColor(int color){
		currentColor = color;
	}
	public void setWidthOfStroke(int width){
		this.width = width;
	}
	public NTouchPaintView getView(){
		if(mNTouchPaintView==null)
			mNTouchPaintView = new NTouchPaintView(mContext);
		return mNTouchPaintView;
	}
	public void setUndoRedoListener(NTouchPointUndoRedoListener mUndoRedoCallback){
		this.mUndoRedoCallback = mUndoRedoCallback;
	}

	public void setSoundListener(NTouchPointSoundListener mNTouchPointSoundListener){
		this.mNTouchPointSoundListener = mNTouchPointSoundListener;
	}
	public void setFloodFillProgressListener(NTouchPaintFloodFillProgressListener mNTouchPaintFloodFillProgressListener){
		this.mNTouchPaintFloodFillProgressListener = mNTouchPaintFloodFillProgressListener;
	}
	public void undo(){
		if(mNTouchPaintView!=null)
			mNTouchPaintView.undo();
	}

	public void clearUndo(){
		if(mNTouchPaintView!=null)
			mNTouchPaintView.clearUndos();
	}

	public class NTouchPaintView extends View implements Runnable{

		
		private Canvas mCanvas;
		private  Paint mPaint;
		
		private Bitmap zBitmap;
		public Bitmap mBitmap;
		private Bitmap patternBitmap;
		
		private boolean mCurDown;
		private boolean mCurUp;

		private int pattern =1;
		
		private Coordinate oldCord = new Coordinate(0, 0);
		private Coordinate newCord = new Coordinate(0, 0);
			
		private LinkedList<Bitmap> undoBitmapList = new LinkedList<Bitmap>();
		
		int counter=0;	
		private int mCurMoveCounter;
		private boolean canPlay=true;
		private int drawCounter =0;
		public boolean lineOrFill = true;

		Boolean[][] ba;
		private ArrayList<Coordinate> list;

		private boolean onTouch;
		private int floodFill_queueCounter;
	
		protected boolean floodfillDone=true;
		private Thread thread=null;



		public NTouchPaintView(Context c) {
			super(c);
			mNTouchPaintView=this;
			mPaint = new Paint();   
		}


		public void undo(){
			if(undoBitmapList.size()>0){
				mCanvas.drawBitmap(mBitmap=undoBitmapList.getLast(), 0, 0, null);
				undoBitmapList.removeLast();
				invalidate(); 
				if(undoBitmapList.size()==0 && mUndoRedoCallback !=null)
					mUndoRedoCallback.undo(false);
			}
			else
				mUndoRedoCallback.undo(false);

			mCanvas.setBitmap(mBitmap);
		}
		public void clearUndos(){
			try{
				undoBitmapList.remove();
				if(mUndoRedoCallback!=null)
					mUndoRedoCallback.undo(false);
			}
			catch(Exception e){

			}
		}

		private void addBitmapToArray(){  	
			undoBitmapList.add(toBitmap(mCanvas));    		
			if(undoBitmapList.size()>5){
				undoBitmapList.removeFirst();
			}
			if(mUndoRedoCallback!=null)
				mUndoRedoCallback.undo(true);
		}
		public Bitmap toBitmap(Canvas canvas) {

			Bitmap b = Bitmap.createBitmap(canvas.getWidth(), canvas.getHeight(), Bitmap.Config.ARGB_4444);
			canvas.setBitmap(b);
			draw(canvas);          
			canvas.setBitmap(mBitmap);
			return b;
		}

		public void clear() {
			clearUndos();
			if (mCanvas != null) {
				mPaint.setARGB(0xff, 255, 255, 255);
				mCanvas.drawPaint(mPaint);           
				usePattern();

				invalidate();  
				drawCounter=0;                
			}
		}

		public void setPatternNumber(int n){
			pattern=n;
		}
		private void usePattern(){   
			
			
			
			//			if(patternBitmap!=null)
			//				patternBitmap.recycle();
			//
			//			if(zBitmap!=null)
			//				patternBitmap.recycle();
			//			BitmapFactory.Options options = new BitmapFactory.Options();
			//			options.inTempStorage = new byte[16*1024];
			//			options.inScaled=true;
			//			options.inPreferredConfig=Config.RGB_565;
			//			options.inSampleSize=1;
			//
			//			if(pattern==0){
			//				mContext.findViewById(R.id.patternLayer).setBackgroundResource(R.drawable.p0);
			//				patternBitmap = BitmapFactory.decodeResource(c.getResources(),R.drawable.p0,options);
			//				zBitmap = BitmapFactory.decodeResource(c.getResources(),R.drawable.p0_zone,options);
			//			}
			//			if(pattern==1){
			//				mContext.findViewById(R.id.patternLayer).setBackgroundResource(R.drawable.p1);
			//				patternBitmap = BitmapFactory.decodeResource(c.getResources(),R.drawable.p1,options);
			//				zBitmap = BitmapFactory.decodeResource(c.getResources(),R.drawable.p1_zone,options);
			//			}
			//			if(pattern==2){
			//				mContext.findViewById(R.id.patternLayer).setBackgroundResource(R.drawable.p2);
			//				patternBitmap = BitmapFactory.decodeResource(c.getResources(),R.drawable.p2,options);
			//				zBitmap = BitmapFactory.decodeResource(c.getResources(),R.drawable.p2_zone,options);
			//			}
			//			if(pattern==3){
			//				mContext.findViewById(R.id.patternLayer).setBackgroundResource(R.drawable.p3);
			//				patternBitmap = BitmapFactory.decodeResource(c.getResources(),R.drawable.p3,options);
			//				zBitmap = BitmapFactory.decodeResource(c.getResources(),R.drawable.p3_zone,options);
			//			}
			//			if(pattern==4){
			//				mContext.findViewById(R.id.patternLayer).setBackgroundResource(R.drawable.p4);
			//				patternBitmap = BitmapFactory.decodeResource(c.getResources(),R.drawable.p4,options);
			//				zBitmap = BitmapFactory.decodeResource(c.getResources(),R.drawable.p4_zone,options);
			//			}    
			//			if(pattern==5){
			//				mContext.findViewById(R.id.patternLayer).setBackgroundResource(R.drawable.p6);
			//				patternBitmap = BitmapFactory.decodeResource(c.getResources(),R.drawable.p5,options);
			//				zBitmap = BitmapFactory.decodeResource(c.getResources(),R.drawable.p5_zone,options);
			//			}
			//			if(pattern==6){
			//				mContext.findViewById(R.id.patternLayer).setBackgroundResource(R.drawable.p7);
			//				patternBitmap = BitmapFactory.decodeResource(c.getResources(),R.drawable.p6,options);
			//				zBitmap = BitmapFactory.decodeResource(c.getResources(),R.drawable.p6_zone,options);
			//			}
			//			if(pattern==7){
			//				mContext.findViewById(R.id.patternLayer).setBackgroundResource(R.drawable.p7);
			//				patternBitmap = BitmapFactory.decodeResource(c.getResources(),R.drawable.p7,options);
			//				zBitmap = BitmapFactory.decodeResource(c.getResources(),R.drawable.p7_zone,options);
			//			}
			//			if(pattern==8){
			//				mContext.findViewById(R.id.patternLayer).setBackgroundResource(R.drawable.p8);
			//				patternBitmap = BitmapFactory.decodeResource(c.getResources(),R.drawable.p8,options);
			//				zBitmap = BitmapFactory.decodeResource(c.getResources(),R.drawable.p8_zone,options);
			//			}
			//			if(pattern==9){
			//				mContext.findViewById(R.id.patternLayer).setBackgroundResource(R.drawable.p9);
			//				patternBitmap = BitmapFactory.decodeResource(c.getResources(),R.drawable.p9,options);
			//				zBitmap = BitmapFactory.decodeResource(c.getResources(),R.drawable.p9_zone,options);
			//			}
			//			if(pattern==10){
			//				mContext.findViewById(R.id.patternLayer).setBackgroundResource(R.drawable.p10);
			//				patternBitmap = BitmapFactory.decodeResource(c.getResources(),R.drawable.p10,options);
			//				zBitmap = BitmapFactory.decodeResource(c.getResources(),R.drawable.p10_zone,options);
			//			}
			//			if(pattern==11){
			//				mContext.findViewById(R.id.patternLayer).setBackgroundResource(R.drawable.p11);
			//				patternBitmap = BitmapFactory.decodeResource(c.getResources(),R.drawable.p11,options);
			//				zBitmap = BitmapFactory.decodeResource(c.getResources(),R.drawable.p11_zone,options);
			//			}
			//			if(pattern==12){
			//				mContext.findViewById(R.id.patternLayer).setBackgroundResource(R.drawable.p12);
			//				patternBitmap = BitmapFactory.decodeResource(c.getResources(),R.drawable.p12,options);
			//				zBitmap = BitmapFactory.decodeResource(c.getResources(),R.drawable.p12_zone,options);
			//			}
			//			if(pattern==13){
			//				mContext.findViewById(R.id.patternLayer).setBackgroundResource(R.drawable.p13);
			//				patternBitmap = BitmapFactory.decodeResource(c.getResources(),R.drawable.p13,options);
			//				zBitmap = BitmapFactory.decodeResource(c.getResources(),R.drawable.p13_zone,options);
			//			}
			//			if(pattern==14){
			//				mContext.findViewById(R.id.patternLayer).setBackgroundResource(R.drawable.p14);
			//				patternBitmap = BitmapFactory.decodeResource(c.getResources(),R.drawable.p14,options);
			//				zBitmap = BitmapFactory.decodeResource(c.getResources(),R.drawable.p14_zone,options);
			//			}
			//
			//			if(pattern==15){
			//				mContext.findViewById(R.id.patternLayer).setBackgroundResource(R.drawable.p15);
			//				patternBitmap = BitmapFactory.decodeResource(c.getResources(),R.drawable.p15,options);
			//				zBitmap = BitmapFactory.decodeResource(c.getResources(),R.drawable.p15_zone,options);
			//			}
			//			if(pattern==16){
			//				mContext.findViewById(R.id.patternLayer).setBackgroundResource(R.drawable.p16);
			//				patternBitmap = BitmapFactory.decodeResource(c.getResources(),R.drawable.p16,options);
			//				zBitmap = BitmapFactory.decodeResource(c.getResources(),R.drawable.p16_zone,options);
			//			}
			//			if(pattern==17){
			//				mContext.findViewById(R.id.patternLayer).setBackgroundResource(R.drawable.p17);
			//				patternBitmap = BitmapFactory.decodeResource(c.getResources(),R.drawable.p17,options);
			//				zBitmap = BitmapFactory.decodeResource(c.getResources(),R.drawable.p17_zone,options);
			//			}
			//			if(pattern==18){
			//				mContext.findViewById(R.id.patternLayer).setBackgroundResource(R.drawable.p18);
			//				patternBitmap = BitmapFactory.decodeResource(c.getResources(),R.drawable.p18,options);
			//				zBitmap = BitmapFactory.decodeResource(c.getResources(),R.drawable.p18_zone,options);
			//			}
			//			if(pattern==19){
			//				mContext.findViewById(R.id.patternLayer).setBackgroundResource(R.drawable.p19);
			//				patternBitmap = BitmapFactory.decodeResource(c.getResources(),R.drawable.p19,options);
			//				zBitmap = BitmapFactory.decodeResource(c.getResources(),R.drawable.p19_zone,options);
			//			}
			//
			//				patternBitmap=mContext.sizeBitmap(patternBitmap);
			//				zBitmap=mContext.sizeBitmap(zBitmap);	

		}
		public void deleteAll(){
			try{
				if(mBitmap!=null)
					mBitmap.recycle();
				if(patternBitmap!=null)
					patternBitmap.recycle(); 

				if(undoBitmapList!=null)
					undoBitmapList.remove();
			}
			catch(Exception e){

			}
		}


		@Override protected void onSizeChanged(int w, int h, int oldw,
				int oldh) {
			int curW = mBitmap != null ? mBitmap.getWidth() : 0;
			int curH = mBitmap != null ? mBitmap.getHeight() : 0;
			if (curW >= w && curH >= h) {
				return;
			}

			if (curW < w) curW = w;
			if (curH < h) curH = h;



			Bitmap newBitmap = Bitmap.createBitmap(curW, curH,Bitmap.Config.RGB_565);

			Canvas newCanvas = new Canvas();

			newCanvas.setBitmap(newBitmap);    


			if (mBitmap != null) {

				mCanvas.drawBitmap(newBitmap, 0, 0, mPaint);

			}

			mBitmap = newBitmap;
			mCanvas = newCanvas;

			clear();
		}

		@Override protected void onDraw(Canvas canvas) {
			if (mBitmap != null) {
				canvas.drawBitmap(mBitmap, 0, 0, null);
			}

		}
		public String takePicture(){
			addBitmapToArray();
			mCanvas.drawBitmap(patternBitmap, new Matrix(), mPaint); 
			File rootsd = Environment.getExternalStorageDirectory();
			String internalStorage =rootsd.getAbsolutePath() + "/DCIM";

			OutputStream outStream = null;

			File file = new File(internalStorage+"/Svante&maggie_tegning"+counter+".png");
			while(true){
				file = new File(internalStorage+"/Svante&maggie_tegning"+counter+".png");
				if(file.exists()){
					counter++;
				}
				else{
					break;
				}           
			}

			file = new File(internalStorage, "Svante&maggie_tegning"+counter+".png");            
			try {
				outStream = new FileOutputStream(file);
				mBitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream);
				outStream.flush();
				outStream.close();            
			}
			catch(Exception e)
			{
				return null;
			}
			//            Log.d("FILE SAVED AT:",file.getPath());
			undo();
			return file.getPath();
		}

		@Override public boolean onTouchEvent(MotionEvent event) {
			int action = event.getAction();
			if(!floodfillDone)
				return false;
			mCurDown = action == MotionEvent.ACTION_DOWN;
			mCurUp = action == MotionEvent.ACTION_UP;



			if(mCurDown){
				addBitmapToArray();
				oldCord.setCoordinate(event.getX(), event.getY());
				onTouch=true;
			}         
			drawPoint(oldCord.x, oldCord.y, event.getX(), event.getY(),lineOrFill);

			double x =Math.abs(oldCord.x-event.getX());
			double y= Math.abs(oldCord.y-event.getY());

			if(x<=1&&y<=1)
				mCurMoveCounter++;

			else
				mCurMoveCounter=0;

			if(mCurMoveCounter>2){
				if(!canPlay && mNTouchPointSoundListener!=null){
					mNTouchPointSoundListener.stopSound();
					canPlay=true;
				}
			}
			else{
				if(canPlay){
					if(mNTouchPointSoundListener!=null)
						mNTouchPointSoundListener.startSound();
					canPlay=false;
				}
			}

			if(mCurUp){      
				if(mNTouchPointSoundListener!=null)
					mNTouchPointSoundListener.stopSound();
				canPlay=true;
			}
			oldCord.setCoordinate(event.getX(), event.getY());

			return true;
		}
		public void setWidth(int w){
			width=w;
		}
		public void setColor(int c){
			currentColor =c;
		}
		private void usePickedColor(){
			mPaint.setColor(currentColor);		
		}    
		private void drawPoint(final float x, final float y, float nextx, float nexty,boolean b) {
			if(!floodfillDone)
				return;

			if (mBitmap != null) { 
				if(b){    
					usePickedColor();
					mPaint.setStrokeWidth(width);
					mCanvas.drawCircle(x, y, width/2, mPaint);
					mCanvas.drawLine(x, y, nextx, nexty, mPaint);     

					invalidate(calculateRect(x,y,nextx,nexty));
				}
				else{
					if(onTouch){
						usePickedColor();
						onTouch=false;    
						newCord.setCoordinate(y, y);

						if(thread==null){
							thread = new Thread(this);
							thread.start();
						}
						else
							return;
					}

					drawCounter();
				}  
			}
		}
		private void drawCounter() {
			drawCounter++;
			if(drawCounter>=14){

				drawCounter=0;
			}
		}
		private Rect calculateRect(float x, float y, float nextx, float nexty) {

			Rect mRect = new Rect();
			if(x<=nextx&&y<=nexty)
				mRect.set((int)x, (int)y, (int)nextx, (int)nexty);		
			else if(x<=nextx&&nexty<y)
				mRect.set((int)x, (int)nexty, (int)nextx, (int)y);	
			else if(nextx<=x&&nexty<y)
				mRect.set((int)nextx, (int)nexty, (int)x, (int)y);	
			else if(nextx<=x&&y<nexty)
				mRect.set((int)nextx, (int)y, (int)x, (int)nexty);	

			//			mRect.inset(-SIZEFACTOR, -SIZEFACTOR);

			return  mRect;

		}   
		public boolean floodFill(int x, int y,int w, int maxQueue) {
			floodFill_queueCounter=0;
			int orgc=0;
			if(ba!=null)
				ba=null;
			System.gc();

			ba = new Boolean[mBitmap.getWidth()][mBitmap.getHeight()];			
			for(int x_=0;x_<mBitmap.getWidth();x_++){
				for(int y_=0;y_<mBitmap.getHeight();y_++){
					ba[x_][y_]=false;
				}
			}

			try{
				orgc= zBitmap.getPixel(x, y);
			}
			catch(Exception e){
				return true;
			}			

			ba[x][y]=true;		
			if(floodFillEngine(x,y,orgc,w, maxQueue))
			{
				for(int x_=0;x_<mBitmap.getWidth();x_++){
					for(int y_=0;y_<mBitmap.getHeight();y_++){
						if(ba[x_][y_])
							mCanvas.drawPoint(x_, y_, mPaint);
					}
				}

				return true;
			}
			else{
				return false;
			}
		}
		private boolean floodFillEngine(int x, int y,int orgc,int w, int maxQueue){
			Boolean f=true;
			list = new ArrayList<Coordinate>();
			list.add(new Coordinate(x,y));
			while(list.size()>0){		
				if(this.floodFill_queueCounter>maxQueue){					
					return false;
				}
				f=true;
				if(floodfillCheck(x+w,y,orgc)){
					if(f){
						f=false;				
						x=x+w;;
						ba[x][y]=true;
					}	
				}

				if(floodfillCheck(x-w,y,orgc)){

					if(f){	
						f=false;
						x=x-w;
						ba[x][y]=true;
					}
					else{
						if(floodFillCheckQueue(x,y))
							list.add(new Coordinate(x-w,y));						
					}
				}

				if(floodfillCheck(x,y+w,orgc)){				

					if(f){							
						f=false;
						y=y+w;
						ba[x][y]=true;
					}
					else{
						if(floodFillCheckQueue(x,y))
							list.add(new Coordinate(x,y+w));						
					}
				}


				if(floodfillCheck(x,y-w,orgc)){

					if(f){						
						f=false;
						y=y-w;
						ba[x][y]=true;
					}
					else{
						if(floodFillCheckQueue(x,y))
							list.add(new Coordinate(x,y-w));

					}

				}
				if(f){

					x=(int) list.get(0).x;
					y=(int) list.get(0).y;
					list.remove(0);							
				}	
			}
			return true;
		}	
		private int checkFloodFillPotentialSize(int x, int y){
			int summed =0;
			int a=0;
			try{
				int org =zBitmap.getPixel(x, y);

				for(a=0;a<mBitmap.getWidth();a++){
					try{
						if(org!=zBitmap.getPixel(x+a, y))
							break;
					}
					catch(Exception e){
						break;
					}
				}
				summed = summed+a;	

				for(a=0;a<mBitmap.getWidth();a++){
					try{
						if(org!=zBitmap.getPixel(x-a, y))
							break;
					}
					catch(Exception e){
						break;
					}
				}
				summed = summed+a;	

				for(a=0;a<mBitmap.getHeight();a++){
					try{
						if(org!=zBitmap.getPixel(x, y+a))
							break;
					}
					catch(Exception e){
						break;
					}
				}
				summed = summed+a;	

				for(a=0;a<mBitmap.getHeight();a++){
					try{
						if(org!=zBitmap.getPixel(x, y-a))
							break;
					}
					catch(Exception e){
						break;
					}
				}
				summed = summed+a;	
			}
			catch(Exception e){

			}
			try{
				return (mBitmap.getHeight()*mBitmap.getWidth())/summed;
			}
			catch(Exception e){
				return 0;
			}
		}



		private boolean floodFillCheckQueue(int x, int y){
			if( Math.abs(list.get(list.size()-1).y-y)==1 && Math.abs(list.get(list.size()-1).x-x)==1){
				return false;
			}

			floodFill_queueCounter++;
			return true;
		}



		private boolean floodfillCheck(int x, int y,int orgc){
			try{

				if(orgc==zBitmap.getPixel(x, y)){
					try{
						if(!ba[x][y])
							return true;
						else
							return false;

					}
					catch(Exception e){
						return false;
					}
				}
				else{
					return false;
				}

			}
			catch(Exception e){		
				return false;
			}
		}
		public void run() {
			usePickedColor(); 
			floodfillDone=false;
			//						if(checkFloodFillPotentialSize((int)coord_x,(int)coord_y)<400&&mContext.screensize>2){
			//							mPaint.setStrokeWidth(12);         
			//							floodFill((int) coord_x, (int) coord_y,10,100000);
			//						}         		
			//						else if(checkFloodFillPotentialSize((int)coord_x,(int)coord_y)<700&&mContext.screensize>2){
			//							mPaint.setStrokeWidth(7);         
			//							floodFill((int) coord_x, (int) coord_y,5,100000);
			//						}
			//						else{
			mPaint.setStrokeWidth(1);         
			if(!floodFill((int) newCord.x, (int) newCord.y,1,25000)){
				mPaint.setStrokeWidth(2);         
				if(!floodFill((int) newCord.x, (int) newCord.y,2,25000)){
					mPaint.setStrokeWidth(5);
					floodFill((int)newCord.x, (int)newCord.y,4,100000);
				}       			
			}
			//			}
			//      		mCanvas.drawBitmap(patternBitmap, new Matrix(), mPaint); 	
			handler.sendEmptyMessage(0);    		
		}
	}
	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {     	
			if(mNTouchPaintFloodFillProgressListener!=null)
				mNTouchPaintFloodFillProgressListener.stopProgress();

			mNTouchPaintView.thread.interrupt();
			mNTouchPaintView.thread = null;    
			mNTouchPaintView.invalidate();
			mNTouchPaintView.floodfillDone=true;
		}
	};
}

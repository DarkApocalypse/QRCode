package fr.jcwebinfo.qrcode;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;

public class Overlay extends View {

	public static interface DrawInstruction{
		public void draw(Canvas canvas);
	}
	public static class DrawPoint implements DrawInstruction{
		int x,y;
		Color c;
		public DrawPoint(int x, int y, Color c){
			this.x = x;
			this.y = y;
			this.c = c;
		}
		public void draw(Canvas canvas){
			Paint p = new Paint();
			p.setStrokeWidth(0);
			p.setColor(this.c.toArgb());
			canvas.drawPoint(this.x, this.y, p);
		}
	}
	public static class DrawBitmap implements DrawInstruction{
		int x,y;
		Bitmap bitmap;
		public DrawBitmap(int x, int y, Bitmap bitmap){
			this.x=x;
			this.y=y;
			this.bitmap=bitmap;
		}

		@Override
		public void draw(Canvas canvas) {
			canvas.drawBitmap(this.bitmap, this.x,this.y, new Paint());
		}
	}
	public static class DrawFill implements DrawInstruction {
		Color c;
		public DrawFill(Color c) {
			this.c = c;
		}

		@Override
		public void draw(Canvas canvas) {
			canvas.drawColor(c.toArgb());
		}
	}
	public static class DrawRect implements DrawInstruction {
		DetectorThread.Point point0;
		DetectorThread.Point point1;
		DetectorThread.Point point2;
		DetectorThread.Point point3;
		Color c;
		public DrawRect(DetectorThread.Point point0, DetectorThread.Point point1, DetectorThread.Point point2, DetectorThread.Point point3, Color c) {
			this.point0 = point0;
			this.point1 = point1;
			this.point2 = point2;
			this.point3 = point3;
			this.c = c;
		}

		@Override
		public void draw(Canvas canvas) {
			Paint p = new Paint();
			p.setStrokeWidth(0);
			p.setColor(this.c.toArgb());
			canvas.drawLine(
					point0.x,
					point0.y,
					point1.x,
					point1.y,
					p
			);
			canvas.drawLine(
					point1.x,
					point1.y,
					point2.x,
					point2.y,
					p
			);
			canvas.drawLine(
					point2.x,
					point2.y,
					point3.x,
					point3.y,
					p
			);
			canvas.drawLine(
					point3.x,
					point3.y,
					point0.x,
					point0.y,
					p
			);
		}
	}
	private ArrayList<DrawInstruction> mDrawInst;
	private boolean reset;
	public Overlay(Context context) {
		super(context);
		init();
	}
	public Overlay(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	public Overlay(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private void init() {
		mDrawInst = new ArrayList<>();
		reset = false;
	}
	private void resetView(){
		/*
		if(reset)
			return;
		reset = true;
		invalidate();
		*/
	}

	public void clear(){
		mDrawInst.clear();
		resetView();
	}
	public void AddFill(Color c){
		mDrawInst.add(new DrawFill(c));
		resetView();
	}
	public void AddBitmap(int x, int y, Bitmap bitmap){
		mDrawInst.add(new DrawBitmap(x,y,bitmap));
		resetView();
	}
	public void AddRect(DetectorThread.Point point0, DetectorThread.Point point1, DetectorThread.Point point2, DetectorThread.Point point3, Color c) {
		mDrawInst.add(new DrawRect(
				point0,
				point1,
				point2,
				point3,
				c
		));
		resetView();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		reset = false;

		for(DrawInstruction di : mDrawInst){
			Log.i(MainActivity.TAG, "Draw instruction: " + di.getClass());
			di.draw(canvas);
		}
	}

}
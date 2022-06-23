package fr.jcwebinfo.qrcode;

import static java.lang.Integer.min;

import android.graphics.Bitmap;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.objdetect.QRCodeDetector;

import java.util.ArrayList;

public class DetectorThread extends Thread {
	public static class Point{
		public int x, y;
		public Point(double[] p){
			this.x = (int)p[0];
			this.y = (int)p[1];
		}
	}
	public static class Result{
		Point[] points;
		String tag;
		public Result(String tag, Point[] points){
			this.tag = tag;
			this.points = points;
		}
	}

	private Mat img;
	private Mat points;
	private QRCodeDetector detector;
	private ArrayList<Bitmap> bitmaps;
	private ArrayList<Result> results;
	private Boolean running;
	public DetectorThread(){
		img = new Mat();
		points = new Mat();
		detector = new QRCodeDetector();
		bitmaps = new ArrayList<>();
		results = new ArrayList<>();
		running = new Boolean(false);
	}

	public boolean post(Bitmap bitmap) {
		synchronized (bitmaps){
			if(bitmaps.size() > 0)
				return false;
			bitmaps.add(bitmap);
		}
		return true;
	}
	public ArrayList<Result> getResults(){
		synchronized (results){
			return (ArrayList<Result>) results.clone();
		}
	}
	@Override
	public void run() {
		boolean runningHere = true;
		synchronized (running){
			running = new Boolean(true);
		}
		Log.i(MainActivity.TAG, "Detector is running");
		while(runningHere){
			boolean newImg = false;
			synchronized (bitmaps) {
				if(bitmaps.size() > 0){
					Utils.bitmapToMat(bitmaps.get(0), img);
					bitmaps.remove(0);
					newImg = true;
				}
			}
			if(newImg) {
				ArrayList<String> res = new ArrayList<>();
				boolean found = detector.detectAndDecodeMulti(img, res, points);
				if (found) {
					Log.i(MainActivity.TAG, String.format("%d points, %d tags", points.rows(), res.size()));

					synchronized (results) {
						results.clear();
						int size = min(res.size(), points.rows());
						for (int i = 0; i < size; i++) {
							String tag = res.get(i);
							Point[] ps = new Point[4];
							try {
								ps[0] = new Point(points.get(i, 0));
								ps[1] = new Point(points.get(i, 1));
								ps[2] = new Point(points.get(i, 2));
								ps[3] = new Point(points.get(i, 3));
								results.add(new Result(tag, ps));
							}
							catch (Exception e){
								Log.e(MainActivity.TAG, "Error?!\n" + e.toString());
							}
						}
					}
				}
			}
			try {
				sleep(5);
			}
			catch (InterruptedException e) {
				e.printStackTrace();
				synchronized (running){
					running = new Boolean(false);
				}
			}
			synchronized (running){
				runningHere = running.booleanValue();
			}
		}
		Log.i(MainActivity.TAG, "Detector stopped!");
	}

	public void stopThread(){
		synchronized (this.running){
			this.running = false;
		}
		try {
			this.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
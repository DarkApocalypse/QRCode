package fr.jcwebinfo.qrcode;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.common.util.concurrent.ListenableFuture;
import org.opencv.android.OpenCVLoader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

	private static final String[] REQUIRED_PERMISSIONS = {
			Manifest.permission.CAMERA
	};
	private static String FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS";
	private static final int REQUEST_CODE_PERMISSIONS = 10;
	private ExecutorService cameraExecutor;
	private PreviewView viewFinder;
	static final String TAG = "OPENCVAPP";
	static {
		if (!OpenCVLoader.initDebug())
			Log.d(TAG, "Unable to load OpenCV");
		else
			Log.d(TAG, "OpenCV loaded");
	}

	private DetectorThread mDetectorThread;
	private Overlay mOverlay;
	private ImageAnalysis.Analyzer mAnalyzer = new ImageAnalysis.Analyzer() {
		@SuppressLint("UnsafeOptInUsageError")
		@Override
		public void analyze(@NonNull ImageProxy imageProxy) {
			imageProxy.close();

			if(mDetectorThread!=null) {
				mOverlay.clear();
				for (DetectorThread.Result res : mDetectorThread.getResults()) {
					Log.i(TAG, String.format("found Tag: %s", res.tag));

					mOverlay.AddRect(
							res.points[0],
							res.points[1],
							res.points[2],
							res.points[3],
							res.tag.equals("") ? Color.valueOf(255, 0, 0, 128) : Color.valueOf(0, 255, 0, 128)
					);
				}
				mOverlay.invalidate();
			}
			Bitmap bitmap = viewFinder.getBitmap();
			if(bitmap!=null){
				if(mDetectorThread!=null)
					mDetectorThread.post(bitmap);
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mDetectorThread = new DetectorThread();
		mDetectorThread.start();

		if (allPermissionsGranted()) {
			startCamera();
		} else {
			ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
		}

		viewFinder = findViewById(R.id.viewFinder);
		cameraExecutor = Executors.newSingleThreadExecutor();

		mOverlay = findViewById(R.id.overlay);
		mOverlay.setOnClickListener(view -> {
			Log.i(TAG, "click on overlay");
		});
	}

	private void startCamera() {
		ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
		cameraProviderFuture.addListener(new Runnable() {
			@Override
			public void run() {
				try{
					ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
					Preview preview = new Preview.Builder()
							.build();
					preview.setSurfaceProvider(viewFinder.getSurfaceProvider());

					cameraProvider.unbindAll();
					CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;


					ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
							.setTargetName("ImageAnalysis")
							.build();
					// Make the analysis idling resource non-idle, until a frame received.
					imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(MainActivity.this), mAnalyzer);

					cameraProvider.bindToLifecycle(MainActivity.this, cameraSelector, preview, imageAnalysis);
				}
				catch (Exception e){
					e.printStackTrace();
				}
			}
		}, ContextCompat.getMainExecutor(this));
	}

	private boolean allPermissionsGranted() {
		for(String it : REQUIRED_PERMISSIONS){
			if (ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED)
				return false;
		}
		return true;
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		if (requestCode == REQUEST_CODE_PERMISSIONS) {
			if (allPermissionsGranted()) {
				startCamera();
			} else {
				Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show();
				finish();
			}
		}
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
		cameraExecutor.shutdown();
		mDetectorThread.stopThread();
	}
}
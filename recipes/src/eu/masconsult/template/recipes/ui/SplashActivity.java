package eu.masconsult.template.recipes.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.google.analytics.tracking.android.EasyTracker;

import eu.masconsult.template.recipes.R;

public class SplashActivity extends Activity implements Runnable {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_splash);

		findViewById(android.R.id.content).postDelayed(this, 1000);
	}

	@Override
	public void run() {
		startActivity(new Intent(SplashActivity.this, RecipeItemListActivity.class));
		finish();
	}

	@Override
	protected void onStart() {
		super.onStart();
		EasyTracker.getInstance(this).activityStart(this);
	}

	@Override
	protected void onStop() {
		super.onStop();
		EasyTracker.getInstance(this).activityStop(this);
	}

}

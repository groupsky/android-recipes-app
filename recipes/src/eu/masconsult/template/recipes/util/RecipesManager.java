package eu.masconsult.template.recipes.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.robotoworks.mechanoid.ops.OperationResult;
import com.robotoworks.mechanoid.ops.OperationServiceListener;
import com.robotoworks.mechanoid.ops.Ops;

import eu.masconsult.template.recipes.R;
import eu.masconsult.template.recipes.RecipesApplication;
import eu.masconsult.template.recipes.ops.ImportRecipesOperation;
import eu.masconsult.template.recipes.prefs.RecipesPreferences;

public class RecipesManager {

	private static final String TAG = RecipesManager.class.getSimpleName();

	public static final String ACTION_START_LOADING_RECIPES = "startLoadingRecipes";
	public static final String ACTION_FINISH_LOADING_RECIPES = "finishLoadingRecipes";

	private Context context;

	private RecipesManager(Context context) {
		this.context = context;
	}

	private static RecipesManager instance;

	public static RecipesManager getInstance(Context context) {
		if (instance == null) {
			instance = new RecipesManager(context);
		}
		return instance;
	}

	private List<Integer> opIds = new ArrayList<Integer>();
	private OperationServiceListener operationServiceListener = new OperationServiceListener() {
		@Override
		public void onOperationAborted(int id, android.content.Intent intent, int reason,
				android.os.Bundle data) {
			finishLoading(id);
		};

		@Override
		public void onOperationStarting(int id, android.content.Intent intent,
				android.os.Bundle data) {
			startLoading(id);
		};

		@Override
		public void onOperationComplete(int id, OperationResult result) {
			Log.d(TAG, "Operation completed");
			finishLoading(id);
		}

		private void startLoading(int id) {
			if (opIds.contains(id) && !RecipesApplication.isLoadingRecipes) {
				Log.d(TAG, "Start loading recipes....");
				RecipesApplication.isLoadingRecipes = true;
				LocalBroadcastManager.getInstance(context).sendBroadcast(
						new Intent(ACTION_START_LOADING_RECIPES));
			}
		}

		private void finishLoading(int id) {
			if (opIds.contains(id)) {
				opIds.remove(opIds.indexOf(id));
				if (opIds.isEmpty()) {
					Log.d(TAG, "Finished loading recipes.");
					RecipesApplication.isLoadingRecipes = false;
					Ops.unbindListener(this);
					LocalBroadcastManager.getInstance(context).sendBroadcast(
							new Intent(ACTION_FINISH_LOADING_RECIPES));
				}
			}
		}
	};

	public void importRecipes() {
		Ops.bindListener(operationServiceListener);

		opIds = new ArrayList<Integer>();

		String remoteUrl = context.getString(R.string.recipes_url);

		if (!TextUtils.isEmpty(RecipesPreferences.getInstance().getNextUpdateUrl())) {
			remoteUrl = RecipesPreferences.getInstance().getNextUpdateUrl();
		}

		if (TextUtils.isEmpty(remoteUrl)) {
			boolean shouldSync = true;
			try {
				PackageManager pm = context.getPackageManager();
				ApplicationInfo appInfo = pm.getApplicationInfo(context.getPackageName(), 0);
				String appFile = appInfo.sourceDir;
				long installed = new File(appFile).lastModified(); // Epoch Time

				if (RecipesPreferences.getInstance().getFirstLaunchTimestamp() != installed) {
					Log.d(TAG, "First launch after installation. Should sync recipes");
					RecipesPreferences.getInstance().updateFirstLaunchTimestamp(installed);
				} else {
					Log.d(TAG, "Not first launch after installation. Skip sync recipes");
					shouldSync = false;
				}
			} catch (Exception e) {
				Log.e(TAG, "Error while reading install date", e);
			}

			if (shouldSync) {
				opIds.add(Ops.execute(ImportRecipesOperation.newIntent("recipes.json", true)));
				opIds.add(Ops.execute(ImportRecipesOperation.newIntent("recipes-keenan.json", true)));
				opIds.add(Ops.execute(ImportRecipesOperation.newIntent("recipes-1001.json", true)));
			}
		} else {
			opIds.add(Ops.execute(ImportRecipesOperation.newIntent(remoteUrl, false)));
		}
	}
}

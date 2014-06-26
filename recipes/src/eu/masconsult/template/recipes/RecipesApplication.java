// Generated on 2014-01-21 using generator-android 0.1.0
package eu.masconsult.template.recipes;

import android.app.Application;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.robotoworks.mechanoid.Mechanoid;

import eu.masconsult.template.recipes.service.NotificationService;
import eu.masconsult.template.recipes.util.RecipesManager;

public class RecipesApplication extends Application {

	public static RecipesApplication INSTANCE = null;

	public static final String TAG = "Recipes";
	public static final DisplayImageOptions defaultDisplayImageOptions = new DisplayImageOptions.Builder()
			.cacheInMemory(true).cacheOnDisc(true).build();

	public static boolean isLoadingRecipes = false;

	public RecipesApplication() {
		if (INSTANCE == null) {
			INSTANCE = this;
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();

		Mechanoid.init(this);

		ImageLoader.getInstance().init(
				new ImageLoaderConfiguration.Builder(this).defaultDisplayImageOptions(
						defaultDisplayImageOptions).build());

		RecipesManager.getInstance(this).importRecipes();

		startService(NotificationService.getIntentForAlarm(this));
	}

}
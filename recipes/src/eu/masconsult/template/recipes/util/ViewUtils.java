package eu.masconsult.template.recipes.util;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import eu.masconsult.template.recipes.Constants;
import eu.masconsult.template.recipes.R;
import android.app.Activity;
import android.widget.ImageView;

public class ViewUtils implements Constants {

	private static final DisplayImageOptions options = new DisplayImageOptions.Builder()
	.cacheInMemory(true)
	.cacheOnDisc(true)
	.resetViewBeforeLoading(true)
	.showImageForEmptyUri(R.drawable.empty)
	.build();

	public static void displayImageFromAssets(final Activity context,
			final ImageView imageView, final String imageUrl) {
		ImageLoader.getInstance().displayImage(
				imageUrl, imageView, options);
	}

}

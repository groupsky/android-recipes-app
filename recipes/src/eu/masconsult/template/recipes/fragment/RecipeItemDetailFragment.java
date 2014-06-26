package eu.masconsult.template.recipes.fragment;

import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.Tracker;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.robotoworks.mechanoid.db.SQuery;
import com.robotoworks.mechanoid.db.SQuery.Op;

import eu.masconsult.template.recipes.Constants;
import eu.masconsult.template.recipes.R;
import eu.masconsult.template.recipes.content.IngredientsRecord;
import eu.masconsult.template.recipes.content.RecipesDBContract.Ingredients;
import eu.masconsult.template.recipes.content.RecipesRecord;
import eu.masconsult.template.recipes.ui.RecipeItemDetailActivity;
import eu.masconsult.template.recipes.ui.RecipeItemListActivity;
import eu.masconsult.template.recipes.util.ViewUtils;

/**
 * A fragment representing a single RecipeItem detail screen. This fragment is either contained in a
 * {@link RecipeItemListActivity} in two-pane mode (on tablets) or a
 * {@link RecipeItemDetailActivity} on handsets.
 */
public class RecipeItemDetailFragment extends Fragment implements Constants {

	public static Fragment create(long itemId) {
		Bundle arguments = new Bundle();
		arguments.putLong(RecipeItemDetailFragment.ARG_ITEM_ID, itemId);
		RecipeItemDetailFragment fragment = new RecipeItemDetailFragment();
		fragment.setArguments(arguments);
		return fragment;
	}

	/**
	 * The fragment argument representing the item ID that this fragment represents.
	 */
	public static final String ARG_ITEM_ID = "item_id";
	private RecipesRecord recipesRecord;
	private AdView adView;
	private String ingredientsString;

	/**
	 * The dummy content this fragment is presenting.
	 */

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the fragment (e.g. upon
	 * screen orientation changes).
	 */
	public RecipeItemDetailFragment() {
		setHasOptionsMenu(true);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getArguments().containsKey(ARG_ITEM_ID)) {
			// Load the dummy content specified by the fragment
			// arguments. In a real-world scenario, use a Loader
			// to load content from a content provider.
			long recipeId = getArguments().getLong(ARG_ITEM_ID);
			recipesRecord = RecipesRecord.get(recipeId);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_recipeitem_detail, container, false);

		final TextView nameView = (TextView) rootView.findViewById(R.id.recipe_name);
		final TextView prepTimeView = (TextView) rootView.findViewById(R.id.recipe_prep_time);
		final TextView cookTimeView = (TextView) rootView.findViewById(R.id.recipe_cook_time);
		final TextView totalTimeView = (TextView) rootView.findViewById(R.id.recipe_total_time);
		final TextView servesView = (TextView) rootView.findViewById(R.id.recipe_serves);
		final TextView summaryView = (TextView) rootView.findViewById(R.id.recipe_summary);
		final TextView ingredientsView = (TextView) rootView.findViewById(R.id.recipe_ingredients);
		final TextView directionsView = (TextView) rootView.findViewById(R.id.recipe_directions);
		final ImageView imageView = (ImageView) rootView.findViewById(R.id.recipe_image);

		if (recipesRecord != null) {
			nameView.setText(recipesRecord.getName());
			getActivity().setTitle(recipesRecord.getName());
			if (getResources().getBoolean(R.bool.show_prep_time)) {
				prepTimeView.setText(recipesRecord.getPrepTime() + " " + getString(R.string.min));
			} else {
				rootView.findViewById(R.id.recipe_prep_time_layout).setVisibility(View.GONE);
			}
			if (getResources().getBoolean(R.bool.show_cook_time)) {
				cookTimeView.setText(recipesRecord.getCookTime() + " " + getString(R.string.min));
			} else {
				rootView.findViewById(R.id.recipe_cook_time_layout).setVisibility(View.GONE);
			}
			if (getResources().getBoolean(R.bool.show_total_time)) {
				totalTimeView.setText(recipesRecord.getTotalTime() + " " + getString(R.string.min));
			} else {
				rootView.findViewById(R.id.recipe_total_time_layout).setVisibility(View.GONE);
			}
			if (getResources().getBoolean(R.bool.show_servings)) {
				servesView.setText(recipesRecord.getServes() + "");
			} else {
				rootView.findViewById(R.id.recipe_servings_layout).setVisibility(View.GONE);
			}
			if (!TextUtils.isEmpty(recipesRecord.getSummary())) {
				summaryView.setText(recipesRecord.getSummary());
			} else {
				rootView.findViewById(R.id.recipe_summary_layout).setVisibility(View.GONE);
			}
			directionsView.setText(recipesRecord.getDirections());

			List<IngredientsRecord> ingredients = SQuery.newQuery()
					.expr(Ingredients.RECIPE_ID, Op.EQ, recipesRecord.getId())
					.select(Ingredients.CONTENT_URI);

			StringBuilder sb = new StringBuilder();
			for (IngredientsRecord ingredient : ingredients) {
				sb.append(ingredient.getIngredient());
				sb.append("\n");
			}

			ingredientsString = sb.toString();

			ingredientsView.setText(sb.toString());

			ViewUtils.displayImageFromAssets(getActivity(), imageView, recipesRecord.getImage());
		}

		if (!TextUtils.isEmpty(getString(R.string.admob_unit_id))) {
			adView = new AdView(getActivity());
			adView.setAdUnitId(getString(R.string.admob_unit_id));
			((LinearLayout) rootView.findViewById(R.id.recipeitem_detail_container))
					.addView(adView);

			final AdRequest adRequest = new AdRequest.Builder() //
					.addTestDevice(AdRequest.DEVICE_ID_EMULATOR) // Emulator
					.build();

			adView.post(new Runnable() {
				@Override
				public void run() {
					adView.setAdSize(new AdSize((int) (adView.getWidth() / getResources()
							.getDisplayMetrics().density), AdSize.SMART_BANNER.getHeight()));
					adView.loadAd(adRequest);
				}
			});

		}
		return rootView;
	}

	@Override
	public void onStart() {
		super.onStart();
		final Tracker tracker = EasyTracker.getInstance(getActivity());
		tracker.set(Fields.SCREEN_NAME, "Recipe Details");
		tracker.send(MapBuilder.createAppView().build());
	}

	@Override
	public void onPause() {
		if (adView != null) {
			adView.pause();
		}
		super.onPause();
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		MenuItem findItem = menu.findItem(R.id.menu_favorites);
		if (recipesRecord.getFavorite()) {
			findItem.setIcon(R.drawable.ic_action_start_on);
		} else {
			findItem.setIcon(R.drawable.ic_action_start_off);
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.details_menu, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.menu_favorites) {
			recipesRecord.setFavorite(!recipesRecord.getFavorite());
			getActivity().supportInvalidateOptionsMenu();
			recipesRecord.update(false);
			if (recipesRecord.getFavorite()) {
				EasyTracker.getInstance(getActivity()).send(
						MapBuilder.createEvent("recipe details", "add", "favorites", 1l).build());
			} else {
				EasyTracker.getInstance(getActivity())
						.send(MapBuilder.createEvent("recipe details", "remove", "favorites", 1l)
								.build());
			}
			return true;
		} else if (item.getItemId() == R.id.menu_share) {
			shareRecipe();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onResume() {
		super.onResume();
		if (adView != null) {
			adView.resume();
		}
	}

	@Override
	public void onDestroy() {
		if (adView != null) {
			adView.destroy();
		}
		super.onDestroy();
	}

	private void shareRecipe() {
		Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
		sharingIntent.setType("text/plain");
		sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, recipesRecord.getName());
		sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, generateShareText());
		getActivity().startActivity(
				Intent.createChooser(sharingIntent, getActivity().getString(R.string.share_via)));
		EasyTracker.getInstance(getActivity()).send(
				MapBuilder.createEvent("recipe details", "share", "share", 1l).build());
	}

	private String generateShareText() {
		StringBuilder sb = new StringBuilder();

		sb.append(recipesRecord.getName());
		sb.append("\n");
		sb.append("\n");
		sb.append("\n");
		if (!TextUtils.isEmpty(recipesRecord.getSummary())) {
			sb.append(getString(R.string.summary));
			sb.append("\n");
			sb.append("\n");
			sb.append(recipesRecord.getSummary());
			sb.append("\n");
			sb.append("\n");
			sb.append("\n");
		}
		sb.append(getString(R.string.ingredients));
		sb.append("\n");
		sb.append("\n");
		sb.append(ingredientsString);
		sb.append("\n");
		sb.append("\n");
		sb.append(getString(R.string.directions));
		sb.append("\n");
		sb.append("\n");
		sb.append(recipesRecord.getDirections());
		sb.append("\n");
		sb.append("\n");
		sb.append("\n");
		sb.append("Shared via " + getString(R.string.app_name)
				+ "(http://play.google.com/store/apps/details?id="
				+ getActivity().getApplicationInfo().packageName
				+ "&referrer=utm_source%3Dshare&recipe=" + recipesRecord.getId() + ")");

		return sb.toString();
	}
}

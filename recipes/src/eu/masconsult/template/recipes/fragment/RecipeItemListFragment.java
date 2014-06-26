package eu.masconsult.template.recipes.fragment;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.Tracker;
import com.robotoworks.mechanoid.db.SQuery;

import eu.masconsult.template.recipes.Constants;
import eu.masconsult.template.recipes.R;
import eu.masconsult.template.recipes.RecipesApplication;
import eu.masconsult.template.recipes.content.RecipesDBContract.Recipes;
import eu.masconsult.template.recipes.content.RecipesDBContract.SearchWithRecipe;
import eu.masconsult.template.recipes.util.RecipesManager;
import eu.masconsult.template.recipes.util.ViewUtils;

/**
 * A list fragment representing a list of RecipeItems. This fragment also supports tablet devices by
 * allowing list items to be given an 'activated' state upon selection. This helps indicate which
 * item is currently being viewed in a {@link RecipeItemDetailFragment}.
 * <p>
 * Activities containing this fragment MUST implement the {@link Callbacks} interface.
 */
public class RecipeItemListFragment extends ListFragment implements LoaderCallbacks<Cursor>,
		Constants {

	private static final String[] COLUMNS = { Recipes.NAME, Recipes.PREP_TIME, Recipes.COOK_TIME,
			Recipes.IMAGE, Recipes.TOTAL_TIME };

	private static final int[] VIEW_IDS = { R.id.recipe_name, R.id.recipe_prep_time,
			R.id.recipe_cook_time, R.id.recipe_image, R.id.recipe_total_time };

	public static String[] PROJECTION = { SearchWithRecipe._ID, SearchWithRecipe.NAME,
			SearchWithRecipe.IMAGE, SearchWithRecipe.PREP_TIME, SearchWithRecipe.COOK_TIME,
			SearchWithRecipe.CATEGORY, SearchWithRecipe.FAVORITE, SearchWithRecipe.TOTAL_TIME };

	/**
	 * The serialization (saved instance state) Bundle key representing the activated item position.
	 * Only used on tablets.
	 */
	private static final String STATE_ACTIVATED_POSITION = "activated_position";

	/**
	 * The fragment's current callback object, which is notified of list item clicks.
	 */
	private Callbacks mCallbacks = sDummyCallbacks;

	/**
	 * The current activated item position. Only used on tablets.
	 */
	private int mActivatedPosition = ListView.INVALID_POSITION;

	private SimpleCursorAdapter adapter;

	private String searchQuery;

	private List<Long> recipeIds;

	private long recipeId;
	private String category;

	private boolean showFavorites = false;

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the fragment (e.g. upon
	 * screen orientation changes).
	 */
	public RecipeItemListFragment() {
	}

	/**
	 * A callback interface that all activities containing this fragment must implement. This
	 * mechanism allows activities to be notified of item selections.
	 */
	public interface Callbacks {
		/**
		 * Callback for when an item has been selected.
		 */
		public void onItemSelected(long id);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}

	@Override
	public void onStart() {
		super.onStart();
		final Tracker tracker = EasyTracker.getInstance(getActivity());
		tracker.set(Fields.SCREEN_NAME, "Recipes list");
		tracker.send(MapBuilder.createAppView().build());
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(RecipesManager.ACTION_FINISH_LOADING_RECIPES);
		intentFilter.addAction(RecipesManager.ACTION_START_LOADING_RECIPES);

		if (RecipesApplication.isLoadingRecipes) {
			getActivity().setProgressBarIndeterminateVisibility(true);
		} else {
			getActivity().setProgressBarIndeterminateVisibility(false);
		}
		LocalBroadcastManager.getInstance(getActivity()).registerReceiver(broadcastReceiver,
				intentFilter);
	}

	@Override
	public void onStop() {
		LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(broadcastReceiver);
		super.onStop();
	}

	private void createAdapter() {
		if (adapter == null) {
			adapter = new SimpleCursorAdapter(getActivity(), R.layout.list_item_recipe, null,
					COLUMNS, VIEW_IDS, 0);
			setListAdapter(adapter);
			adapter.setViewBinder(new RecipeViewBinder());
		}
	}

	@SuppressLint("NewApi")
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		// Restore the previously serialized activated item position.
		if (savedInstanceState != null && savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
			setActivatedPosition(savedInstanceState.getInt(STATE_ACTIVATED_POSITION));
		}
		setEmptyText(getActivity().getString(R.string.no_data));
		TextView emptyView = (TextView) getListView().getEmptyView();
		emptyView.setTextAppearance(getActivity(), android.R.style.TextAppearance_Medium);
		getListView().setDrawSelectorOnTop(true);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
			getListView().setOverScrollMode(ListView.OVER_SCROLL_NEVER);
		}

		getLoaderManager().restartLoader(0, null, this);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		// Activities containing this fragment must implement its callbacks.
		if (!(activity instanceof Callbacks)) {
			throw new IllegalStateException("Activity must implement fragment's callbacks.");
		}

		mCallbacks = (Callbacks) activity;
	}

	@Override
	public void onDetach() {
		super.onDetach();

		// Reset the active callbacks interface to the dummy implementation.
		mCallbacks = sDummyCallbacks;
	}

	@Override
	public void onListItemClick(ListView listView, View view, int position, long id) {
		super.onListItemClick(listView, view, position, id);

		// Notify the active callbacks interface (the activity, if the
		// fragment is attached to one) that an item has been selected.
		mCallbacks.onItemSelected(id);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mActivatedPosition != ListView.INVALID_POSITION) {
			// Serialize and persist the activated item position.
			outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
		}
	}

	/**
	 * Turns on activate-on-click mode. When this mode is on, list items will be given the
	 * 'activated' state when touched.
	 */
	public void setActivateOnItemClick(boolean activateOnItemClick) {
		// When setting CHOICE_MODE_SINGLE, ListView will automatically
		// give items the 'activated' state when touched.
		getListView().setChoiceMode(
				activateOnItemClick ? ListView.CHOICE_MODE_SINGLE : ListView.CHOICE_MODE_NONE);
	}

	private void setActivatedPosition(int position) {
		if (position == ListView.INVALID_POSITION) {
			getListView().setItemChecked(mActivatedPosition, false);
		} else {
			getListView().setItemChecked(position, true);
		}

		mActivatedPosition = position;
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		SQuery newQuery = SQuery.newQuery();

		Uri uri;

		String query = " 1 ";

		if (!TextUtils.isEmpty(searchQuery)) {
			String q = "%" + searchQuery + "%";
			query += " AND " + SearchWithRecipe.STRING + "  like " + "'" + q + "'";
			uri = SearchWithRecipe.CONTENT_URI;
		} else {
			uri = Recipes.CONTENT_URI;
		}

		if (showFavorites) {
			if (query.contains("where")) {
				query += " AND ";
			} else {
				// searchQuery += " WHERE ";
			}
			query += " AND " + Recipes.FAVORITE + " = 1 ";
		} else {
			if (!TextUtils.isEmpty(category)) {
				query += " AND " + SearchWithRecipe.CATEGORY + "='" + category + "'";
			}
		}

		query += "GROUP BY _id";
		CursorLoader createSupportLoader = newQuery.append(query).createSupportLoader(uri,
				PROJECTION, SearchWithRecipe.NAME + " ASC");

		return createSupportLoader;
	}

	public void showRecipesFromCategory(String category) {
		this.category = category;
		showFavorites = false;
		getLoaderManager().restartLoader(0, null, this);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		createAdapter();
		if (recipeIds == null) {
			recipeIds = new ArrayList<Long>();
		}

		if (data.moveToFirst()) {
			recipeIds.clear();
			do {
				recipeIds.add(data.getLong(data.getColumnIndex(Recipes._ID)));
			} while (data.moveToNext());
			data.moveToFirst();
			if (recipeId > 0) {
				setSelectionById(recipeId);
				recipeId = 0;
			}
		}

		adapter.swapCursor(data);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		createAdapter();
		adapter.swapCursor(null);
	}

	private final class RecipeViewBinder implements ViewBinder {
		@Override
		public boolean setViewValue(final View view, final Cursor cursor, final int columnIndex) {
			if (view.getId() == R.id.recipe_image) {
				ViewUtils.displayImageFromAssets(getActivity(), (ImageView) view,
						cursor.getString(columnIndex));
				return true;

			}
			if (view.getId() == R.id.recipe_prep_time) {
				if (getResources().getBoolean(R.bool.show_prep_time)) {
					((TextView) view).setText(cursor.getLong(columnIndex) + " "
							+ getString(R.string.min));
					((View) view.getParent()).setVisibility(View.VISIBLE);
				} else {
					((View) view.getParent()).setVisibility(View.GONE);
				}
				return true;
			}

			if (view.getId() == R.id.recipe_cook_time) {
				if (getResources().getBoolean(R.bool.show_cook_time)) {
					((TextView) view).setText(cursor.getLong(columnIndex) + " "
							+ getString(R.string.min));
					((View) view.getParent()).setVisibility(View.VISIBLE);
				} else {
					((View) view.getParent()).setVisibility(View.GONE);
				}
				return true;
			}

			if (view.getId() == R.id.recipe_total_time) {
				if (getResources().getBoolean(R.bool.show_total_time)) {
					((TextView) view).setText(cursor.getLong(columnIndex) + " "
							+ getString(R.string.min));
					((View) view.getParent()).setVisibility(View.VISIBLE);
				} else {
					((View) view.getParent()).setVisibility(View.GONE);
				}
				return true;
			}
			return false;
		}
	}

	/**
	 * A dummy implementation of the {@link Callbacks} interface that does nothing. Used only when
	 * this fragment is not attached to an activity.
	 */
	private static Callbacks sDummyCallbacks = new Callbacks() {
		@Override
		public void onItemSelected(long id) {
		}
	};

	public void setSelectionById(long recipeId) {
		this.recipeId = recipeId;
		if (recipeIds != null) {
			setSelection(recipeIds.indexOf(recipeId));
		}
	}

	public void showFavorites() {
		showFavorites = true;
		getLoaderManager().restartLoader(0, null, this);
	}

	public void setQuery(String query) {
		searchQuery = query;
		getLoaderManager().restartLoader(0, null, this);
	}

	BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (RecipesManager.ACTION_START_LOADING_RECIPES.equals(intent.getAction())) {
				RecipeItemListFragment.this.getActivity().setProgressBarIndeterminateVisibility(
						true);
			} else if (RecipesManager.ACTION_FINISH_LOADING_RECIPES.equals(intent.getAction())) {
				RecipeItemListFragment.this.getActivity().setProgressBarIndeterminateVisibility(
						false);
				getLoaderManager().restartLoader(0, null, RecipeItemListFragment.this);
			}
		}

	};

}

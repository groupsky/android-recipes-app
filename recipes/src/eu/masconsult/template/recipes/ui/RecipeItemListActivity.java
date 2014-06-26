package eu.masconsult.template.recipes.ui;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.MenuItemCompat.OnActionExpandListener;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.OnNavigationListener;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SearchView.OnSuggestionListener;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;

import com.commonsware.cwac.merge.MergeSpinnerAdapter;
import com.robotoworks.mechanoid.db.SQuery;

import eu.masconsult.template.recipes.R;
import eu.masconsult.template.recipes.content.CategoriesRecord;
import eu.masconsult.template.recipes.content.RecipesDBContract.Categories;
import eu.masconsult.template.recipes.content.SearchRecipeSuggestionsProvider;
import eu.masconsult.template.recipes.fragment.RecipeItemDetailFragment;
import eu.masconsult.template.recipes.fragment.RecipeItemListFragment;
import eu.masconsult.template.recipes.service.NotificationService;

/**
 * An activity representing a list of RecipeItems. This activity has different presentations for
 * handset and tablet-size devices. On handsets, the activity presents a list of items, which when
 * touched, lead to a {@link RecipeItemDetailActivity} representing item details. On tablets, the
 * activity presents the list of items and item details side-by-side using two vertical panes.
 * <p>
 * The activity makes heavy use of fragments. The list of items is a {@link RecipeItemListFragment}
 * and the item details (if present) is a {@link RecipeItemDetailFragment}.
 * <p>
 * This activity also implements the required {@link RecipeItemListFragment.Callbacks} interface to
 * listen for item selections.
 */
public class RecipeItemListActivity extends ActionBarActivity implements
		RecipeItemListFragment.Callbacks, OnNavigationListener, LoaderCallbacks<Cursor>,
		OnActionExpandListener, OnSuggestionListener {

	private static final String EXTRA_RECIPE_ID = "recipeID";

	private static final int FAVORITES_ITEM_INDEX = 1;
	private static final int ALL_RECIPES_ITEM_INDEX = 0;

	/**
	 * Whether or not the activity is in two-pane mode, i.e. running on a tablet device.
	 */
	private boolean mTwoPane;
	private SimpleCursorAdapter categoriesAdapter;
	private RecipeItemListFragment recipeListFragment;
	private SearchView searchView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

		getSupportActionBar().setDisplayShowTitleEnabled(false);
		getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		setContentView(R.layout.activity_recipeitem_list);
		recipeListFragment = (RecipeItemListFragment) getSupportFragmentManager().findFragmentById(
				R.id.recipeitem_list);
		setUpCategoriesSpinner();
		if (findViewById(R.id.recipeitem_detail_container) != null) {
			// The detail container view will be present only in the
			// large-screen layouts (res/values-large and
			// res/values-sw600dp). If this view is present, then the
			// activity should be in two-pane mode.
			mTwoPane = true;

			// In two-pane mode, list items should be given the
			// 'activated' state when touched.
			recipeListFragment.setActivateOnItemClick(true);
			((RecipeItemListFragment) getSupportFragmentManager().findFragmentById(
					R.id.recipeitem_list)).setActivateOnItemClick(true);

			if (getIntent().hasExtra(EXTRA_RECIPE_ID) && savedInstanceState == null) {
				((RecipeItemListFragment) getSupportFragmentManager().findFragmentById(
						R.id.recipeitem_list)).setSelectionById(getIntent().getLongExtra(
						EXTRA_RECIPE_ID, 0));
			}

		}

		if (getIntent().hasExtra(EXTRA_RECIPE_ID) && savedInstanceState == null) {
			startService(NotificationService.getIntentForAlarm(this));
			onItemSelected(getIntent().getLongExtra(EXTRA_RECIPE_ID, 0));
		}

	}

	@Override
	protected void onResume() {
		super.onResume();
		Fragment fragment = getSupportFragmentManager().findFragmentById(
				R.id.recipeitem_detail_container);
		if (fragment != null && fragment.isAdded()) {
			findViewById(R.id.detail_empty_view).setVisibility(View.GONE);
		}
	}

	private void setUpCategoriesSpinner() {
		String[] columns = new String[] { Categories.CATEGORY };
		getSupportLoaderManager().restartLoader(1, null, this);
		categoriesAdapter = new SimpleCursorAdapter(this, R.layout.spinner_category, null, columns,
				new int[] { android.R.id.text1 }, 0);

		ArrayAdapter<String> allRecipesAdapter = new ArrayAdapter<String>(this,
				R.layout.spinner_category, android.R.id.text1, new String[] {
						getString(R.string.all_recipes), getString(R.string.favorites) });
		allRecipesAdapter.setDropDownViewResource(R.layout.drop_down_item_category);
		categoriesAdapter.setDropDownViewResource(R.layout.drop_down_item_category);
		MergeSpinnerAdapter mergeAdapter = new MergeSpinnerAdapter();
		mergeAdapter.addAdapter(allRecipesAdapter);
		mergeAdapter.addAdapter(categoriesAdapter);

		getSupportActionBar().setListNavigationCallbacks(mergeAdapter, this);
	}

	/**
	 * Callback method from {@link RecipeItemListFragment.Callbacks} indicating that the item with
	 * the given ID was selected.
	 */
	@Override
	public void onItemSelected(long id) {
		if (mTwoPane) {
			// In two-pane mode, show the detail view in this activity by
			// adding or replacing the detail fragment using a
			// fragment transaction.

			findViewById(R.id.detail_empty_view).setVisibility(View.GONE);

			getSupportFragmentManager().beginTransaction()
					.replace(R.id.recipeitem_detail_container, RecipeItemDetailFragment.create(id))
					.commit();

		} else {
			// In single-pane mode, simply start the detail activity
			// for the selected item ID.
			Intent detailIntent = new Intent(this, RecipeItemDetailActivity.class);
			detailIntent.putExtra(RecipeItemDetailFragment.ARG_ITEM_ID, id);
			startActivity(detailIntent);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.action_search) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.search_menu, menu);
		setUpSearchView(menu);
		return super.onCreateOptionsMenu(menu);
	}

	private void setUpSearchView(Menu menu) {
		MenuItem menuItem = menu.findItem(R.id.action_search);
		searchView = (SearchView) MenuItemCompat.getActionView(menuItem);
		searchView.setIconifiedByDefault(false);
		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
		MenuItemCompat.setOnActionExpandListener(menuItem, this);
		searchView.setOnSuggestionListener(this);

	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			String query = intent.getStringExtra(SearchManager.QUERY);
			if (!TextUtils.isEmpty(query)) {
				SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
						SearchRecipeSuggestionsProvider.AUTHORITY,
						SearchRecipeSuggestionsProvider.MODE);
				suggestions.saveRecentQuery(query, null);

			}
			recipeListFragment.setQuery(query);
		}

	}

	@Override
	public boolean onNavigationItemSelected(int which, long id) {
		RecipeItemListFragment listFragment = (RecipeItemListFragment) getSupportFragmentManager()
				.findFragmentById(R.id.recipeitem_list);
		if (which == ALL_RECIPES_ITEM_INDEX) {
			listFragment.showRecipesFromCategory(null);
			return true;
		} else if (which == FAVORITES_ITEM_INDEX) {
			listFragment.showFavorites();
			return true;
		}

		Cursor item = (Cursor) categoriesAdapter.getItem(which - 2);
		String category = CategoriesRecord.fromCursor(item).getCategory();
		listFragment.showRecipesFromCategory(category);
		return true;
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return SQuery.newQuery().createSupportLoader(Categories.CONTENT_URI,
				CategoriesRecord.PROJECTION);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		categoriesAdapter.swapCursor(data);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		categoriesAdapter.swapCursor(null);
	}

	@Override
	public boolean onMenuItemActionExpand(MenuItem item) {
		return true;
	}

	@Override
	public boolean onMenuItemActionCollapse(MenuItem item) {
		recipeListFragment.setQuery(null);
		return true;
	}

	public static Intent newIntentForRecipe(Context context, long recipeId) {
		Intent intent = new Intent(context, RecipeItemListActivity.class);
		intent.putExtra(EXTRA_RECIPE_ID, recipeId);
		return intent;
	}

	@Override
	public boolean onSuggestionClick(int position) {
		CursorAdapter selectedView = searchView.getSuggestionsAdapter();
		Cursor cursor = (Cursor) selectedView.getItem(position);
		int index = cursor.getColumnIndexOrThrow(SearchManager.SUGGEST_COLUMN_TEXT_1);
		searchView.setQuery(cursor.getString(index), true);
		return true;
	}

	@Override
	public boolean onSuggestionSelect(int position) {
		return true;
	}
}

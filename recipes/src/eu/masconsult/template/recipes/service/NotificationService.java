package eu.masconsult.template.recipes.service;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.robotoworks.mechanoid.db.SQuery;

import eu.masconsult.template.recipes.R;
import eu.masconsult.template.recipes.content.RecipesDBContract.Recipes;
import eu.masconsult.template.recipes.content.RecipesRecord;
import eu.masconsult.template.recipes.ui.RecipeItemListActivity;

public class NotificationService extends IntentService {

	private static final String TAG = NotificationService.class.getSimpleName();

	private static final String ACTION_REGISTER_ALARM = "eu.masconsult.template.recipes.NotificationService.ACTION_REGISTER_ALARM";

	private static final String ACTION_DISPLAY_NOTIFICATION = "eu.masconsult.template.recipes.NotificationService.ACTION_DISPLAY_NOTIFICATION";

	private static final String ACTION_NOTIFICATION_DELETE = "eu.masconsult.template.recipes.NotificationService.ACTION_NOTIFICATION_DELETE";

	private NotificationManager notificationManager;
	private AlarmManager alarmManager;

	public NotificationService() {
		super(TAG);
	}

	@Override
	public void onCreate() {
		Log.d(TAG, "onCreate");
		super.onCreate();
		notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Log.d(TAG, String.format("onHandleIntent: %s", intent));
		if (ACTION_REGISTER_ALARM.equals(intent.getAction())) {
			registerAlarm();
		} else if (ACTION_DISPLAY_NOTIFICATION.equals(intent.getAction())) {
			displayNotification();
		} else if (ACTION_NOTIFICATION_DELETE.equals(intent.getAction())) {
			registerAlarm();
			EasyTracker.getInstance(this).send(
					MapBuilder.createEvent("notification", "dismiss", "recipe notification", 1l)
							.build());
		}
	}

	private void registerAlarm() {
		Log.d(TAG, "registerAlarm");

		int interval = getResources().getInteger(R.integer.notifications_interval);

		PendingIntent operation = PendingIntent.getService(this, 0, getIntentForNotification(this),
				0);
		alarmManager.cancel(operation);

		if (interval > 0) {
			long time = System.currentTimeMillis() + interval * 60 * 60 * 1000;
			Log.d(TAG, String.format("registerAlarm for %s", time));
			alarmManager.setInexactRepeating(AlarmManager.RTC, time,
					AlarmManager.INTERVAL_HALF_HOUR * interval, operation);
		}
	}

	private void displayNotification() {
		Log.d(TAG, "displayNotification");

		RecipesRecord record = SQuery.newQuery().selectFirst(Recipes.CONTENT_URI, "random()");

		Bitmap image = ImageLoader.getInstance().loadImageSync(record.getImage());

		Log.d(TAG, String.format("chosen %s for notification", record.getId()));

		Intent intent = RecipeItemListActivity.newIntentForRecipe(this, record.getId())
				.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
		NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
				.setAutoCancel(true)
				.setSmallIcon(R.drawable.ic_launcher)
				.setPriority(NotificationCompat.PRIORITY_LOW)
				.setLargeIcon(image)
				.setDeleteIntent(
						PendingIntent.getService(this, 0, getIntentForDelete(this),
								PendingIntent.FLAG_UPDATE_CURRENT))
				.setContentIntent(
						PendingIntent.getActivity(this, 0, intent,
								PendingIntent.FLAG_UPDATE_CURRENT))
				.setContentTitle(record.getName());

		NotificationCompat.BigPictureStyle bigStyle = new NotificationCompat.BigPictureStyle()
				.bigLargeIcon(
						((BitmapDrawable) getResources().getDrawable(R.drawable.ic_launcher))
								.getBitmap()).bigPicture(image);

		// Moves the big view style object into the notification object.
		builder.setStyle(bigStyle);

		// mId allows you to update the notification later on.
		Log.d(TAG, "displaying notification");
		notificationManager.notify(1, builder.build());

		EasyTracker.getInstance(this).send(
				MapBuilder.createEvent("notification", "display", "recipe notification", 1l)
						.build());
	}

	public static Intent getIntentForAlarm(Context context) {
		return new Intent(context, NotificationService.class).setAction(ACTION_REGISTER_ALARM);
	}

	public static Intent getIntentForNotification(Context context) {
		return new Intent(context, NotificationService.class)
				.setAction(ACTION_DISPLAY_NOTIFICATION);
	}

	public static Intent getIntentForDelete(Context context) {
		return new Intent(context, NotificationService.class).setAction(ACTION_NOTIFICATION_DELETE);
	}

}

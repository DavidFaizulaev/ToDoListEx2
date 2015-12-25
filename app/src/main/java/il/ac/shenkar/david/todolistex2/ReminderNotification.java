package il.ac.shenkar.david.todolistex2;

import android.content.Context;
import android.content.Intent;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;

/**
 * Created by David on 21-Dec-15.
 */

public class ReminderNotification extends BroadcastReceiver
{
    public ReminderNotification()
    {
        // TODO Auto-generated constructor stub
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // The PendingIntent to launch our activity if the user selects this notification
        Task task = (Task)intent.getSerializableExtra("task");
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, new Intent(context, MainActivity.class), 0);
        // Set the info for the views that show in the notification panel.

        Intent snzInt = new Intent(context, SnoozeReminderReceiver.class);
        snzInt.putExtra("task", task);
        PendingIntent snoozeIntent = PendingIntent.getBroadcast(context, 0,snzInt, PendingIntent.FLAG_CANCEL_CURRENT);

        Intent doneInt = new Intent(context,DoneActionReceiver.class);
        doneInt.putExtra("task", task);
        PendingIntent doneIntent = PendingIntent.getBroadcast(context, 0,doneInt, PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationManager notificationManager =
                (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
    }
}

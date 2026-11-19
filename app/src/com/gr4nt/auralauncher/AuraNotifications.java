package com.gr4nt.auralauncher;
import android.content.Intent;
import android.service.notification.*;

public final class AuraNotifications extends NotificationListenerService {
    static volatile AuraNotifications active;
    @Override public void onListenerConnected(){active=this;changed();}
    @Override public void onListenerDisconnected(){active=null;changed();}
    @Override public void onNotificationPosted(StatusBarNotification notification){changed();}
    @Override public void onNotificationRemoved(StatusBarNotification notification){changed();}
    void changed(){sendBroadcast(new Intent(getPackageName()+".NOTIFICATIONS_CHANGED").setPackage(getPackageName()));}
    static StatusBarNotification[] notifications(){AuraNotifications service=active;if(service==null)return new StatusBarNotification[0];try{StatusBarNotification[] items=service.getActiveNotifications();return items==null?new StatusBarNotification[0]:items;}catch(Exception e){return new StatusBarNotification[0];}}
}

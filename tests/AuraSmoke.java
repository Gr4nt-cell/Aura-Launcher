package com.gr4nt.auralauncher;
import android.app.*;
import android.content.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import org.json.*;
import java.util.*;
import java.io.*;
import android.graphics.Bitmap;

public final class AuraSmoke extends Instrumentation {
    int assertions=0;
    void stage(String message){Bundle status=new Bundle();status.putString("stream","STEP: "+message+"\n");sendStatus(0,status);}
    void check(boolean ok,String message){assertions++;if(!ok)throw new AssertionError(message);stage("Passed: "+message);}
    boolean contains(View view,String needle){if(view instanceof TextView&&((TextView)view).getText().toString().contains(needle))return true;if(view instanceof ViewGroup){ViewGroup g=(ViewGroup)view;for(int i=0;i<g.getChildCount();i++)if(contains(g.getChildAt(i),needle))return true;}return false;}
    boolean hasDescription(View view,String name){if(name.contentEquals(view.getContentDescription()==null?"":view.getContentDescription()))return true;if(view instanceof ViewGroup){ViewGroup group=(ViewGroup)view;for(int i=0;i<group.getChildCount();i++)if(hasDescription(group.getChildAt(i),name))return true;}return false;}
    void swipe(LauncherActivity a,float startX,float startY,float endX,float endY){long now=SystemClock.uptimeMillis();for(int action:new int[]{MotionEvent.ACTION_DOWN,MotionEvent.ACTION_MOVE,MotionEvent.ACTION_UP}){MotionEvent event=MotionEvent.obtain(now,now+(action==MotionEvent.ACTION_DOWN?0:120),action,action==MotionEvent.ACTION_DOWN?startX:endX,action==MotionEvent.ACTION_DOWN?startY:endY,0);a.dispatchTouchEvent(event);event.recycle();}}
    void screenshot(String name)throws Exception{stage("Capturing "+name);Thread.sleep(250);Bitmap bitmap=getUiAutomation().takeScreenshot();if(bitmap==null)throw new IOException("Screenshot unavailable");try(FileOutputStream out=new FileOutputStream(new File(getTargetContext().getExternalFilesDir(null),name))){bitmap.compress(Bitmap.CompressFormat.PNG,100,out);}bitmap.recycle();}
    @Override public void onCreate(Bundle arguments){super.onCreate(arguments);start();}
    @Override public void onStart(){Bundle report=new Bundle();try{stage("Starting Aura runtime checks");
        LauncherActivity activity=(LauncherActivity)startActivitySync(new Intent().setClassName(getTargetContext(),"com.gr4nt.auralauncher.LauncherActivity").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        for(int i=0;i<40;i++){Thread.sleep(100);waitForIdleSync();if(!activity.catalog.apps.isEmpty())break;}
        check(!activity.catalog.apps.isEmpty(),"Installed apps must be discovered");
        String pkg=activity.catalog.apps.get(0).pkg;
        String original=activity.store.backup(),originalLabel=activity.store.name(pkg,activity.catalog.apps.get(0).label);
        runOnMainSync(()->{activity.store.put("layout","phone");activity.render();});waitForIdleSync();check(hasDescription(activity.root,"Aura home"),"Phone Home is visible");
        runOnMainSync(()->{activity.store.put("layout","tablet");activity.render();});waitForIdleSync();check(activity.mode.equals("tablet"),"Tablet layout");
        runOnMainSync(()->{activity.store.put("layout","desktop");activity.render();});waitForIdleSync();check(contains(activity.root,"AURA")&&contains(activity.root,"Files"),"Desktop menu is visible");
        LauncherStore store=activity.store;store.rename(pkg,"Aura test label");check(store.name(pkg,"wrong").equals("Aura test label"),"Renaming persists");store.add("dock",pkg);check(new LauncherStore(activity).list("dock").contains(pkg),"Dock survives new store");
        String folder=store.folder("Test folder",Collections.singletonList(pkg));check(store.folderApps(folder).contains(pkg),"Folder content persists");
        String exported=store.backup();JSONObject backup=new JSONObject(exported);check(!backup.getJSONObject("settings").has("widgets"),"Device-specific widget IDs must not be exported");
        store.rename(pkg,"Changed");store.restore(exported);check(store.name(pkg,"").equals("Aura test label"),"Backup restores names");
        boolean rejected=false;try{store.restore("{\"app\":\"Other\",\"version\":1,\"settings\":{}}");}catch(Exception expected){rejected=true;}check(rejected,"Wrong backup format rejected");
        store.restore(original);check(store.name(pkg,activity.catalog.apps.get(0).label).equals(originalLabel),"Full restore removes later labels and folder settings");
        runOnMainSync(()->{activity.store.put("layout","phone");activity.render();activity.drawer(0);});waitForIdleSync();check(!activity.dialogs.isEmpty(),"App drawer opens");
        screenshot("Aura-drawer.png");
        runOnMainSync(()->{for(Dialog d:new ArrayList<>(activity.dialogs))d.dismiss();activity.store.put("home_mode","classic");activity.render();});waitForIdleSync();
        check(MobileHome.classic(activity)&&!MobileHome.pages(activity).isEmpty(),"Classic pages contain installed apps");
        runOnMainSync(()->{activity.store.put("home_mode","drawer");activity.store.add("recent",pkg);activity.render();activity.homeAction(GestureRules.Action.RECENTS);});waitForIdleSync();check(!activity.dialogs.isEmpty(),"Own recent-app carousel opens");screenshot("Aura-recents.png");
        runOnMainSync(()->{for(Dialog d:new ArrayList<>(activity.dialogs))d.dismiss();swipe(activity,activity.root.getWidth()*.8f,activity.dp(140),activity.root.getWidth()*.8f,activity.dp(240));});waitForIdleSync();check(!activity.dialogs.isEmpty(),"Right swipe opens Control Center");screenshot("Aura-control-center.png");
        runOnMainSync(()->{for(Dialog d:new ArrayList<>(activity.dialogs))d.dismiss();swipe(activity,activity.root.getWidth()*.2f,activity.dp(140),activity.root.getWidth()*.2f,activity.dp(240));});waitForIdleSync();check(!activity.dialogs.isEmpty(),"Left swipe opens Notifications");screenshot("Aura-notifications.png");
        check(!new JSONObject(activity.store.backup()).getJSONObject("settings").has("helper_consent"),"Backup cannot grant helper consent");
        runOnMainSync(()->{for(Dialog d:new ArrayList<>(activity.dialogs))d.dismiss();activity.glassSettings("dock");});waitForIdleSync();check(!activity.dialogs.isEmpty(),"Glass controls open");
        screenshot("Aura-glass.png");
        runOnMainSync(()->{for(Dialog d:new ArrayList<>(activity.dialogs))d.dismiss();activity.settings();});waitForIdleSync();check(!activity.dialogs.isEmpty(),"Settings opens");
        screenshot("Aura-settings.png");
        runOnMainSync(()->{for(Dialog d:new ArrayList<>(activity.dialogs))d.dismiss();activity.store.prefs.edit().putString("layout","auto").commit();activity.render();});
        report.putString("stream","PASS: "+assertions+" Android integration checks\n");finish(Activity.RESULT_OK,report);
    }catch(Throwable failure){report.putString("stream","FAIL: "+failure.toString()+"\n");finish(Activity.RESULT_CANCELED,report);}}
}

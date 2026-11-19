package com.gr4nt.auralauncher;
import android.app.*;
import android.content.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import org.json.*;
import java.util.*;

public final class AuraSmoke extends Instrumentation {
    int assertions=0;
    void check(boolean ok,String message){assertions++;if(!ok)throw new AssertionError(message);}
    boolean contains(View view,String needle){if(view instanceof TextView&&((TextView)view).getText().toString().contains(needle))return true;if(view instanceof ViewGroup){ViewGroup g=(ViewGroup)view;for(int i=0;i<g.getChildCount();i++)if(contains(g.getChildAt(i),needle))return true;}return false;}
    @Override public void onCreate(Bundle arguments){super.onCreate(arguments);start();}
    @Override public void onStart(){Bundle report=new Bundle();try{
        LauncherActivity activity=(LauncherActivity)startActivitySync(new Intent().setClassName(getTargetContext(),"com.gr4nt.auralauncher.LauncherActivity").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        for(int i=0;i<40;i++){Thread.sleep(100);waitForIdleSync();if(!activity.catalog.apps.isEmpty())break;}
        check(!activity.catalog.apps.isEmpty(),"Installed apps must be discovered");
        String pkg=activity.catalog.apps.get(0).pkg;
        runOnMainSync(()->{activity.store.put("layout","phone");activity.render();});waitForIdleSync();check(contains(activity.root,"Aura"),"Phone Home is visible");
        runOnMainSync(()->{activity.store.put("layout","tablet");activity.render();});waitForIdleSync();check(activity.mode.equals("tablet"),"Tablet layout");
        runOnMainSync(()->{activity.store.put("layout","desktop");activity.render();});waitForIdleSync();check(contains(activity.root,"AURA")&&contains(activity.root,"Files"),"Desktop menu is visible");
        LauncherStore store=activity.store;store.rename(pkg,"Aura test label");check(store.name(pkg,"wrong").equals("Aura test label"),"Renaming persists");store.add("dock",pkg);check(new LauncherStore(activity).list("dock").contains(pkg),"Dock survives new store");
        String folder=store.folder("Test folder",Collections.singletonList(pkg));check(store.folderApps(folder).contains(pkg),"Folder content persists");
        String exported=store.backup();JSONObject backup=new JSONObject(exported);check(!backup.getJSONObject("settings").has("widgets"),"Device-specific widget IDs must not be exported");
        store.rename(pkg,"Changed");store.restore(exported);check(store.name(pkg,"").equals("Aura test label"),"Backup restores names");
        boolean rejected=false;try{store.restore("{\"app\":\"Other\",\"version\":1,\"settings\":{}}");}catch(Exception expected){rejected=true;}check(rejected,"Wrong backup format rejected");
        runOnMainSync(()->{activity.store.put("layout","phone");activity.render();activity.drawer(0);});waitForIdleSync();check(!activity.dialogs.isEmpty(),"App drawer opens");
        runOnMainSync(()->{for(Dialog d:new ArrayList<>(activity.dialogs))d.dismiss();activity.glassSettings("dock");});waitForIdleSync();check(!activity.dialogs.isEmpty(),"Glass controls open");
        runOnMainSync(()->{for(Dialog d:new ArrayList<>(activity.dialogs))d.dismiss();activity.settings();});waitForIdleSync();check(!activity.dialogs.isEmpty(),"Settings opens");
        runOnMainSync(()->{for(Dialog d:new ArrayList<>(activity.dialogs))d.dismiss();activity.store.put("layout","auto");activity.render();});
        report.putString("stream","PASS: "+assertions+" Android integration checks\n");finish(Activity.RESULT_OK,report);
    }catch(Throwable failure){report.putString("stream","FAIL: "+failure.toString()+"\n");finish(Activity.RESULT_CANCELED,report);}}
}

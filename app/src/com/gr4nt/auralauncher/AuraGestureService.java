package com.gr4nt.auralauncher;

import android.accessibilityservice.*;
import android.app.KeyguardManager;
import android.content.*;
import android.graphics.*;
import android.hardware.HardwareBuffer;
import android.os.*;
import android.util.DisplayMetrics;
import android.view.*;
import android.view.accessibility.AccessibilityEvent;
import java.util.*;
import java.util.concurrent.*;

/** Optional user-enabled helper. Observes package changes, never retrieves window text. */
public final class AuraGestureService extends AccessibilityService {
    static volatile AuraGestureService active;final Handler handler=new Handler(Looper.getMainLooper());final ExecutorService files=Executors.newSingleThreadExecutor();
    final List<View> edges=new ArrayList<>();LauncherStore store;WindowManager wm;String foreground="";boolean captureBusy=false;
    @Override protected void onServiceConnected(){active=this;store=new LauncherStore(this);wm=getSystemService(WindowManager.class);updateEdges();}
    @Override public void onAccessibilityEvent(AccessibilityEvent event){if(event.getEventType()!=AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED||store==null)return;updateEdges();if(!store.flag("helper_consent",false))return;CharSequence source=event.getPackageName();if(source==null)return;String pkg=source.toString();if(pkg.equals(getPackageName())){foreground="";return;}
        if(getPackageManager().getLaunchIntentForPackage(pkg)==null){foreground="";return;}foreground=pkg;List<String> recent=store.list("recent");recent.remove(pkg);recent.add(0,pkg);if(recent.size()>30)recent=recent.subList(0,30);store.list("recent",recent);
        handler.postDelayed(()->{if(foreground.equals(pkg))capture(null);},650);
    }
    @Override public void onInterrupt(){}
    @Override public void onDestroy(){removeEdges();handler.removeCallbacksAndMessages(null);files.shutdownNow();if(active==this)active=null;SnapshotCache.clear(this);super.onDestroy();}
    void removeEdges(){if(wm!=null)for(View edge:edges)try{wm.removeView(edge);}catch(Exception ignored){}edges.clear();}
    void updateEdges(){if(wm==null||store==null)return;boolean enabled=store.flag("helper_consent",false)&&store.flag("global_gestures",false)&&!DesktopPadActivity.running&&!getSystemService(KeyguardManager.class).isKeyguardLocked();if(!enabled){removeEdges();return;}if(!edges.isEmpty())return;
        DisplayMetrics metrics=new DisplayMetrics();wm.getDefaultDisplay().getRealMetrics(metrics);int width=metrics.widthPixels,height=metrics.heightPixels,nav=0;if(Build.VERSION.SDK_INT>=30)nav=wm.getCurrentWindowMetrics().getWindowInsets().getInsetsIgnoringVisibility(WindowInsets.Type.navigationBars()).bottom;else{int id=getResources().getIdentifier("navigation_bar_height","dimen","android");if(id!=0)nav=getResources().getDimensionPixelSize(id);}int usableHeight=height-nav;float density=metrics.density;int strip=Math.max(4,Math.round(store.number("edge_width",9)*density));
        addEdge(strip,height-Math.round(90*density),0,Math.round(30*density),width,height,density);
        addEdge(strip,height-Math.round(90*density),width-strip,Math.round(30*density),width,height,density);
        addEdge(width,Math.round(20*density),0,usableHeight-Math.round(22*density),width,usableHeight,density);
    }
    @Override public void onConfigurationChanged(android.content.res.Configuration configuration){super.onConfigurationChanged(configuration);removeEdges();updateEdges();}
    void addEdge(int width,int height,int x,int y,int screenW,int screenH,float density){View view=new View(this);GestureRules rules=new GestureRules(density);final boolean[] down={false};Runnable hold=new Runnable(){public void run(){if(!down[0])return;GestureRules.Action action=rules.poll(SystemClock.uptimeMillis());if(action!=GestureRules.Action.NONE)act(action);else handler.postDelayed(this,80);}};
        view.setOnTouchListener((v,event)->{long now=SystemClock.uptimeMillis();GestureRules.Action action=GestureRules.Action.NONE;if(event.getActionMasked()==MotionEvent.ACTION_DOWN){rules.down(event.getRawX(),event.getRawY(),screenW,screenH,now);down[0]=true;handler.postDelayed(hold,80);}else if(event.getActionMasked()==MotionEvent.ACTION_MOVE)action=rules.move(event.getRawX(),event.getRawY(),now);else if(event.getActionMasked()==MotionEvent.ACTION_UP){down[0]=false;handler.removeCallbacks(hold);action=rules.up(event.getRawX(),event.getRawY(),now);}else if(event.getActionMasked()==MotionEvent.ACTION_CANCEL){down[0]=false;rules.cancel();handler.removeCallbacks(hold);}if(action!=GestureRules.Action.NONE)act(action);return true;});
        WindowManager.LayoutParams params=new WindowManager.LayoutParams(width,Math.max(1,height),WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL|WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,android.graphics.PixelFormat.TRANSLUCENT);params.gravity=Gravity.TOP|Gravity.LEFT;params.x=x;params.y=y;params.setTitle("Aura navigation edge");try{wm.addView(view,params);edges.add(view);}catch(Exception ignored){}
    }
    void act(GestureRules.Action action){if(action==GestureRules.Action.BACK)performGlobalAction(GLOBAL_ACTION_BACK);else if(action==GestureRules.Action.HOME)capture(()->performGlobalAction(GLOBAL_ACTION_HOME));else if(action==GestureRules.Action.RECENTS)capture(()->startActivity(new Intent(this,LauncherActivity.class).setAction(LauncherActivity.ACTION_RECENTS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)));}
    void capture(Runnable after){String pkg=foreground;if(Build.VERSION.SDK_INT<30||!store.flag("helper_consent",false)||!store.flag("recents_previews",true)||pkg.isEmpty()||captureBusy){if(after!=null)after.run();return;}captureBusy=true;
        try{takeScreenshot(Display.DEFAULT_DISPLAY,getMainExecutor(),new TakeScreenshotCallback(){
            public void onSuccess(ScreenshotResult result){try{HardwareBuffer buffer=result.getHardwareBuffer();Bitmap hardware=Bitmap.wrapHardwareBuffer(buffer,result.getColorSpace());if(hardware==null)throw new IllegalStateException();Bitmap copy=hardware.copy(Bitmap.Config.ARGB_8888,false);hardware.recycle();buffer.close();int w=Math.min(420,copy.getWidth());Bitmap scaled=Bitmap.createScaledBitmap(copy,w,Math.max(1,copy.getHeight()*w/copy.getWidth()),true);if(copy!=scaled)copy.recycle();files.execute(()->{try{SnapshotCache.write(AuraGestureService.this,pkg,scaled);}catch(Exception ignored){}scaled.recycle();handler.post(()->{captureBusy=false;if(after!=null)after.run();});});}catch(Exception e){captureBusy=false;if(after!=null)after.run();}}
            public void onFailure(int code){captureBusy=false;SnapshotCache.blocked(AuraGestureService.this,pkg);if(after!=null)after.run();}
        });}catch(Exception e){captureBusy=false;if(after!=null)after.run();}
    }
    boolean remoteTap(int display,float x,float y){return remoteStroke(display,x,y,x,y,80);}
    boolean remoteStroke(int display,float x,float y,float toX,float toY,long duration){if(Build.VERSION.SDK_INT<30||!store.flag("helper_consent",false))return false;try{Path path=new Path();path.moveTo(x,y);if(x!=toX||y!=toY)path.lineTo(toX,toY);GestureDescription.Builder builder=new GestureDescription.Builder().setDisplayId(display);builder.addStroke(new GestureDescription.StrokeDescription(path,0,duration));return dispatchGesture(builder.build(),null,null);}catch(Exception e){return false;}}
}

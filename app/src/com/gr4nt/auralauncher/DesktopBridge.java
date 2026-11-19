package com.gr4nt.auralauncher;
import android.content.*;
import android.graphics.*;
import android.os.Build;
import android.provider.Settings;
import android.view.*;
import android.widget.FrameLayout;
import java.lang.ref.WeakReference;

final class DesktopBridge {
    static WeakReference<FrameLayout> surface=new WeakReference<>(null);static int display=-1;
    static void attach(FrameLayout root,int id){surface=new WeakReference<>(root);display=id;}
    static boolean click(int id,float x,float y){AuraGestureService helper=AuraGestureService.active;if(helper!=null&&Build.VERSION.SDK_INT>=30&&helper.remoteTap(id,x,y))return true;FrameLayout root=surface.get();if(root==null||display!=id)return false;long now=android.os.SystemClock.uptimeMillis();MotionEvent down=MotionEvent.obtain(now,now,MotionEvent.ACTION_DOWN,x,y,0),up=MotionEvent.obtain(now,now+50,MotionEvent.ACTION_UP,x,y,0);root.dispatchTouchEvent(down);root.dispatchTouchEvent(up);down.recycle();up.recycle();return true;}
    static boolean scroll(int id,float x,float y,float distance){AuraGestureService helper=AuraGestureService.active;if(helper!=null&&Build.VERSION.SDK_INT>=30)return helper.remoteStroke(id,x,y,x,Math.max(10,y-distance),240);FrameLayout root=surface.get();if(root==null||display!=id)return false;long now=android.os.SystemClock.uptimeMillis();MotionEvent.PointerProperties property=new MotionEvent.PointerProperties();property.id=0;property.toolType=MotionEvent.TOOL_TYPE_MOUSE;MotionEvent.PointerCoords coords=new MotionEvent.PointerCoords();coords.x=x;coords.y=y;coords.setAxisValue(MotionEvent.AXIS_VSCROLL,-distance/40f);MotionEvent event=MotionEvent.obtain(now,now,MotionEvent.ACTION_SCROLL,1,new MotionEvent.PointerProperties[]{property},new MotionEvent.PointerCoords[]{coords},0,0,1,1,0,0,InputDevice.SOURCE_MOUSE,0);root.dispatchGenericMotionEvent(event);event.recycle();return true;}
    static final class Cursor {
        final Context context;final int displayId;WindowManager manager;View pointer;float x,y;
        Cursor(Context owner,Display display){context=owner.createDisplayContext(display);displayId=display.getDisplayId();manager=context.getSystemService(WindowManager.class);}
        void move(float px,float py){x=px;y=py;if(!Settings.canDrawOverlays(context))return;if(pointer==null){pointer=new View(context){final Paint p=new Paint(3);@Override protected void onDraw(Canvas canvas){Path path=new Path();path.moveTo(2,1);path.lineTo(2,25);path.lineTo(8,19);path.lineTo(13,29);path.lineTo(17,27);path.lineTo(12,17);path.lineTo(22,17);path.close();p.setStyle(Paint.Style.FILL);p.setColor(Color.WHITE);canvas.drawPath(path,p);p.setStyle(Paint.Style.STROKE);p.setColor(Color.BLACK);p.setStrokeWidth(1.6f);canvas.drawPath(path,p);}};pointer.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);WindowManager.LayoutParams lp=new WindowManager.LayoutParams(26,34,WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE|WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,android.graphics.PixelFormat.TRANSLUCENT);lp.alpha=.7f;lp.gravity=Gravity.TOP|Gravity.LEFT;lp.x=(int)x;lp.y=(int)y;try{manager.addView(pointer,lp);}catch(Exception e){pointer=null;return;}}WindowManager.LayoutParams lp=(WindowManager.LayoutParams)pointer.getLayoutParams();lp.x=(int)x;lp.y=(int)y;try{manager.updateViewLayout(pointer,lp);}catch(Exception ignored){}}
        void close(){if(pointer!=null)try{manager.removeView(pointer);}catch(Exception ignored){}pointer=null;}
    }
}

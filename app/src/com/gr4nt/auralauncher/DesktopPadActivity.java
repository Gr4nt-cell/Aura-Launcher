package com.gr4nt.auralauncher;
import android.app.Activity;
import android.content.*;
import android.graphics.Color;
import android.hardware.display.DisplayManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;

/** Non-focusable phone controller preserves the external editor's input-method connection. */
public final class DesktopPadActivity extends Activity {
    static volatile boolean running;int displayId,width,height;float x,y,lastX,lastY,scrollStart;boolean scrolling,moved;DesktopBridge.Cursor cursor;
    @Override public void onCreate(Bundle state){super.onCreate(state);getWindow().addFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);displayId=getIntent().getIntExtra("display",-1);Display display=getSystemService(DisplayManager.class).getDisplay(displayId);if(display==null||displayId==0){Toast.makeText(this,"Connect an external display first.",Toast.LENGTH_LONG).show();finish();return;}DisplayMetrics metrics=new DisplayMetrics();display.getRealMetrics(metrics);width=metrics.widthPixels;height=metrics.heightPixels;x=width/2f;y=height/2f;cursor=new DesktopBridge.Cursor(this,display);running=true;if(AuraGestureService.active!=null)AuraGestureService.active.updateEdges();
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(DesktopKeyboard.px(this,12),DesktopKeyboard.px(this,28),DesktopKeyboard.px(this,12),DesktopKeyboard.px(this,12));root.setBackgroundColor(0xff172638);LinearLayout header=new LinearLayout(this);TextView title=new TextView(this);title.setText("Aura desktop controller");title.setTextSize(20);title.setTextColor(Color.WHITE);header.addView(title,new LinearLayout.LayoutParams(0,DesktopKeyboard.px(this,60),1));Button close=new Button(this);close.setText("Close");close.setFocusable(false);close.setOnClickListener(v->finish());header.addView(close);root.addView(header);
        TextView pad=new TextView(this);pad.setText("Trackpad\n\nMove one finger to point\nTap to click · two fingers to scroll");pad.setTextColor(0xffc9e4ec);pad.setTextSize(18);pad.setGravity(Gravity.CENTER);android.graphics.drawable.GradientDrawable background=new android.graphics.drawable.GradientDrawable();background.setColor(0xff31495f);background.setCornerRadius(28);pad.setBackground(background);root.addView(pad,new LinearLayout.LayoutParams(-1,0,1));pad.setOnTouchListener((v,event)->{
            int action=event.getActionMasked();if(action==MotionEvent.ACTION_DOWN){lastX=event.getX();lastY=event.getY();moved=false;scrolling=false;return true;}if(action==MotionEvent.ACTION_POINTER_DOWN&&event.getPointerCount()>1){scrolling=true;scrollStart=event.getY();return true;}if(action==MotionEvent.ACTION_MOVE){float dx=event.getX()-lastX,dy=event.getY()-lastY;if(Math.abs(dx)+Math.abs(dy)>2)moved=true;if(!scrolling){x=Math.max(1,Math.min(width-2,x+dx*2.2f));y=Math.max(1,Math.min(height-2,y+dy*2.2f));cursor.move(x,y);}lastX=event.getX();lastY=event.getY();return true;}if(action==MotionEvent.ACTION_UP){if(scrolling){if(!DesktopBridge.scroll(displayId,x,y,(event.getY()-scrollStart)*2))help();}else if(!moved&&!DesktopBridge.click(displayId,x,y))help();return true;}return true;});
        LinearLayout buttons=new LinearLayout(this);buttons.addView(button("Click",()->{if(!DesktopBridge.click(displayId,x,y))help();}));buttons.addView(button("Keyboard setup",()->startActivity(new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))));buttons.addView(button("Select keyboard",()->getSystemService(InputMethodManager.class).showInputMethodPicker()));root.addView(buttons);root.addView(DesktopKeyboard.create(this));TextView note=new TextView(this);note.setText("Cross-app pointer clicks need the Aura accessibility helper on Android 11+. Select Aura Desktop Keyboard to type into the focused desktop editor. Text is never stored.");note.setTextColor(0xffbdcbd7);note.setTextSize(12);root.addView(note);setContentView(root);cursor.move(x,y);
    }
    Button button(String title,Runnable action){Button button=new Button(this);button.setText(title);button.setTextSize(11);button.setLayoutParams(new LinearLayout.LayoutParams(0,-2,1));button.setAllCaps(false);button.setFocusable(false);button.setOnClickListener(v->action.run());return button;}
    void help(){Toast.makeText(this,"Enable the Aura accessibility helper to control other apps on this display.",Toast.LENGTH_LONG).show();}
    @Override public void onDestroy(){if(cursor!=null)cursor.close();running=false;if(AuraGestureService.active!=null)AuraGestureService.active.updateEdges();super.onDestroy();}
}

package com.gr4nt.auralauncher;
import android.inputmethodservice.InputMethodService;
import android.view.*;
import android.view.inputmethod.*;

/** User-selected input method; forwards only explicit keyboard button presses, never records text. */
public final class AuraDesktopIME extends InputMethodService {
    static volatile AuraDesktopIME active;
    @Override public void onCreate(){super.onCreate();active=this;}
    @Override public void onDestroy(){if(active==this)active=null;super.onDestroy();}
    @Override public View onCreateInputView(){return DesktopKeyboard.create(this);}
    @Override public boolean onEvaluateInputViewShown(){return !DesktopPadActivity.running;}
    static boolean type(String text){AuraDesktopIME ime=active;InputConnection connection=ime==null?null:ime.getCurrentInputConnection();if(connection==null)return false;return connection.commitText(text,1);}
    static boolean key(int code){AuraDesktopIME ime=active;InputConnection connection=ime==null?null:ime.getCurrentInputConnection();if(connection==null)return false;if(code==KeyEvent.KEYCODE_DEL)return connection.deleteSurroundingText(1,0);if(code==KeyEvent.KEYCODE_ENTER){EditorInfo info=ime.getCurrentInputEditorInfo();int action=info==null?EditorInfo.IME_ACTION_NONE:info.imeOptions&EditorInfo.IME_MASK_ACTION;if(action!=EditorInfo.IME_ACTION_NONE&&action!=EditorInfo.IME_ACTION_UNSPECIFIED)return connection.performEditorAction(action);}connection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,code));return connection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP,code));}
    static boolean edit(int action){AuraDesktopIME ime=active;InputConnection connection=ime==null?null:ime.getCurrentInputConnection();return connection!=null&&connection.performContextMenuAction(action);}
}

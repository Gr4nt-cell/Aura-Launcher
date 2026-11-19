package com.gr4nt.auralauncher;

/** Deterministic gesture recognizer shared by Home and the optional system edge helper. */
public final class GestureRules {
    public enum Action { NONE, BACK, HOME, RECENTS, CONTROL, NOTIFICATIONS, UP, NEXT, PREVIOUS }
    final float density;float sx,sy,x,y,width,height,anchorX,anchorY;long stableSince;boolean active,bottom,left,right,raised,consumed;
    public GestureRules(float density){this.density=density;}
    public void down(float x,float y,float w,float h,long now){sx=this.x=x;sy=this.y=y;width=w;height=h;active=true;consumed=false;raised=false;bottom=y>=h-28*density;left=x<=20*density;right=x>=w-20*density;stableSince=now;anchorX=x;anchorY=y;}
    public Action move(float x,float y,long now){this.x=x;this.y=y;if(!active||consumed)return Action.NONE;float dx=x-sx,dy=y-sy;
        if(bottom){if(-dy>=60*density){if(!raised){raised=true;stableSince=now;anchorX=x;anchorY=y;}else if(Math.hypot(x-anchorX,y-anchorY)>18*density){stableSince=now;anchorX=x;anchorY=y;}}return poll(now);}
        if(Math.abs(dx)>64*density&&Math.abs(dy)<70*density&&((left&&dx>0)||(right&&dx<0)))return finish(Action.BACK);
        if(dy>80*density&&Math.abs(dx)<70*density)return finish(sx>=width/2?Action.CONTROL:Action.NOTIFICATIONS);
        return Action.NONE;
    }
    public Action poll(long now){if(active&&!consumed&&bottom&&raised&&now-stableSince>=2000)return finish(Action.RECENTS);return Action.NONE;}
    public Action up(float x,float y,long now){Action a=move(x,y,now);if(a!=Action.NONE){active=false;return a;}if(!active||consumed){active=false;return Action.NONE;}float dx=x-sx,dy=y-sy;active=false;
        if(bottom&&raised)return Action.HOME;
        if(-dy>80*density&&Math.abs(dx)<70*density)return Action.UP;
        if(Math.abs(dx)>80*density&&Math.abs(dy)<70*density)return dx<0?Action.NEXT:Action.PREVIOUS;
        return Action.NONE;
    }
    Action finish(Action action){consumed=true;return action;}
    public boolean dragging(){return active&&(consumed||Math.hypot(x-sx,y-sy)>22*density);}
    public boolean isBottom(){return bottom;}
    public void cancel(){active=false;consumed=false;}
}

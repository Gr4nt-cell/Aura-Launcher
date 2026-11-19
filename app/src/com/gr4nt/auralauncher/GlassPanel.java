package com.gr4nt.auralauncher;

import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.view.View;
import android.widget.FrameLayout;

/** Wallpaper-backed glass. Text and controls sit above the separately blurred background layer. */
final class GlassPanel extends FrameLayout {
    static final String[] PARAMETERS={"blur","luminance","saturation","brightness","alpha","refraction","thickness","bg_saturation","bg_brightness","burn","edge","edge_feather","reflection_offset","light","darker"};
    static final String[] LABELS={"Blur","Luminance","Saturation","Brightness","Opacity","Refraction","Thickness","Background saturation","Background brightness","Burn","Edge","Edge softness","Reflection position","Highlight","Darkening"};
    static final int[] DEFAULTS={35,105,110,100,42,100,22,100,100,8,35,40,35,30,10};
    final LauncherActivity owner;final String prefix;
    GlassPanel(LauncherActivity a,Context c,String group) {
        super(c);owner=a;prefix=group;setClipToOutline(true);
        int radius=a.store.number(group+"_radius",group.equals("dock")?30:24);
        GradientDrawable shape=new GradientDrawable();shape.setColor(Color.TRANSPARENT);shape.setCornerRadius(a.dp(radius));setBackground(shape);
        View material=new View(c){
            final Paint paint=new Paint(3);final int[] location=new int[2];
            @Override protected void onDraw(Canvas canvas){
                int[] v=new int[PARAMETERS.length];for(int i=0;i<v.length;i++)v[i]=LayoutRules.clamp(owner.store.number(prefix+"_"+PARAMETERS[i],DEFAULTS[i]),0,200);
                getLocationInWindow(location);canvas.save();
                float refract=1+(v[5]-100)/1600f;canvas.scale(refract,refract,getWidth()/2f,getHeight()/2f);
                canvas.translate(-location[0],-location[1]);
                ColorMatrix cm=new ColorMatrix();cm.setSaturation(v[2]*v[7]/10000f);
                float gain=v[1]/100f,offset=(v[3]+v[8]-200)*0.65f;
                cm.postConcat(new ColorMatrix(new float[]{gain,0,0,0,offset,0,gain,0,0,offset,0,0,gain,0,offset,0,0,0,1,0}));
                paint.setColorFilter(new ColorMatrixColorFilter(cm));
                owner.paintWallpaper(canvas,Math.max(owner.root.getWidth(),getWidth()+location[0]),Math.max(owner.root.getHeight(),getHeight()+location[1]),paint);canvas.restore();
                boolean light=owner.store.text("glass_theme","dark").equals("light");
                canvas.drawColor(Color.argb(Math.min(230,v[4]*2),light?238:20,light?245:31,light?248:49));
                canvas.drawColor(Color.argb(Math.min(150,v[9]+v[14]),0,0,0));
            }
        };
        if(Build.VERSION.SDK_INT>=31)material.setRenderEffect(RenderEffect.createBlurEffect(Math.max(0.1f,a.dp(a.store.number(group+"_blur",35))/2f),Math.max(0.1f,a.dp(a.store.number(group+"_blur",35))/2f),Shader.TileMode.CLAMP));
        addView(material,new LayoutParams(0,0));
        View edges=new View(c){final Paint p=new Paint(3);@Override protected void onDraw(Canvas canvas){
            int edge=owner.store.number(prefix+"_edge",35),soft=owner.store.number(prefix+"_edge_feather",40),light=owner.store.number(prefix+"_light",30),thickness=owner.store.number(prefix+"_thickness",22);
            float r=owner.dp(owner.store.number(prefix+"_radius",prefix.equals("dock")?30:24));
            p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(owner.dp(0.5f+thickness/45f));p.setColor(Color.argb(LayoutRules.clamp(edge+80-soft/2,0,220),255,255,255));
            canvas.drawRoundRect(1,1,getWidth()-1,getHeight()-1,r,r,p);
            p.setStyle(Paint.Style.FILL);float y=getHeight()*owner.store.number(prefix+"_reflection_offset",35)/200f;
            p.setShader(new LinearGradient(0,0,getWidth(),Math.max(2,y+owner.dp(60)),new int[]{Color.argb(LayoutRules.clamp(light*2,0,180),255,255,255),Color.TRANSPARENT},null,Shader.TileMode.CLAMP));
            canvas.drawRoundRect(0,0,getWidth(),getHeight(),r,r,p);p.setShader(null);
        }};
        addView(edges,new LayoutParams(0,0));
    }
    @Override protected void onLayout(boolean changed,int l,int t,int r,int b){super.onLayout(changed,l,t,r,b);for(int i=0;i<Math.min(2,getChildCount());i++)getChildAt(i).layout(0,0,r-l,b-t);}
}

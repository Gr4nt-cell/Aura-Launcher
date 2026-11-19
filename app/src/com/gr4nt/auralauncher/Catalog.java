package com.gr4nt.auralauncher;

import android.content.*;
import android.content.pm.*;
import android.content.res.Resources;
import android.graphics.drawable.*;
import android.os.Build;
import org.xmlpull.v1.XmlPullParser;
import java.util.*;

final class Catalog {
    static final class App {String pkg,label;ComponentName component;Drawable icon;}
    final LauncherActivity owner;final List<App> apps=new ArrayList<>();final Map<String,App> byPackage=new HashMap<>();
    final Map<String,String> packMap=new HashMap<>();Resources packResources;String packPackage="";
    Catalog(LauncherActivity a){owner=a;}
    void refresh(){
        apps.clear();byPackage.clear();PackageManager pm=owner.getPackageManager();
        for(ResolveInfo r:pm.queryIntentActivities(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER),0)){
            String pkg=r.activityInfo.packageName;if(pkg.equals(owner.getPackageName())||byPackage.containsKey(pkg))continue;
            App app=new App();app.pkg=pkg;app.label=r.loadLabel(pm).toString();app.component=new ComponentName(pkg,r.activityInfo.name);try{int icon=r.getIconResource();app.icon=icon==0?r.loadIcon(pm):pm.getResourcesForApplication(r.activityInfo.applicationInfo).getDrawableForDensity(icon,Math.max(owner.getResources().getDisplayMetrics().densityDpi,android.util.DisplayMetrics.DENSITY_XXXHIGH),null);}catch(Exception e){app.icon=r.loadIcon(pm);}apps.add(app);byPackage.put(pkg,app);
        }
        apps.sort((a,b)->a.label.compareToIgnoreCase(b.label));loadPack();
    }
    List<App> visible(String query){List<App> out=new ArrayList<>();String q=query.toLowerCase(Locale.ROOT).trim();for(App app:apps)if(!owner.store.hidden(app.pkg)&&(owner.store.name(app.pkg,app.label).toLowerCase(Locale.ROOT).contains(q)||app.pkg.toLowerCase(Locale.ROOT).contains(q)))out.add(app);return out;}
    void loadPack(){
        packMap.clear();packResources=null;packPackage=owner.store.text("icon_pack","");if(packPackage.isEmpty())return;
        try{
            packResources=owner.getPackageManager().getResourcesForApplication(packPackage);
            int id=packResources.getIdentifier("appfilter","xml",packPackage);XmlPullParser parser;
            if(id!=0)parser=packResources.getXml(id);else{parser=android.util.Xml.newPullParser();parser.setInput(packResources.getAssets().open("appfilter.xml"),"UTF-8");}
            int event;int count=0;while((event=parser.next())!=XmlPullParser.END_DOCUMENT&&count++<100000)if(event==XmlPullParser.START_TAG&&"item".equals(parser.getName())){
                String component=parser.getAttributeValue(null,"component"),drawable=parser.getAttributeValue(null,"drawable");if(component!=null&&drawable!=null)packMap.put(component,drawable);
            }
        }catch(Exception ignored){packResources=null;}
    }
    Drawable icon(App app){
        if(packResources!=null)try{String name=packMap.get("ComponentInfo{"+app.component.flattenToString()+"}");if(name==null)name=packMap.get("ComponentInfo{"+app.component.flattenToShortString()+"}");if(name!=null){int id=packResources.getIdentifier(name,"drawable",packPackage);if(id!=0)return packResources.getDrawable(id,null);}}catch(Exception ignored){}
        if(Build.VERSION.SDK_INT>=33&&owner.store.flag("themed_icons",false)&&app.icon instanceof AdaptiveIconDrawable){Drawable mono=((AdaptiveIconDrawable)app.icon).getMonochrome();if(mono!=null){mono=mono.mutate();mono.setTint(0xffc2e9df);return mono;}}
        return app.icon;
    }
}

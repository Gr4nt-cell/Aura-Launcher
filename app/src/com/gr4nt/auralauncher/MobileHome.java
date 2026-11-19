package com.gr4nt.auralauncher;

import android.content.*;
import android.graphics.*;
import android.provider.AlarmClock;
import android.view.*;
import android.widget.*;
import java.text.SimpleDateFormat;
import java.util.*;
import org.json.*;

final class MobileHome {
    static boolean classic(LauncherActivity a){return a.store.text("home_mode","drawer").equals("classic");}
    static int capacity(LauncherActivity a){return LayoutRules.columns(a.widthDp()-32,"phone",a.store.number("columns",0))*4;}
    static List<List<String>> pages(LauncherActivity a){
        List<List<String>> result=new ArrayList<>();
        if(!classic(a)){for(int i=0;i<3;i++)result.add(a.store.list("home_"+i));return result;}
        List<String> installed=new ArrayList<>();for(Catalog.App app:a.catalog.visible(""))installed.add(app.pkg);
        List<String> order=a.store.list("classic_order");boolean initial=order.isEmpty();for(int i=0;i<3;i++)for(String id:a.store.list("home_"+i))if((initial||id.startsWith("folder:"))&&!order.contains(id))order.add(id);
        Map<String,List<String>> folders=new HashMap<>();JSONObject definitions=a.store.object("folders");for(Iterator<String> i=definitions.keys();i.hasNext();){String id=i.next();folders.put(id,a.store.folderApps(id));}
        order=WorkspaceRules.classic(installed,order,folders,a.store.list("dock"));a.store.list("classic_order",order);int cap=Math.max(4,capacity(a));for(int i=0;i<order.size();i+=cap)result.add(new ArrayList<>(order.subList(i,Math.min(order.size(),i+cap))));if(result.isEmpty())result.add(new ArrayList<>());return result;
    }
    static void build(LauncherActivity a,Context c,FrameLayout surface){
        List<List<String>> pages=pages(a);a.page=LayoutRules.clamp(a.page,-1,pages.size()-1);
        LinearLayout body=a.column(c);body.setPadding(a.dp(14),a.insetTop+a.dp(8),a.dp(14),a.insetBottom+a.dp(4));surface.addView(body,new FrameLayout.LayoutParams(-1,-1));
        if(a.page==0){LinearLayout clock=a.column(c);clock.setContentDescription("Aura home");clock.setPadding(a.dp(10),a.dp(18),0,a.dp(24));TextView time=a.text(c,android.text.format.DateFormat.format(android.text.format.DateFormat.is24HourFormat(c)?"HH:mm":"h:mm",new Date()).toString(),58);time.setTextColor(Color.WHITE);time.setTypeface(android.graphics.Typeface.create("sans-serif-light",0));a.homeClocks.add(time);clock.addView(time);TextView date=a.text(c,new SimpleDateFormat("EEEE, d MMMM",Locale.getDefault()).format(new Date()),14);date.setTextColor(0xfff0f2f6);clock.addView(date);clock.setOnClickListener(v->a.open(new Intent(AlarmClock.ACTION_SHOW_ALARMS)));body.addView(clock,new LinearLayout.LayoutParams(-1,-2));}
        else {TextView marker=a.text(c,"",1);marker.setContentDescription("Aura home");body.addView(marker,new LinearLayout.LayoutParams(1,a.dp(24)));}
        ScrollView scroll=new ScrollView(c);scroll.setFillViewport(true);scroll.setVerticalScrollBarEnabled(false);LinearLayout workspace=a.column(c);workspace.setOnLongClickListener(v->{a.homeMenu(v);return true;});scroll.addView(workspace);body.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        if(a.page<0)a.buildToday(c,workspace);else{
            a.buildWidgets(c,workspace,false);
            GridLayout grid=a.appGrid(c,pages.get(a.page),LayoutRules.columns(a.widthDp()-28,"phone",a.store.number("columns",0)),0,false);workspace.addView(grid,new LinearLayout.LayoutParams(-1,-2));
            workspace.setOnDragListener((v,e)->{if(e.getAction()==DragEvent.ACTION_DRAG_STARTED)return true;if(e.getAction()==DragEvent.ACTION_DROP){String pkg=a.dragPackage(e);if(pkg!=null){if(classic(a))a.store.add("classic_order",pkg);else a.store.add("home_"+a.page,pkg);a.render();}return true;}return true;});
        }
        LinearLayout lower=a.column(c);lower.setGravity(Gravity.CENTER);lower.setPadding(0,a.dp(6),0,0);
        LinearLayout indicators=a.row(c);indicators.setGravity(Gravity.CENTER);for(int i=0;i<Math.min(12,pages.size());i++){final int index=i;TextView dot=a.text(c,i==a.page?"●":"•",i==a.page?10:9);dot.setTextColor(i==a.page?Color.WHITE:0x80ffffff);dot.setGravity(Gravity.CENTER);dot.setPadding(a.dp(5),a.dp(6),a.dp(5),a.dp(6));dot.setOnClickListener(v->{a.page=index;a.render();});indicators.addView(dot);}lower.addView(indicators);
        if(a.store.flag("search_capsule",true)){GlassPanel search=new GlassPanel(a,c,"home");TextView label=a.text(c,"⌕  Search",13);label.setTextColor(Color.WHITE);label.setGravity(Gravity.CENTER);label.setOnClickListener(v->{if(classic(a))a.googleSearch();else a.drawer(0);});search.addView(label,new FrameLayout.LayoutParams(-1,-1));LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(a.dp(108),a.dp(29));sp.setMargins(0,0,0,a.dp(10));lower.addView(search,sp);}
        GlassPanel dock=new GlassPanel(a,c,"dock");LinearLayout pins=a.row(c);pins.setPadding(a.dp(8),a.dp(6),a.dp(8),a.dp(6));List<String> visible=new ArrayList<>();for(String pkg:a.store.list("dock"))if(a.catalog.byPackage.containsKey(pkg)&&!a.store.hidden(pkg))visible.add(pkg);
        for(String pkg:visible){LinearLayout icon=(LinearLayout)a.appCell(c,pkg,0,true);View image=icon.getChildAt(0);int size=a.dp(Math.min(58,Math.max(32,(a.widthDp()-48)/Math.max(1,visible.size())-16)));image.setLayoutParams(new LinearLayout.LayoutParams(size,size));pins.addView(icon,new LinearLayout.LayoutParams(0,-1,1));}
        if(visible.isEmpty()){TextView hint=a.text(c,"Drag apps here",14);hint.setGravity(Gravity.CENTER);pins.addView(hint,new LinearLayout.LayoutParams(-1,-1));}
        dock.addView(pins,new FrameLayout.LayoutParams(-1,-1));dock.setOnDragListener((v,e)->{if(e.getAction()==DragEvent.ACTION_DRAG_STARTED)return true;if(e.getAction()==DragEvent.ACTION_DROP){String pkg=a.dragPackage(e);if(pkg!=null&&a.store.list("dock").size()<8){a.store.add("dock",pkg);a.render();}return true;}return true;});lower.addView(dock,new LinearLayout.LayoutParams(-1,a.dp(a.store.number("dock_height",82))));
        TextView handle=a.text(c,"━━━━",18);handle.setTextColor(0xddeef4f8);handle.setGravity(Gravity.CENTER);handle.setContentDescription("Swipe up for Home; hold two seconds for recent apps");handle.setOnClickListener(v->a.showRecents());lower.addView(handle,new LinearLayout.LayoutParams(a.dp(70),a.dp(20)));body.addView(lower);
    }
    static void replaceWithFolder(LauncherActivity a,String first,String second,String folder){List<String> order=a.store.list("classic_order");int index=Math.max(0,order.indexOf(first));order.remove(first);order.remove(second);order.add(Math.min(index,order.size()),folder);a.store.list("classic_order",order);}
}

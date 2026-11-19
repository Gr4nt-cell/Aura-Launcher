package com.gr4nt.auralauncher;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.*;
import java.util.*;

final class LauncherStore {
    final SharedPreferences prefs;
    LauncherStore(Context c) { prefs=c.getSharedPreferences("aura",0); }
    int number(String k,int d) { Object value=prefs.getAll().get(k);return value instanceof Number?((Number)value).intValue():d; }
    boolean flag(String k,boolean d) { Object value=prefs.getAll().get(k);return value instanceof Boolean?(Boolean)value:d; }
    String text(String k,String d) { Object value=prefs.getAll().get(k);return value instanceof String?(String)value:d; }
    void put(String k,int v) { prefs.edit().putInt(k,v).apply(); }
    void put(String k,boolean v) { prefs.edit().putBoolean(k,v).apply(); }
    void put(String k,String v) { prefs.edit().putString(k,v).apply(); }
    JSONArray array(String key) { try { return new JSONArray(text(key,"[]")); }catch(Exception e){return new JSONArray();} }
    JSONObject object(String key) { try { return new JSONObject(text(key,"{}")); }catch(Exception e){return new JSONObject();} }
    List<String> list(String key) { JSONArray a=array(key);List<String> r=new ArrayList<>();int limit=key.equals("dock")?8:key.startsWith("home_")?128:512;for(int i=0;i<a.length()&&r.size()<limit;i++){String value=a.optString(i);if(!value.isEmpty()&&!r.contains(value))r.add(value);}return r; }
    void list(String key,List<String> values) { put(key,new JSONArray(values).toString()); }
    boolean hidden(String pkg) { return list("hidden").contains(pkg); }
    String name(String pkg,String fallback) { return object("names").optString(pkg,fallback); }
    void rename(String pkg,String name) { try{JSONObject o=object("names");if(name.trim().isEmpty())o.remove(pkg);else o.put(pkg,name.trim());put("names",o.toString());}catch(Exception ignored){} }
    void add(String key,String item) { List<String> items=list(key);if(!items.contains(item)){items.add(item);list(key,items);} }
    void remove(String key,String item) { List<String> items=list(key);items.remove(item);list(key,items); }
    void move(String key,String item,int to) { List<String> a=list(key);a.remove(item);a.add(Math.min(Math.max(0,to),a.size()),item);list(key,a); }
    String folder(String name, List<String> apps) {
        String id="folder:"+UUID.randomUUID();try {JSONObject folders=object("folders");folders.put(id,new JSONObject().put("name",name).put("apps",new JSONArray(apps)));put("folders",folders.toString());}catch(Exception ignored){}
        return id;
    }
    void folderApps(String id,List<String> apps) {try{JSONObject f=object("folders");JSONObject o=f.optJSONObject(id);if(o!=null){o.put("apps",new JSONArray(apps));put("folders",f.toString());}}catch(Exception ignored){} }
    List<String> folderApps(String id) {JSONObject f=object("folders").optJSONObject(id);List<String> r=new ArrayList<>();if(f!=null){JSONArray a=f.optJSONArray("apps");if(a!=null)for(int i=0;i<a.length();i++)r.add(a.optString(i));}return r;}
    String backup() throws Exception {
        JSONObject values=new JSONObject();
        for(Map.Entry<String,?> e:prefs.getAll().entrySet())if(e.getValue() instanceof String||e.getValue() instanceof Number||e.getValue() instanceof Boolean)values.put(e.getKey(),e.getValue());
        // Widget IDs are host/device specific and must be rebound after import.
        values.remove("widgets");values.remove("wallpaper_uri");
        JSONArray providers=new JSONArray();JSONArray widgets=array("widgets");for(int i=0;i<widgets.length();i++){JSONObject w=widgets.optJSONObject(i);if(w!=null)providers.put(w.optString("provider"));}
        return new JSONObject().put("app","Aura Launcher").put("version",1).put("settings",values).put("widget_providers_to_rebind",providers).toString(2);
    }
    void restore(String json) throws Exception {
        if(json.length()>1048576)throw new Exception("Backup is too large");JSONObject o=new JSONObject(json);
        if(!"Aura Launcher".equals(o.optString("app"))||o.optInt("version")!=1)throw new Exception("Not an Aura Launcher backup");
        JSONObject v=o.getJSONObject("settings");SharedPreferences.Editor e=prefs.edit();
        for(Iterator<String> it=v.keys();it.hasNext();){String k=it.next();if("widgets".equals(k)||"wallpaper_uri".equals(k))continue;Object x=v.get(k);if(x instanceof Boolean)e.putBoolean(k,(Boolean)x);else if(x instanceof Number)e.putInt(k,boundedNumber(k,((Number)x).intValue()));else if(x instanceof String&&((String)x).length()<=262144)e.putString(k,(String)x);}
        e.apply();
    }
    static int boundedNumber(String key,int value){
        if(key.equals("icon_size"))return LayoutRules.clamp(value,36,86);
        if(key.equals("columns"))return LayoutRules.clamp(value,0,12);
        if(key.endsWith("_radius"))return LayoutRules.clamp(value,0,60);
        if(key.equals("dock_height"))return LayoutRules.clamp(value,56,110);
        if(key.equals("dock_margin"))return LayoutRules.clamp(value,8,64);
        if(key.equals("dock_icon_offset"))return LayoutRules.clamp(value,-20,20);
        if(key.equals("search_above_gap")||key.equals("search_below_gap"))return LayoutRules.clamp(value,0,40);
        return LayoutRules.clamp(value,0,200);
    }
}

package com.gr4nt.auralauncher;
import java.util.*;

public final class WorkspaceRules {
    public static List<String> classic(List<String> installed,List<String> order,Map<String,List<String>> folders,List<String> dock){
        Set<String> known=new HashSet<>(installed),covered=new HashSet<>(dock);List<String> out=new ArrayList<>();
        for(String key:order)if(folders.containsKey(key))covered.addAll(folders.get(key));
        for(String key:order)if(folders.containsKey(key)){if(!out.contains(key))out.add(key);}else if(known.contains(key)&&!covered.contains(key)&&!out.contains(key))out.add(key);
        for(String pkg:installed)if(!covered.contains(pkg)&&!out.contains(pkg))out.add(pkg);return out;
    }
    public static float scale(float distance){float d=Math.min(2,Math.abs(distance));return d<=1?1-.16f*d:.84f-.18f*(d-1);}
    public static float opacity(float distance){float d=Math.min(2,Math.abs(distance));return d<=1?1-.15f*d:.85f-.30f*(d-1);}
    private WorkspaceRules(){}
}

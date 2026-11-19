import com.gr4nt.auralauncher.*;
import java.util.*;
public final class GestureRulesTest {
 static int checks;static void check(boolean value){checks++;if(!value)throw new AssertionError("Gesture/workspace check "+checks);}
 public static void main(String[] args){for(float density:new float[]{1,2,3}){GestureRules g=new GestureRules(density);float w=400*density,h=800*density;
 g.down(w*.8f,100*density,w,h,0);check(g.move(w*.8f,190*density,100)==GestureRules.Action.CONTROL);
 g.down(w*.2f,100*density,w,h,0);check(g.move(w*.2f,190*density,100)==GestureRules.Action.NOTIFICATIONS);
 g.down(5*density,300*density,w,h,0);check(g.up(90*density,300*density,100)==GestureRules.Action.BACK);
 g.down(w-5*density,300*density,w,h,0);check(g.up(w-90*density,300*density,100)==GestureRules.Action.BACK);
 g.down(w/2,h-5*density,w,h,0);check(g.move(w/2,h-100*density,100)==GestureRules.Action.NONE);check(g.poll(2099)==GestureRules.Action.NONE);check(g.poll(2100)==GestureRules.Action.RECENTS);check(g.up(w/2,h-100*density,2200)==GestureRules.Action.NONE);
 g.down(w/2,h-5*density,w,h,0);check(g.up(w/2,h-100*density,100)==GestureRules.Action.HOME);
 g.down(w/2,h-5*density,w,h,0);g.move(w/2,h-100*density,100);g.move(w/2,h-140*density,1900);check(g.poll(2100)==GestureRules.Action.NONE);check(g.poll(3900)==GestureRules.Action.RECENTS);
 g.down(w/2,500*density,w,h,0);check(g.up(w/2,390*density,100)==GestureRules.Action.UP);
 g.down(w/2,500*density,w,h,0);g.cancel();check(g.poll(9999)==GestureRules.Action.NONE);
 }
 Map<String,List<String>> folders=new HashMap<>();folders.put("folder:one",Arrays.asList("a","b"));List<String> all=WorkspaceRules.classic(Arrays.asList("a","b","c","d","e"),Arrays.asList("folder:one","c","c","uninstalled"),folders,Arrays.asList("d"));check(all.equals(Arrays.asList("folder:one","c","e")));check(WorkspaceRules.classic(Arrays.asList("a","b"),Collections.emptyList(),Collections.emptyMap(),Collections.emptyList()).equals(Arrays.asList("a","b")));
 check(Math.abs(WorkspaceRules.scale(1)-.84f)<.001f);check(Math.abs(WorkspaceRules.scale(2)-.66f)<.001f);check(Math.abs(WorkspaceRules.opacity(2)-.55f)<.001f);System.out.println("PASS: "+checks+" gesture and workspace checks");
 }
}

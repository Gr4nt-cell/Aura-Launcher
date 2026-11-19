import com.gr4nt.auralauncher.LayoutRules;
public class LayoutRulesTest {
    static int checks=0;
    static void check(boolean condition){checks++;if(!condition)throw new AssertionError("Layout check "+checks);}
    public static void main(String[] args){
        check(LayoutRules.mode(360,"auto").equals("phone"));check(LayoutRules.mode(800,"auto").equals("tablet"));check(LayoutRules.mode(1400,"auto").equals("desktop"));
        check(LayoutRules.mode(360,"desktop").equals("desktop"));check(LayoutRules.mode(1400,"phone").equals("phone"));
        for(int width:new int[]{180,320,360,600,800,1080,1920})for(int wanted:new int[]{0,2,4,8,12}){
            int columns=LayoutRules.columns(width,"tablet",wanted);check(columns>=2);check(columns<=Math.max(2,width/74));
        }
        for(int width:new int[]{280,360,800,1280})for(int count=1;count<=11;count++){int cell=LayoutRules.dockCell(width,count,65);check(cell>=28&&cell<=65);}
        System.out.println("PASS: "+checks+" responsive layout checks");
    }
}

package com.gr4nt.auralauncher;
import android.content.Context;
import android.graphics.*;
import java.io.*;
import java.security.MessageDigest;

final class SnapshotCache {
    static File directory(Context c){File f=new File(c.getCacheDir(),"aura-recents");f.mkdirs();return f;}
    static String key(String pkg){try{byte[] hash=MessageDigest.getInstance("SHA-256").digest(pkg.getBytes("UTF-8"));StringBuilder s=new StringBuilder();for(byte b:hash)s.append(String.format("%02x",b&255));return s.toString();}catch(Exception e){return Integer.toHexString(pkg.hashCode());}}
    static File file(Context c,String pkg){return new File(directory(c),key(pkg)+".png");}
    static void write(Context c,String pkg,Bitmap bitmap)throws Exception{File target=file(c,pkg);try(FileOutputStream out=new FileOutputStream(target)){bitmap.compress(Bitmap.CompressFormat.PNG,100,out);}new File(directory(c),key(pkg)+".blocked").delete();}
    static Bitmap read(Context c,String pkg){File file=file(c,pkg);if(!file.isFile()||System.currentTimeMillis()-file.lastModified()>12*60*60*1000)return null;return BitmapFactory.decodeFile(file.getPath());}
    static void remove(Context c,String pkg){file(c,pkg).delete();new File(directory(c),key(pkg)+".blocked").delete();}
    static void blocked(Context c,String pkg){file(c,pkg).delete();try{new File(directory(c),key(pkg)+".blocked").createNewFile();}catch(Exception ignored){}}
    static boolean protectedPreview(Context c,String pkg){return new File(directory(c),key(pkg)+".blocked").isFile();}
    static void clear(Context c){File[] files=directory(c).listFiles();if(files!=null)for(File f:files)if(f.isFile())f.delete();}
}

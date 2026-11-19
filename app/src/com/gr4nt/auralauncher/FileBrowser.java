package com.gr4nt.auralauncher;

import android.app.*;
import android.content.*;
import android.database.Cursor;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.view.*;
import android.widget.*;
import java.io.*;
import java.util.*;

/** Real document-provider operations, scoped to the folder permission the user grants. */
final class FileBrowser {
    final LauncherActivity owner;final Uri tree;final List<String> path=new ArrayList<>();LinearLayout box;Dialog dialog;
    Uri copied;String copiedName,copiedMime;int generation=0;
    static final class Entry{String id,name,mime;long size;boolean directory;}
    FileBrowser(LauncherActivity activity,Uri uri){owner=activity;tree=uri;path.add(DocumentsContract.getTreeDocumentId(uri));}
    void show(){ScrollView scroll=new ScrollView(owner);box=owner.column(owner);scroll.addView(box);dialog=owner.panel("Files",scroll);load();}
    Uri document(String id){return DocumentsContract.buildDocumentUriUsingTree(tree,id);}
    void load(){final int current=++generation;String id=path.get(path.size()-1);box.removeAllViews();box.addView(owner.action(owner,"Loading folder…",()->{}));owner.io.execute(()->{
        List<Entry> entries=new ArrayList<>();String error=null;
        try(Cursor cursor=owner.getContentResolver().query(DocumentsContract.buildChildDocumentsUriUsingTree(tree,id),new String[]{DocumentsContract.Document.COLUMN_DOCUMENT_ID,DocumentsContract.Document.COLUMN_DISPLAY_NAME,DocumentsContract.Document.COLUMN_MIME_TYPE,DocumentsContract.Document.COLUMN_SIZE},null,null,null)){
            if(cursor!=null)while(cursor.moveToNext()){Entry e=new Entry();e.id=cursor.getString(0);e.name=cursor.getString(1);e.mime=cursor.getString(2);e.size=cursor.getLong(3);e.directory=DocumentsContract.Document.MIME_TYPE_DIR.equals(e.mime);entries.add(e);}
            entries.sort((a,b)->a.directory==b.directory?a.name.compareToIgnoreCase(b.name):a.directory?-1:1);
        }catch(Exception e){error="Folder cannot be read. Choose a folder you have permission to use.";}
        final String failure=error;owner.handler.post(()->{if(current!=generation||!dialog.isShowing())return;box.removeAllViews();LinearLayout tools=owner.row(owner);tools.addView(owner.action(owner,"Up",()->{if(path.size()>1){path.remove(path.size()-1);load();}}));tools.addView(owner.action(owner,"New folder",this::newFolder));tools.addView(owner.action(owner,"Paste copy",this::paste));box.addView(tools);if(failure!=null){box.addView(owner.text(owner,failure,15));return;}if(entries.isEmpty())box.addView(owner.action(owner,"This folder is empty",()->{}));for(Entry e:entries){TextView row=owner.action(owner,(e.directory?"▱  ":"▤  ")+e.name,()->{if(e.directory){path.add(e.id);load();}else owner.open(new Intent(Intent.ACTION_VIEW).setDataAndType(document(e.id),e.mime).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION));});row.setOnLongClickListener(v->{menu(row,e);return true;});row.setOnGenericMotionListener((v,event)->{if((event.getButtonState()&android.view.MotionEvent.BUTTON_SECONDARY)!=0){menu(row,e);return true;}return false;});box.addView(row);}});
    });}
    void newFolder(){owner.prompt("New folder","New folder",name->run(()->{Uri created=DocumentsContract.createDocument(owner.getContentResolver(),document(path.get(path.size()-1)),DocumentsContract.Document.MIME_TYPE_DIR,name);if(created==null)throw new IOException();}));}
    interface Operation{void run()throws Exception;}
    void run(Operation operation){owner.io.execute(()->{boolean ok;try{operation.run();ok=true;}catch(Exception e){ok=false;}final boolean completed=ok;owner.handler.post(()->{owner.toast(completed?"File operation completed":"The document provider could not complete this operation.");load();});});}
    void menu(View anchor,Entry e){PopupMenu menu=new PopupMenu(owner,anchor);menu.getMenu().add("Rename").setOnMenuItemClickListener(i->{owner.prompt("Rename",e.name,name->run(()->{if(DocumentsContract.renameDocument(owner.getContentResolver(),document(e.id),name)==null)throw new IOException();}));return true;});
        if(!e.directory){menu.getMenu().add("Copy").setOnMenuItemClickListener(i->{copied=document(e.id);copiedName=e.name;copiedMime=e.mime;owner.toast("File selected. Navigate to a destination and choose Paste copy.");return true;});menu.getMenu().add("Share").setOnMenuItemClickListener(i->{Uri uri=document(e.id);owner.open(Intent.createChooser(new Intent(Intent.ACTION_SEND).setType(e.mime).putExtra(Intent.EXTRA_STREAM,uri).setClipData(ClipData.newRawUri(e.name,uri)).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION),"Share file"));return true;});}
        menu.getMenu().add("Delete…").setOnMenuItemClickListener(i->{new AlertDialog.Builder(owner).setTitle("Delete "+e.name+"?").setMessage("The document provider will delete this item. This may be permanent.").setPositiveButton("Delete",(d,n)->run(()->{if(!DocumentsContract.deleteDocument(owner.getContentResolver(),document(e.id)))throw new IOException();})).setNegativeButton("Cancel",null).show();return true;});menu.show();}
    void paste(){if(copied==null){owner.toast("Long-press a file and choose Copy first.");return;}final Uri source=copied,parent=document(path.get(path.size()-1));final String name=copiedName,mime=copiedMime;owner.prompt("Copy as",name,destinationName->run(()->{Uri destination=DocumentsContract.createDocument(owner.getContentResolver(),parent,mime,destinationName);if(destination==null)throw new IOException();try(InputStream in=owner.getContentResolver().openInputStream(source);OutputStream out=owner.getContentResolver().openOutputStream(destination,"wt")){byte[] buffer=new byte[65536];int n;while((n=in.read(buffer))!=-1)out.write(buffer,0,n);}catch(Exception failure){try{DocumentsContract.deleteDocument(owner.getContentResolver(),destination);}catch(Exception ignored){}throw failure;}}));}
}

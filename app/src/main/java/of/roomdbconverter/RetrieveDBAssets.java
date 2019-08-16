package of.roomdbconverter;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class RetrieveDBAssets {

    private static ArrayList<AssetEntry> dbassets;
    private static HashMap<String,AssetEntry> assethashmap;

    public static ArrayList<AssetEntry> getAssets(Context context) {
        dbassets = new ArrayList<>();
        assethashmap = new HashMap<>();
        AssetManager am = context.getAssets();
        //getAssetEntries(context,"",am);
        getAssets(context,"");
        /*
        for (HashMap.Entry<String, AssetEntry> h: assethashmap.entrySet()) {
            if (h.getValue().getAssetType() == AssetEntry.ASSETTYPE_SQLITEDB) {
                dbassets.add(h.getValue());
            }
        }
        */
        return dbassets;
    }

    private static void getAssets(Context context, String path) {
        AssetFileDescriptor afd;
        if (path.length() == 0) {
            assethashmap.clear();
        }
        else {
        }
        try {
            String[] assetlist = context.getAssets().list(path);
            StringBuilder sb = new StringBuilder();
            for (String s: assetlist) {
                sb.append("\n\t\tFound asset ").append(s).append(" at path ").append(path);
            }
            //Log.d("GAASSETLIST","Asset List for path " + path + sb.toString());
            for (String s: assetlist) {
                if (!assethashmap.containsKey(path+s)) {
                    AssetEntry ae = new AssetEntry(s,path);
                    if (ae.isAssetDirectory(context,path,s)) {
                        ae.setAssetType(AssetEntry.ASSETTYPE_DIRECTORY);
                        getAssets(context,ae.getAssetPath());
                    } else {
                        ae.setAssetType(AssetEntry.ASSETTYPE_FILE);
                        if (ae.getAssetPath().length() > 0) {
                            ae.setAssetPath(ae.getAssetPath() + File.separator);
                        }
                        if (AssetEntry.checkAssetDBValidity(context,ae.getAssetPath()+ae.getAssetName())) {
                            ae.setAssetSize(AssetEntry.getLastAssetSize());
                            ae.setAssetType(AssetEntry.ASSETTYPE_SQLITEDB);
                            dbassets.add(ae);
                        }
                    }
                    assethashmap.put(path+s,new AssetEntry(s,path));
                }
            }
        } catch (IOException e) {
            //Log.d("GAGETASSETLIST","Exception when trying to retrieve list for path " + path);
        }
    }



    private static void getAssetEntries(Context context, String path, AssetManager am) {
        String[] assetlist;
        AssetFileDescriptor afd;
        try {
            //Log.d("GAE","Retrieving List for path " + path); //TODO remove
            assetlist = am.list(path);
            for (String s: assetlist) {
                //Log.d("GAE","Extracted file " + s + " from assets using path " + path ); //TODO remove
            }
            if (assetlist.length > 0) {
                for (String s : assetlist) {
                    //Log.d("GAE","Attempting to retrieve AFD for " + path + s); //TODO remove
                    try {
                        path.replace(File.separator + File.separator,File.separator);
                        if (path.length() > 0) {
                            afd = am.openFd(path + s);
                        } else {
                            afd = am.openFd(path + s);
                        }
                        afd.close();
                    } catch (FileNotFoundException e) {
                        //e.printStackTrace();
                        if (e.getMessage().contains("compressed")) {
                            String pathtoopen = path;
                            if (path.length() > 0) {
                                pathtoopen = path + File.separator;
                                pathtoopen.replace(File.separator + File.separator,File.separator);
                            }
                            if (AssetEntry.checkAssetDBValidity(context,(pathtoopen + s).replace(File.separator + File.separator,File.separator))) {
                                //Log.d("GAE","Valid Database found"); //TODO remove
                                dbassets.add(new AssetEntry(s,pathtoopen + s));
                            } else {
                                //Log.d("GAE",s +" is not a Database"); //TODO remove
                            }
                        } else {
                            if (path.length() > 0) {
                                path = path + File.separator;
                            }
                            getAssetEntries(context, path +  s, am);
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

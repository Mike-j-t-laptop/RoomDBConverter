package of.roomdbconverter;

import android.content.Context;
import android.content.res.AssetFileDescriptor;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class AssetEntry {

    public static final int BUFFERSIZE = 4 * 1024 * 1024;
    public static final int ASSETTYPE_UNDEFINED = 0;
    public static final int ASSETTYPE_DIRECTORY = 1;
    public static final int ASSETTYPE_FILE = 2;
    public static final int ASSETTYPE_SQLITEDB = 3;

    static int assetSize = 0;

    String mAssetName;
    String mAssetPath;
    int mAssetType = ASSETTYPE_UNDEFINED;
    int mAssetSize;
    boolean mAssetLoaded = false;

    public AssetEntry(String assetName, String path) {
        this.mAssetName = assetName;
        this.mAssetPath = path;
        this.mAssetSize = 0;
    }

    public String getAssetName() {
        return mAssetName;
    }

    public void setAssetName(String assetName) {
        this.mAssetName = assetName;
    }

    public String getAssetPath() {
        if (mAssetType == ASSETTYPE_DIRECTORY) {
            if (mAssetPath.length() > 0) {
                return mAssetPath + File.separator + mAssetName;
            } else {
                return mAssetName;
            }
        } else {
            return mAssetPath;
        }
    }

    public void setAssetPath(String assetPath) {
        this.mAssetPath = assetPath;
    }

    public int getAssetType() {
        return mAssetType;
    }

    public void setAssetType(int mAssetType) {
        this.mAssetType = mAssetType;
    }

    public void setAssetSize(int assetSize) {
        this.mAssetSize = assetSize;
    }

    @NonNull
    @Override
    public String toString() {
        return mAssetPath + mAssetName;
    }

    public boolean isAssetLoaded() {
        return mAssetLoaded;
    }

    public void setAssetLoaded(boolean assetLoaded) {
        this.mAssetLoaded = assetLoaded;
    }

    /**
     * Build the path within the assets folder applying subfolders.
     * @param directories the directories (subfolders) within the assets folder (null or empty if no sub-directories)
     * @param filename the filename in the assets folder
     * @return
     */
    public static String buildAssetPath(String[] directories, String filename) {
        //Log.d("BUILDASSETPATH","Building Asset path for asset " + filename);
        String rv ="";
        StringBuilder sb = new StringBuilder();
        final String SEPERATOR = File.separator;
        if (directories != null && directories.length > 0) {
            for (String s: directories) {
                sb.append(s);
                if (s.length() > 0) {
                    if (!s.substring(s.length() - 1, s.length()).equals(SEPERATOR)) {
                        sb.append(SEPERATOR);
                    }
                }
            }
            sb.append(filename);
            rv = sb.toString();
        } else {
            rv = filename;
        }
        //Log.d("BUILDASSETPATH","Built Asset path for asset " + filename + " as " + rv);
        return rv;
    }

    public static String buildAssetPath(ArrayList<String> directories, String filename) {
        String[] d = new String[directories.size()];
        for (int i=0;i < directories.size();i++) {
            d[i] = directories.get(i);
        }
        return buildAssetPath(d,filename);
    }



    /**
     * Check that the asset exists and is a valid database according to the header
     * @param context       The context for determining the location of the database
     * @param assetPath     The path to the asset within the assets folder
     * @return              true if the asset is a database
     */
    public static boolean checkAssetDBValidity(Context context, String assetPath) {
        //Log.d("CHECKVALIDASSET","Checking that asset at " + assetPath + " is valid DB."); //TODO remove
        InputStream asset_is;
        byte[] header = new byte[16];
        try {
            asset_is = context.getAssets().open(assetPath);
        } catch (IOException e) {
            throw new RuntimeException("Unable to Open Asset at " + assetPath);
        }
        try {
            asset_is.read(header);
            asset_is.close();
            if (!new String(header).equals(SQLiteConstants.SQLITEFILEHEADER)) {
                //Log.d("CHECKVALIDASSET","Invalid header found >>" + String.valueOf(header) + "<< instead of " + SQLITEFILEHEADER); //TODO remove
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Unable to read Asset header (first 16 bytes)");
        }
        //Log.d("CHECKVALIDASSET","Validated " + assetPath); //TODO remove
        return true;
    }

    /**
     * Copy the asset to the location as a database
     * @param context       The context for retrieving the asset
     * @param assetPath     The path (within asset manager) of the asset
     * @param databasePath  The path to the database that is to be created.
     */
    public static boolean copyAsset(Context context, String assetPath, String databasePath) {
        //Log.d("COPYASSET","Attemtping to copy asset " + assetPath + " to " + databasePath);
        InputStream asset_is;
        OutputStream db;
        byte[] buffer = new byte[BUFFERSIZE];
        int length;
        int read = 0;
        int copied = 0;
        try {
            asset_is = context.getAssets().open(assetPath);
            db = new FileOutputStream(new File(databasePath));
            while ((length = asset_is.read(buffer)) > 0) {
                read = read + length;
                db.write(buffer,0,length);
                copied = copied + length;
            }
            db.flush();
            db.close();
            asset_is.close();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(
                    "IO Error copying asset. " +
                            "Bytes successfully read = " + String.valueOf(read) +
                            " Bytes Successfully written = " + String.valueOf(copied));
        }
        assetSize = copied;
        return true;
        //Log.d("COPYASSET","Copied " + String.valueOf(copied) + " bytes from " + assetPath + " to " + databasePath); //TODO remove
    }

    public static int getLastAssetSize() {
        return assetSize;
    }

    public boolean isAssetDirectory(Context context, String path, String asset) {
        boolean rv = true;
        AssetFileDescriptor afd = null;
        if (path.length() > 0 ) {
            path = path + File.separator;
        }
        try {
            afd = context.getAssets().openFd(path + asset);
            afd.close();
        } catch (FileNotFoundException e) {
            if (e.getMessage().contains("compressed")) {
                rv = false;
            }
        } catch (IOException e) {
        }
        return rv;
    }
}

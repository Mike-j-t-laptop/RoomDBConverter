package of.roomdbconverter;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements Serializable {

    public static final int REQUESTCODE_TABLEINFO = 99;
    public static final int REQUESTCODE_COLUMNINFO = 98;
    public static final String INTENTKEY_TABLEINFO = "ik_tableinfo";
    public static final String INTENTKEY_COLUMNINFO = "ik_columninfo";
    public static final String INTENTKEY_CHANGEDCOLUMNINFO = "ik_changedcolumninfo";

    private static final int ENTITY_TABLE = 0;
    private static final int ENTITY_COLUMN = 1;
    private static final int ENTITY_INDEX = 2;
    private static final int ENTITY_TRIGGER = 3;
    private static final int ENTITY_VIEW = 4;
    private static final int ENTITY_FOREIGNKEY = 5;
    private static String[] ENTITYTITLE;

    int mCurrentEntity = ENTITY_TABLE;

    Context mContext;
    Button mConvert;
    ListView mDBAssetsListView, mDBEntityLisView;
    LinearLayout mDBInfoHdr, mDBInfo,  mDBPathInfoHdr;
    TextView mSelectedDBInfoHdr, mDBName, mDBVersion,  mDBDiskSize, mDBPath,
            mDBTablesHdr, mDBColumnsHdr, mDBIndexesHdr, mDBFrgnKeysHdr, mDBTriggersHdr,mDBViewsHdr,
            mDBTables,mDBColumns, mDBIndexes, mDBFrgnKeys, mDBTriggers, mDBViews, mDBEntitiesListHdr;

    ArrayList<AssetEntry> mDBAssets;
    ArrayAdapter<AssetEntry> mDBAssetsAA;
    EntityTableAdapter mDBTablesAA;
    EntityColumnAdapter mDBColumnsAA;
    EntityIndexAdapter mDBIndexesAA;
    EntityFKeyAdapter mDBForeignKeysAA;
    EntityTriggerAdapter mDBTriggersAA;
    EntityViewAdapter mDBViewsAA;

    ArrayList<PreExistingAssetDBInspect> mPEADBIList;
    ArrayList<TableInfo> mCurrentTables;
    ArrayList<ForeignKeyInfo> mCurrentForeignKeys;
    ArrayList<ColumnInfo> mCurrentColumns;
    ArrayList<IndexInfo> mCurrentIndexes;
    ArrayList<TriggerInfo> mCurrentTriggers;
    ArrayList<ViewInfo> mCurrentViews;
    PreExistingAssetDBInspect mCurrentPEADBI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        ENTITYTITLE = this.getResources().getStringArray(R.array.entitytypes);
        setContentView(R.layout.activity_main);
        ExternalStoragePermissions.verifyStoragePermissions(this);

        mConvert = this.findViewById(R.id.convert);

        mDBAssetsListView = this.findViewById(R.id.dbassetsList);
        (mDBEntityLisView = this.findViewById(R.id.dbentitieslist)).setVisibility(View.GONE);
        (mSelectedDBInfoHdr = this.findViewById(R.id.selectedassetheading)).setVisibility(View.GONE);
        (mDBInfoHdr = this.findViewById(R.id.dbinfohdr)).setVisibility(View.GONE);
        (mDBInfo = this.findViewById(R.id.dbinfo)).setVisibility(View.GONE);
        mDBName = this.findViewById(R.id.dbname);
        mDBVersion = this.findViewById(R.id.dbversion);
        mDBDiskSize = this.findViewById(R.id.dbdisksize);
        mDBTables = this.findViewById(R.id.dbtablecount);
        mDBPath = this.findViewById(R.id.dbpath);
        mDBColumns = this.findViewById(R.id.dbcolumncount);
        mDBIndexes = this.findViewById(R.id.dbindexcount);
        mDBFrgnKeys = this.findViewById(R.id.dbforeignkeycount);
        mDBTriggers = this.findViewById(R.id.dbtriggercount);
        mDBViews = this.findViewById(R.id.dbviewcount);
        mDBTablesHdr = this.findViewById(R.id.dbtablecounthdr);
        mDBColumnsHdr = this.findViewById(R.id.dbtablecolumncounthdr);
        mDBIndexesHdr = this.findViewById(R.id.dbindexcounthdr);
        mDBFrgnKeysHdr = this.findViewById(R.id.dbforeignkeycounthdr);
        mDBTriggersHdr = this.findViewById(R.id.dbtriggercounthdr);
        mDBViewsHdr = this.findViewById(R.id.dbviewcounthdr);
        (mDBPathInfoHdr = this.findViewById(R.id.pathinfosection)).setVisibility(View.GONE);
        (mDBEntitiesListHdr = this.findViewById(R.id.dbentitieslisthdr)).setVisibility(View.GONE);
        manageDBAssetsListView();
        manageDatabaseInformationListeners();
        mDBAssets = RetrieveDBAssets.getAssets(this);
        mConvert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (ConvertPreExistingDatabaseToRoom.Convert(mContext,mCurrentPEADBI,"MyConversion20190815_12:16","java","java",ConvertPreExistingDatabaseToRoom.MESSAGELEVEL_ERROR) ==0) {
                            showConversionResults();
                        } else {
                            showConversionResults();
                        }

                    }
                }).start();
            }
        });
    }

    /**
     * Manage the list of SQlite Database assets,
     * first; get the assets (SQLite databases only),
     * second; if the adapter is null then instantiate it and tie it to the
     * ListView,
     * third; if if the adapter is null then set the item on click listener to pass
     * the select asset to the handleSelected method
     * fourth; if the adapter is not null, notify the adapter that changes may have
     * been made to the underlying data.
     *
     */
    private void manageDBAssetsListView() {
        mDBAssets = RetrieveDBAssets.getAssets(this);
        if (mDBAssetsAA == null) {
            mDBAssetsAA = new ArrayAdapter<>(this,R.layout.assetlist_layout,R.id.assetpath,mDBAssets);
            mDBAssetsListView.setAdapter(mDBAssetsAA);
            mDBAssetsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    hanldeSelected(mDBAssetsAA.getItem(position));
                }
            });
        } else {
            mDBAssetsAA.notifyDataSetChanged();
        }
    }

    /**
     * Manage the Database information headers and actual data to add on click
     * listeners that result in the respective adapter being used for the database list
     * information (i.e. switch between the various list tables, columns, indexes, triggers and views)
     */
    private void manageDatabaseInformationListeners() {

        mDBTables.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCurrentEntity = ENTITY_TABLE;
                selectAdapter();
            }
        });
        mDBTablesHdr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCurrentEntity = ENTITY_TABLE;
                selectAdapter();
            }
        });
        mDBColumns.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCurrentEntity = ENTITY_COLUMN;
                selectAdapter();
            }
        });
        mDBColumnsHdr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCurrentEntity = ENTITY_COLUMN;
                selectAdapter();
            }
        });
        mDBIndexes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCurrentEntity = ENTITY_INDEX;
                selectAdapter();
            }
        });
        mDBIndexesHdr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCurrentEntity = ENTITY_INDEX;
                selectAdapter();
            }
        });
        mDBFrgnKeys.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCurrentEntity = ENTITY_FOREIGNKEY;
                selectAdapter();
            }
        });
        mDBFrgnKeysHdr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCurrentEntity = ENTITY_FOREIGNKEY;
                selectAdapter();
            }
        });
        mDBTriggers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCurrentEntity = ENTITY_TRIGGER;
                selectAdapter();
            }
        });
        mDBTriggersHdr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCurrentEntity = ENTITY_TRIGGER;
                selectAdapter();
            }
        });
        mDBViews.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCurrentEntity = ENTITY_VIEW;
                selectAdapter();
            }
        });
        mDBViewsHdr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCurrentEntity = ENTITY_VIEW;
                selectAdapter();
            }
        });

    }

    /**
     * Manage the database information listable data (tables, columns, indexes, triggers and views)
     * first; call the setDBEntityList method to refresh/build the underlying data
     * second; if the adapters are null (should all be in the same state) then instantiate the
     * adapters
     * third; if already instantiated then notify the adapters that the underlying data may have changed
     * fourth; call the selectAdapter method to tie the respective adapter to the ListView
     */
    private void manageDBEntityListView() {
        setDBEntityLists();
        if (mDBTablesAA == null) {
            mDBTablesAA = new EntityTableAdapter(this, mCurrentTables);
            mDBColumnsAA = new EntityColumnAdapter(this,mCurrentColumns);
            mDBIndexesAA = new EntityIndexAdapter(this,mCurrentIndexes);
            mDBForeignKeysAA = new EntityFKeyAdapter(this,mCurrentForeignKeys);
            mDBTriggersAA = new EntityTriggerAdapter(this,mCurrentTriggers);
            mDBViewsAA = new EntityViewAdapter(this,mCurrentViews);
            manageDBEntityListListViewListeners();
        } else {
            mDBTablesAA.notifyDataSetChanged();
            mDBColumnsAA.notifyDataSetChanged();
            mDBIndexesAA.notifyDataSetChanged();
            mDBTriggersAA.notifyDataSetChanged();
            mDBViewsAA.notifyDataSetChanged();
            mDBForeignKeysAA.notifyDataSetChanged();

        }
        selectAdapter();
    }

    /**
     * Manage the EntityListView Listeners, currently longclicking a column when viewing
     * columns will allow some column attributes to be changed.
     */
    private void manageDBEntityListListViewListeners() {
        mDBEntityLisView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Object x = parent.getItemAtPosition(position);
                if (x instanceof TableInfo) {
                    return true;
                    //Intent intent = new Intent(view.getContext(),TableInfoActivity.class);
                    //intent.putExtra(INTENTKEY_TABLEINFO,(TableInfo)x);
                    //startActivityForResult(intent,REQUESTCODE_TABLEINFO);
                }
                if (x instanceof ColumnInfo) {
                    Intent intent = new Intent(view.getContext(),ColumnInfoActivity.class);
                    intent.putExtra(INTENTKEY_COLUMNINFO,(ColumnInfo)x);
                    startActivityForResult(intent,REQUESTCODE_COLUMNINFO);
                }
                return true ;
            }
        });
    }

    /**
     * Tie the respective adapter to the database information list ListView
     */
    private void selectAdapter() {
        switch (mCurrentEntity) {
            case ENTITY_COLUMN:
                mDBEntityLisView.setAdapter(mDBColumnsAA);
                mDBEntitiesListHdr.setText(ENTITYTITLE[ENTITY_COLUMN]);
                break;
            case ENTITY_INDEX:
                mDBEntityLisView.setAdapter(mDBIndexesAA);
                mDBEntitiesListHdr.setText(ENTITYTITLE[ENTITY_INDEX]);
                break;
            case ENTITY_TRIGGER:
                mDBEntityLisView.setAdapter(mDBTriggersAA);
                mDBEntitiesListHdr.setText(ENTITYTITLE[ENTITY_TRIGGER]);
                break;
            case ENTITY_VIEW:
                mDBEntityLisView.setAdapter(mDBViewsAA);
                mDBEntitiesListHdr.setText(ENTITYTITLE[ENTITY_VIEW]);
                break;
            case ENTITY_FOREIGNKEY:
                mDBEntityLisView.setAdapter(mDBForeignKeysAA);
                mDBEntitiesListHdr.setText(ENTITYTITLE[ENTITY_FOREIGNKEY]);
                break;
            default:
                mDBEntityLisView.setAdapter(mDBTablesAA);
                mDBEntitiesListHdr.setText(ENTITYTITLE[ENTITY_TABLE]);
        }
    }

    /**
     * Handle an Asset Entry being clicked,
     * 1; if the List of PreExisting Assets is null initialise the list as an empty ArrayList
     * of PreExistingAssetDBInspect (PEADBI) objects,
     * 2; search the PEADBI ArrayList to see if this asset already exists in the ArrayList,
     * 3, if it does not already exist create a new PEADBI object,
     * this includes importing the database (copying from the assets)
     * 4; if imported then add the PEADBI to the ArrayList
     * 5; if imported select the last object of the PEADBI ArrayList as the current
     * 6, if not imported (already exists) set the current PEADBI to the matched PEADBI
     * 7; set the Database Information layout to VISIBLE as data now exists
     * 8; set the various TextViews with the respective values
     * 9; call manageDBEntityListView to setup/refresh the Database Information List
     * @param ae    The clicked AssetEntry
     */
    public void hanldeSelected(AssetEntry ae) {
        if (mPEADBIList == null) {
            mPEADBIList = new ArrayList<>();
        }
        //Toast.makeText(this,"You clicked on DB " + ae.getAssetName() + " in path " + ae.getAssetPath(),Toast.LENGTH_SHORT).show();
        boolean stored = false;
        int i =0;
        for (PreExistingAssetDBInspect p: mPEADBIList ) {
            if (p.getAssetFileName().equals(ae.getAssetName()) && p.getAssetPath().equals(ae.getAssetPath()+ae.getAssetName())) {
                stored = true;
                break;
            }
            i++;
        }
        if (!stored) {
            mPEADBIList.add(new PreExistingAssetDBInspect(this,ae.getAssetName(),ae.getAssetName(),ae.getAssetPath().split(File.pathSeparator)));
            mCurrentPEADBI = mPEADBIList.get(mPEADBIList.size()-1);
            ae.setAssetLoaded(true);
        } else {
            mCurrentPEADBI = mPEADBIList.get(i);
        }
        mSelectedDBInfoHdr.setVisibility(View.VISIBLE);
        mDBInfo.setVisibility(View.VISIBLE);
        mDBInfoHdr.setVisibility(View.VISIBLE);
        mDBPathInfoHdr.setVisibility(View.VISIBLE);
        mDBEntityLisView.setVisibility(View.VISIBLE);
        mDBEntitiesListHdr.setVisibility(View.VISIBLE);
        mDBName.setText(mCurrentPEADBI.getDatabaseName());
        mDBVersion.setText(String.valueOf(mCurrentPEADBI.getDatabaseVersion()));
        mDBDiskSize.setText(String.valueOf(mCurrentPEADBI.getDatabaseDiskSize()));
        mDBTables.setText(String.valueOf(mCurrentPEADBI.getTableCount()));
        mDBColumns.setText(String.valueOf(mCurrentPEADBI.getColumnCount()));
        mDBIndexes.setText(String.valueOf(mCurrentPEADBI.getIndexCount()));
        mDBFrgnKeys.setText(String.valueOf(mCurrentPEADBI.getForeignKeyCount()));
        mDBTriggers.setText(String.valueOf(mCurrentPEADBI.getTriggerCount()));
        mDBViews.setText(String.valueOf(mCurrentPEADBI.getViewCount()));
        mDBPath.setText(mCurrentPEADBI.getAssetPath());
        manageDBEntityListView();
    }

    /**
     * Rebuild the Entity Lists according to the current PEADBI
     */
    private void setDBEntityLists() {
        if (mCurrentTables == null) {
            mCurrentTables = new ArrayList<>();
        }
        mCurrentTables.clear();
        for (TableInfo ti: mCurrentPEADBI.getTableInfo()) {
            TableInfo newti = new TableInfo(ti.getTableName(),ti.getSQL(),
                    ti.getColumns(), ti.getColumnLookup(),
                    ti.getForeignKeyList(), ti.getPrimaryKeyList(), ti.getPrimaryKeyListAlternativeNames(),
                    ti.getReferencelevel(),ti.getIndexCount(),ti.getTriggerCount(),ti.isRowid(),ti.isRoomTable()
            );
            mCurrentTables.add(newti);
        }

        if (mCurrentColumns == null) {
            mCurrentColumns = new ArrayList<>();
        }
        mCurrentColumns.clear();
        for (ColumnInfo ci: mCurrentPEADBI.getColumnInfo()) {
            ColumnInfo newci = new ColumnInfo(
                    ci.getColumnName(), ci.getAlternativeColumnName(),
                    ci.getOwningTable(),
                    ci.getColumnType(),ci.getDerivedTypeAffinity(),ci.getFinalTypeAffinity(),ci.getObjectElementType(),
                    ci.isNotNull(),
                    ci.getCID(),
                    ci.getPrimaryKeyPosition(),
                    ci.getDefaultValue(),
                    ci.isUnique(),
                    ci.isRowidAlias(),
                    ci.isAutoIncrementCoded(),
                    ci.getColumnCreateSQL(),ci.getOriginalColumnName(),ci.getOriginalAlternativeColumnName()
            );
            mCurrentColumns.add(newci);
        }

        if (mCurrentIndexes == null) {
            mCurrentIndexes = new ArrayList<>();
        }
        mCurrentIndexes.clear();
        for (IndexInfo ii: mCurrentPEADBI.getIndexInfo()) {
            ArrayList<IndexColumnInfo> newici = new ArrayList<>();
            for (IndexColumnInfo ici: ii.getColumns()) {
                newici.add(new IndexColumnInfo(ici.getColumnName(),ici.getColumnIndexRank(),ici.getColumnTableRank()));
            }
            IndexInfo newii = new IndexInfo(ii.getIndexName(),ii.getSQL(),ii.isUnique(),ii.getTableName(),newici,ii.getWhereClause());
            mCurrentIndexes.add(newii);
        }

        if (mCurrentForeignKeys == null) {
            mCurrentForeignKeys = new ArrayList<>();
        }
        mCurrentForeignKeys.clear();
        for(TableInfo ti: mCurrentPEADBI.getTableInfo()) {
            ArrayList<ForeignKeyInfo> newfki = new ArrayList<>();
            for (ForeignKeyInfo ifki: ti.getForeignKeyList()) {
                newfki.add(new ForeignKeyInfo(ti.getTableName(),ifki.getParentTableName(),ifki.getChildColumnNames(),ifki.getParentColumnNames(),ifki.getOnUpdate(),ifki.getOnDelete(),ifki.isDeferable()));
            }
            mCurrentForeignKeys.addAll(newfki);
        }

        if (mCurrentTriggers == null) {
            mCurrentTriggers = new ArrayList<>();
        }
        mCurrentTriggers.clear();
        for (TriggerInfo tri: mCurrentPEADBI.getTriggerInfo()) {
            TriggerInfo newtri = new TriggerInfo(tri.getTriggerName(),tri.getTriggerTable(),tri.getTriggerSQL());
            mCurrentTriggers.add(newtri);
        }
        if (mCurrentViews == null) {
            mCurrentViews = new ArrayList<>();
        }
        mCurrentViews.clear();
        for (ViewInfo vi: mCurrentPEADBI.getViewInfo()) {
            ViewInfo newvi = new ViewInfo(vi.getViewName(),vi.getViewTable(),vi.getViewSQL());
            mCurrentViews.add(newvi);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUESTCODE_COLUMNINFO:
                    ColumnInfo changes = (ColumnInfo) data.getSerializableExtra(INTENTKEY_CHANGEDCOLUMNINFO);
                    ColumnInfo original = (ColumnInfo) data.getSerializableExtra(INTENTKEY_COLUMNINFO);
                    applyColumnChanges(changes,original);
                    break;
                case REQUESTCODE_TABLEINFO:
                    break;
            }
        }
        if (requestCode == RESULT_CANCELED) {
        }
        super.onActivityResult(requestCode,resultCode,data);
    }

    private void applyColumnChanges(ColumnInfo changes, ColumnInfo original) {
        boolean changes_applied = false;
        if (
                original.getColumnName().equals(changes.getColumnName())
                        && original.getAlternativeColumnName().equals(changes.getAlternativeColumnName())
                        && original.getFinalTypeAffinity().equals(changes.getFinalTypeAffinity())
                        && original.getObjectElementType().equals(changes.getObjectElementType())
                        && original.isNotNull() == changes.isNotNull()
        ) {
            return;
        }
        for (TableInfo ti: mCurrentPEADBI.getTableInfo()) {
            if (ti.getTableName().equals(original.getOwningTable())) {
                for (ColumnInfo ci: ti.getColumns()) {
                    if (ci.getColumnName().equals(original.getColumnName())) {
                        ci.setColumnName(changes.getColumnName());
                        ci.setAlternativeColumnName(changes.getAlternativeColumnName());
                        ci.setFinalTypeAffinity(changes.getFinalTypeAffinity());
                        ci.setObjectElementType(changes.getObjectElementType());
                        ci.setNotNull(changes.isNotNull());
                        changes_applied = true;
                    }
                }
            }
        }
        if (changes_applied) {
            manageDBEntityListView();
        }
    }

    public void showConversionResults() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                String preamble = "Copied a total of " +
                        String.valueOf(ConvertPreExistingDatabaseToRoom.getTotalCopiedRows() +
                                " rows, " +
                                "out of " +
                                String.valueOf(ConvertPreExistingDatabaseToRoom.getTotalOriginalRows())
                                + "\nDouble check Original Count is " + String.valueOf(ConvertPreExistingDatabaseToRoom.getTor()) +
                                " Copied count is " + String.valueOf(ConvertPreExistingDatabaseToRoom.getTcr())
                                + ".\n\n"
                        );
                AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                alertDialog.setTitle("Result");
                alertDialog.setMessage(preamble + ConvertPreExistingDatabaseToRoom.getMessagesAsString());
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        for (PreExistingAssetDBInspect p: mPEADBIList) {
            p.closeInspectionDatabase();
        }
        super.onDestroy();
    }
}

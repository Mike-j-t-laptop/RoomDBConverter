package of.roomdbconverter;

import java.util.ArrayList;
import java.util.List;

import static of.roomdbconverter.RoomCodeCommonUtils.*;

public class RoomEntityBuilder {
    private static final String ENTITYSTART = "@Entity(tableName = \"";
    private static final String ENTITYEND = "\n)";
    private static final String ENTITYNOTNULL = "@NonNull";
    private static final String ENTITYNULLABLE = "@Nullable";

    private static final String CLASSSTART = "public class ";
    private static final String CLASSEND = "\n}";

    private static final String MEMBERSTART = "private ";
    private static final String MEMBEREND = ";\n";

    private static final String GETTERSTART = INDENT + "public ";
    private static final String GETTERNAMEPREFIX = " get";
    private static final String GETTERNAMESUFFIX = "() {\n" + INDENT + INDENT;
    private static final String GETTERRETURNPREFIX = "return this.";
    private static final String GETTERRETURNSUFFIX = ";\n" + INDENT + "}";
    private static final String SETTERSTART = GETTERSTART + "void set";
    private static final String SETTERPARAMETERPREFIX = "(";
    private static final String SETTERPARAMETERSUFFIX = ") {\n" + INDENT + INDENT + "this.";
    private static final String SETTERASSIGN = " = ";
    private static final String SETTERASSIGNSUFFIX = ";\n" + INDENT + "}";

    private static final String PRIMARYKEYS_START = "\n" + INDENT + "primaryKeys = {";
    private static final String PRIMARYKEYSTART = "@PrimaryKey(";
    private static final String PRIMARYKEYEND = ")";
    private static final String AUTOGENERATE = "autoGenerate = true";

    private static final String COLUMNINFOSTART = "@ColumnInfo(name = \"";
    private static final String COLUMNINFOEND = "\")";

    private static final String INDICIES_START = "\n" + INDENT + "indices = {";
    private static final String INDEXSTART = "\n" + INDENT + INDENT + INDENT + "@Index(";
    private static final String INDEXNAME = "name = \"";
    private static final String INDEXVALUE = "value = {";
    private static final String INDEXUNIQUE = "unqiue = true,";
    private static final String INDEXCOLUMNSSTART = "\n" + INDENT + INDENT + INDENT + INDENT + "";

    private static final String FOREIGNKEYS_START = "\n" + INDENT + ",foreignKeys = {";
    private static final String FOREIGNKEYS_END = "\n" + INDENT + "}";
    private static final String FOREIGNKEYSTART = "\n" + INDENT + INDENT + "@ForeignKey(\n" + INDENT + INDENT + INDENT + "entity=";
    private static final String FOREIGNKEYEND = ")";
    private static final String FOREIGNKEYPARENTCOLUMNSTART = "\n" + INDENT + INDENT + INDENT + ",parentColumns = {";
    private static final String FOREIGNKEYCHILDCOLUMNSTART = "\n" + INDENT + INDENT + INDENT + ",childColumns = {";
    private static final String FOREIGNKEYCOLUMNEND = "\n" + INDENT + INDENT + INDENT + "}";
    private static final String FOREIGNKEYDEFERRED = "\n" + INDENT + INDENT + INDENT + ",deferred = true";
    private static final String FOREIGNKEYONUPDATE = "\n" + INDENT + INDENT + INDENT + ",onUpdate = ";
    private static final String FOREIGNKEYONDELETE = "\n" + INDENT + INDENT + INDENT + ",onDelete = ";
    private static final String FOREIGNKEYACTIONCASCADE = "ForeignKey.CASCADE";
    private static final String FOREIGNKEYACTIONSETNULL = "ForeignKey.SET_NULL";
    private static final String FOREIGNKEYACTIONSETDEFAULT = "ForeignKey.SET_DEFAULT";
    private static final String FOREIGNKEYACTIONRESTRICT = "ForeignKey.RESTRICT";
    private static final String FOREIGNKEYACTIONNOACTION = "ForeignKey.NO_ACTION";

    /**
     * Extarct the code for the current table (Entity)
     * @param peadbi    The entire database (needed for getting indexes associated/ON with the table)
     * @param ti        The TableInformation
     * @return          The generated code
     */
    public static ArrayList<String> extractEntityCode(PreExistingFileDBInspect peadbi, TableInfo ti) {

        String primaryKeysCode = buildPrimaryKeysIfAny(ti);
        String indiciesCode = buildIndexes(peadbi, ti);
        String foreignKeysCode = buildForeignKeys(ti,peadbi);
        boolean primaryKeysDone = (primaryKeysCode.length() > 0);
        ArrayList<String> entityCode = new ArrayList<>();
        String tableNameToCode = swapEnclosersForRoom(ti.getEnclosedTableName());
        if (tableNameToCode.length() < 1) {
            tableNameToCode = ti.getTableName();
        }
        String entityClassName = capitalise(ti.getTableName());

        entityCode.add(ENTITYSTART + tableNameToCode + "\""
                + primaryKeysCode
                + indiciesCode
                + foreignKeysCode
                + ENTITYEND
        );

        entityCode.add(CLASSSTART + entityClassName + "{");
        ArrayList<String> gettersAndSettersCode = new ArrayList<>();
        for (ColumnInfo ci: ti.getColumns()) {
            String columnNameToCode = swapEnclosersForRoom(ci.getAlternativeColumnName());
            if (columnNameToCode.length() < 1) {
                columnNameToCode = ci.getColumnName();
            }
            if (ci.isRowidAlias() || ci.isAutoIncrementCoded() && !primaryKeysDone) {
                entityCode.add(INDENT + PRIMARYKEYSTART + AUTOGENERATE + PRIMARYKEYEND);
            }
            boolean notNullCoded = false;
            if (ci.isNotNull()) {
                entityCode.add(INDENT + ENTITYNOTNULL);
                notNullCoded = true;
            }
            if (ci.getPrimaryKeyPosition() > 0 && !notNullCoded) {
                entityCode.add(INDENT + ENTITYNOTNULL);
                notNullCoded = true;
            }
            if (isColumnPartForeignKeyChild(ti,ci.getColumnName()) && !notNullCoded) {
                entityCode.add(INDENT + ENTITYNOTNULL);
                notNullCoded = true;
            }
            entityCode.add(INDENT + COLUMNINFOSTART + columnNameToCode + COLUMNINFOEND);
            entityCode.add(INDENT + MEMBERSTART + ci.getObjectElementType() + " " + lowerise(ci.getColumnName()) + MEMBEREND);
            // Build getter code
            gettersAndSettersCode.add(
                    GETTERSTART + ci.getObjectElementType() +
                            GETTERNAMEPREFIX + capitalise(ci.getColumnName()) +
                            GETTERNAMESUFFIX +
                            GETTERRETURNPREFIX + lowerise(ci.getColumnName()) +
                            GETTERRETURNSUFFIX
            );
            // Build setter code
            gettersAndSettersCode.add(
                    SETTERSTART + capitalise(ci.getColumnName()) +
                            SETTERPARAMETERPREFIX + ci.getObjectElementType() + " " + lowerise(ci.getColumnName()) +
                            SETTERPARAMETERSUFFIX + lowerise(ci.getColumnName()) +
                            SETTERASSIGN + lowerise(ci.getColumnName()) +
                            SETTERASSIGNSUFFIX
            );
        }
        // Add the getter and setter code
        entityCode.addAll(gettersAndSettersCode);
        // end the class
        entityCode.add(CLASSEND);
        return entityCode;
    }

    /**
     * Build and return a string for the ROOM Entity primaryKeys part
     * @param ti    The TableInfo object
     * @return      The ROOM Entity primaryKeys string
     */
    private static String buildPrimaryKeysIfAny(TableInfo ti) {
        ArrayList<String> columnsToCode = new ArrayList<>();
        List<String> pklist = ti.getPrimaryKeyList();
        List<String> enclpklist = ti.getPrimaryKeyListAlternativeNames();
        if (ti.getPrimaryKeyList().size() > 1) {
            for (int i=0; i < pklist.size(); i++) {
                if (enclpklist.get(i).length() > 0) {
                    columnsToCode.add(swapEnclosersForRoom(enclpklist.get(i)));
                } else {
                    columnsToCode.add(pklist.get(i));
                }
            }
        }
        if (columnsToCode.size() > 0 && ti.getPrimaryKeyList().size() == 0) {
            for (ColumnInfo ci: ti.getColumns()) {
                if (ci.getAlternativeColumnName().length() > 0) {
                    columnsToCode.add(swapEnclosersForRoom(ci.getAlternativeColumnName()));
                } else {
                    columnsToCode.add(ci.getColumnName());
                }
            }
        }
        StringBuilder pk = new StringBuilder();
        for (String s: columnsToCode) {
            if (pk.length() < 1) {
                pk.append(PRIMARYKEYS_START);
            }
            if (pk.length() > PRIMARYKEYS_START.length()) {
                pk.append(",\n").append(INDENT).append(INDENT);
            }
            pk.append("\"").append(s).append("\"");
        }
        if (pk.length() > 0) {
            pk.append("}");
            return "," + pk.toString();
        }
        return "";
    }

    //indices = {@Index(name = "ix01",value = {"myLong","myInt2"},unique = true)}

    /**
     * Build the ROOM indicies clause for the table
     * @param peadbi    The PreExistingAssetDBInspect object that the table belongs to
     * @param ti        The TableInfo object
     * @return          The ROOM indicies clause to be embedded in the @Entity for the table
     */
    private static String buildIndexes(PreExistingFileDBInspect peadbi, TableInfo ti) {
        StringBuilder ix = new StringBuilder();
        for (IndexInfo ii: peadbi.getIndexInfo()) {
            //NOTE skip if a partial index (i.e. if a WHERE clause exists) as ROOM does not support partial indexes
            if (ii.getTableName().equals(ti.getTableName()) || ii.getTableName().equals(ti.getEnclosedTableName()) && ii.getWhereClause().length() < 1) {
                if (ix.length() < 1) {
                    ix.append(INDICIES_START);
                }
                if (ix.length() > INDICIES_START.length()) {
                    ix.append(",\n").append(INDENT).append(INDENT);
                }
                ix.append(buildIndex(ii,ti));
            }
        }
        if (ix.length() > 0) {
            ix.append("\n" + INDENT + "}");
            return "," + ix.toString();
        }
        return "";
    }

    /**
     * Build a single @Index to be included in the indicies clause
     * @param ii    The IndexInfo object
     * @param ti    The Table to which the index belongs
     * @return      The generated @Index clause with enclosed column name if they have been coded
     *              noting that the swapEnclosersForRoom method is applied
     */
    private static String buildIndex(IndexInfo ii, TableInfo ti) {
        StringBuilder ix = new StringBuilder().append(INDEXSTART);
        ix.append(INDEXNAME).append(ii.getIndexName()).append("\"," );
        if (ii.isUnique()) {
            ix.append(INDEXUNIQUE);
        }
        StringBuilder ixcols = new StringBuilder().append(INDEXVALUE);
        boolean afterfirst = false;
        for (IndexColumnInfo ici: ii.getColumns()) {
            ColumnInfo ci = ti.getColumnInfoByName(ici.getColumnName());
            if (afterfirst) {
                ixcols.append(",");
            }
            String columnToCode = swapEnclosersForRoom(ci.getAlternativeColumnName());
            if (columnToCode.length() < 1) {
                columnToCode = ci.getColumnName();
            }
            ixcols.append("\"").append(columnToCode).append("\"");
            afterfirst = true;
        }
        return ix.append(ixcols.toString()).append("})").toString();
    }

    //foreignKeys = { @ForeignKey(entity = Test001.class, parentColumns = {"x"},  childColumns = {"a"}) }
    /**
     * Build the lines for the ROOM foreignKeys clause that is placed in the @Entity code
     *
     * @param ti        The table (Entity) to be processed
     * @param peadbi    The PreExistingAssetDBInspect object in which the child table is located
     * @return          The generated foreignKeys clause
     */
    private static String buildForeignKeys(TableInfo ti, PreExistingFileDBInspect peadbi) {

        boolean afterfirstfkstart = false;

        if (ti.getForeignKeyList().size() < 1) return "";
        StringBuilder fk = new StringBuilder().append(FOREIGNKEYS_START);
        for (ForeignKeyInfo fki: ti.getForeignKeyList()) {
            if (afterfirstfkstart) {
                fk.append(",");
            }
            afterfirstfkstart = true;
            fk.append(FOREIGNKEYSTART)
                    .append(capitalise(swapEnclosersForRoom(fki.getParentTableName()))).append(".class");
            if (fki.isDeferable()) {
                fk.append(FOREIGNKEYDEFERRED);
            }
            fk.append(setForeignKeyAction(true,fki.getOnUpdate()));
            fk.append(setForeignKeyAction(false,fki.getOnDelete()));
            fk.append(
                    buildColumnList(
                            FOREIGNKEYCHILDCOLUMNSTART,
                            fki.getChildColumnNames(),
                            ti)
            )
                    .append(
                            buildColumnList(
                                    FOREIGNKEYPARENTCOLUMNSTART,
                                    fki.getParentColumnNames(),
                                    getParentTable(
                                            peadbi,
                                            fki.getParentTableName()
                                    )
                            )
                    )
                    .append("\n"+INDENT+INDENT+FOREIGNKEYEND);
        }
        return fk.toString()+FOREIGNKEYS_END;
    }

    /**
     * Build a CSV list of columns using the enclosed column name if it exists, noting that
     * the SwapEnclosersForRoom method is invoked
     * @param starter   the preamble before the column list e,g, KEYWORD (
     * @param columns   The source list of columns
     * @param ti        The TableInfo object to search for the enclosed column name
     * @return          The CSV list of columns including the pre-amble
     */
    private static String buildColumnList(String starter, List<String> columns, TableInfo ti) {

        StringBuilder fkcl = new StringBuilder().append(starter);
        for (String s: columns) {
            if (fkcl.length() > starter.length()) {
                fkcl.append(",");
            }
            String columnToCode = s;
            for (ColumnInfo ci: ti.getColumns()) {
                if (s.equals(ci.getColumnName()) && ci.getAlternativeColumnName().length() > 0) {
                    columnToCode = swapEnclosersForRoom(ci.getAlternativeColumnName());
                }
            }
            fkcl.append("\n"+INDENT+INDENT+INDENT+INDENT+"").append("\"").append(columnToCode).append("\"");
        }
        return fkcl.append(FOREIGNKEYCOLUMNEND).toString();
    }

    /**
     * Generate a JAVA string for the onUpdate or onDelete action as usable by ROOM
     * @param isOnUpdate    true if result is for the onUpdate, false if for onDelete
     * @param actionAsInt   the action code (as per this code)
     * @return              The generated String for the complete clause
     */
    private static String setForeignKeyAction(boolean isOnUpdate, int actionAsInt) {
        StringBuilder rv = new StringBuilder();
        if (actionAsInt == ForeignKeyInfo.ACTION_NOACTION) return "";
        if (isOnUpdate) {
            rv.append(FOREIGNKEYONUPDATE);
        } else {
            rv.append(FOREIGNKEYONDELETE);
        }
        switch (actionAsInt) {
            case ForeignKeyInfo.ACTION_RESTRICT:
                rv.append(FOREIGNKEYACTIONRESTRICT);
                break;
            case ForeignKeyInfo.ACTION_SETNULL:
                rv.append(FOREIGNKEYACTIONSETNULL);
                break;
            case    ForeignKeyInfo.ACTION_SETDEFAULT:
                rv.append(FOREIGNKEYACTIONSETDEFAULT);
                break;
            case ForeignKeyInfo.ACTION_CASCADE:
                rv.append(FOREIGNKEYACTIONCASCADE);
                break;
        }
        return rv.toString();
    }
}

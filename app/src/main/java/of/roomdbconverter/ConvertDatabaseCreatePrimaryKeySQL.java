package of.roomdbconverter;

import java.util.ArrayList;

import static of.roomdbconverter.RoomCodeCommonUtils.swapEnclosersForRoom;

public class ConvertDatabaseCreatePrimaryKeySQL {

    private static final String PRIMARYKEYCLAUSESTART = "PRIMARY KEY (";
    private static final String PRIMARYKEYCLAUSEEND = ")";
    public static String getPrimaryKeyClause(PreExistingAssetDBInspect peadbi, TableInfo ti) {
        StringBuilder pkeys = new StringBuilder();
        boolean hasrowidalias = false;
        for (ColumnInfo ci: ti.getColumns()) {
            if (ci.isRowidAlias() || ci.isAutoIncrementCoded()) return "";
        }
        String columnNameToCode = "";
        pkeys.append(PRIMARYKEYCLAUSESTART);
        boolean afterFirst = false;
        ArrayList columns = ti.getPrimaryKeyList();
        // If no Primary Key then generate Primary key on all columns as ROOM requires
        // all tables to have a Primary Key
        if (columns.size() < 1) {
            for (ColumnInfo ci : ti.getColumns()) {
                columns.add("`"+ ci.getColumnName() + "`");
            }
        }
        for (String s: ti.getPrimaryKeyList()) {
            if (afterFirst) {
                pkeys.append(",");
            }
            ColumnInfo ci = ti.getColumnInfoByName(s);
            columnNameToCode = swapEnclosersForRoom(ci.getAlternativeColumnName());
            if (columnNameToCode.length() < 1) {
                columnNameToCode = ci.getColumnName();
            }
            pkeys.append("`"+ columnNameToCode+ "`");
            afterFirst = true;
        }
        return pkeys.append(PRIMARYKEYCLAUSEEND).toString();
    }
}

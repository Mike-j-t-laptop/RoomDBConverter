package of.roomdbconverter;

import static of.roomdbconverter.ConvertDatabaseCreateColumnDefineSQL.getColumnDefineClauses;
import static of.roomdbconverter.ConvertDatabaseCreateForeignKeySQL.getForeignKeyClauses;
import static of.roomdbconverter.ConvertDatabaseCreatePrimaryKeySQL.getPrimaryKeyClause;
import static of.roomdbconverter.RoomCodeCommonUtils.swapEnclosersForRoom;

public class ConvertedDatabaseCreateTableSQL {

    private static final String CREATESTART = "CREATE TABLE IF NOT EXISTS /*GENERATED BY CONVERT*/ ";
    private static final String CREATEDEFINESTART = "(";
    private static final String CREATEDEFINEEND = ")";

    /**
     *
     * @param peadbi
     * @param ti
     * @return
     */
    public static String createTableCreateSQL(PreExistingFileDBInspect peadbi, TableInfo ti) {

        StringBuilder crtsql = new StringBuilder();
        String columnsToCode = getColumnDefineClauses(ti);
        String primaryKeysToCode = getPrimaryKeyClause(peadbi, ti);
        String foreignKeysToCode = getForeignKeyClauses(peadbi, ti);
        String tableNameToCode = swapEnclosersForRoom(ti.getEnclosedTableName());
        if (tableNameToCode.length() < 1) {
            tableNameToCode = ti.getTableName();
        }
        crtsql.append(CREATESTART).append("`" + tableNameToCode + "`").append(CREATEDEFINESTART).append(columnsToCode);
        if (primaryKeysToCode.length() > 0) {
            crtsql.append(",").append(primaryKeysToCode);
        }
        if (foreignKeysToCode.length() > 0) {
            crtsql.append(",").append(foreignKeysToCode);
        }
        crtsql.append(CREATEDEFINEEND);
        return crtsql.toString();
    }
}

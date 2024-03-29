package of.roomdbconverter;

import java.util.ArrayList;

import static of.roomdbconverter.RoomCodeCommonUtils.*;

public class RoomDaoBuilder {

    public static final String DAOEXTENSION = "Dao";
    private static final String DAOINSERTANNOTATION = "@Insert()";
    private static final String DAOINSERTONEMETHODPREFIX = " Long insert";
    private static final String DAOINSERTMANYMETHODPREFIX = " Long[] insert";
    private static final String DAOUPDATEANNOTATION = "@Update()";
    private static final String DAOUPDATEMETHODPREFIX = " int update";
    private static final String DAODELETEANNOTATION = "@Delete()";
    private static final String DAODELETEMETHODPREFIX = "int delete";
    private static final String DAOQUERYANNOTATIONSTART = "@Query(\"SELECT * FROM `";
    private static final String DAOQUERYANNOTATIONEND = "`\")";
    private static final String DAOQUERYMETHODPREFIX = "getEvery";
    private static final String DAOMANYSUFFIX = "...";

    public static ArrayList<String> extractDaoCode(TableInfo ti) {
        ArrayList<String> rv = new ArrayList<>();
        rv.add("@Dao");
        rv.add("interface " + capitalise(ti.getTableName()) + DAOEXTENSION + "{");
        rv.add("");
        rv.add(INDENT + DAOINSERTANNOTATION);
        rv.add(INDENT + DAOINSERTONEMETHODPREFIX + capitalise(ti.getTableName()) + "(" + capitalise(ti.getTableName()) + " " + lowerise(ti.getTableName()) + ");");
        rv.add("");
        rv.add(INDENT + DAOINSERTANNOTATION);
        rv.add(INDENT + DAOINSERTMANYMETHODPREFIX + capitalise(ti.getTableName()) + "(" + capitalise(ti.getTableName()) + DAOMANYSUFFIX + " " + lowerise(ti.getTableName()) +");");
        rv.add("");
        rv.add(INDENT + DAOUPDATEANNOTATION);
        rv.add(INDENT + DAOUPDATEMETHODPREFIX + capitalise(ti.getTableName()) + "(" + capitalise(ti.getTableName()) + " " + lowerise(ti.getTableName()) + ");");
        rv.add("");
        rv.add(INDENT + DAOUPDATEANNOTATION);
        rv.add(INDENT + DAOUPDATEMETHODPREFIX + capitalise(ti.getTableName()) + "(" + capitalise(ti.getTableName()) + DAOMANYSUFFIX + " " + lowerise(ti.getTableName()) + ");");
        rv.add("");
        rv.add(INDENT + DAODELETEANNOTATION);
        rv.add(INDENT + DAODELETEMETHODPREFIX + capitalise(ti.getTableName()) + "(" + capitalise(ti.getTableName()) + " " + lowerise(ti.getTableName()) + ");");
        rv.add("");
        rv.add(INDENT + DAODELETEANNOTATION);
        String tableNameToCode =  swapEnclosersForRoom(ti.getEnclosedTableName());
        if (tableNameToCode.length() < 1) {
            tableNameToCode = ti.getTableName();
        }
        rv.add(INDENT + DAODELETEMETHODPREFIX + capitalise(ti.getTableName()) + "(" + capitalise(ti.getTableName()) + DAOMANYSUFFIX + " " + lowerise(ti.getTableName()) + ");");
        rv.add("");
        rv.add(INDENT + DAOQUERYANNOTATIONSTART + tableNameToCode + DAOQUERYANNOTATIONEND);
        rv.add(INDENT + "List<" + capitalise(ti.getTableName()) + "> " +
                DAOQUERYMETHODPREFIX + capitalise(ti.getTableName()) + "();");
        rv.add("}");
        return rv;
    }
}

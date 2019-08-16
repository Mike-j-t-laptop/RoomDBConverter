# RoomDBConverter
Utility App to convert existing databases to Room databases including generating the Entity and basic DAO code.

Intended to be a development App that you run from the IDE used for developing an App after copying the database(s) t be converted into the assets folder.

The App searches the assets folder and sub-folders checking to see if any files have the SQlite Header.

The assets that are deemed as SQLite databases are listed. 

Clicking an asset in the list list database information and initially a list of the databases tables.

Clicking the headings for Tables/Columns/Indexes/Triggers/FK/Views will switch to displaying relevant information.

Changes/omissions that will be made will be highlighted 
(e.g. column types will be either INTEGER, REAL, TEXT or BLOB so VARCHAR(100) would be highlighted as being changed to TEXT).

Clicking the CONVERT button converts the database and creates the java code for the Room @Entity and for a basic DAO, one of each for each table.
The converted database can then be copied into the asset folder of the App being developed (e.g. via AS's Device Explorer), the java code can also be copied (the package and imports need to then be added) and then the Database should be usable in the App using Room.


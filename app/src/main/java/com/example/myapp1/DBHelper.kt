package com.example.myapp1
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

/**
 * (12/01) MW:
 * to JC: I created some methods in DBHelper for querying the DB.
 * This will be used in collection functions, etc.
 * The db structure is not touched.
 */
class DBHelper(
    context: Context?,
    name: String?,
    factory: SQLiteDatabase.CursorFactory?,
    version: Int
): SQLiteOpenHelper(context, name, factory, version){
    override fun onCreate(db: SQLiteDatabase) {
        var sql: String = "CREATE TABLE if not exists location(" + //location 테이블 만들기
                "id integer primary key autoincrement," + //id속성은 키속성, 자동 할당
                "lat real,"+ //실수형의 lat 속성 위도 저장용
                "long real,"+ //실수형의 long 속성 경도 저장용
                "filename text,"+ // 문자열 형의 fileName 속성 파일 이름 저장용
                "class text," + // 문자열 형의 class 속성 클래스 이름 저장용
                "comment text);" // comment 저장용
        db.execSQL(sql)
    }


    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        val sql: String = "DROP TABLE if exists location"
        db.execSQL(sql)
        onCreate(db)
    }

    // mw: delete data by id
    fun deleteData(id: String): Int {
        val db = this.writableDatabase;
        return db.delete("location", "id = ?", arrayOf(id))
    }

    // mw: return Cursor that query is 'SELECT ALL'
    fun getAllDataCursor(): Cursor {
        val db: SQLiteDatabase = writableDatabase;
        return db.rawQuery("SELECT * FROM location", null);
    }

    // mw: get current size of db
    fun getCount(): Int {
        val db: SQLiteDatabase = readableDatabase
        val query: String = "SELECT * FROM location"
        val cursor: Cursor = db.rawQuery(query, null)
        val cnt:Int = cursor.count
        cursor.close()
        return cnt
    }

    // mw: get last raw's id
    fun getLastIndex(): Int {
        val db: SQLiteDatabase = readableDatabase
        val query: String = "SELECT id FROM location"
        val cursor: Cursor = db.rawQuery(query, null)
        cursor.moveToFirst();
        var lastIndex: Int = -1;
        for (i:Int in 0 until cursor.count){
            if(lastIndex < cursor.getInt(0))
                lastIndex = cursor.getInt(0);
            cursor.moveToNext()
        }

        cursor.close()
        return lastIndex;
    }

    // mw: search className, return true if exists
    fun classNameExists(className : String): Boolean {
        val db: SQLiteDatabase = readableDatabase
        val query: String = "SELECT class FROM location"
        val cursor: Cursor = db.rawQuery(query, null)
        cursor.moveToFirst();

        for (i:Int in 0 until cursor.count){
            if(cursor.getString(0).equals(className)) {
                cursor.close()
                return true
            }
            cursor.moveToNext()
        }

        cursor.close()
        return false
    }

    // mw: get filePath( or 'filename' in db ) by className( or 'class' in db )
    fun getFilePathByClassName(className: String): String{
        val db: SQLiteDatabase = readableDatabase;
        val query = "SELECT * FROM location"
        val cursor:Cursor = db.rawQuery(query, null);
        cursor.moveToFirst();
        for(i:Int in 0 until  cursor.count){
            if(cursor.getString(4).equals(className)){
                val result: String = cursor.getString(3);
                cursor.close();
                return result
            }
            cursor.moveToNext();
        }
        return "there is no data [$className] in db"
    }
}
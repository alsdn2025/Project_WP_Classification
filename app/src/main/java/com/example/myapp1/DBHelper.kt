package com.example.myapp1
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

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
//                "class text);"
        db.execSQL(sql)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        val sql: String = "DROP TABLE if exists location"
        db.execSQL(sql)
        onCreate(db)
    }
}
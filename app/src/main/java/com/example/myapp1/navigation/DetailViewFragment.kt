package com.example.myapp1.navigation

import android.annotation.SuppressLint
import android.database.Cursor
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapp1.R
import com.example.myapp1.navigation.model.ContentDTO
import android.database.sqlite.SQLiteDatabase
import java.io.File
import java.util.ArrayList


/**
 * @author TJ
 */
class DetailViewFragment :Fragment(){
    private val DB_PATH = "/data/data/com.example.myapp1/databases/"
    private val DB_NAME = "mw_temp.db"
    private val IMAGE_PATH = "/sdcard/DCIM/WP_Classification/"

    lateinit var database:SQLiteDatabase

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var view = LayoutInflater.from(activity).inflate(R.layout.fragment_detail,container,false)

        view.findViewById<RecyclerView>(R.id.detailviewfragment_recyclerview).adapter = DetailViewRecyclerViewAdapter()
        view.findViewById<RecyclerView>(R.id.detailviewfragment_recyclerview).layoutManager = LinearLayoutManager(activity)
        return view
    }

    @SuppressLint("Range")
    inner class DetailViewRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(){

        val idList: MutableList<Int> = ArrayList()
        val latList: MutableList<Double> = ArrayList() // 위도
        val longList: MutableList<Double> = ArrayList() // 경도
        val fileNameList: MutableList<String> = ArrayList() // 파일명
        val classNameList: MutableList<String> = ArrayList() // 클래스명
        val commentList: MutableList<String> = ArrayList()

        init{

            val query = "SELECT * FROM location"
            if (File(DB_PATH + DB_NAME).exists()) {
                database = SQLiteDatabase.openDatabase(
                    DB_PATH + DB_NAME,
                    null,
                    SQLiteDatabase.OPEN_READONLY
                )
                val cursor: Cursor = database.rawQuery(query, null)
                while (cursor.moveToNext()) {
                    idList.add(cursor.getInt(cursor.getColumnIndex("id")))
                    latList.add(cursor.getDouble(cursor.getColumnIndex("lat")))
                    longList.add(cursor.getDouble(cursor.getColumnIndex("long")))
                    fileNameList.add(cursor.getString(cursor.getColumnIndex("filename")))
                    classNameList.add(cursor.getString(cursor.getColumnIndex("class")))
                    commentList.add(cursor.getString(cursor.getColumnIndex("comment")))
                }

                idList.reverse()
                latList.reverse()
                longList.reverse()
                fileNameList.reverse()
                classNameList.reverse()
                commentList.reverse()

                notifyDataSetChanged()
            }
        }

        // 만들어진 viewholder가 없을때 생성하는 함수
        override fun onCreateViewHolder(p0: ViewGroup, p1: Int): RecyclerView.ViewHolder {
            var view = LayoutInflater.from(p0.context).inflate(R.layout.item_detail, p0, false)
            return CustomViewHolder(view)
        }

        // viewholder : 데이터가 틀 안에 들어갈 수 있도록 하는 기능을 정의
        inner class CustomViewHolder(view: View) : RecyclerView.ViewHolder(view)

        // recyclerview가 viewholder를 가져와 데이터를 연결할때 호출
        // 적절한 데이터를 가져와서 그 데이터를 사용하여 viewholder의 layout 채움
        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            var viewholder = (holder as CustomViewHolder).itemView

            //Id
            //  contentDTOs!![position].userId
            viewholder.findViewById<TextView>(R.id.detailviewitem_classname_textview).text = classNameList[position]

            viewholder.findViewById<TextView>(R.id.detailviewitem_location_textview).text =
                "위치 : " + latList[position].toString() + " " + longList[position].toString()

            // mw : image path test
            Glide.with(holder.itemView.context).load(fileNameList[position]).into(viewholder.findViewById(R.id.detailviewitem_imageview_content))

            //Explain
            viewholder.findViewById<TextView>(R.id.detailviewitem_explain_textview).text = commentList[position]
        }

        override fun getItemCount(): Int {
            return idList.size
        }

    }
}
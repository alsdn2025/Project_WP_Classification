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
        //var contentDTOs : ArrayList<ContentDTO> = arrayListOf()
        //var contentUidList : ArrayList<String> = arrayListOf()

        val idList: MutableList<Int> = ArrayList()
        val latList: MutableList<Double> = ArrayList() // 위도
        val longList: MutableList<Double> = ArrayList() // 경도
        val fileNameList: MutableList<String> = ArrayList() // 파일명
        val classNameList: MutableList<String> = ArrayList() // 클래스명
        val commentList: MutableList<String> = ArrayList()

        init{

            //contentDTOs.clear()
            //contentUidList.clear()

            //contentDTOs.add(ContentDTO("2022년 3월 14일의 기록\n여기는 오스트리아.. 날씨가 너무 좋다...\n놀러가고싶다", "https://youimg1.tripcdn.com/target/10051f000001gsu1kA30F_D_1180_558.jpg?proc=source%2Ftrip",
            //    "uid", "taejun", 10, 0))
            // contentUidList.add("hih")

            //contentDTOs.add(ContentDTO("2022년 11월 15일의 기록\nzzz...", "https://a.cdn-hotels.com/gdcs/production99/d638/1ed9319f-17d1-4f1b-be12-6ec1ea643a23.jpg?impolicy=fcrop&w=800&h=533&q=medium",
            //    "uid", "taejun", 2, 0))
            //contentUidList.add("hih")

            //notifyDataSetChanged()

            // println(contentDTOs.toString())

            //초기 default값 설정
            //idList.add(1234567890)
            //latList.add(1.0)
            //longList.add(2.0)
            //fileNameList.add("test filename")
            //classNameList.add("식물의 이름")
            //commentList.add("이곳에는 여러분의 코멘트가 적힐 장소입니다.")


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

            //Image
            //contentDTOs!![position].imageUrl
//            Glide.with(holder.itemView.context).load(IMAGE_PATH+fileNameList[position]).into(viewholder.findViewById(R.id.detailviewitem_imageview_content))
            // mw : image path test
            Glide.with(holder.itemView.context).load(fileNameList[position]).into(viewholder.findViewById(R.id.detailviewitem_imageview_content))
            //Explain
            viewholder.findViewById<TextView>(R.id.detailviewitem_explain_textview).text = commentList[position]

            //likes
            //viewholder.findViewById<TextView>(R.id.detailviewitem_favoritecounter_textview).text = "Likes " + contentDTOs!![position].favoriteCount

            //ProfileImage
            //Glide.with(holder.itemView.context).load(R.drawable.logo_team2).into(viewholder.findViewById(R.id.detailviewitem_profile_image))
        }

        override fun getItemCount(): Int {
            return idList.size
        }

    }
}
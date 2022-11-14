package com.example.myapp1.navigation

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

class DetailViewFragment :Fragment(){

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

    inner class DetailViewRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(){
        var contentDTOs : ArrayList<ContentDTO> = arrayListOf()
        var contentUidList : ArrayList<String> = arrayListOf()

        init{

            contentDTOs.clear()
            contentUidList.clear()

            contentDTOs.add(ContentDTO("2022년 3월 14일의 기록\n여기는 오스트리아.. 날씨가 너무 좋다...\n놀러가고싶다", "https://youimg1.tripcdn.com/target/10051f000001gsu1kA30F_D_1180_558.jpg?proc=source%2Ftrip",
                "uid", "taejun", 10, 0))
            contentUidList.add("hih")

            contentDTOs.add(ContentDTO("2022년 11월 15일의 기록\nzzz...", "https://a.cdn-hotels.com/gdcs/production99/d638/1ed9319f-17d1-4f1b-be12-6ec1ea643a23.jpg?impolicy=fcrop&w=800&h=533&q=medium",
                "uid", "taejun", 2, 0))
            contentUidList.add("hih")

            notifyDataSetChanged()

            // println(contentDTOs.toString())


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
            viewholder.findViewById<TextView>(R.id.detailviewitem_profile_textview).text = contentDTOs!![position].userId

            //Image
            Glide.with(holder.itemView.context).load(contentDTOs!![position].imageUrl).into(viewholder.findViewById(R.id.detailviewitem_imageview_content))

            //Explain
            viewholder.findViewById<TextView>(R.id.detailviewitem_explain_textview).text = contentDTOs!![position].explain

            //likes
            //viewholder.findViewById<TextView>(R.id.detailviewitem_favoritecounter_textview).text = "Likes " + contentDTOs!![position].favoriteCount

            //ProfileImage
            Glide.with(holder.itemView.context).load(R.drawable.logo_team2).into(viewholder.findViewById(R.id.detailviewitem_profile_image))
        }

        override fun getItemCount(): Int {
            return contentDTOs.size
        }

    }
}
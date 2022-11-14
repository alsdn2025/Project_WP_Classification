package com.example.myapp1.navigation

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.myapp1.InputDataActivity
import com.example.myapp1.PlantOrgans
import com.example.myapp1.R

class SearchFragment :Fragment(){

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var view = LayoutInflater.from(activity).inflate(R.layout.fragment_search,container,false)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val FlowerBtn: Button = view.findViewById<Button>(R.id.FlowerBtn)
        val FruitBtn: Button = view.findViewById<Button>(R.id.FruitBtn)
        val LeafBtn: Button = view.findViewById<Button>(R.id.LeafBtn)

        FlowerBtn.setOnClickListener { view: View? ->
            val i = Intent(context, InputDataActivity::class.java)
            //i.putExtra("organ", PlantOrgans.FLOWER)
            startActivity(i)
        }
        FruitBtn.setOnClickListener { view: View? ->
            val i = Intent(context, InputDataActivity::class.java)
            //i.putExtra("organ", PlantOrgans.FRUIT)
            startActivity(i)
        }
        LeafBtn.setOnClickListener { view: View? ->
            val i = Intent(context, InputDataActivity::class.java)
            //i.putExtra("organ", PlantOrgans.LEAF)
            startActivity(i)
        }
    }

}
package com.example.adding_removing_fromlistview

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

lateinit var languageLV: ListView
lateinit var addBtn: Button
lateinit var itemEdt: EditText
lateinit var lngList: ArrayList<String>

class MainActivity : AppCompatActivity() {

    private lateinit var languageLV: ListView
    private lateinit var addBtn: Button
    private lateinit var itemEdt: EditText
    private lateinit var lngList: ArrayList<String>
    private lateinit var adapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        languageLV = findViewById(R.id.idLVLanguages)
        addBtn = findViewById(R.id.idBtnAdd)
        itemEdt = findViewById(R.id.idEdtItemName)
        lngList = arrayListOf("C++", "Python")

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, lngList)
        languageLV.adapter = adapter

        addBtn.setOnClickListener {
            val item = itemEdt.text.toString().trim()
            if (item.isNotEmpty()) {
                lngList.add(item)
                adapter.notifyDataSetChanged()
                itemEdt.text.clear()
            }
        }

        languageLV.setOnItemClickListener { _, _, position, _ ->
            lngList.removeAt(position)
            adapter.notifyDataSetChanged()
            Toast.makeText(this, "Item removed", Toast.LENGTH_SHORT).show()
        }
    }
}
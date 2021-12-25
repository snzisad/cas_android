package com.client.snzisad.casmobile

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.CardView
import android.view.View
import android.widget.*
import com.android.volley.*
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.client.snzisad.casmobile.BusinessClass.APILink
import com.client.snzisad.casmobile.BusinessClass.DataHandler
import com.google.firebase.messaging.FirebaseMessaging
import java.lang.Exception

class StartActivity : AppCompatActivity() {
    private lateinit var cardCity: CardView
    private lateinit var cardCategory: CardView
    private lateinit var cardSubCategory: CardView
    private lateinit var cardContinue: CardView

    private lateinit var spnCity: Spinner
    private lateinit var spnCategory: Spinner
    private lateinit var spnSubCategory: Spinner

    private lateinit var pbLoading: ProgressBar

    private lateinit var cityNameList: MutableList<String>
    private lateinit var cityIDList: MutableList<Int>
    private lateinit var categoryNameList: MutableList<String>
    private lateinit var categoryIDList: MutableList<Int>
    private lateinit var subcategoryNameList: MutableList<String>
    private lateinit var subcategoryIDList: MutableList<Int>

    private lateinit var arrayRequest: JsonArrayRequest
    private lateinit var stringRequest: StringRequest
    private lateinit var objectRequest: JsonObjectRequest
    private lateinit var requestQueue: RequestQueue

    private lateinit var cityAdapter: ArrayAdapter<String>
    private lateinit var categoryAdapter: ArrayAdapter<String>
    private lateinit var subcategoryAdapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        init()
    }

    private fun init(){
        cardCity = findViewById(R.id.cardCity)
        cardCategory = findViewById(R.id.cardCategory)
        cardSubCategory = findViewById(R.id.cardSubCategory)
        cardContinue = findViewById(R.id.cardContinue)

        spnCity = findViewById(R.id.spnCity)
        spnCategory = findViewById(R.id.spnCategory)
        spnSubCategory = findViewById(R.id.spnSubCategory)

        pbLoading = findViewById(R.id.pbLoading)

        cityNameList = ArrayList<String>()
        cityIDList = ArrayList<Int>()
        categoryNameList = ArrayList<String>()
        categoryIDList = ArrayList<Int>()
        subcategoryNameList = ArrayList<String>()
        subcategoryIDList = ArrayList<Int>()

        cityAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, cityNameList)
        categoryAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categoryNameList)
        subcategoryAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, subcategoryNameList)

        cityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        subcategoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spnCity.adapter =cityAdapter
        spnCategory.adapter =categoryAdapter
        spnSubCategory.adapter =subcategoryAdapter

        cardCity.visibility = View.INVISIBLE
        cardCategory.visibility = View.INVISIBLE
        cardSubCategory.visibility = View.INVISIBLE
        cardContinue.visibility = View.VISIBLE

        getCityFromServer()
        getCategoryFromServer(0, categoryNameList, categoryIDList, categoryAdapter, cardCategory)

        spnCategory.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position == 0){
                    cardSubCategory.visibility = View.INVISIBLE
                    subcategoryIDList.clear()
                }
                else{
                    getCategoryFromServer(categoryIDList.get(position), subcategoryNameList, subcategoryIDList, subcategoryAdapter, cardSubCategory)
                }
            }

        }

        cardContinue.setOnClickListener {
            gotoProductListActivity()
        }
    }

    private fun getCityFromServer(){
        pbLoading.visibility = View.VISIBLE
        cardContinue.visibility = View.INVISIBLE
        val url = APILink.getcity
        cityNameList.clear()
        cityIDList.clear()

        cityNameList.add("Select your Location")
        cityIDList.add(0)

        requestQueue = Volley.newRequestQueue(this)

        arrayRequest = JsonArrayRequest(url,
            Response.Listener{response->
                try{

                    val status = response.getJSONObject(0).get("status");

                    if(status == 1){
                        val data = response.getJSONObject(0).getJSONArray("data");
                        requestQueue.stop()

                        for(i in 0 until data.length()){
                            val city = data.getJSONObject(i)
                            cityNameList.add(city.getString("city_name"))
                            cityIDList.add(city.getInt("id"))
                        }
                        cityAdapter.notifyDataSetChanged()
                        cardCity.visibility = View.VISIBLE
                        cardContinue.visibility = View.VISIBLE
                        pbLoading.visibility = View.INVISIBLE
                    }
                    else{
                        requestQueue.stop()
                        Toast.makeText(this, "Something went wrong. Please try again", Toast.LENGTH_SHORT).show()
                        pbLoading.visibility = View.INVISIBLE
                    }
                }
                catch (e: Exception){
                    requestQueue.stop()
                    Toast.makeText(this, "Error: "+e.localizedMessage, Toast.LENGTH_SHORT).show()
                    pbLoading.visibility = View.INVISIBLE
                }

            },
            Response.ErrorListener {
                Toast.makeText(this, "error: "+it.toString(), Toast.LENGTH_SHORT).show()
                pbLoading.visibility = View.INVISIBLE
                requestQueue.stop()
            }
        )
        
//        arrayRequest.setRetryPolicy(DefaultRetryPolicy(2000, 0, 0f))
        requestQueue.add(arrayRequest)
    }

    private fun getCategoryFromServer(parent: Int, name: MutableList<String>, id: MutableList<Int>, adapter: ArrayAdapter<String>, cardView: CardView){
        pbLoading.visibility = View.VISIBLE
        cardContinue.visibility = View.INVISIBLE
        val url = APILink.getcategory+"parent="+parent

        requestQueue = Volley.newRequestQueue(this)

        name.clear()
        id.clear()

        id.add(0)
        if(parent == 0){
            name.add("Select a Category")
        }
        else{
            name.add("Select a sub Category")
        }


        arrayRequest = JsonArrayRequest(url,
            Response.Listener{response->
                try {

                    val status = response.getJSONObject(0).get("status");

                    if (status == 1) {
                        val data = response.getJSONObject(0).getJSONArray("data");
                        requestQueue.stop()

                        for (i in 0 until data.length()) {
                            val city = data.getJSONObject(i)
                            name.add(city.getString("category_name"))
                            id.add(city.getInt("id"))
                        }

                        adapter.notifyDataSetChanged()
                        cardView.visibility = View.VISIBLE
                        cardContinue.visibility = View.VISIBLE
                        pbLoading.visibility = View.INVISIBLE
                    }
                    else {
                        requestQueue.stop()
                        Toast.makeText(this, "Something went wrong. Please try again", Toast.LENGTH_SHORT).show()
                        pbLoading.visibility = View.INVISIBLE
                    }
                }
                catch (e: Exception){
                    requestQueue.stop()
                    Toast.makeText(this, "Error: "+e.localizedMessage, Toast.LENGTH_SHORT).show()
                    pbLoading.visibility = View.INVISIBLE

                }

            },
            Response.ErrorListener {
                Toast.makeText(this, "error: "+it.toString(), Toast.LENGTH_SHORT).show()
                pbLoading.visibility = View.INVISIBLE
                requestQueue.stop()
            }
        )

        requestQueue.add(arrayRequest)
        requestQueue.start()
    }

    private fun gotoProductListActivity(){
        if(spnCity.selectedItemPosition == 0 && spnCategory.selectedItemPosition ==0){
            Toast.makeText(this, "Please select any location or category", Toast.LENGTH_SHORT).show()
        }
        else if(spnCategory.selectedItemPosition !=0 && spnSubCategory.selectedItemPosition == 0 && subcategoryIDList.size > 1){
            Toast.makeText(this, "Please select a sub category", Toast.LENGTH_SHORT).show()
        }
        else{
            val locationPosition = spnCity.selectedItemPosition
            val subcategoryPosition = spnSubCategory.selectedItemPosition
            val categoryPosition = spnCategory.selectedItemPosition

            if(locationPosition != 0){
                DataHandler.locationID = cityIDList.get(locationPosition)
            }
            else{
                DataHandler.locationID = 0
            }

            if(subcategoryIDList.size>0 && subcategoryPosition != 0){
                DataHandler.categoryID = subcategoryIDList.get(subcategoryPosition)
            }
            else if(categoryPosition != 0){
                DataHandler.categoryID = categoryIDList.get(categoryPosition)
            }
            else{
                DataHandler.categoryID = 0
            }

            startActivity(Intent(this@StartActivity, ProductActivity::class.java))
        }
    }

}

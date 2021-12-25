package com.client.snzisad.casmobile

import android.app.ProgressDialog
import android.content.Intent
import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Switch
import android.widget.Toast
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.client.snzisad.casmobile.BusinessClass.APILink
import com.client.snzisad.casmobile.BusinessClass.DataHandler
import com.client.snzisad.casmobile.BusinessClass.MessageListActivity
import com.client.snzisad.casmobile.BusinessClass.ProductListAdapter
import com.google.firebase.messaging.FirebaseMessaging
import java.lang.Exception

class ProductActivity : AppCompatActivity() {
    private lateinit var rvProductList: RecyclerView
    private lateinit var fabNewAd: FloatingActionButton
    private lateinit var adapter: ProductListAdapter
    private lateinit var progressDialog: ProgressDialog
    private lateinit var productListData: MutableList<HashMap<String, String>>
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product)

        init()

        //subscribe to toic to get notification
        FirebaseMessaging.getInstance().subscribeToTopic("Posts")
    }

    private fun init(){

        sharedPreferences = getSharedPreferences("Status", 0)
        editor = sharedPreferences.edit()

        rvProductList = findViewById(R.id.rvProductList)
        fabNewAd = findViewById(R.id.fabNewAd)

        rvProductList.visibility = View.GONE

        productListData = ArrayList<HashMap<String, String>>()
        adapter = ProductListAdapter(this, productListData)

        progressDialog = ProgressDialog(this)
        progressDialog!!.setMessage("Please wait...")

        rvProductList.adapter = adapter
        val layoutManager = LinearLayoutManager(this)
        rvProductList.layoutManager = layoutManager

        fabNewAd.setOnClickListener {
            if(DataHandler.postOrAuction == 0){
                DataHandler.nextActin = "newpost"
            }
            else{
                DataHandler.nextActin = "newauction"
            }
            startActivity(Intent(this, LoginActivity::class.java))
        }
        if(DataHandler.postOrAuction == 0){
            getProductListFromServer()
        }
        else{
            getAuctionListFromServer()
        }

    }

    private fun getProductListFromServer(){
        progressDialog.show()
        super.setTitle("Posts")

        val url = generateURL()
        productListData.clear()
        val requestQueue = Volley.newRequestQueue(this)

        val arrayRequest = JsonArrayRequest(url,
            Response.Listener{ response->
                try{

                    val status = response.getJSONObject(0).get("status");

                    if(status == 1){
                        val data = response.getJSONObject(0).getJSONArray("data");
                        requestQueue.stop()

                        for(i in 0 until data.length()){
                            val city = data.getJSONObject(i)
                            val product = HashMap<String, String>()

                            product.put("post_title", city.getString("post_title"))
                            product.put("id", city.getString("id"))
                            product.put("location", city.getString("location"))
                            product.put("price", city.getString("price"))
                            product.put("image", city.getString("image"))

                            productListData.add(product)
                        }

                        adapter.notifyDataSetChanged()
                        rvProductList.visibility = View.VISIBLE

                    }
                    else{
                        requestQueue.stop()
                        Toast.makeText(this, "Something went wrong. Please try again", Toast.LENGTH_SHORT).show()
                    }
                }
                catch (e: Exception){
                    requestQueue.stop()
                    Toast.makeText(this, "Error: "+e.localizedMessage, Toast.LENGTH_SHORT).show()
                }
                progressDialog.cancel()

            },
            Response.ErrorListener {
                Toast.makeText(this, "error: "+it.toString(), Toast.LENGTH_SHORT).show()
                progressDialog.cancel()
                requestQueue.stop()
            }
        )

        requestQueue.add(arrayRequest)
        requestQueue.start()
    }

    private fun getAuctionListFromServer(){
        progressDialog.show()
        super.setTitle("Auction")

        val url = generateURL()
        productListData.clear()
        val requestQueue = Volley.newRequestQueue(this)

        val arrayRequest = JsonArrayRequest(url,
            Response.Listener{ response->
                try{

                    val status = response.getJSONObject(0).get("status");

                    if(status == 1){
                        val data = response.getJSONObject(0).getJSONArray("data");
                        requestQueue.stop()

                        for(i in 0 until data.length()){
                            val city = data.getJSONObject(i)
                            val product = HashMap<String, String>()

                            product.put("auction_title", city.getString("auction_title"))
                            product.put("id", city.getString("id"))
                            product.put("ending_date", city.getString("ending_date"))
                            product.put("price", city.getString("starting_price"))
                            product.put("image", city.getString("image"))

                            productListData.add(product)
                        }

                        adapter.notifyDataSetChanged()
                        rvProductList.visibility = View.VISIBLE

                    }
                    else{
                        requestQueue.stop()
                        Toast.makeText(this, "Something went wrong. Please try again", Toast.LENGTH_SHORT).show()
                    }
                }
                catch (e: Exception){
                    requestQueue.stop()
                    Toast.makeText(this, "Error: "+e.localizedMessage, Toast.LENGTH_SHORT).show()
                }
                progressDialog.cancel()

            },
            Response.ErrorListener {
                Toast.makeText(this, "error: "+it.toString(), Toast.LENGTH_SHORT).show()
                progressDialog.cancel()
                requestQueue.stop()
            }
        )

        requestQueue.add(arrayRequest)
        requestQueue.start()
    }

    private fun generateURL() : String{

        var url = APILink.getproductlist

        if(DataHandler.postOrAuction == 0){

            url = APILink.getproductlist
        }
        else{

            url = APILink.getauctionlist
        }

        if(DataHandler.locationID != 0 && DataHandler.categoryID != 0){
            url = url + "city="+DataHandler.locationID + "&cat="+DataHandler.categoryID
        }

        else if(DataHandler.locationID != 0){
            url = url + "city="+DataHandler.locationID
        }

        else if(DataHandler.categoryID != 0){
            url = url + "cat="+DataHandler.categoryID
        }

        return url
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        menuInflater.inflate(R.menu.menu_home, menu)
        val logout_menu = menu!!.findItem(R.id.logout)
        val login_menu = menu!!.findItem(R.id.login)
        val message_menu = menu!!.findItem(R.id.message)
        val toggle_menu = menu!!.findItem(R.id.toggle)
        message_menu.isVisible = false

        val v = toggle_menu.actionView
        val toggleButton = v.findViewById<Switch>(R.id.switchToggle)

        if(DataHandler.postOrAuction == 1){
            toggleButton.isChecked = true
        }
        else{
            toggleButton.isChecked = false
        }

        toggleButton.setOnClickListener {
            DataHandler.categoryID = 0
            DataHandler.locationID = 0
            if(toggleButton.isChecked){
                DataHandler.postOrAuction = 1
                getAuctionListFromServer()
                Toast.makeText(this, "Loading Auction", Toast.LENGTH_SHORT).show()
            }
            else{
                DataHandler.postOrAuction = 0
                getProductListFromServer()
                Toast.makeText(this, "Loading Posts", Toast.LENGTH_SHORT).show()
            }
        }

        if(sharedPreferences.getBoolean("logged", false)) {
            logout_menu.isVisible = true
            login_menu.isVisible = false
        }
        else{
            logout_menu.isVisible = false
            login_menu.isVisible = true
        }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.logout -> {
                logoutFromServer()
                editor.putBoolean("logged", false)
                editor.apply()

                startActivity(Intent(this, ProductActivity::class.java))
                finish()
            }

            R.id.login -> {
                startActivity(Intent(this, LoginActivity::class.java))
            }

            R.id.message -> {
                startActivity(Intent(this, MessageListActivity::class.java))
            }

            R.id.filter -> {
                startActivity(Intent(this, StartActivity::class.java))
            }

            else -> return super.onOptionsItemSelected(item)
        }

        return true
    }


    private fun logoutFromServer(){
        val url = APILink.logout+DataHandler.userID

        val requestQueue = Volley.newRequestQueue(this)

        val arrayRequest = JsonArrayRequest(url,
            Response.Listener{ response->
                try{
                    val status = response.getJSONObject(0).get("status");

                    if(status != 1){
                        Toast.makeText(this, "Error logging out from server", Toast.LENGTH_SHORT).show()
                    }

                    requestQueue.stop()
                }
                catch (e: Exception){
                    requestQueue.stop()
                    Toast.makeText(this, "Error logging out from server", Toast.LENGTH_SHORT).show()
                }

            },
            Response.ErrorListener {
                requestQueue.stop()
            }
        )

        requestQueue.add(arrayRequest)
    }

}

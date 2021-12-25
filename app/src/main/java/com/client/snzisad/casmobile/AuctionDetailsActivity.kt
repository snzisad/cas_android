package com.client.snzisad.casmobile

import android.Manifest
import android.app.ProgressDialog
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.text.TextUtils
import android.view.View
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.client.snzisad.casmobile.BusinessClass.APILink
import com.client.snzisad.casmobile.BusinessClass.DataHandler
import java.lang.Exception
import android.Manifest.permission
import android.Manifest.permission.CALL_PHONE
import android.support.v4.app.ActivityCompat
import android.content.pm.PackageManager
import android.support.v4.content.ContextCompat
import android.os.Build
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.view.LayoutInflater
import android.widget.*
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_auction_details.*
import org.json.JSONArray
import org.json.JSONObject


class AuctionDetailsActivity : AppCompatActivity() {
    private lateinit var tvProductName: TextView
    private lateinit var tvProductPrice: TextView
    private lateinit var tvEndDate: TextView
    private lateinit var tvProductCondition: TextView
    private lateinit var tvProductLocation: TextView
    private lateinit var tvProductDesc: TextView
    private lateinit var layoutBidInfo: LinearLayout
    private lateinit var imgProductImage: ImageView
    private lateinit var imgNext: ImageView
    private lateinit var imgPrev: ImageView
    private lateinit var progressDialog: ProgressDialog
    private lateinit var svAuctionDetails: ScrollView
    private var currentImagePosition: Int = 0
    private lateinit var imageList: MutableList<String>
    private val host = APILink.host
    private lateinit var sharedPreferences: SharedPreferences


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auction_details)

        val intent = getIntent()

        if(intent.getIntExtra("pid", 0) != 0){
            DataHandler.productID = intent.getIntExtra("pid", 0)
        }


        init()

    }

    private fun init(){
        tvProductName = findViewById(R.id.tvProductName)
        tvProductPrice = findViewById(R.id.tvProductPrice)
        tvEndDate = findViewById(R.id.tvEndDate)
        tvProductCondition = findViewById(R.id.tvProductCondition)
        tvProductLocation = findViewById(R.id.tvProductLocation)
        tvProductDesc = findViewById(R.id.tvProductDesc)
        imgProductImage = findViewById(R.id.imgProductImage)
        imgNext = findViewById(R.id.imgNext)
        imgPrev = findViewById(R.id.imgPrev)
        layoutBidInfo = findViewById(R.id.layoutBidInfo)
        svAuctionDetails = findViewById(R.id.svAuctionDetails)

        imageList = ArrayList<String>()
        sharedPreferences = getSharedPreferences("Status", 0)

        if(sharedPreferences.getBoolean("logged", false)) {
            DataHandler.userID = sharedPreferences.getInt("id", 0)
            DataHandler.userName = sharedPreferences.getString("name", "")
            DataHandler.userType = sharedPreferences.getInt("type", 0)
            
            tvLoginAlert.visibility = View.GONE
            layoutBidProduct.visibility = View.VISIBLE
        }
        else{
            tvLoginAlert.visibility = View.VISIBLE
            layoutBidProduct.visibility = View.GONE
        }

        progressDialog = ProgressDialog(this)
        progressDialog!!.setMessage("Please wait...")

        tvLoginAlert.setOnClickListener {
            DataHandler.nextActin = "bid";
            startActivity(Intent(this, LoginActivity::class.java))
        }

        btnBidProduct.setOnClickListener {
            if(TextUtils.isEmpty(edtBidPrice.text.toString())){
                edtBidPrice.setError("Reqired")
            }
            else{
                addToTableAndSendToServer(edtBidPrice.text.toString())

                edtBidPrice.setText("")
            }
        }

        imgNext.setOnClickListener{
            currentImagePosition = currentImagePosition+1
            swipProductImage(currentImagePosition)
        }

        imgPrev.setOnClickListener{
            currentImagePosition = currentImagePosition-1
            swipProductImage(currentImagePosition)
        }

        svAuctionDetails.visibility = View.GONE

        getAuctionDetailsFromServer()
    }


    private fun getAuctionDetailsFromServer(){
        progressDialog.show()
        val url = APILink.getauction+DataHandler.productID

        val requestQueue = Volley.newRequestQueue(this)

        val arrayRequest = JsonArrayRequest(url,
            Response.Listener{ response->
                try{

                    val status = response.getJSONObject(0).get("status");

                    if(status == 1){
                        val data = response.getJSONObject(0).getJSONArray("data").getJSONObject(0);
                        val image = response.getJSONObject(0).getJSONArray("image");
                        val bidInfo = response.getJSONObject(0).getJSONArray("bid");
                        requestQueue.stop()

                        loadAuctionDetails(data)
                        loadImages(image)
                        loadBidInformation(bidInfo)
                    }
                    else{
                        requestQueue.stop()
                        Toast.makeText(this, "No Auction found", Toast.LENGTH_SHORT).show()
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


    private fun loadAuctionDetails(data: JSONObject){
        tvProductName.setText(data.getString("auction_title"))
        tvProductPrice.setText("Tk. "+data.getString("starting_price"))
        tvEndDate.setText("End: "+data.getString("ending_date"))
        tvProductCondition.setText("Condition: "+data.getString("condition"))
        tvProductLocation.setText("Location: "+data.getString("location"))
        tvProductDesc.setText("Description: \n"+data.getString("auction_desc"))
    }

    private fun loadImages(imageArray: JSONArray){
//        progressDialog.show()

        for (i in 0 until imageArray.length()){
            val image = imageArray.getJSONObject(i)
            imageList.add(image.getString("image"))
        }

        currentImagePosition = 0
        swipProductImage(currentImagePosition)

        svAuctionDetails.visibility = View.VISIBLE
        progressDialog.cancel()
    }

    private fun loadBidInformation(bidInfoArray: JSONArray){
        layoutBidInfo.removeAllViews()

        for (i in 0 until bidInfoArray.length()){
            val bidInfo = bidInfoArray.getJSONObject(i)
            addIntoBidInfoTable(bidInfo.getString("username"), bidInfo.getString("bid_price"))
        }
    }

    private fun addIntoBidInfoTable(username:String, price:String){
        val view = LayoutInflater.from(this).inflate(R.layout.layout_bid_row, null)
        val tvUserName = view.findViewById<TextView>(R.id.tvUserName);
        val tvBidPrice = view.findViewById<TextView>(R.id.tvBidPrice);

        tvUserName.setText(username)
        tvBidPrice.setText(price+" Tk")

        layoutBidInfo.addView(view)
    }

    private fun swipProductImage(position: Int){

//        if the position is first, hide prev button and show next button
        if(position == 0){
            imgNext.visibility = View.VISIBLE
            imgPrev.visibility = View.GONE
        }

//        if the position is last, hide next button and show prev button
        else if(position == imageList.size-1){
            imgNext.visibility = View.GONE
            imgPrev.visibility = View.VISIBLE
        }

//        if the position is between first and last, show both button
        if(position > 0 && position < imageList.size-1){
            imgNext.visibility = View.VISIBLE
            imgPrev.visibility = View.VISIBLE
        }

//        if the position is first and last, hide both button
        else if(position <= 0 && position >= imageList.size-1){
            imgNext.visibility = View.GONE
            imgPrev.visibility = View.GONE
        }

        if(imageList.size>position){
            Glide.with(this).load(host+imageList.get(position)).into(imgProductImage)
        }
    }

    private fun addToTableAndSendToServer(price: String){

        val name = DataHandler.userName

        //add to table
        addIntoBidInfoTable(name, price)
        svAuctionDetails.fullScroll(ScrollView.FOCUS_DOWN)

        val requestQueue = Volley.newRequestQueue(this)

        val stringRequest = object : StringRequest(
            Request.Method.POST, APILink.bidproduct,
            Response.Listener { response ->
                requestQueue.stop()

                try {
                    val response = JSONArray(response).getJSONObject(0)

                    val status = response.get("status")
                    if(status != 1){
                        Toast.makeText(this, "Failed to bit this auction", Toast.LENGTH_SHORT).show()
                    }

                } catch (e: Exception) {
                    Toast.makeText(this, e.localizedMessage, Toast.LENGTH_LONG).show()
                    e.printStackTrace()
                }

            }, Response.ErrorListener {
                requestQueue.stop()
                Toast.makeText(this, it.localizedMessage, Toast.LENGTH_LONG).show()
            }) {
            @Throws(AuthFailureError::class)
            override fun getParams(): Map<String, String> {
                val params = HashMap<String, String>()

                params.put("userid", DataHandler.userID.toString())
                params.put("pid", DataHandler.productID.toString())
                params.put("price", price)
                return params
            }

        }

        requestQueue.add(stringRequest)
    }
}

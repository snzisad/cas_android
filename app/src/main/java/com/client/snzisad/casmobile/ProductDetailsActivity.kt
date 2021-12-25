package com.client.snzisad.casmobile

import android.Manifest
import android.app.ProgressDialog
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
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
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_product_details.*
import org.json.JSONArray
import org.json.JSONObject


class ProductDetailsActivity : AppCompatActivity() {
    private lateinit var tvProductName: TextView
    private lateinit var tvProductPrice: TextView
    private lateinit var tvProductType: TextView
    private lateinit var tvProductCondition: TextView
    private lateinit var tvProductLocation: TextView
    private lateinit var tvProductDesc: TextView
    private lateinit var tvSellerName: TextView
    private lateinit var tvSellerEmail: TextView
    private lateinit var tvSellerPhone: TextView
    private lateinit var fabCall: FloatingActionButton
    private lateinit var imgProductImage: ImageView
    private lateinit var imgNext: ImageView
    private lateinit var imgPrev: ImageView
    private lateinit var progressDialog: ProgressDialog
    private lateinit var svProductDetails: ScrollView
    private lateinit var phone: String
    private var currentImagePosition: Int = 0
    private lateinit var imageList: MutableList<String>
    private val host = APILink.host
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_details)

        val intent = getIntent()

        if(intent.getIntExtra("pid", 0) != 0){
            DataHandler.productID = intent.getIntExtra("pid", 0)
        }

        init()
    }

    private fun init(){
        tvProductName = findViewById(R.id.tvProductName)
        tvProductPrice = findViewById(R.id.tvProductPrice)
        tvProductType = findViewById(R.id.tvProductType)
        tvProductCondition = findViewById(R.id.tvProductCondition)
        tvProductLocation = findViewById(R.id.tvProductLocation)
        tvProductDesc = findViewById(R.id.tvProductDesc)
        tvSellerName = findViewById(R.id.tvSellerName)
        tvSellerEmail = findViewById(R.id.tvSellerEmail)
        tvSellerPhone = findViewById(R.id.tvSellerPhone)
        fabCall = findViewById(R.id.fabCall)
        imgProductImage = findViewById(R.id.imgProductImage)
        imgNext = findViewById(R.id.imgNext)
        imgPrev = findViewById(R.id.imgPrev)
        svProductDetails = findViewById(R.id.svProductDetails)

        imageList = ArrayList<String>()
        phone = ""

        sharedPreferences = getSharedPreferences("Status", 0)

        if(sharedPreferences.getBoolean("logged", false)) {
            DataHandler.userID = sharedPreferences.getInt("id", 0)
            DataHandler.userName = sharedPreferences.getString("name", "")
            DataHandler.userType = sharedPreferences.getInt("type", 0)
        }

        progressDialog = ProgressDialog(this)
        progressDialog!!.setMessage("Please wait...")

        fabCall.setOnClickListener {
            callNow()
        }

        btnCall.setOnClickListener {
            callNow()
        }

        btnChat.setOnClickListener {
            DataHandler.nextActin = "message";
            startActivity(Intent(this, LoginActivity::class.java))
        }

        imgNext.setOnClickListener{
            currentImagePosition = currentImagePosition+1
            swipProductImage(currentImagePosition)
        }

        imgPrev.setOnClickListener{
            currentImagePosition = currentImagePosition-1
            swipProductImage(currentImagePosition)
        }

        svProductDetails.visibility = View.GONE

        getProductDetailsFromServer()
    }

    private fun callNow(){
        if(TextUtils.isEmpty(phone)){
            Toast.makeText(this, "Phone number not found", Toast.LENGTH_SHORT).show()
        }
        else{
            callToPhone()
        }
    }


    private fun getProductDetailsFromServer(){
        progressDialog.show()
        val url = APILink.getproduct+DataHandler.productID+"&uid="+DataHandler.userID;

        val requestQueue = Volley.newRequestQueue(this)

        val arrayRequest = JsonArrayRequest(url,
            Response.Listener{ response->
                try{

                    val status = response.getJSONObject(0).get("status");

                    if(status == 1){
                        val data = response.getJSONObject(0).getJSONArray("data").getJSONObject(0);
                        val image = response.getJSONObject(0).getJSONArray("image");
                        requestQueue.stop()

                        loadProductDetails(data)
                        loadImages(image)
                    }
                    else{
                        requestQueue.stop()
                        Toast.makeText(this, "No Product found", Toast.LENGTH_SHORT).show()
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

    private fun callToPhone(){
        val REQUEST_PHONE_CALL = 1
        val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phone))

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(
                    this@ProductDetailsActivity,
                    Manifest.permission.CALL_PHONE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this@ProductDetailsActivity,
                    arrayOf(Manifest.permission.CALL_PHONE),
                    REQUEST_PHONE_CALL
                )
            } else {
                startActivity(intent)
            }
        } else {
            startActivity(intent)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            1 -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phone))
                    startActivity(intent);
                } else {
                    ActivityCompat.requestPermissions(
                        this@ProductDetailsActivity,
                        arrayOf(Manifest.permission.CALL_PHONE),
                        1
                    )
                }
                return;
            }
        }
    }

    private fun loadProductDetails(data: JSONObject){
        phone = data.getString("mobile")

        tvProductName.setText(data.getString("post_title"))
        tvProductPrice.setText("Tk. "+data.getString("price"))
        tvProductType.setText(data.getString("product_type"))
        tvProductCondition.setText("Condition: "+data.getString("condition1"))
        tvProductLocation.setText("Location: "+data.getString("location"))
        tvProductDesc.setText("Description: \n"+data.getString("post_desc"))
        tvSellerName.setText("Seller: "+data.getString("name"))
        tvSellerEmail.setText("Email: "+data.getString("email"))
        tvSellerPhone.setText("Phone: "+phone)
    }

    private fun loadImages(imageArray: JSONArray){
//        progressDialog.show()

        for (i in 0 until imageArray.length()){
            val image = imageArray.getJSONObject(i)
            imageList.add(image.getString("image"))
        }

        currentImagePosition = 0
        swipProductImage(currentImagePosition)

        svProductDetails.visibility = View.VISIBLE
        progressDialog.cancel()
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
}

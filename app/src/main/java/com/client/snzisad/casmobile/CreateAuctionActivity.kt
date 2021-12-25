package com.client.snzisad.casmobile

import android.app.Activity
import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.widget.CardView
import android.text.TextUtils
import android.util.Base64
import android.view.View
import android.widget.*
import com.android.volley.*
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.client.snzisad.casmobile.BusinessClass.APILink
import com.client.snzisad.casmobile.BusinessClass.DataHandler
import kotlinx.android.synthetic.main.activity_create_auction.*
import org.json.JSONArray
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

class CreateAuctionActivity : AppCompatActivity() {
    private lateinit var progressDialog: ProgressDialog
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    val calendar = Calendar.getInstance()
    private lateinit var edittextList: MutableList<EditText>

    private lateinit var cityNameList: MutableList<String>
    private lateinit var cityIDList: MutableList<Int>
    private lateinit var categoryNameList: MutableList<String>
    private lateinit var categoryIDList: MutableList<Int>
    private lateinit var subcategoryNameList: MutableList<String>
    private lateinit var subcategoryIDList: MutableList<Int>

    private lateinit var tvImageTitleList: MutableList<TextView>

    private lateinit var arrayRequest: JsonArrayRequest
    private lateinit var requestQueue: RequestQueue

    private lateinit var cityAdapter: ArrayAdapter<String>
    private lateinit var categoryAdapter: ArrayAdapter<String>
    private lateinit var subcategoryAdapter: ArrayAdapter<String>

    private var category_id = 0
    private var imageSelected = false
    private var selectedImagePosition = 0
    private val imageBitmap = arrayOfNulls<Bitmap>(3)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_auction)

        init()
        buttonClickHandle()
        setDatePicker()
    }


    private fun init(){
        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Please Wait...")
        progressDialog.setCancelable(false)

        sharedPreferences = getSharedPreferences("Status", 0)
        editor = sharedPreferences.edit()

        edittextList = ArrayList<EditText>()
        edittextList.addAll(listOf(edtLocation, edtTitle, edtDescription, edtKeyword, edtStartDate, edtEndDate, edtStartPrice, edtReservePrice))

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

        tvImageTitleList = ArrayList<TextView>()
        tvImageTitleList.addAll(listOf(tvPic1, tvPic2, tvPic3))

        getCityFromServer()
        getCategoryFromServer(0, categoryNameList, categoryIDList, categoryAdapter)

    }

    private fun buttonClickHandle(){

        spnCategory.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position == 0){
                    subcategoryIDList.clear()
                    llSubcategory.visibility = View.GONE
                }
                else{
                    getCategoryFromServer(categoryIDList.get(position), subcategoryNameList, subcategoryIDList, subcategoryAdapter)
                }
            }

        }

        btnPost.setOnClickListener {
            if(checkBlank()){
                sendDataToServer()
            }
        }

        btnChoosePic1.setOnClickListener {
            selectedImagePosition = 0
            if(checkGalleryPermission()){
                chooseImage()
            }
        }

        btnChoosePic2.setOnClickListener {
            selectedImagePosition = 1
            if(checkGalleryPermission()){
                chooseImage()
            }
        }

        btnChoosePic3.setOnClickListener {
            selectedImagePosition = 2
            if(checkGalleryPermission()){
                chooseImage()
            }
        }
    }

    private fun checkBlank(): Boolean{
        for (i in 0 until edittextList.size){
            if(
                TextUtils.isEmpty(edittextList.get(i).text)
            ){
                Toast.makeText(this, "Please fill up all field", Toast.LENGTH_SHORT).show()
                return false;
            }
        }

        if(!imageSelected){
            Toast.makeText(this, "Please select at least one picture", Toast.LENGTH_SHORT).show()
            return false;
        }

        if(subcategoryIDList.size>0){
            category_id = subcategoryIDList.get(spnSubCategory.selectedItemPosition)
        }
        else{
            category_id = categoryIDList.get(spnCategory.selectedItemPosition)
        }

        return true;
    }

    private fun getCityFromServer(){
        progressDialog.show()
        val url = APILink.getcity
        cityNameList.clear()
        cityIDList.clear()

        requestQueue = Volley.newRequestQueue(this)

        arrayRequest = JsonArrayRequest(url,
            Response.Listener{response->
                try{
                    requestQueue.stop()

                    val status = response.getJSONObject(0).get("status");

                    if(status == 1){
                        val data = response.getJSONObject(0).getJSONArray("data");

                        for(i in 0 until data.length()){
                            val city = data.getJSONObject(i)
                            cityNameList.add(city.getString("city_name"))
                            cityIDList.add(city.getInt("id"))
                        }
                        cityAdapter.notifyDataSetChanged()
                    }
                    else{
                        requestQueue.stop()
                        Toast.makeText(this, "Something went wrong. Please try again", Toast.LENGTH_SHORT).show()
                    }
                }
                catch (e: java.lang.Exception){
                    requestQueue.stop()
                    Toast.makeText(this, "Error: "+e.localizedMessage, Toast.LENGTH_SHORT).show()
                }

                progressDialog.cancel()

            },
            Response.ErrorListener {
                requestQueue.stop()
                progressDialog.cancel()
                Toast.makeText(this, "error: "+it.toString(), Toast.LENGTH_SHORT).show()
            }
        )

        requestQueue.add(arrayRequest)
    }

    private fun getCategoryFromServer(parent: Int, name: MutableList<String>, id: MutableList<Int>, adapter: ArrayAdapter<String>){
        progressDialog.show()
        val url = APILink.getcategory+"parent="+parent

        name.clear()
        id.clear()

        requestQueue = Volley.newRequestQueue(this)
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

                        if(subcategoryIDList.size>0){
                            llSubcategory.visibility = View.VISIBLE
                        }
                        else{
                            llSubcategory.visibility = View.GONE
                        }

                        adapter.notifyDataSetChanged()
                    }
                    else {
                        requestQueue.stop()
                        Toast.makeText(this, "Something went wrong. Please try again", Toast.LENGTH_SHORT).show()
                    }

                }
                catch (e: java.lang.Exception){
                    requestQueue.stop()
                    Toast.makeText(this, "Error: "+e.localizedMessage, Toast.LENGTH_SHORT).show()

                }
                progressDialog.cancel()

            },
            Response.ErrorListener {
                requestQueue.stop()
                progressDialog.cancel()
                Toast.makeText(this, "error: "+it.toString(), Toast.LENGTH_SHORT).show()
            }
        )

        requestQueue.add(arrayRequest)
    }

    private fun checkGalleryPermission(): Boolean {
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)){
                ActivityCompat.requestPermissions(this, arrayOf(
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                ), 1000
                )
            }
            else{
                ActivityCompat.requestPermissions(this, arrayOf(
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                ), 1000
                )
            }
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when(requestCode){
            1000 ->{
                if(grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                        chooseImage()
                    }
                    else{
                        Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == 1000 && resultCode == Activity.RESULT_OK && data!=null){
            val filpath: Uri = data.getData()

            try{
                imageSelected = true
                val inputStream = contentResolver.openInputStream(filpath)
                val bitmap = BitmapFactory.decodeStream(inputStream)

                imageBitmap.set(selectedImagePosition, bitmap)
                tvImageTitleList.get(selectedImagePosition).setText(filpath.toString())
            }
            catch (e: Exception){
                e.printStackTrace()
            }

        }
    }

    private fun chooseImage(){
        val intent = Intent(Intent.ACTION_PICK)
        intent.setType("image/*")
        startActivityForResult(Intent.createChooser(intent, "Select Image"), 1000)
    }

    private fun sendDataToServer() {
        progressDialog.show()

        val requestQueue = Volley.newRequestQueue(this)

        val stringRequest = object : StringRequest(
            Request.Method.POST, APILink.createauction,
            Response.Listener { response ->
                requestQueue.stop()

                try {
                    val response = JSONArray(response).getJSONObject(0)
                    val status = response.getInt("status")
                    val post_id = response.getInt("data");

                    if(status == 1 && post_id != 0){
                        DataHandler.productID =  post_id
                        startActivity(Intent(this, AuctionDetailsActivity::class.java))
                        finish()

                    }
                    else{
                        Toast.makeText(this, "Failed to create auction. Please try again", Toast.LENGTH_SHORT).show()
                    }

                    progressDialog.cancel()
                } catch (e: Exception) {
                    Toast.makeText(this, e.localizedMessage, Toast.LENGTH_LONG).show()
                    e.printStackTrace()
                }

            }, Response.ErrorListener {
                requestQueue.stop()
                progressDialog.cancel()
            }) {
            @Throws(AuthFailureError::class)
            override fun getParams(): Map<String, String> {
                val param = HashMap<String, String>()

                param["images"] = convertBitmaptoString()
                param["city_id"] = cityIDList.get(spnCity.selectedItemPosition).toString()
                param["location"] = edtLocation.getText().toString()
                param["category_id"] = category_id.toString()
                param["auction_title"] = edtTitle.getText().toString()
                param["auction_desc"] = edtDescription.getText().toString()
                param["condition"] = spnCondition.selectedItem.toString()
                param["product_type"] = spnItemType.selectedItem.toString()
                param["starting_price"] = edtStartPrice.getText().toString()
                param["reserve_price"] = edtReservePrice.getText().toString()
                param["keyword"] = edtKeyword.getText().toString()
                param["starting_date"] = edtStartDate.getText().toString()
                param["ending_date"] = edtEndDate.getText().toString()
                param["com_id"] = DataHandler.userID.toString()

                return param
            }

        }

//        stringRequest.setRetryPolicy(DefaultRetryPolicy(5000, 0, 0f))
        requestQueue.add(stringRequest)
    }


    private fun convertBitmaptoString():String{

        var imagesString:String = ""

        for(i in 0 until imageBitmap.size){
            val bitmap = imageBitmap.get(i)

            if( bitmap!= null){
                val outputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                val imageByte = outputStream.toByteArray()
                val encodedImage = Base64.encodeToString(imageByte, Base64.DEFAULT)

                imagesString+=encodedImage+"||"
            }
        }

        return imagesString;
    }

    fun setDatePicker(){
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val month = calendar.get(Calendar.MONTH)
        val year = calendar.get(Calendar.YEAR)

        edtStartDate.setOnClickListener {
            val datepicker = DatePickerDialog(this, DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                calendar.set(Calendar.YEAR, year)

                val date = SimpleDateFormat("yyyy-MM-dd").format(calendar.time)
                edtStartDate.setText(date+" 00:00:00")

            }, year, month, day)
            datepicker.show()
        }

        edtEndDate.setOnClickListener {
            val datepicker = DatePickerDialog(this, DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                calendar.set(Calendar.YEAR, year)

                val date = SimpleDateFormat("yyyy-MM-dd").format(calendar.time)
                edtEndDate.setText(date+" 00:00:00")

            }, year, month, day)
            datepicker.show()
        }

    }

}


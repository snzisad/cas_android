package com.client.snzisad.casmobile

import android.app.ProgressDialog
import android.content.Intent
import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.EditText
import android.widget.Toast
import com.android.volley.AuthFailureError
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.client.snzisad.casmobile.BusinessClass.APILink
import com.client.snzisad.casmobile.BusinessClass.DataHandler
import kotlinx.android.synthetic.main.activity_registation.*
import org.json.JSONArray

class RegistrationActivity : AppCompatActivity() {
    private lateinit var progressDialog: ProgressDialog
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var edittextList: MutableList<EditText>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registation)

        init()
    }

    private fun init(){

        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Please Wait...")
        progressDialog.setCancelable(false)

        sharedPreferences = getSharedPreferences("Status", 0)
        editor = sharedPreferences.edit()

        edittextList = ArrayList<EditText>()
        edittextList.addAll(listOf(edtName, edtAddress, edtPhone, edtEmail, edtNID, edtUsername, edtPassword))

        btnRegister.setOnClickListener {
            if(checkBlank()){
                sendDataToServer()
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

        return true;
    }

    private fun sendDataToServer() {
        progressDialog.show()

        val requestQueue = Volley.newRequestQueue(this)

        val stringRequest = object : StringRequest(
            Request.Method.POST, APILink.register,
            Response.Listener { response ->
                requestQueue.stop()

                try {
                    val response = JSONArray(response).getJSONObject(0)
                    val status = response.get("status")
                    val data = response.getJSONArray("data");

                    if(status == 1){
                        val user = data.getJSONObject(0)

                        DataHandler.userID = user.getInt("id")
                        DataHandler.userType = user.getInt("user_type_id")
                        DataHandler.userName = user.getString("username")

                        editor.putBoolean("logged", true)
                        editor.putInt("id", DataHandler.userID)
                        editor.putString("name", DataHandler.userName)
                        editor.putInt("type", DataHandler.userType)
                        editor.apply()

                        gotoNext()
                        finish()

                    }
                    else{
                        Toast.makeText(this, "Email or Username already has taken", Toast.LENGTH_SHORT).show()
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
                param["username"] = edtUsername.getText().toString()
                param["password"] = edtPassword.getText().toString()
                param["name"] = edtName.getText().toString()
                param["contact"] = edtPhone.getText().toString()
                param["address"] = edtAddress.getText().toString()
                param["email"] = edtEmail.getText().toString()
                param["nid"] = edtNID.getText().toString()
                param["user_type_id"] = (spType.selectedItemPosition+2).toString()
                param["firebase_uid"] = DataHandler.FCMToken

                return param
            }

        }


        stringRequest.setRetryPolicy(DefaultRetryPolicy(5000, 0, 0f))
        requestQueue.add(stringRequest)
    }


    private fun gotoNext(){
        val next = DataHandler.nextActin

        if(next == "newpost"){
            startActivity(Intent(this, CreateAdActivity::class.java))
        }
        else if(next == "newauction" && DataHandler.userType == 1){
            startActivity(Intent(this, CreateAuctionActivity::class.java))
        }
        else if(next == "newauction" && DataHandler.userType == 0){
            startActivity(Intent(this, CreateAdActivity::class.java))
        }
        else if(next.equals("message")){
            startActivity(Intent(this, ChatActivity::class.java))
        }
        else if(next.equals("bid")){
            startActivity(Intent(this, AuctionDetailsActivity::class.java))
        }
        else{
            DataHandler.locationID = 0
            DataHandler.categoryID = 0
            startActivity(Intent(this, ProductActivity::class.java))
        }

    }

}

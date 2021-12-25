package com.client.snzisad.casmobile

import android.app.ProgressDialog
import android.content.Intent
import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.client.snzisad.casmobile.BusinessClass.APILink
import com.client.snzisad.casmobile.BusinessClass.DataHandler
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.android.synthetic.main.activity_login.*
import org.json.JSONArray
import org.json.JSONObject

class LoginActivity : AppCompatActivity() {
    private lateinit var progressDialog: ProgressDialog
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        getFirebaseMessingUID()
        init()
    }

    private fun init(){

        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Please Wait...")
        progressDialog.setCancelable(false)

        sharedPreferences = getSharedPreferences("Status", 0)
        editor = sharedPreferences.edit()

        btnLogin.setOnClickListener {
            if(checkBlank()){
                sendDataToServer()
            }
        }

        tvNewAccount.setOnClickListener {
            startActivity(Intent(this, RegistrationActivity::class.java))
        }

        if(sharedPreferences.getBoolean("logged", false)) {
            DataHandler.userID = sharedPreferences.getInt("id", 0)
            DataHandler.userName = sharedPreferences.getString("name", "")
            DataHandler.userType = sharedPreferences.getInt("type", 0)

            gotoNext()

            finish()
        }

    }

    private fun checkBlank(): Boolean{
        if(
            TextUtils.isEmpty(edtUsername.text.toString()) ||
            TextUtils.isEmpty(edtPassword.text.toString())
        ){
            Toast.makeText(this, "Please fill up all field", Toast.LENGTH_SHORT).show()
            return false;
        }

        return true;
    }

    private fun sendDataToServer() {
        progressDialog.show()

        val requestQueue = Volley.newRequestQueue(this)

        val stringRequest = object : StringRequest(
            Request.Method.POST, APILink.login,
            Response.Listener { response ->
                requestQueue.stop()

                try {
                    val response = JSONArray(response).getJSONObject(0)
                    val status = response.get("status")
                    if(status == 1){

                        val data = response.getJSONArray("data");
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
                        Toast.makeText(this, "Invalid Username or Password", Toast.LENGTH_SHORT).show()
                    }

                    progressDialog.cancel()
                } catch (e: Exception) {
                    Toast.makeText(this, e.localizedMessage, Toast.LENGTH_LONG).show()
                    e.printStackTrace()
                }

            }, Response.ErrorListener {
                progressDialog.cancel()
                requestQueue.stop()
            }) {
            @Throws(AuthFailureError::class)
            override fun getParams(): Map<String, String> {
                val param = HashMap<String, String>()
                param["username"] = edtUsername.getText().toString()
                param["password"] = edtPassword.getText().toString()
                param["user_type"] = (spType.selectedItemPosition+2).toString()
                param["firebase_uid"] = DataHandler.FCMToken

                return param
            }

        }

        requestQueue.add(stringRequest)
    }

    private fun getFirebaseMessingUID(){
        FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener(OnCompleteListener {
            if (it.isSuccessful){
                val token = it.result?.token
                DataHandler.FCMToken = token

                Log.e("FCMToken\n", token)
            }
        })
    }

    private fun gotoNext(){
        val next = DataHandler.nextActin
        if(next == "newpost"){
            startActivity(Intent(this, CreateAdActivity::class.java))
        }
        else if(next == "newauction" && DataHandler.userType == 3){
            startActivity(Intent(this, CreateAuctionActivity::class.java))
        }
        else if(next == "newauction" && DataHandler.userType == 2){
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

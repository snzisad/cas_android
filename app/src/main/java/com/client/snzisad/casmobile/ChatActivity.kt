package com.client.snzisad.casmobile

import android.app.ProgressDialog
import android.content.Intent
import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.client.snzisad.casmobile.BusinessClass.APILink
import com.client.snzisad.casmobile.BusinessClass.DataHandler
import com.client.snzisad.casmobile.BusinessClass.MessageListAdapter
import com.client.snzisad.casmobile.BusinessClass.ProductListAdapter
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.activity_login.*
import org.json.JSONArray
import java.lang.Exception

class ChatActivity : AppCompatActivity() {
    private lateinit var adapter: MessageListAdapter
    private lateinit var userAdapter: ArrayAdapter<String>
    private lateinit var progressDialog: ProgressDialog
    private lateinit var messageList: MutableList<HashMap<String, String>>
    private lateinit var usersID: MutableList<String>
    private lateinit var usersName: MutableList<String>
    private lateinit var sharedPreferences: SharedPreferences
    var mentionUserID = "0"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        val intent = getIntent()

        if(intent.getIntExtra("pid", 0) != 0){
            DataHandler.productID = intent.getIntExtra("pid", 0)
        }

        init()

    }

    private fun init(){
        messageList = ArrayList<HashMap<String, String>>()
        usersID = ArrayList<String>()
        usersName = ArrayList<String>()
        adapter = MessageListAdapter(this, messageList)
        userAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, usersName)

        progressDialog = ProgressDialog(this)
        progressDialog!!.setMessage("Please wait...")
        progressDialog.setCancelable(false)


        sharedPreferences = getSharedPreferences("Status", 0)

        if(sharedPreferences.getBoolean("logged", false)) {
            DataHandler.userID = sharedPreferences.getInt("id", 0)
            DataHandler.userName = sharedPreferences.getString("name", "")
            DataHandler.userType = sharedPreferences.getInt("type", 0)
        }
        else{
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        rvMessages.adapter = adapter

        edtMessage.setAdapter(userAdapter)

        edtMessage.setOnItemClickListener { parent, view, position, id ->
            val p = usersName.indexOf(parent.getItemAtPosition(position))
            mentionUserID = usersID.get(p)
        }

        edtMessage.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {

            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }


            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
//                var position = 0
//
//                if(p3>p2) position = p2
//                else{
//                    position = (p3-1)
//                }
//
//                if(position>=0 && p0!![position] == '@' ){
//                    edtMessage.setAdapter(userAdapter)
//                    edtMessage.showDropDown()
////                    && p0!![position].equals("@")

//                if(p1==0){
//                    mentionUserID = "0"
//                    Toast.makeText(this@ChatActivity, "Mention removed", Toast.LENGTH_SHORT).show()
//                }
//                }

            }
        })

        val layoutManager = LinearLayoutManager(this)
        layoutManager.reverseLayout = true
        layoutManager.stackFromEnd = false
        rvMessages.layoutManager = layoutManager

        getMessagesFromServer()

        imgSendMessage.setOnClickListener{
            sendMessage()
        }
    }

    private fun getMessagesFromServer(){
        progressDialog.show()
        val url = APILink.getchat+DataHandler.productID
        messageList.clear()

        val requestQueue = Volley.newRequestQueue(this)

        val arrayRequest = JsonArrayRequest(url,
            Response.Listener{ response->
                try{

                    val status = response.getJSONObject(0).get("status");

                    if(status == 1){
                        val data = response.getJSONObject(0).getJSONArray("data");
                        requestQueue.stop()

                        for(i in 0 until data.length()){
                            val message = data.getJSONObject(i)
                            val chat = HashMap<String, String>()

                            val uid = message.getString("userid")
                            val uname = message.getString("name")

                            chat.put("userid", uid)
                            chat.put("name", uname)
                            chat.put("chat_msg", message.getString("chat_msg"))

                            messageList.add(chat)

                            if(!usersID.contains(uid)){
                                usersID.add(uid)
                                usersName.add(uname)
                            }
                        }

                        adapter.notifyDataSetChanged()
                        userAdapter.notifyDataSetChanged()

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
    }

    private fun sendMessage(){
        val message = edtMessage.text
        edtMessage.setText("")
        if(TextUtils.isEmpty(message)){
            Toast.makeText(this, "Write your message", Toast.LENGTH_SHORT).show()
        }
        else{
            addToListAndSendToServer(message.toString())
        }
    }

    private fun addToListAndSendToServer(message: String){
        val newMessage = HashMap<String, String>()

        newMessage.put("userid", DataHandler.userID.toString())
        newMessage.put("pid", DataHandler.productID.toString())
        newMessage.put("chat_msg", message)
        newMessage.put("mentionID", mentionUserID)

        messageList.add(0, newMessage)
        adapter.notifyDataSetChanged()

        val requestQueue = Volley.newRequestQueue(this)

        val stringRequest = object : StringRequest(
            Request.Method.POST, APILink.sendmessage,
            Response.Listener { response ->
                requestQueue.stop()

                try {
                    val response = JSONArray(response).getJSONObject(0)

                    val status = response.get("status")

//                    Toast.makeText(this, "Mention USER: "+mentionUserID, Toast.LENGTH_SHORT).show()

                    if(status != 1){
                        Toast.makeText(this, "Failed to send message", Toast.LENGTH_SHORT).show()
                    }

                } catch (e: Exception) {
//                    Toast.makeText(this, "Exception: "+e.localizedMessage, Toast.LENGTH_LONG).show()
                    e.printStackTrace()
                }

            }, Response.ErrorListener {
                requestQueue.stop()
                Toast.makeText(this, "Error: "+it.localizedMessage, Toast.LENGTH_LONG).show()
            }) {
            @Throws(AuthFailureError::class)
            override fun getParams(): Map<String, String> {
                return newMessage
            }

        }

        requestQueue.add(stringRequest)
    }


}

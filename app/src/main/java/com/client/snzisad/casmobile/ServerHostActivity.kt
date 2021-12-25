package com.client.snzisad.casmobile

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.client.snzisad.casmobile.BusinessClass.APILink
import kotlinx.android.synthetic.main.activity_server_host.*

class ServerHostActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_server_host)

        btnSaveHost.setOnClickListener {
//            APILink.host = edtHostAddress.text.toString()
//            APILink.setLink()

            startActivity(Intent(this, ProductActivity::class.java))
        }
    }
}

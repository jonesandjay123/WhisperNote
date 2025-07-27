package com.jovicheer.whispernote

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.wearable.*

class MainActivity : Activity(), DataClient.OnDataChangedListener {
    
    private lateinit var titleTextView: TextView
    private lateinit var startReceivingButton: Button
    private lateinit var messageTextView: TextView
    private lateinit var statusTextView: TextView
    private lateinit var dataClient: DataClient
    private var isReceiving = false
    
    companion object {
        private const val MESSAGE_PATH = "/message"
        private const val MESSAGE_KEY = "message_data"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        initViews()
        initWearableClient()
    }
    
    private fun initViews() {
        titleTextView = findViewById(R.id.titleTextView)
        startReceivingButton = findViewById(R.id.startReceivingButton)
        messageTextView = findViewById(R.id.messageTextView)
        statusTextView = findViewById(R.id.statusTextView)
        
        startReceivingButton.setOnClickListener {
            toggleReceiving()
        }
    }
    
    private fun initWearableClient() {
        dataClient = Wearable.getDataClient(this)
    }
    
    private fun toggleReceiving() {
        isReceiving = !isReceiving
        if (isReceiving) {
            startReceivingButton.text = "Stop Receiving"
            statusTextView.text = "Status: Listening for messages..."
            Toast.makeText(this, "Started listening for messages", Toast.LENGTH_SHORT).show()
        } else {
            startReceivingButton.text = "Start Receiving"
            statusTextView.text = "Status: Not listening"
            messageTextView.text = "No message received"
            Toast.makeText(this, "Stopped listening", Toast.LENGTH_SHORT).show()
        }
    }
    
    override fun onResume() {
        super.onResume()
        dataClient.addListener(this)
    }
    
    override fun onPause() {
        super.onPause()
        dataClient.removeListener(this)
    }
    
    override fun onDataChanged(dataEvents: DataEventBuffer) {
        if (!isReceiving) return
        
        for (event in dataEvents) {
            if (event.type == DataEvent.TYPE_CHANGED) {
                val dataItem = event.dataItem
                if (dataItem.uri.path == MESSAGE_PATH) {
                    val dataMap = DataMapItem.fromDataItem(dataItem).dataMap
                    val message = dataMap.getString(MESSAGE_KEY)
                    
                    runOnUiThread {
                        messageTextView.text = "Received: $message"
                        statusTextView.text = "Status: Message received!"
                        Toast.makeText(this, "New message from phone!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}
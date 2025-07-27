package com.jovicheer.whispernote

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.wearable.*

class MainActivity : AppCompatActivity(), DataClient.OnDataChangedListener {
    
    private lateinit var messageEditText: EditText
    private lateinit var sendButton: Button
    private lateinit var statusTextView: TextView
    private lateinit var dataClient: DataClient
    
    companion object {
        private const val MESSAGE_PATH = "/message"
        private const val MESSAGE_KEY = "message_data"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        
        initViews()
        initWearableClient()
    }
    
    private fun initViews() {
        messageEditText = findViewById(R.id.messageEditText)
        sendButton = findViewById(R.id.sendButton)
        statusTextView = findViewById(R.id.statusTextView)
        
        sendButton.setOnClickListener {
            sendMessageToWatch()
        }
    }
    
    private fun initWearableClient() {
        dataClient = Wearable.getDataClient(this)
    }
    
    private fun sendMessageToWatch() {
        val message = messageEditText.text.toString().trim()
        if (message.isEmpty()) {
            Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show()
            return
        }
        
        statusTextView.text = "Status: Sending..."
        
        val putDataReq = PutDataMapRequest.create(MESSAGE_PATH).apply {
            dataMap.putString(MESSAGE_KEY, message)
            dataMap.putLong("timestamp", System.currentTimeMillis())
        }.asPutDataRequest().setUrgent()
        
        dataClient.putDataItem(putDataReq).addOnSuccessListener {
            statusTextView.text = "Status: Message sent successfully!"
            messageEditText.text.clear()
            Toast.makeText(this, "Message sent to watch!", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener { exception ->
            statusTextView.text = "Status: Failed to send message"
            Toast.makeText(this, "Failed to send: ${exception.message}", Toast.LENGTH_LONG).show()
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
        // Handle any data changes from watch if needed
    }
}
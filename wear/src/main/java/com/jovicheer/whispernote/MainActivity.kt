package com.jovicheer.whispernote

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.*
import com.google.android.gms.wearable.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MainActivity : Activity(), MessageClient.OnMessageReceivedListener {
    
    private lateinit var syncButton: Button
    private lateinit var sendButton: Button
    private lateinit var inputLayout: LinearLayout
    private lateinit var newIdeaEditText: EditText
    private lateinit var submitButton: Button
    private lateinit var statusTextView: TextView
    private lateinit var ideasContainer: LinearLayout
    
    private lateinit var messageClient: MessageClient
    private lateinit var nodeClient: NodeClient
    
    private val ideasList = mutableListOf<String>()
    private val gson = Gson()
    
    companion object {
        private const val REQUEST_LIST_PATH = "/request_list"
        private const val RESPONSE_LIST_PATH = "/response_list"
        private const val ADD_IDEA_PATH = "/add_idea"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        initViews()
        initWearableClient()
        setupIdeasContainer()
    }
    
    private fun initViews() {
        syncButton = findViewById(R.id.syncButton)
        sendButton = findViewById(R.id.sendButton)
        inputLayout = findViewById(R.id.inputLayout)
        newIdeaEditText = findViewById(R.id.newIdeaEditText)
        submitButton = findViewById(R.id.submitButton)
        statusTextView = findViewById(R.id.statusTextView)
        ideasContainer = findViewById(R.id.ideasContainer)
        
        syncButton.setOnClickListener {
            syncWithPhone()
        }
        
        sendButton.setOnClickListener {
            toggleInputLayout()
        }
        
        submitButton.setOnClickListener {
            sendNewIdeaToPhone()
        }
    }
    
    private fun initWearableClient() {
        messageClient = Wearable.getMessageClient(this)
        nodeClient = Wearable.getNodeClient(this)
    }
    
    private fun setupIdeasContainer() {
        updateIdeasDisplay()
    }
    
    private fun updateIdeasDisplay() {
        ideasContainer.removeAllViews()
        
        for (idea in ideasList) {
            val ideaView = TextView(this).apply {
                text = idea
                textSize = 11f
                setTextColor(0xFFFFFFFF.toInt())
                setBackgroundColor(0xFF2A2A2A.toInt())
                setPadding(16, 12, 16, 12)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 0, 4)
                }
                minHeight = 72 // 36dp * 2 for better touch target on watch
                gravity = android.view.Gravity.CENTER_VERTICAL
            }
            ideasContainer.addView(ideaView)
        }
    }
    
    private fun syncWithPhone() {
        statusTextView.text = "Status: Syncing..."
        Toast.makeText(this, "Syncing with phone...", Toast.LENGTH_SHORT).show()
        
        nodeClient.connectedNodes.addOnSuccessListener { connectedNodes ->
            if (connectedNodes.isNotEmpty()) {
                val phoneNodeId = connectedNodes.first().id
                sendSyncRequestToPhone(phoneNodeId)
            } else {
                statusTextView.text = "Status: No phone connected"
                Toast.makeText(this, "No phone connected", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { exception ->
            statusTextView.text = "Status: Failed to find phone"
            Toast.makeText(this, "Failed to find phone: ${exception.message}", Toast.LENGTH_LONG).show()
        }
    }
    
    private fun sendSyncRequestToPhone(nodeId: String) {
        val requestBytes = "sync_request".toByteArray()
        
        messageClient.sendMessage(nodeId, REQUEST_LIST_PATH, requestBytes)
            .addOnSuccessListener {
                statusTextView.text = "Status: Sync request sent..."
            }
            .addOnFailureListener { exception ->
                statusTextView.text = "Status: Failed to sync"
                Toast.makeText(this, "Failed to sync: ${exception.message}", Toast.LENGTH_LONG).show()
            }
    }
    
    private fun toggleInputLayout() {
        if (inputLayout.visibility == View.GONE) {
            inputLayout.visibility = View.VISIBLE
            sendButton.text = "✕ Cancel"
            newIdeaEditText.requestFocus()
        } else {
            inputLayout.visibility = View.GONE
            sendButton.text = "📝 Send"
            newIdeaEditText.text.clear()
        }
    }
    
    private fun sendNewIdeaToPhone() {
        val idea = newIdeaEditText.text.toString().trim()
        if (idea.isEmpty()) {
            Toast.makeText(this, "Please enter an idea", Toast.LENGTH_SHORT).show()
            return
        }
        
        statusTextView.text = "Status: Sending idea..."
        
        nodeClient.connectedNodes.addOnSuccessListener { connectedNodes ->
            if (connectedNodes.isNotEmpty()) {
                val phoneNodeId = connectedNodes.first().id
                sendIdeaToPhone(phoneNodeId, idea)
            } else {
                statusTextView.text = "Status: No phone connected"
                Toast.makeText(this, "No phone connected", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { exception ->
            statusTextView.text = "Status: Failed to find phone"
            Toast.makeText(this, "Failed to find phone: ${exception.message}", Toast.LENGTH_LONG).show()
        }
    }
    
    private fun sendIdeaToPhone(nodeId: String, idea: String) {
        val ideaBytes = idea.toByteArray()
        
        messageClient.sendMessage(nodeId, ADD_IDEA_PATH, ideaBytes)
            .addOnSuccessListener {
                statusTextView.text = "Status: Idea sent successfully!"
                Toast.makeText(this, "Idea sent to phone!", Toast.LENGTH_SHORT).show()
                toggleInputLayout() // 關閉輸入框
                // 自動同步更新清單
                syncWithPhone()
            }
            .addOnFailureListener { exception ->
                statusTextView.text = "Status: Failed to send idea"
                Toast.makeText(this, "Failed to send: ${exception.message}", Toast.LENGTH_LONG).show()
            }
    }
    
    override fun onResume() {
        super.onResume()
        messageClient.addListener(this)
    }
    
    override fun onPause() {
        super.onPause()
        messageClient.removeListener(this)
    }
    
    override fun onMessageReceived(messageEvent: MessageEvent) {
        if (messageEvent.path == RESPONSE_LIST_PATH) {
            val jsonList = String(messageEvent.data)
            val type = object : TypeToken<List<String>>() {}.type
            val newIdeasList: List<String> = gson.fromJson(jsonList, type)
            
            runOnUiThread {
                ideasList.clear()
                ideasList.addAll(newIdeasList)
                updateIdeasDisplay()
                statusTextView.text = "Status: Synced! (${ideasList.size} ideas)"
                Toast.makeText(this, "Synced ${ideasList.size} ideas from phone!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
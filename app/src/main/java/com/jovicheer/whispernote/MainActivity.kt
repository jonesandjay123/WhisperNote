package com.jovicheer.whispernote

import android.app.AlertDialog
import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.wearable.*
import com.google.gson.Gson

/**
 * WhisperNote Phone App - Main Activity
 * 
 * Features:
 * - Manage a list of ideas/notes with add, edit, delete operations
 * - Respond to Wear OS watch requests for idea list synchronization
 * - Receive new ideas from the watch and add them to the list
 * 
 * Communication Protocol:
 * - REQUEST_LIST_PATH: Watch requests the current ideas list
 * - RESPONSE_LIST_PATH: Phone sends ideas list to watch
 * - ADD_IDEA_PATH: Watch sends a new idea to be added
 */
class MainActivity : AppCompatActivity(), MessageClient.OnMessageReceivedListener {
    
    private lateinit var newIdeaEditText: EditText
    private lateinit var addIdeaButton: Button
    private lateinit var statusTextView: TextView
    private lateinit var ideasListView: ListView
    private lateinit var messageClient: MessageClient
    
    private val ideasList = mutableListOf<String>()
    private lateinit var listAdapter: ArrayAdapter<String>
    private val gson = Gson()
    
    companion object {
        // Wear OS communication paths
        private const val REQUEST_LIST_PATH = "/request_list"    // Watch → Phone: Request ideas list
        private const val RESPONSE_LIST_PATH = "/response_list"  // Phone → Watch: Send ideas list
        private const val ADD_IDEA_PATH = "/add_idea"           // Watch → Phone: Add new idea
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        
        initViews()
        initWearableClient()
        setupListView()
        loadSampleData()
    }
    
    private fun initViews() {
        newIdeaEditText = findViewById(R.id.newIdeaEditText)
        addIdeaButton = findViewById(R.id.addIdeaButton)
        statusTextView = findViewById(R.id.statusTextView)
        ideasListView = findViewById(R.id.ideasListView)
        
        addIdeaButton.setOnClickListener {
            addNewIdea()
        }
    }
    
    private fun initWearableClient() {
        messageClient = Wearable.getMessageClient(this)
    }
    
    private fun setupListView() {
        listAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, ideasList)
        ideasListView.adapter = listAdapter
        
        // 點擊編輯
        ideasListView.setOnItemClickListener { _, _, position, _ ->
            editIdea(position)
        }
        
        // 長按刪除
        ideasListView.setOnItemLongClickListener { _, _, position, _ ->
            deleteIdea(position)
            true
        }
    }
    
    private fun loadSampleData() {
        ideasList.addAll(listOf(
            "Learn Kotlin for Android development",
            "Build a Wear OS app",
            "Implement data synchronization",
            "Create a note-taking system"
        ))
        listAdapter.notifyDataSetChanged()
        statusTextView.text = "Status: ${ideasList.size} ideas loaded"
    }
    
    private fun addNewIdea() {
        val idea = newIdeaEditText.text.toString().trim()
        if (idea.isEmpty()) {
            Toast.makeText(this, "Please enter an idea", Toast.LENGTH_SHORT).show()
            return
        }
        
        ideasList.add(idea)
        listAdapter.notifyDataSetChanged()
        newIdeaEditText.text.clear()
        statusTextView.text = "Status: Idea added (${ideasList.size} total)"
        Toast.makeText(this, "Idea added!", Toast.LENGTH_SHORT).show()
    }
    
    private fun editIdea(position: Int) {
        val editText = EditText(this)
        editText.setText(ideasList[position])
        
        AlertDialog.Builder(this)
            .setTitle("Edit Idea")
            .setView(editText)
            .setPositiveButton("Save") { _, _ ->
                val newText = editText.text.toString().trim()
                if (newText.isNotEmpty()) {
                    ideasList[position] = newText
                    listAdapter.notifyDataSetChanged()
                    statusTextView.text = "Status: Idea updated"
                    Toast.makeText(this, "Idea updated!", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun deleteIdea(position: Int) {
        AlertDialog.Builder(this)
            .setTitle("Delete Idea")
            .setMessage("Delete \"${ideasList[position]}\"?")
            .setPositiveButton("Delete") { _, _ ->
                ideasList.removeAt(position)
                listAdapter.notifyDataSetChanged()
                statusTextView.text = "Status: Idea deleted (${ideasList.size} total)"
                Toast.makeText(this, "Idea deleted!", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun sendIdeasListToWatch(nodeId: String) {
        val jsonList = gson.toJson(ideasList)
        val messageBytes = jsonList.toByteArray()
        
        messageClient.sendMessage(nodeId, RESPONSE_LIST_PATH, messageBytes)
            .addOnSuccessListener {
                statusTextView.text = "Status: List sent to watch (${ideasList.size} ideas)"
                Toast.makeText(this, "Ideas list sent to watch!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { exception ->
                statusTextView.text = "Status: Failed to send list"
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
        when (messageEvent.path) {
            REQUEST_LIST_PATH -> {
                // 手錶請求清單
                val nodeId = messageEvent.sourceNodeId
                sendIdeasListToWatch(nodeId)
            }
            ADD_IDEA_PATH -> {
                // 手錶傳來新增的想法
                val newIdea = String(messageEvent.data)
                runOnUiThread {
                    ideasList.add(newIdea)
                    listAdapter.notifyDataSetChanged()
                    statusTextView.text = "Status: New idea from watch! (${ideasList.size} total)"
                    Toast.makeText(this, "New idea added from watch: $newIdea", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
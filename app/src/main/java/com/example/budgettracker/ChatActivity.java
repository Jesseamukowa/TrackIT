package com.example.budgettracker;

import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.ChatFutures;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView rvChat;
    private ChatAdapter adapter;
    private List<ChatMessage> messageList;
    private EditText etInput;
    private ImageButton btnSend;

    // Tracky AI Variables
    private GenerativeModelFutures model;
    private ChatFutures chatSession;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // 1. Setup Toolbar
        Toolbar toolbar = findViewById(R.id.chatToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // 2. Initialize UI components
        rvChat = findViewById(R.id.rvChat);
        etInput = findViewById(R.id.etChatInput);
        btnSend = findViewById(R.id.btnSend);

        // 3. Setup RecyclerView
        messageList = new ArrayList<>();
        messageList.add(new ChatMessage("Wazi! I'm Tracky. I'm ready to help Jesse save money. How can I assist today?", ChatMessage.TYPE_BOT));

        adapter = new ChatAdapter(messageList);
        rvChat.setLayoutManager(new LinearLayoutManager(this));
        rvChat.setAdapter(adapter);

        // 4. Initialize Gemini AI & Chat Session (The Memory)
        setupTrackyAI();

        // 5. Send Button Logic
        btnSend.setOnClickListener(v -> {
            String userText = etInput.getText().toString().trim();
            if (!userText.isEmpty()) {
                sendMessage(userText);
            }
        });
    }

    private void setupTrackyAI() {
        // Try the most stable base name
        GenerativeModel gm = new GenerativeModel("gemini-pro", BuildConfig.GEMINI_API_KEY);
        model = GenerativeModelFutures.from(gm);

        // FIX: Break the chain for the system instruction
        Content.Builder systemBuilder = new Content.Builder();
        systemBuilder.setRole("user");
        systemBuilder.addText("You are Tracky, a friendly, witty financial advisor for Jesse at Maseno University. Keep advice short and student-friendly.");
        Content systemInstruction = systemBuilder.build();

        // FIX: Break the chain for the bot acknowledgment
        Content.Builder botBuilder = new Content.Builder();
        botBuilder.setRole("model");
        botBuilder.addText("Niaje! I'm ready to help Jesse save money.");
        Content botAck = botBuilder.build();

        // Start the chat session
        chatSession = model.startChat(Arrays.asList(systemInstruction, botAck));
    }

    private void sendMessage(String userText) {
        // 1. Add User Message to UI
        messageList.add(new ChatMessage(userText, ChatMessage.TYPE_USER));
        adapter.notifyItemInserted(messageList.size() - 1);
        rvChat.scrollToPosition(messageList.size() - 1);
        etInput.setText("");

        // FIX: Break the chain here too
        Content.Builder userMsgBuilder = new Content.Builder();
        userMsgBuilder.setRole("user");
        userMsgBuilder.addText(userText);
        Content userMessage = userMsgBuilder.build();

        ListenableFuture<GenerateContentResponse> response = chatSession.sendMessage(userMessage);

        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                String botReply = result.getText();
                runOnUiThread(() -> {
                    messageList.add(new ChatMessage(botReply, ChatMessage.TYPE_BOT));
                    adapter.notifyItemInserted(messageList.size() - 1);
                    rvChat.scrollToPosition(messageList.size() - 1);
                });
            }

            @Override
            public void onFailure(Throwable t) {
                Log.e("TrackyAI", "Error: " + t.getMessage());
                runOnUiThread(() -> {
                    messageList.add(new ChatMessage("Sorry, I'm having trouble connecting to the network.", ChatMessage.TYPE_BOT));
                    adapter.notifyItemInserted(messageList.size() - 1);
                });
            }
        }, androidx.core.content.ContextCompat.getMainExecutor(this));
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.chat_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == R.id.action_clear_chat) {
            clearChatSession();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void clearChatSession() {
        // 1. Wipe the UI list
        messageList.clear();

        // 2. Add the initial welcome message back
        messageList.add(new ChatMessage("Chat cleared! How can I help you start fresh, Jesse?", ChatMessage.TYPE_BOT));
        adapter.notifyDataSetChanged();

        // 3. Re-initialize Tracky to wipe his 'Memory'
        setupTrackyAI();

        // 4. Feedback for the user
        android.widget.Toast.makeText(this, "Memory Reset", android.widget.Toast.LENGTH_SHORT).show();
    }
}
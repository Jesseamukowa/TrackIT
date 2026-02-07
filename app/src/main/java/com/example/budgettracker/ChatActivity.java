package com.example.budgettracker;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

// Modern 2026 Firebase AI Imports
import com.google.firebase.ai.FirebaseAI;
import com.google.firebase.ai.GenerativeModel;
import com.google.firebase.ai.java.GenerativeModelFutures;
import com.google.firebase.ai.type.HarmBlockMethod;
import com.google.firebase.ai.type.HarmBlockThreshold;
import com.google.firebase.ai.type.Content;
import com.google.firebase.ai.type.GenerateContentResponse;
import com.google.firebase.ai.type.HarmCategory;
import com.google.firebase.ai.type.SafetySetting;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;

public class ChatActivity extends AppCompatActivity {
    private RecyclerView rvChat;
    private ChatAdapter adapter;
    private List<ChatMessage> messageList;
    private EditText etInput;
    private ImageButton btnSend, btnClear;
    private ProgressBar progressBar;

    private GenerativeModelFutures model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // 1. Initialize UI Elements
        rvChat = findViewById(R.id.rvChat);
        etInput = findViewById(R.id.etChatInput);
        btnSend = findViewById(R.id.btnSend);
        btnClear = findViewById(R.id.btnClear);
        progressBar = findViewById(R.id.progressBar);

        // 2. Setup RecyclerView
        messageList = new ArrayList<>();
        adapter = new ChatAdapter(messageList);
        rvChat.setLayoutManager(new LinearLayoutManager(this));
        rvChat.setAdapter(adapter);

        // 3. Setup Firebase Gemini Model (Gemini 3 Flash)
        SafetySetting harassment = new SafetySetting(
                HarmCategory.HARASSMENT,
                HarmBlockThreshold.ONLY_HIGH,
                HarmBlockMethod.PROBABILITY
        );

        SafetySetting hateSpeech = new SafetySetting(
                HarmCategory.HATE_SPEECH,
                HarmBlockThreshold.ONLY_HIGH,
                HarmBlockMethod.PROBABILITY
        );

        // In 2026, we use the FirebaseAI instance which handles the API Key via google-services.json
        FirebaseAI ai = FirebaseAI.getInstance();

        // "gemini-3-flash" is the recommended model for speed and accuracy in 2026
        GenerativeModel gm = ai.generativeModel("gemini-3-flash",
                null, // GenerationConfig (Optional)
                Arrays.asList(harassment, hateSpeech));

        model = GenerativeModelFutures.from(gm);

        // 4. Button Listeners
        btnSend.setOnClickListener(v -> sendMessage());

        btnClear.setOnClickListener(v -> {
            int size = messageList.size();
            messageList.clear();
            adapter.notifyItemRangeRemoved(0, size);
            Toast.makeText(this, "Chat cleared", Toast.LENGTH_SHORT).show();
        });
    }

    private void sendMessage() {
        String userText = etInput.getText().toString().trim();
        if (userText.isEmpty()) return;

        // Professional System Context for Jesse's App
        String systemInstruction = "You are 'Tracky', a budget assistant for Jesse at Maseno University. " +
                "Keep responses short and use KES (Kenyan Shillings). Context: " + userText;

        // Update UI
        messageList.add(new ChatMessage(userText, ChatMessage.TYPE_USER));
        adapter.notifyItemInserted(messageList.size() - 1);
        rvChat.scrollToPosition(messageList.size() - 1);

        etInput.setText("");
        progressBar.setVisibility(View.VISIBLE);

        // Request content generation
        Content userContent = new Content.Builder().addText(systemInstruction).build();
        ListenableFuture<GenerateContentResponse> response = model.generateContent(userContent);

        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                String botReply = result.getText();
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    if (botReply != null) {
                        messageList.add(new ChatMessage(botReply, ChatMessage.TYPE_BOT));
                        adapter.notifyItemInserted(messageList.size() - 1);
                        rvChat.scrollToPosition(messageList.size() - 1);
                    }
                });
            }

            @Override
            public void onFailure(Throwable t) {
                Log.e("TRACKY_ERROR", "Firebase AI Failure: " + t.getMessage());
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(ChatActivity.this, "Connection Error. Check google-services.json", Toast.LENGTH_LONG).show();
                });
            }
        }, Executors.newSingleThreadExecutor());
    }
}
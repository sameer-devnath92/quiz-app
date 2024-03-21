package com.mca.quiz;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ScoreActivity extends AppCompatActivity {

    public TextView scoreTextView;
    public Button returnButton;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score);

        scoreTextView = findViewById(R.id.scoreTextView);
        Button retryButton = findViewById(R.id.retryButton);
        returnButton = findViewById(R.id.returnButton);

        // Retrieve score from intent extra
        int score = getIntent().getIntExtra("score", 0);
        scoreTextView.setText("Your Score: "+ score +"%");

        retryButton.setOnClickListener(v -> retryQuiz());

        returnButton.setOnClickListener(v -> returnToQuizSelection());
    }

    private void retryQuiz() {
        // Navigate back to MainQuizActivity to retry the quiz
        Intent intent = new Intent(ScoreActivity.this, MainQuizActivity.class);
        startActivity(intent);
        finish();
    }

    private void returnToQuizSelection() {
        // Navigate back to CatalogueActivity to select another quiz
        Intent intent = new Intent(ScoreActivity.this, CatalogueActivity.class);
        startActivity(intent);
        finish();
    }
}


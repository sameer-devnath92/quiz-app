package com.mca.quiz;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class MainQuizActivity extends AppCompatActivity {

    private TextView questionTextView;
    private Button option1Button, option2Button, option3Button, option4Button;
    private FirebaseFirestore db;
    private final List<Question> questionList = new ArrayList<>();
    private int currentQuestionIndex = 0;
    private int score = 0;
    private String selectedDepartment, selectedCourse, selectedQuiz; // Variables for selected quiz IDs

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_quiz);

        db = FirebaseFirestore.getInstance();

        questionTextView = findViewById(R.id.questionTextView);
        option1Button = findViewById(R.id.option1Button);
        option2Button = findViewById(R.id.option2Button);
        option3Button = findViewById(R.id.option3Button);
        option4Button = findViewById(R.id.option4Button);

        // Retrieve selected quiz IDs from Intent
        Intent intent = getIntent();
        if (intent != null) {
            selectedDepartment = intent.getStringExtra("department_id");
            selectedCourse = intent.getStringExtra("course_id");
            selectedQuiz = intent.getStringExtra("quiz_id");
        }

        // Retrieve questions from Firestore
        loadQuestions();
    }

    @SuppressWarnings("unchecked")
    private void loadQuestions() {
        db.collection("department").document(selectedDepartment)
                .collection("course").document(selectedCourse)
                .collection("quizzes").document(selectedQuiz)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot quizDocument = task.getResult();
                        if (quizDocument.exists()) {
                            // Assuming questions are stored in a field called "questions"
                            List<Map<String, Object>> questionsData = (List<Map<String, Object>>) quizDocument.get("questions");
                            if (questionsData != null) {
                                for (Map<String, Object> questionData : questionsData) {
                                    // Extract question data
                                    String question = (String) questionData.get("question");
                                    List<String> options = (List<String>) questionData.get("options");
                                    String correctAnswer = (String) questionData.get("correctAnswer");
                                    // Create a Question object and add it to the list
                                    Question question1 = new Question(question, options, correctAnswer);
                                    questionList.add(question1);
                                }
                                // Shuffle the questions
                                Collections.shuffle(questionList);
                                // Start the quiz after loading questions
                                showNextQuestion();
                            }
                        } else {
                            Toast.makeText(MainQuizActivity.this, "Quiz document does not exist", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(MainQuizActivity.this, "Error retrieving quiz document", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void showNextQuestion() {
        if (currentQuestionIndex < questionList.size()) {
            Question currentQuestion = questionList.get(currentQuestionIndex);
            questionTextView.setText(currentQuestion.getQuestion());

            // Set options for buttons
            List<String> options = currentQuestion.getOptions();
            option1Button.setText(options.get(0));
            option2Button.setText(options.get(1));
            option3Button.setText(options.get(2));
            option4Button.setText(options.get(3));

            currentQuestionIndex++;
        } else {
            // Quiz completed
            finishQuiz();
        }
    }

    private void finishQuiz() {
        // Calculate score and navigate to ScoreActivity
        int totalQuestions = questionList.size();
        int percentageScore = (score * 100) / totalQuestions;
        Intent intent = new Intent(MainQuizActivity.this, ScoreActivity.class);
        intent.putExtra("score", percentageScore);
        startActivity(intent);
        finish();
    }

    public void onOptionSelected(View view) {
        Button selectedOption = (Button) view;
        String selectedAnswer = selectedOption.getText().toString();
        Question currentQuestion = questionList.get(currentQuestionIndex - 1);
        if (selectedAnswer.equals(currentQuestion.getCorrectAnswer())) {
            // Correct answer selected
            score++;
        }
        showNextQuestion();
    }
}

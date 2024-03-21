package com.mca.quiz;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuizCreationActivity extends AppCompatActivity {

    private Spinner departmentSpinner, courseSpinner;
    private EditText quizNameEditText, questionEditText, option1EditText, option2EditText, option3EditText, option4EditText, correctAnswerEditText;
    private final List<Question> questionList = new ArrayList<>();
    private FirebaseFirestore db;
    private String selectedDepartment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_quiz);

        db = FirebaseFirestore.getInstance();

        departmentSpinner = findViewById(R.id.spinnerDepartment);
        courseSpinner = findViewById(R.id.spinnerCourse);
        quizNameEditText = findViewById(R.id.editTextQuizName);
        questionEditText = findViewById(R.id.editTextQuestion);
        option1EditText = findViewById(R.id.editTextOption1);
        option2EditText = findViewById(R.id.editTextOption2);
        option3EditText = findViewById(R.id.editTextOption3);
        option4EditText = findViewById(R.id.editTextOption4);
        correctAnswerEditText = findViewById(R.id.editTextCorrectAnswer);
        Button createQuizButton = findViewById(R.id.buttonCreateQuiz);
        Button addQuestionButton = findViewById(R.id.buttonAddQuestion);

        // Populate department Spinner
        populateDepartmentSpinner();

        // Set a listener for department Spinner selection
        departmentSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedDepartment = parent.getItemAtPosition(position).toString();
                populateCourseSpinner(selectedDepartment);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        addQuestionButton.setOnClickListener(v -> addQuestion());
        createQuizButton.setOnClickListener(v -> createQuiz());
    }

    private void populateDepartmentSpinner() {
        db.collection("department")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<String> departmentNames = new ArrayList<>();
                        for (DocumentSnapshot document : task.getResult()) {
                            String departmentName = document.getId();
                            departmentNames.add(departmentName);
                        }
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, departmentNames);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        departmentSpinner.setAdapter(adapter);
                    } else {
                        Toast.makeText(this, "Error retrieving departments: " + task.getException(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void populateCourseSpinner(String department) {
        db.collection("department").document(department).collection("course")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<String> courseNames = new ArrayList<>();
                        for (DocumentSnapshot document : task.getResult()) {
                            String courseName = document.getId();
                            courseNames.add(courseName);
                        }
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, courseNames);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        courseSpinner.setAdapter(adapter);
                    } else {
                        Toast.makeText(this, "Error retrieving courses for department " + department + ": " + task.getException(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void addQuestion() {
        String question = questionEditText.getText().toString().trim();
        String option1 = option1EditText.getText().toString().trim();
        String option2 = option2EditText.getText().toString().trim();
        String option3 = option3EditText.getText().toString().trim();
        String option4 = option4EditText.getText().toString().trim();
        String correctAnswer = correctAnswerEditText.getText().toString().trim();

        if (question.isEmpty() || option1.isEmpty() || option2.isEmpty() || option3.isEmpty() || option4.isEmpty() || correctAnswer.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> options = new ArrayList<>();
        options.add(option1);
        options.add(option2);
        options.add(option3);
        options.add(option4);

        Question newQuestion = new Question(question, options, correctAnswer);
        questionList.add(newQuestion);

        // Clear EditText fields
        questionEditText.getText().clear();
        option1EditText.getText().clear();
        option2EditText.getText().clear();
        option3EditText.getText().clear();
        option4EditText.getText().clear();
        correctAnswerEditText.getText().clear();

        Toast.makeText(this, "Question added successfully", Toast.LENGTH_SHORT).show();
    }

    private void createQuiz() {
        String quizName = quizNameEditText.getText().toString().trim();
        if (quizName.isEmpty()) {
            Toast.makeText(this, "Please enter quiz name", Toast.LENGTH_SHORT).show();
            return;
        }

        if (questionList.isEmpty()) {
            Toast.makeText(this, "Please add at least one question", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a map to store quiz data
        Map<String, Object> quizData = new HashMap<>();
        quizData.put("quiz_name", quizName);
        quizData.put("questions", questionList);

        // Get the selected course
        String selectedCourse = courseSpinner.getSelectedItem().toString();

        // Add the quiz data to the Firestore sub-collection under the selected course
        db.collection("department").document(selectedDepartment)
                .collection("course").document(selectedCourse)
                .collection("quizzes")
                .add(quizData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(QuizCreationActivity.this, "Quiz created successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(QuizCreationActivity.this, "Failed to create quiz: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}

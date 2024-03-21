package com.mca.quiz;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class CatalogueActivity extends AppCompatActivity {

    private Spinner departmentSpinner, courseSpinner, quizSpinner;
    private FirebaseFirestore db;
    private String selectedDepartment, selectedCourse, selectedQuiz;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalogue);

        db = FirebaseFirestore.getInstance();

        departmentSpinner = findViewById(R.id.departmentSpinner);
        courseSpinner = findViewById(R.id.courseSpinner);
        quizSpinner = findViewById(R.id.quizSpinner);
        Button startQuizButton = findViewById(R.id.startQuizButton);

        populateDepartmentSpinner();

        departmentSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                selectedDepartment = parentView.getItemAtPosition(position).toString();
                populateCourseSpinner(selectedDepartment);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do nothing
            }
        });

        courseSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                selectedCourse = parentView.getItemAtPosition(position).toString();
                populateQuizSpinner(selectedDepartment, selectedCourse);
                quizSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        selectedQuiz = parent.getItemAtPosition(position).toString();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        // Handle case when nothing is selected
                    }
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do nothing
            }
        });

        startQuizButton.setOnClickListener(v -> {
            if (selectedQuiz != null) {
                // Start the selected quiz
                Intent intent = new Intent(CatalogueActivity.this, MainQuizActivity.class);
                intent.putExtra("department_id", selectedDepartment);
                intent.putExtra("course_id", selectedCourse);
                intent.putExtra("quiz_id", selectedQuiz);
                startActivity(intent);
            } else {
                Toast.makeText(CatalogueActivity.this, "Please select a quiz", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void populateDepartmentSpinner() {
        db.collection("department")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<String> departments = new ArrayList<>();
                        for (DocumentSnapshot document : task.getResult()) {
                            departments.add(document.getId());
                        }
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(CatalogueActivity.this,
                                android.R.layout.simple_spinner_item, departments);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        departmentSpinner.setAdapter(adapter);
                    } else {
                        Toast.makeText(CatalogueActivity.this, "Error retrieving departments", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void populateCourseSpinner(String department) {
        db.collection("department").document(department).collection("course")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<String> courses = new ArrayList<>();
                        for (DocumentSnapshot document : task.getResult()) {
                            courses.add(document.getId());
                        }
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(CatalogueActivity.this,
                                android.R.layout.simple_spinner_item, courses);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        courseSpinner.setAdapter(adapter);
                    } else {
                        Toast.makeText(CatalogueActivity.this, "Error retrieving courses", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void populateQuizSpinner(String department, String course) {
        db.collection("department").document(department).collection("course").document(course).collection("quizzes")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<String> quizzes = new ArrayList<>();
                        for (DocumentSnapshot document : task.getResult()) {
                            quizzes.add(document.getId());
                        }
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(CatalogueActivity.this,
                                android.R.layout.simple_spinner_item, quizzes);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        quizSpinner.setAdapter(adapter);
                    } else {
                        Toast.makeText(CatalogueActivity.this, "Error retrieving quizzes", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}

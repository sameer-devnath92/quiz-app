package com.mca.quiz;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class FacultyLoginActivity extends AppCompatActivity {

    private Spinner departmentSpinner;
    private EditText passwordEditText;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faculty_login);

        db = FirebaseFirestore.getInstance();

        departmentSpinner = findViewById(R.id.spinnerDepartment);
        passwordEditText = findViewById(R.id.editTextPassword);

        // Retrieve department names from Firestore and populate the spinner
        populateDepartmentSpinner();
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

                        ArrayAdapter<String> adapter = new ArrayAdapter<>(FacultyLoginActivity.this, android.R.layout.simple_spinner_item, departmentNames);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        departmentSpinner.setAdapter(adapter);
                    } else {
                        Toast.makeText(FacultyLoginActivity.this, "Error accessing Firestore", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void onLoginClick(View view) {
        String selectedDepartment = departmentSpinner.getSelectedItem().toString();
        String password = passwordEditText.getText().toString().trim();

        if (selectedDepartment.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please select a department and enter the password", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("department").document(selectedDepartment)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            String storedPassword = document.getString("password");
                            if (storedPassword != null && storedPassword.equals(password)) {
                                // Password correct, navigate to QuizCreationActivity
                                Intent intent = new Intent(FacultyLoginActivity.this, QuizCreationActivity.class);
                                intent.putExtra("selected_department", selectedDepartment);
                                startActivity(intent);
                                finish();
                            } else {
                                // Incorrect password
                                Toast.makeText(FacultyLoginActivity.this, "Incorrect password", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            // Department not found
                            Toast.makeText(FacultyLoginActivity.this, "Department not found", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // Error accessing Firestore
                        Toast.makeText(FacultyLoginActivity.this, "Error accessing Firestore", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}

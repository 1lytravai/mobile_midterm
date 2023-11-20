package com.example.lab_5_1;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.ColorSpace;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.Toast;

//import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;


import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.ViewHolder;

import java.sql.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    private CollectionReference studentRef;
    ArrayList<Student> studentArrayList;
    StudentAdapter studentAdapter;
    FirebaseFirestore db;
    private boolean isSearching = false;




    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = FirebaseFirestore.getInstance();
        studentArrayList = new ArrayList<Student>();
        studentRef = db.collection("user");

        setUpRecyclerView();

    }
    private void showDatePickerDialog(EditText etDob) {
        Calendar currentDate = Calendar.getInstance();

        if (!etDob.getText().toString().isEmpty()) {
            String[] dobParts = etDob.getText().toString().split("/");
            int year = Integer.parseInt(dobParts[2]);
            int month = Integer.parseInt(dobParts[1]) - 1;
            int day = Integer.parseInt(dobParts[0]);

            currentDate.set(year, month, day);
        }

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        String selectedDate = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year;
                        etDob.setText(selectedDate);
                    }
                },
                currentDate.get(Calendar.YEAR),
                currentDate.get(Calendar.MONTH),
                currentDate.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.show();
    }

    private void setUpRecyclerView() {
        Query query = studentRef.orderBy("ID", Query.Direction.ASCENDING);
        FirestoreRecyclerOptions<Student> options = new FirestoreRecyclerOptions.Builder<Student>()
                .setQuery(query, Student.class)
                .build();

        studentAdapter = new StudentAdapter(options, this);
        recyclerView = findViewById(R.id.rvStudent);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(studentAdapter);
        studentAdapter.updateOptions(options);
        studentAdapter.startListening();


        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                String studentID = studentAdapter.getItem(position).getID();
                String studentName = studentAdapter.getItem(position).getName();
                showDeleteConfirmationDialog(position, studentName, studentID);
            }
        }).attachToRecyclerView(recyclerView);

    }
        private void showDeleteConfirmationDialog ( final int position, String studentName, String studentID){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Do you want to delete " + studentName + " with ID " + studentID)
                    .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            studentAdapter.deleteItem(position);
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            studentAdapter.notifyItemChanged(position);
                        }
                    });
            builder.create().show();
        }

    private void sortNameAscending() {
        Query query = studentRef.orderBy("Name", Query.Direction.ASCENDING);
        FirestoreRecyclerOptions<Student> options = new FirestoreRecyclerOptions.Builder<Student>()
                .setQuery(query, Student.class)
                .build();

        recyclerView = findViewById(R.id.rvStudent);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        studentAdapter.updateOptions(options);
        studentAdapter.startListening();
    }

    private void sortIdDescending() {
        Query query = studentRef.orderBy("ID", Query.Direction.DESCENDING);
        FirestoreRecyclerOptions<Student> options = new FirestoreRecyclerOptions.Builder<Student>()
                .setQuery(query, Student.class)
                .build();
        recyclerView = findViewById(R.id.rvStudent);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        studentAdapter.updateOptions(options);
        studentAdapter.startListening();

    }

    @Override
    protected void onStop() {
        super.onStop();
        studentAdapter.stopListening();
    }

    @Override
    protected void onStart() {
        super.onStart();
        studentAdapter.startListening();
    }


    private void clearSearchView() {
        SearchView searchView = findViewById(R.id.iSearch);
        searchView.setQuery("", false);
        searchView.clearFocus();

        setUpRecyclerView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_option, menu);

        MenuItem item = menu.findItem(R.id.iSearch);
        SearchView searchView = (SearchView) item.getActionView();

        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isSearching = true;
            }
        });

        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                isSearching = false;
                clearSearchView();
                return false;
            }
        });

        // Tìm kiếm
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                processSearch(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                processSearch(newText);
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);

    }

    public void processSearch(String keyword) {
        Query query = FirebaseFirestore.getInstance()
                .collection("user")
                .whereGreaterThanOrEqualTo("ID", keyword)
                .whereLessThanOrEqualTo("ID", keyword + "\uf8ff")
                .orderBy("ID");

        FirestoreRecyclerOptions<Student> options =
                new FirestoreRecyclerOptions.Builder<Student>()
                        .setQuery(query, Student.class)
                        .build();

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        studentAdapter.updateOptions(options);
        studentAdapter.startListening();
    }

    private void showAddStudentDialog() {
        // Tạo dialog
        final DialogPlus dialog = DialogPlus.newDialog(this)
                .setContentHolder(new ViewHolder(R.layout.activity_update_student))
                .setExpanded(true, 1700)
                .create();

        View view = dialog.getHolderView();

        EditText etId = view.findViewById(R.id.etId);
        EditText etName = view.findViewById(R.id.etName);
        EditText etDob = view.findViewById(R.id.etDob);
        EditText etEmail = view.findViewById(R.id.etEmail);
        EditText etPhone = view.findViewById(R.id.etPhone);

        Button btnAdd = view.findViewById(R.id.btnUpdate);

        dialog.show();

        etDob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(etDob);
            }
        });

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String Id = etId.getText().toString();
                String Name = etName.getText().toString();
                String Dob = etDob.getText().toString();
                String Email = etEmail.getText().toString();
                String Phone = etPhone.getText().toString();

                if (Id.matches("\\d{8}") && !Name.isEmpty() && Dob.matches("\\d{2}/\\d{2}/\\d{4}")
                        && Email.matches("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,4}") && Phone.matches("\\d{10}")) {

                    Map<String, Object> user = new HashMap<>();
                    user.put("ID", Id);
                    user.put("Name", Name);
                    user.put("DoB", Dob);
                    user.put("Email", Email);
                    user.put("phoneNumber", Phone);

                    db.collection("user")
                            .add(user)
                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    Toast.makeText(MainActivity.this, "Add Successful", Toast.LENGTH_SHORT).show();
                                    dialog.dismiss(); // Đóng dialog sau khi thêm thành công
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(MainActivity.this, "Add Fail", Toast.LENGTH_SHORT).show();
                                }
                            });
                } else {
                    Toast.makeText(MainActivity.this, "Please Input Correct Information", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }





    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.iAdd) {
            showAddStudentDialog();
        } else if (itemId == R.id.iSortIDAscending) {
            setUpRecyclerView();
        } else if (itemId == R.id.iSortIDDescending) {
            sortIdDescending();
        }else if (itemId == R.id.iSortNameAscending) {
            sortNameAscending();
        }
        return true;
    }

}



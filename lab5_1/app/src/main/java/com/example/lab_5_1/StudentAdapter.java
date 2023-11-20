package com.example.lab_5_1;

import android.app.DatePickerDialog;
import android.content.ClipData;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.ViewHolder;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class StudentAdapter extends FirestoreRecyclerAdapter<Student, StudentAdapter.StudentViewHolder> {
    private Context context;
    private ArrayList<Student> studentArrayList;
    private FirebaseFirestore db;
    private CollectionReference studentRef;


    public StudentAdapter(@NonNull FirestoreRecyclerOptions<Student> options, Context context) {
        super(options);
        this.context = context;
        db = FirebaseFirestore.getInstance();
        studentArrayList = new ArrayList<>();
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
                context,
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





    @Override
    protected void onBindViewHolder(@NonNull StudentViewHolder holder, int position, @NonNull Student model) {
        holder.tvId.setText(model.getID());
        holder.tvName.setText(model.getName());
        holder.tvDob.setText(model.getDoB());
        holder.tvEmail.setText(model.getEmail());
        holder.tvPhone.setText(model.getPhoneNumber());

        holder.update.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                final DialogPlus dialogPlus = DialogPlus.newDialog(holder.tvId.getContext())
                        .setContentHolder(new ViewHolder(R.layout.activity_update_student))
                        .setExpanded(true, 1700)
                        .create();


                FirebaseFirestore db = FirebaseFirestore.getInstance();

                View view = dialogPlus.getHolderView();

                EditText ID = view.findViewById(R.id.etId);
                EditText Name = view.findViewById(R.id.etName);
                EditText DoB = view.findViewById(R.id.etDob);
                EditText Email = view.findViewById(R.id.etEmail);
                EditText phoneNumber = view.findViewById(R.id.etPhone);

                DoB.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showDatePickerDialog(DoB);
                    }
                });
                Button btnUpdate = view.findViewById(R.id.btnUpdate);
//
//                int unit = ContextCompat.getColor(holder.tvId.getContext(), R.color.unit);
//                btnUpdate.setBackgroundColor(unit);

                ID.setText(model.getID());
                Name.setText(model.getName());
                DoB.setText(model.getDoB());
                Email.setText(model.getEmail());
                phoneNumber.setText(model.getPhoneNumber());

                dialogPlus.show();

                btnUpdate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String newId = ID.getText().toString();
                        String newName = Name.getText().toString();
                        String newDob = DoB.getText().toString();
                        String newEmail = Email.getText().toString();
                        String newPhone = phoneNumber.getText().toString();

                        DoB.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                showDatePickerDialog(DoB);
                            }
                        });

                        // Tạo một đối tượng Student mới với thông tin đã được cập nhật
                        if (newId.matches("\\d{8}") && !newName.isEmpty() && newDob.matches("\\d{2}/\\d{2}/\\d{4}")
                                && newEmail.matches("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,4}") && newPhone.matches("\\d{10}")) {
                        Map<String, Object> updatedData = new HashMap<>();

                        updatedData.put("ID", newId);
                        updatedData.put("Name", newName);
                        updatedData.put("DoB", newDob);
                        updatedData.put("Email", newEmail);
                        updatedData.put("phoneNumber", newPhone);


                        // Cập nhật thông tin vào Firestore
                        db.collection("user").document(getSnapshots().getSnapshot(holder.getAdapterPosition()).getId())
                                .set(updatedData)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(holder.tvId.getContext(), "Update Successful", Toast.LENGTH_SHORT).show();
                                        dialogPlus.dismiss();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(holder.tvId.getContext(), "Update Fail", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }else {
                            Toast.makeText(holder.tvId.getContext(), "Please Input Correct Information", Toast.LENGTH_SHORT).show();
                        }
                }

                });


            }
        });
    }

    @NonNull
    @Override
    public StudentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.student_item, parent, false);
        return new StudentViewHolder(view);
    }

    // rest of the code...
    public void deleteItem(int position) {
        getSnapshots().getSnapshot(position).getReference().delete();
    }

    class StudentViewHolder extends RecyclerView.ViewHolder {
        TextView tvId, tvName, tvDob, tvEmail, tvPhone;
        ImageView update;
        ;
        public StudentViewHolder(@NonNull View itemView) {
            super(itemView);

            tvId = itemView.findViewById(R.id.tvId);
            tvName = itemView.findViewById(R.id.tvName);
            tvDob = itemView.findViewById(R.id.tvDob);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvPhone = itemView.findViewById(R.id.tvPhone);
            update = itemView.findViewById(R.id.ivAction);

        }
    }
}
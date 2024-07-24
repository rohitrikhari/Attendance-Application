package com.example.attendance;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;


public class StudentActivity extends AppCompatActivity {
    Toolbar toolbar;
    private String className;
    private String subjectName;
    private int position;
    private RecyclerView recyclerView;
    private StudentAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<StudentItem> studentItems = new ArrayList<>();
    private DbHelper dbHelper;
    private long cid;

    private MyCalendar calendar;
    private TextView subtitle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student);


        calendar = new MyCalendar();
        dbHelper = new DbHelper(this);
        Intent intent = getIntent();
        className = intent.getStringExtra("className");
        subjectName = intent.getStringExtra("subjectName");
        position = intent.getIntExtra("position", -1);

        cid = intent.getLongExtra("cid", -1);


        setToolbar();


        loadData();


        recyclerView = findViewById(R.id.student_recycler);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new StudentAdapter(studentItems, this);
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(position -> changeStatus(position));
        loadStatusData();
    }

    public void loadData() {
        try {
            Cursor cursor = dbHelper.getStudentTable(cid);
            studentItems.clear();
            if (cursor != null && cursor.moveToFirst()) {
                int sidIndex = cursor.getColumnIndex(DbHelper.S_ID);
                int rollIndex = cursor.getColumnIndex(DbHelper.STUDENT_ROLL_KEY);
                int nameIndex = cursor.getColumnIndex(DbHelper.STUDENT_NAME_KEY);
                do {
                    long sid = cursor.getLong(sidIndex);
                    int roll = cursor.getInt(rollIndex);
                    String name = cursor.getString(nameIndex);
                    studentItems.add(new StudentItem(sid, roll, name));
                } while (cursor.moveToNext());
                cursor.close();
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }


    private void changeStatus(int position) {
        String status = studentItems.get(position).getStatus();

        if (status.equals("P")) status = "A";
        else status = "P";
        studentItems.get(position).setStatus(status);
        adapter.notifyItemChanged(position);
    }

    private void setToolbar() {
        toolbar = findViewById(R.id.toolbar);
        TextView title = toolbar.findViewById(R.id.title_toolbar);

        subtitle = toolbar.findViewById(R.id.subtitle_toolbar);

        ImageButton save = toolbar.findViewById(R.id.save);
        ImageButton back = toolbar.findViewById(R.id.back);

        save.setOnClickListener(v -> saveStatus());

        title.setText(className);
        subtitle.setText(subjectName + " " + calendar.getDate());

        back.setOnClickListener(v -> onBackPressed());
        toolbar.inflateMenu(R.menu.student_menu);
        toolbar.setOnMenuItemClickListener(menuItem -> onMenuItemClick(menuItem));

    }

    private void saveStatus() {
        for (StudentItem studentItem : studentItems) {
            String status = studentItem.getStatus();
            //

            if (status == null || status.isEmpty()) {
                status = "A";
            }
            //

            //  if (status!="P")  status="A";
            try {
                long value = dbHelper.addStatus(studentItem.getSid(), cid, calendar.getDate(), status);

                if (value == -1) {
                    dbHelper.updateStatus(studentItem.getSid(), calendar.getDate(), status);
                }
            } catch (Exception e) {
                String aa = String.valueOf(e);
                Toast.makeText(this, " " + aa, Toast.LENGTH_SHORT).show();
            }
        }
        Toast.makeText(this, "Your data is  saved", Toast.LENGTH_SHORT).show();
    }

    private void loadStatusData() {
        for (StudentItem studentItem : studentItems) {
            String status = dbHelper.getStatus(studentItem.getSid(), calendar.getDate());
            if (status != null) studentItem.setStatus(status);
            else studentItem.setStatus("");
        }
        adapter.notifyDataSetChanged();
    }

    private boolean onMenuItemClick(MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.add_student) {
            showAddStudentDialog();
        } else if (menuItem.getItemId() == R.id.show_Calendar) {
            showCalendar();
        } else if (menuItem.getItemId() == R.id.show_attendence_sheet) {
            openSheetList();
        }
        return true;
    }

    private void openSheetList() {
        long[] idArray = new long[studentItems.size()];
        String[] nameArray = new String[studentItems.size()];
        int[] rollArray = new int[studentItems.size()];


        for (int i = 0; i < idArray.length; i++) {
            idArray[i] = studentItems.get(i).getSid();
        }

        for (int i = 0; i < rollArray.length; i++) {
            rollArray[i] = (int) studentItems.get(i).getRoll();
        }


        for (int i = 0; i < nameArray.length; i++) {
            nameArray[i] = String.valueOf(studentItems.get(i).getName());
        }


        Intent intent = new Intent(this, SheetListActivity.class);
        intent.putExtra("cid", cid);
        intent.putExtra("idArray", idArray);
        intent.putExtra("rollArray", rollArray);
        intent.putExtra("nameArray", nameArray);

        startActivity(intent);
    }

    private void showCalendar() {

        calendar.show(getSupportFragmentManager(), "");
        calendar.setOnCalendarOkClickListener(this::onCalendarOkClicked);
    }

    private void onCalendarOkClicked(int year, int month, int day) {
        calendar.setDate(year, month, day);
        subtitle.setText(subjectName + "  |" + calendar.getDate());
        loadStatusData();
    }

    private void showAddStudentDialog() {
        MyDialog dialog = new MyDialog();
        dialog.show(getSupportFragmentManager(), MyDialog.STUDENT_ADD_DIALOG);
        dialog.setListener((roll, name) -> addStudent(roll, name));
    }

    @SuppressLint("NotifyDataSetChanged")
    private void addStudent(String roll_string, String name) {
        int roll = Integer.parseInt(roll_string);
        long sid = dbHelper.addStudent(cid, roll, name);
        StudentItem studentItem = new StudentItem(sid, roll, name);
        studentItems.add(studentItem);
        adapter.notifyDataSetChanged();
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case 0:
                showUpdateStudentDialog(item.getGroupId());
                break;
            case 1:
                deleteStudent(item.getGroupId());
        }

        return super.onContextItemSelected(item);
    }

    private void showUpdateStudentDialog(int position) {
        MyDialog dialog = new MyDialog(studentItems.get(position).getRoll(), studentItems.get(position).getName());
        dialog.show(getSupportFragmentManager(), MyDialog.STUDENT_UPDATE_DIALOG);
        dialog.setListener((roll_string, name) -> updateStudent(position, name));
    }

    private void updateStudent(int position, String name) {
        dbHelper.updateStudent(studentItems.get(position).getSid(), name);
        studentItems.get(position).setName(name);
        adapter.notifyItemChanged(position);
    }

    private void deleteStudent(int groupId) {
        dbHelper.deleteStudent(studentItems.get(position).getSid());
        studentItems.remove(groupId);
        adapter.notifyItemRemoved(groupId);
    }


}
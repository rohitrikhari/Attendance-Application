package com.example.attendance;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    FloatingActionButton fab;//ths is for our add button
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    ClassAdapter classAdapter;
    ArrayList<ClassItem> classItems = new ArrayList<>();// creting arraylist to store the data of the class
    Toolbar toolbar;
    DbHelper dbHelper;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//creating an object of our db helper class
        dbHelper = new DbHelper(getApplicationContext());// creating Object  of dbhelper class

        ///accessing ui elements from xml
        fab = findViewById(R.id.fab_main);
        fab.setOnClickListener(view -> showDialog());// setting up onclicklistener

        //below function is necessary for loading the data of classes and subjects when we  open the app
        loadData();



        recyclerView = findViewById(R.id.recylerview);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        classAdapter = new ClassAdapter(classItems, this);//creating object of class Classadapter
        recyclerView.setAdapter(classAdapter);

        classAdapter.setOnItemClickListener(position -> gotoItemActitvity(position));

        setToolbar();//calling settoolbar function

    }

    // defining load data function
    private void loadData() {

        try {
            Cursor cursor = dbHelper.getClassTable();
            classItems.clear();
            if (cursor != null && cursor.moveToFirst()) {
                do {

                    int idIndex = cursor.getColumnIndex(DbHelper.C_ID);
                    int classNameIndex = cursor.getColumnIndex(DbHelper.CLASS_NAME_KEY);
                    int subjectNameIndex = cursor.getColumnIndex(DbHelper.SUBJECT_NAME_KEY);

                    int id = cursor.getInt(idIndex);
                    String className = cursor.getString(classNameIndex);
                    String subjectName = cursor.getString(subjectNameIndex);

                    classItems.add(new ClassItem(id, className, subjectName));

                } while (cursor.moveToNext());
                cursor.close();
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    // defining settoolbar function
    private void setToolbar() {
        toolbar = findViewById(R.id.toolbar);
        TextView title = toolbar.findViewById(R.id.title_toolbar);
        TextView subtitle = toolbar.findViewById(R.id.subtitle_toolbar);
        ImageButton save = toolbar.findViewById(R.id.save);
        ImageButton back = toolbar.findViewById(R.id.back);

        title.setText("Attendance App");
        subtitle.setVisibility(View.GONE);
        back.setVisibility(View.INVISIBLE);
        save.setVisibility(View.INVISIBLE);




    }

//    // defining goto item activity function
    private void gotoItemActitvity(int position) {
        Intent intent = new Intent(this, StudentActivity.class);
        intent.putExtra("className", classItems.get(position).getClassname());
        intent.putExtra("subjectName", classItems.get(position).getSubjectname());
        intent.putExtra("position", position);
        intent.putExtra("cid", classItems.get(position).getCid());
        startActivity(intent);
    }

    // defining showdialog functio
    private void showDialog() {
        try {
            MyDialog dialog = new MyDialog();//creating object of mydialog class
            dialog.show(getSupportFragmentManager(), MyDialog.CLASS_ADD_DIALOG);
            dialog.setListener((className, SubjectName) -> addClass(className, SubjectName));
        } catch (Exception e) {
            String ab = String.valueOf(e);
            Toast.makeText(this, "aaaaaaaa " + ab, Toast.LENGTH_SHORT).show();
        }
    }


    @SuppressLint("NotifyDataSetChanged")
// this function is called when we try to add a new class
    private void addClass(String className, String subjectName) {


        long cid = dbHelper.addClass(className, subjectName);
        ClassItem classItem = new ClassItem(cid, className, subjectName);
        classItems.add(classItem);
        classAdapter.notifyDataSetChanged();
    }

    //on longpreesing the class this function will be called
    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case 0:
                showUpdateDialog(item.getGroupId());// if the case is 0 means the user click on update them this function will be called
                break;
            case 1:
                deleteClass(item.getGroupId());// if the case is 1 means the user click on delete then this function will be called
        }
        return super.onContextItemSelected(item);
    }

    //if te user click on update a dilog box will open
    private void showUpdateDialog(int position) {
        MyDialog dialog = new MyDialog();
        dialog.show(getSupportFragmentManager(), MyDialog.CLASS_UPDATE_DIALOG);
        dialog.setListener((className, subjectName) -> updateClass(position, className, subjectName));
    }

    private void updateClass(int position, String className, String subjectName) {
        dbHelper.updateClass(classItems.get(position).getCid(), className, subjectName);
        classItems.get(position).setClassname(className);
        classItems.get(position).setSubjectname(subjectName);
        classAdapter.notifyItemChanged(position);
    }
    //if the user click on delete than this function will be called
    private void deleteClass(int position) {
        dbHelper.deleteClass(classItems.get(position).getCid());
        classItems.remove(position);
        classAdapter.notifyItemRemoved(position);
    }
}

package com.kls.okane_memo;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.kls.okane_memo.db.Injection;
import com.kls.okane_memo.db.Record;
import com.kls.okane_memo.record.RecordLinearAdapter;
import com.kls.okane_memo.util.MyDatePickerDialog;
import com.kls.okane_memo.util.RecordViewModel;
import com.kls.okane_memo.util.ViewModelFactory;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity implements
        View.OnClickListener,
        DatePickerDialog.OnDateSetListener{

    private ImageView pieBtn;
    private FloatingActionButton recordBtn;
    private TextView dateTextView;
    private TextView inTv, outTv;
    private RecyclerView recordRv;
    private int year, month, dayOfMonth;
    private RecordLinearAdapter adapter;
    private List<Record> recordList;
    private RecordViewModel recordViewModel;
    private ViewModelFactory viewModelFactory;
    private final CompositeDisposable disposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initShow();

    }

    void initShow(){
        // 设置跳转到饼图的Activity
        pieBtn = findViewById(R.id.btn_pie_chart);
        pieBtn.setOnClickListener(this);


        // 设置日期显示选择
        dateTextView = findViewById(R.id.datePicker);
        dateTextView.setOnClickListener(this);
        Calendar calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH) + 1;
        dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        String todayDate = String.format("%d年%d月", year, month);
        dateTextView.setText(todayDate);

        // 设置总金额数字的字体
        inTv = findViewById(R.id.income_money);
        outTv = findViewById(R.id.outcome_money);
        Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/bahnschrift.ttf");
        inTv.setTypeface(tf);
        outTv.setTypeface(tf);

        // 设置显示的记录
        recordList = new ArrayList<>();
        adapter = new RecordLinearAdapter(this, year, month, dayOfMonth, recordList);
        recordRv = findViewById(R.id.record_lv);
        recordRv.setLayoutManager(new LinearLayoutManager(this));
        recordRv.setAdapter(adapter);

        viewModelFactory = Injection.provideViewModelFactory(this);
        recordViewModel = new ViewModelProvider(this, viewModelFactory).get(RecordViewModel.class);

        recordViewModel.getRecordByMonth(year, month)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<Record>>() {
                    @Override
                    public void accept(List<Record> records) throws Exception {
                        Log.d("MainActivity", "监测数据");
                        recordList = records;
                        updateTotalShow();
                        adapter.setRecords(recordList);
                        adapter.notifyDataSetChanged();
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {

                    }
                });

        // 设置记录按钮
        recordBtn = findViewById(R.id.btn_record);
        recordBtn.setOnClickListener(this);
    }

    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
        this.year = year;
        this.month = month + 1;
        this.dayOfMonth = dayOfMonth;
        String date = String.format("%d年%d月", year, this.month);
        dateTextView.setText(date);
        Log.d("日期",date);

        recordViewModel.getRecordByMonth(year, this.month)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<Record>>() {
                    @Override
                    public void accept(List<Record> records) throws Exception {
                        Log.d("MainActivity", "监测数据");
                        recordList = records;
                        updateTotalShow();
                        adapter.setRecords(recordList);
                        adapter.notifyDataSetChanged();
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {

                    }
                });
    }

    @Override
    public void onClick(View view){
        Intent intent = null;
        switch (view.getId()) {
            case R.id.btn_pie_chart:
                intent = new Intent(MainActivity.this, ChartActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
                break;
            case R.id.btn_record:
                intent = new Intent(MainActivity.this, BookkeepingActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
                break;
            case R.id.datePicker:
                Calendar calendar = Calendar.getInstance();
                MyDatePickerDialog dialog = new MyDatePickerDialog(MainActivity.this, 0, this::onDateSet,
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH));
//                dialog.getDatePicker().setMaxDate(calendar.getTimeInMillis());
                Log.d("Adapter里面的条目数", String.valueOf(adapter.getItemCount()));
                dialog.show();
                break;
        }
    }

    void updateTotalShow(){
        double outTotal = 0, inTotal = 0;

        for(Record record : recordList){
            if(record.getKind() == 1)
                inTotal += record.getMoney();
            else
                outTotal += record.getMoney();
        }

        outTv.setText(String.valueOf(outTotal));
        inTv.setText(String.valueOf(inTotal));
    }



}
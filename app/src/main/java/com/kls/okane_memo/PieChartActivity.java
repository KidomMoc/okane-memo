package com.kls.okane_memo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;

import com.kls.okane_memo.db.Injection;
import com.kls.okane_memo.db.Record;
import com.kls.okane_memo.util.MyDatePickerDialog;
import com.kls.okane_memo.util.RecordViewModel;
import com.kls.okane_memo.util.ViewModelFactory;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import lecho.lib.hellocharts.model.PieChartData;
import lecho.lib.hellocharts.model.SliceValue;
import lecho.lib.hellocharts.view.PieChartView;

public class PieChartActivity extends AppCompatActivity implements
        View.OnClickListener,
        DatePickerDialog.OnDateSetListener{

    private ImageView backBtn;
    private int year, month, kind;
    private TextView dateTextView;
    private PieChartView pieChart;

    private List<Record> recordList;    // 存放记录数据
    private RecordViewModel recordViewModel;
    private ViewModelFactory viewModelFactory;
    private final CompositeDisposable disposable = new CompositeDisposable();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pie_chart);

        init();
//        initPieChart();
    }

    private void init(){
        // 设置返回按钮
        backBtn = findViewById(R.id.pie_chart_iv_back);
        backBtn.setOnClickListener(this);

        // 设置日期显示选择
        dateTextView = findViewById(R.id.pie_chart_datePicker);
        dateTextView.setOnClickListener(this);
        Calendar calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH) + 1;
//        dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        String todayDate = String.format("%d年%d月", year, month);
        dateTextView.setText(todayDate);

        // 设置饼图
        kind = -1;
        pieChart=(PieChartView)findViewById(R.id.pie_chart);
        // 饼图是否可旋转
        pieChart.setChartRotationEnabled(true);
        pieChart.setCircleFillRatio((float) 0.5);//设置饼图其中的比例

        recordList = new ArrayList<>();
        viewModelFactory = Injection.provideViewModelFactory(this);
        recordViewModel = new ViewModelProvider(this, viewModelFactory).get(RecordViewModel.class);
        recordViewModel.getRecordByMonth(year, month)    // 获取某月数据
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<Record>>() {
                    @Override
                    public void accept(List<Record> records) throws Exception {
                        recordList = records;
                        setData(-1);

                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {

                    }
                });
        setData(-1);


    }

    private void setData(int kind){
        HashMap<String, Double> mp;
        mp = new HashMap<>();
        Double sum = 0.0;
        // 格式化数字，显示为保留一位小数
        NumberFormat numberFormat = NumberFormat.getInstance();
        numberFormat.setMaximumFractionDigits(1);
        // 计算每种类型的总金额
        for(Record record : recordList)
        {
            String type = record.getTypename();
            Double money = record.getMoney();

            if(record.getKind() == kind)
            {
                sum += money;
                if(mp.get(type) != null) {
                    Double newMoney = mp.get(type) + money;
                    mp.put(type, newMoney);
                }
                else
                    mp.put(type, money);
            }
        }

        List<SliceValue> values = new ArrayList<SliceValue>();
//        SliceValue sliceValue = null;

        // 饼图颜色
        List<Integer> colorList = new ArrayList<>();
//        colorList.add(R.color.pie1);
//        colorList.add(R.color.pie2);
//        colorList.add(R.color.pie3);
//        colorList.add(R.color.pie4);
        colorList.add(Color.rgb(130, 130, 130));
        colorList.add(Color.rgb(202, 225, 255));
        colorList.add(Color.rgb(156, 156, 156));
        colorList.add(Color.rgb(178, 223, 238));

        int cnt = 0;

        // 使用entrySet的迭代器遍历哈希表
        Iterator iter = mp.entrySet().iterator();
        while(iter.hasNext()){

            Map.Entry<String,Double> entry = (Map.Entry<String,Double>)iter.next();
            String type = entry.getKey();
            String info = type + " " + numberFormat.format(entry.getValue() / sum * 100) + "%";
            float value = entry.getValue().floatValue();
            int color = colorList.get(cnt % colorList.size());
            cnt++;
            if(cnt == mp.size() && (cnt - 1) % colorList.size() == 0)
                color = colorList.get(cnt % colorList.size());  // 避免相同颜色相邻

            SliceValue sliceValue = new SliceValue(value, color);
            // 设置每个扇形区域的Lable，不设置的话，默认显示数值
            sliceValue.setLabel(info);
            values.add(sliceValue);
        }

        PieChartData pieChartData=new PieChartData(values);

        /*****************************饼中文字设置************************************/
        //是否显示文本内容(默认为false)
        pieChartData.setHasLabels(true);
        //是否点击饼模块才显示文本（默认为false,为true时，setHasLabels(true)无效）
//		pieChartData.setHasLabelsOnlyForSelected(true);
        //文本内容是否显示在饼图外侧(默认为false)
        pieChartData.setHasLabelsOutside(false);
        //文本字体大小
        pieChartData.setValueLabelTextSize(12);
        //文本文字颜色
        pieChartData.setValueLabelsTextColor(Color.WHITE);
        //设置文本背景颜色
        pieChartData.setValueLabelBackgroundColor(Color.RED);
        //设置文本背景颜色时，必须设置自动背景为false
        pieChartData.setValueLabelBackgroundAuto(false);
        //设置是否有文字背景
        pieChartData.setValueLabelBackgroundEnabled(false);

        /*****************************中心圆设置************************************/
        //饼图是空心圆环还是实心饼状（默认false,饼状）
        pieChartData.setHasCenterCircle(true);
        //中心圆的颜色（需setHasCenterCircle(true)，因为只有圆环才能看到中心圆）
        pieChartData.setCenterCircleColor(Color.WHITE);
        //中心圆所占饼图比例（0-1）
        pieChartData.setCenterCircleScale(0.6f);
		/*=====================中心圆文本（可以只设置一个文本）==========/
		/*--------------------第1个文本----------------------*/
        //中心圆中文本
        String kindInfo = kind == 1 ? "总收入" : "总支出";
        pieChartData.setCenterText1(kindInfo);
        //中心圆的文本颜色
        pieChartData.setCenterText1Color(Color.GRAY);
        //中心圆的文本大小
        pieChartData.setCenterText1FontSize(16);
        /*--------------------第2个文本----------------------*/
        //中心圆中文本
        pieChartData.setCenterText2(sum.toString());
        //中心圆的文本颜色
        pieChartData.setCenterText2Color(Color.GRAY);
        //中心圆的文本大小
        pieChartData.setCenterText2FontSize(16);

        //饼图各模块的间隔(默认为0)
        pieChartData.setSlicesSpacing(5);

        // 标签是否在饼图外面
        pieChartData.setHasLabelsOutside(true);
        pieChartData.setValueLabelsTextColor(Color.GRAY);

        pieChart.setPieChartData(pieChartData);
    }

    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
        this.year = year;
        this.month = month + 1;
//        this.dayOfMonth = dayOfMonth;
        String date = String.format("%d年%d月", year, this.month);
        dateTextView.setText(date);
        Log.d("图表日期",date);

        recordViewModel.getRecordByMonth(year, this.month)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<Record>>() {
                    @Override
                    public void accept(List<Record> records) throws Exception {
                        Log.d("PieChartActivity", "监测数据");
                        recordList = records;
                        setData(-1);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {

                    }
                });
    }

    @Override
    public void onClick(View view) {
        Intent intent = null;
        switch (view.getId()) {
            case R.id.pie_chart_iv_back:
                intent = new Intent(PieChartActivity.this, MainActivity.class);
                startActivity(intent);
//                overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
                break;
            case R.id.pie_chart_datePicker:
                Calendar calendar = Calendar.getInstance();
                MyDatePickerDialog dialog = new MyDatePickerDialog(PieChartActivity.this, 0, this::onDateSet,
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH));
//                dialog.getDatePicker().setMaxDate(calendar.getTimeInMillis());
//                Log.d("Adapter里面的条目数", String.valueOf(adapter.getItemCount()));
                dialog.show();
                break;
        }
    }
}
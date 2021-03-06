package com.kls.okane_memo.record;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.recyclerview.widget.RecyclerView;

import com.kls.okane_memo.R;
import com.kls.okane_memo.SingleRecordActivity;
import com.kls.okane_memo.db.Injection;
import com.kls.okane_memo.db.Record;
import com.kls.okane_memo.util.PopupList;
import com.kls.okane_memo.util.RecordViewModel;
import com.kls.okane_memo.util.ViewModelFactory;
import com.kls.okane_memo.util.type.TypeList;

import java.util.ArrayList;
import java.util.List;

public class RecordLinearAdapter extends RecyclerView.Adapter<RecordLinearAdapter.LinearViewHolder> {
    private Context context;
    int year, month, dayOfMonth;
    private List<Record> records;
    private List<String> popupMenuItemList = new ArrayList<>();

    public RecordLinearAdapter(Context context, int year, int month, int dayOfMonth, List<Record> records){
        this.context = context;
        this.year = year;
        this.month = month;
        this.dayOfMonth = dayOfMonth;
        // 找到当天的记录列表
        this.records = records;
        popupMenuItemList.add(context.getString(R.string.delete));
    }

    @NonNull
    @Override
    public LinearViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new LinearViewHolder(LayoutInflater.from(context).inflate(R.layout.list_item_record, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull LinearViewHolder holder, int position) {
        Record record = records.get(records.size() - 1 - position);
        holder.moneyTv.setText(String.valueOf(record.getMoney()));
        String remark = record.getRemark();
        if(remark == null || remark.length() == 0)
            holder.typeTv.setText(record.getTypename());
        else
            holder.typeTv.setText(remark);
        int imageId = TypeList.getInstance().getImageByName(record.getTypename());
        if(imageId == 0){
            // 抛出异常
            Log.d("RecordLinearAdapter", "找不到图片 " + record.getTypename());
        }
        holder.typeIv.setImageResource(imageId);

        // 设置点击事件
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, SingleRecordActivity.class);
                Bundle bundle = new Bundle();
                bundle.putBoolean("ifCreate", false);
                bundle.putInt("id", record.getId());
                bundle.putDouble("money", record.getMoney());
                bundle.putInt("kind", record.getKind());
                bundle.putInt("year", record.getYear());
                bundle.putInt("month", record.getMonth());
                bundle.putInt("dayOfMonth", record.getDayOfMonth());
                bundle.putString("typename", record.getTypename());
                bundle.putString("remarkInfo", record.getRemark());
                bundle.putInt("imageId", imageId);
                bundle.putBoolean("ifCreate", false);
                intent.putExtras(bundle);
                context.startActivity(intent);
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                int[] location = new int[2];
                view.getLocationOnScreen(location);
                PopupList popupList = new PopupList(view.getContext());
                popupList.showPopupListWindow(view, holder.getAdapterPosition(), location[0] + view.getWidth() / 2,
                        location[1], popupMenuItemList, new PopupList.PopupListListener() {
                            @Override
                            public boolean showPopupList(View adapterView, View contextView, int contextPosition) {
                                return true;
                            }

                            @Override
                            public void onPopupListClick(View contextView, int contextPosition, int position) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                builder.setMessage("确定删除该记录？")
                                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                RecordViewModel recordViewModel;
                                                ViewModelFactory viewModelFactory;
                                                viewModelFactory = Injection.provideViewModelFactory(context);
                                                recordViewModel = new ViewModelProvider((ViewModelStoreOwner) context, viewModelFactory).get(RecordViewModel.class);
                                                recordViewModel.deleteRecord(record);
                                            }
                                        })
                                        .setNeutralButton("取消", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {

                                            }
                                        }).show();
                            }
                        });
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return records.size();
    }

    class LinearViewHolder extends RecyclerView.ViewHolder{
        private TextView moneyTv, typeTv;
        private ImageView typeIv;
        public LinearViewHolder(@NonNull View itemView) {
            super(itemView);
            moneyTv = itemView.findViewById(R.id.item_record_money);
            Typeface tf = Typeface.createFromAsset(context.getAssets(), "fonts/bahnschrift.ttf");
            moneyTv.setTypeface(tf);
            typeIv = itemView.findViewById(R.id.item_record_iv);
            typeTv = itemView.findViewById(R.id.item_record_typename);
        }
    }

    public void setRecords(List<Record> records){
        this.records = records;
    }

}

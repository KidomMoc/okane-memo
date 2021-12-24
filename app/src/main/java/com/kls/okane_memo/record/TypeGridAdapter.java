package com.kls.okane_memo.record;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.kls.okane_memo.R;
import com.kls.okane_memo.SingleRecordActivity;
import com.kls.okane_memo.util.type.TypeBean;
import com.kls.okane_memo.util.type.TypeList;

public class TypeGridAdapter extends RecyclerView.Adapter<TypeGridAdapter.GridViewHolder> {
    private Context mContext;
    private TypeList typeList;
    private int kind;

    public TypeGridAdapter(Context context, int kind){
        this.mContext = context;
        this.kind = kind;
        typeList = TypeList.getInstance();
    }

    @Override
    public TypeGridAdapter.GridViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new GridViewHolder(LayoutInflater.from(mContext).inflate(R.layout.grid_item_type,parent,false));
    }

    @Override
    public void onBindViewHolder(TypeGridAdapter.GridViewHolder holder, final int position) {
        // 根据Item的位置获得类型种类
        TypeBean type;
        if(kind == 1){
            type = typeList.getInTypeBean(position);
        }else{
            type = typeList.getOutTypeBean(position);
        }
        holder.textView.setText(type.getTypename());
        holder.imageView.setImageResource(type.getImageId());

        // 设置每种类型的点击事件
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, SingleRecordActivity.class);
                Bundle bundle = new Bundle();
                bundle.putBoolean("ifCreate", true);
                bundle.putInt("kind", type.getKind());
                bundle.putString("typename", type.getTypename());
                bundle.putInt("imageId", type.getImageId());
                intent.putExtras(bundle);
                mContext.startActivity(intent);
//                overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
            }
        });
    }

    @Override
    public int getItemCount() {
        if(kind == 1){
            return typeList.getSizeOfInTypes();
        }else{
            return typeList.getSizeOfOutTypes();
        }
    }

    class GridViewHolder extends RecyclerView.ViewHolder {

        private TextView textView;
        private ImageView imageView;

        public GridViewHolder(View view) {
            super(view);
            textView = view.findViewById(R.id.item_type_tv);
            imageView = view.findViewById(R.id.item_type_iv);
        }
    }

}

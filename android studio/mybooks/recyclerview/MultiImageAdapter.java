package com.example.mybooks.recyclerview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mybooks.R;

import java.util.ArrayList;

public class MultiImageAdapter extends RecyclerView.Adapter<MultiImageAdapter.ViewHolder>{
    private final String TAG=this.getClass().getSimpleName();
    private ArrayList<Uri> mData = null ;
    private Context mContext = null ;


    // 생성자에서 데이터 리스트 객체, Context를 전달받음.
    public MultiImageAdapter(ArrayList<Uri> list, Context context) {
        mData = list ;
        mContext = context;
    }

    // 아이템 뷰를 저장하는 뷰홀더 클래스.
    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView image;

        ViewHolder(View itemView) {
            super(itemView) ;

            image = itemView.findViewById(R.id.image);
        }
    }


    // onCreateViewHolder() - 아이템 뷰를 위한 뷰홀더 객체 생성하여 리턴.
    // LayoutInflater - XML에 정의된 Resource(자원) 들을 View의 형태로 반환.
    @Override
    public MultiImageAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext() ;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) ;    // context에서 LayoutInflater 객체를 얻는다.
        View view = inflater.inflate(R.layout.rv_multi_image_item, parent, false) ;	// 리사이클러뷰에 들어갈 아이템뷰의 레이아웃을 inflate.
        MultiImageAdapter.ViewHolder vh = new MultiImageAdapter.ViewHolder(view) ;

        return vh ;
    }

    // onBindViewHolder() - position에 해당하는 데이터를 뷰홀더의 아이템뷰에 표시.
    @Override
    public void onBindViewHolder(MultiImageAdapter.ViewHolder holder, int position) { Log.e(TAG, "mData.get(position):"+mData.get(position));

        Glide.with(mContext)
                .load(mData.get(position)) // 진짜경로 string
                .into(holder.image);

    }



    // getItemCount() - 전체 데이터 갯수 리턴.
    @Override
    public int getItemCount() {
        return mData.size() ;
    }




}
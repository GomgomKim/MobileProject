package org.androidtown.sharepic;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder>{
    Context context;

    List<Photo> data;   // 앨범 내 사진 목록

    public ImageAdapter(Context context, List<Photo> data){
        this.context = context;
        this.data = data;
    }

    @NonNull
    @Override
    public ImageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_layout, parent, false);
        ImageAdapter.ViewHolder viewHolder = new ImageAdapter.ViewHolder(view);
        return viewHolder;

    }

    @Override
    public void onBindViewHolder(@NonNull ImageAdapter.ViewHolder holder, int position) {
        System.out.println(data.get(position));
        holder.r_image.setImageURI(data.get(position).getThumbnailUri());   // 앨범 내 사진

        holder.index = position;

    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        Square r_image;

        int index;

        public ViewHolder(View itemView) {
            super(itemView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 한 장
                    Intent intent = new Intent(context, PhotoActivity.class);   // 사진 크게 보여주기
                    intent.putExtra("Uri", data.get(index).getImageUri().toString());
                    context.startActivity(intent);
                }
            });

            r_image = itemView.findViewById(R.id.photo);
        }

    }
}

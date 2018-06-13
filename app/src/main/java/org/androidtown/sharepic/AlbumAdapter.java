package org.androidtown.sharepic;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.ViewHolder> {

    Context context;

    List<Album> data;   // 앨범 목록

    public AlbumAdapter(Context context, List<Album> data){
        this.context = context;
        this.data = data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.album_layout, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        System.out.println(data.get(position).getData());
        holder.r_image.setImageURI(data.get(position).getData().getThumbnailUri()); // 앨범 대표 image
        holder.r_title.setText(data.get(position).getTitle());  // 앨범 제목

        holder.index = position;

    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        Square r_image;
        TextView r_title;

        int index;

        public ViewHolder(View itemView) {
            super(itemView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 앨범 내 사진 목록
                    Intent intent = new Intent(context, ImageActivity.class);
                    intent.putExtra("album_name", data.get(index).getTitle());
                    context.startActivity(intent);

                }
            });

            r_image = itemView.findViewById(R.id.picture);
            r_title = itemView.findViewById(R.id.title);
        }

    }
}

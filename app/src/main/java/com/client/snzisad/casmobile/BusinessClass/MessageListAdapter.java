package com.client.snzisad.casmobile.BusinessClass;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.client.snzisad.casmobile.ProductDetailsActivity;
import com.client.snzisad.casmobile.R;

import java.util.HashMap;
import java.util.List;
import java.util.zip.Inflater;

public class MessageListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private Context context;
    private List<HashMap<String, String>> messages;
    private boolean mychat = false;


    public MessageListAdapter(Context context, List<HashMap<String, String>> messages){
        this.context = context;
        this.messages = messages;
    }

    @Override
    public int getItemViewType(int position) {
        final HashMap<String, String> chat = messages.get(position);

        if(Integer.parseInt(chat.get("userid")) == DataHandler.userID){
            mychat = true;
            return 1;
        }
        else{
            mychat = false;
            return 0;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        if(mychat){
            View v = LayoutInflater.from(context).inflate(R.layout.layout_my_message, viewGroup,false);
            return  new MyMessagesViewHolder(v);
        }
        else{
            View v = LayoutInflater.from(context).inflate(R.layout.layout_message, viewGroup,false);
            return  new OthersMessagesViewHolder(v);
        }
    }


    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        final HashMap<String, String> chat = messages.get(i);

        if(mychat){
            MyMessagesViewHolder holder = (MyMessagesViewHolder) viewHolder;
            holder.tvContent.setText(chat.get("chat_msg"));
        }
        else{
            OthersMessagesViewHolder holder = (OthersMessagesViewHolder) viewHolder;
            holder.tvPersonName.setText(chat.get("name"));
            holder.tvContent.setText(chat.get("chat_msg"));
        }

    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    class OthersMessagesViewHolder extends RecyclerView.ViewHolder{

        TextView tvPersonName, tvContent;

        OthersMessagesViewHolder(@NonNull View itemView) {
            super(itemView);

            tvPersonName = itemView.findViewById(R.id.tvPersonName);
            tvContent = itemView.findViewById(R.id.tvContent);
        }
    }

    class MyMessagesViewHolder extends RecyclerView.ViewHolder{

        TextView tvContent;

        MyMessagesViewHolder(@NonNull View itemView) {
            super(itemView);

            tvContent = itemView.findViewById(R.id.tvContent);
        }
    }
}

package com.kyjsoft.ex88firebasechatting;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import org.w3c.dom.Text;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.VH> {

    Context context;
    ArrayList<MessageItem> items;

    public ChatAdapter(Context context, ArrayList<MessageItem> items) {
        this.context = context;
        this.items = items;
    }

    final int TYPE_MY = 0;
    final int TYPE_OTHER = 1;

    // viewtype 항목 값 설정하는 콜백 메소드
    @Override
    public int getItemViewType(int position) { // 아이템의 개수만큼 호출
        if(items.get(position).name.equals(G.name)) return TYPE_MY;
        else return TYPE_OTHER;
         // 얘를 리턴하면 onCreateViewHolder의 두번째 파라미터인 viewType으로 전달되어 해당하는 뷰모양을 다르게 만들 수 있다.
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) { // parent -> 리사이클러뷰, viewType -> 내 마음대로 정하는 번호에 따라 뷰가달라지도록.
        LayoutInflater layoutInflater = LayoutInflater.from(context);

        // item에 따라 항목뷰의 모양이 다르게 만들기
        View itemview = null;

        if(viewType == TYPE_MY) itemview = layoutInflater.inflate(R.layout.my_msgbox, parent,false);
        else itemview = layoutInflater.inflate(R.layout.my_otherbox, parent, false);

        return new VH(itemview);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
       MessageItem item = items.get(position);

        Glide.with(context).load(item.url).into(holder.civ);
        holder.tvName.setText(item.name);
        holder.tvMsg.setText(item.message);
        holder.tvTime.setText(item.time);

    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class VH extends RecyclerView.ViewHolder{

        CircleImageView civ;
        TextView tvName;
        TextView tvMsg;
        TextView tvTime;

        public VH(@NonNull View itemView) {
            super(itemView);

            civ = itemView.findViewById(R.id.civ);
            tvName = itemView.findViewById(R.id.tv_name);
            tvMsg = itemView.findViewById(R.id.tv_msg);
            tvTime = itemView.findViewById(R.id.tv_time);

        }
    }
}

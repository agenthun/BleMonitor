package com.agenthun.blemonitor.view;

import android.content.Context;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.agenthun.blemonitor.App;
import com.agenthun.blemonitor.R;
import com.agenthun.blemonitor.bean.base.Detail;
import com.agenthun.blemonitor.bean.base.HistoryData;

import java.util.List;

/**
 * @project ESeal
 * @authors agenthun
 * @date 16/3/8 上午12:26.
 */
public class BottomSheetDialogView {
    private static List<HistoryData> details;

    public BottomSheetDialogView(Context context, List<HistoryData> details) {
        BottomSheetDialogView.details = details;

        BottomSheetDialog dialog = new BottomSheetDialog(context);
        View view = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_dialog_recycler_view, null);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.bottom_sheet_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(new SimpleAdapter());

        dialog.setContentView(view);
        dialog.show();
    }

    public static void show(Context context, List<HistoryData> details) {
        new BottomSheetDialogView(context, details);
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView securityLevelImageView;
        private AppCompatTextView timeTextView;
        private AppCompatTextView actionTypeTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            securityLevelImageView = (ImageView) itemView.findViewById(R.id.securityLevel);
            timeTextView = (AppCompatTextView) itemView.findViewById(R.id.createDatetime);
            actionTypeTextView = (AppCompatTextView) itemView.findViewById(R.id.actionType);
        }
    }

    private static class SimpleAdapter extends RecyclerView.Adapter<ViewHolder> {
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View view = inflater.inflate(R.layout.list_item_history, null);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Integer actionType = details.get(position).getActionType();
            switch (actionType) {
                case 0:
                    holder.securityLevelImageView.setImageResource(R.drawable.ic_lock_open_black_24dp);
                    holder.securityLevelImageView.setColorFilter(
                            App.getContext().getResources().getColor(R.color.colorAccentDark));
                    break;
                case 1:
                    holder.securityLevelImageView.setImageResource(R.drawable.ic_warning_black_24dp);
                    holder.securityLevelImageView.setColorFilter(
                            App.getContext().getResources().getColor(R.color.red_500));
                    break;
                case 2:
                    holder.securityLevelImageView.setImageResource(R.drawable.ic_warning_black_24dp);
                    holder.securityLevelImageView.setColorFilter(
                            App.getContext().getResources().getColor(R.color.light_blue_700));
                    break;
                case 3:
                    holder.securityLevelImageView.setImageResource(R.drawable.ic_warning_black_24dp);
                    holder.securityLevelImageView.setColorFilter(
                            App.getContext().getResources().getColor(R.color.indigo_400));
                    break;
                case 4:
                    //发送信息
                    holder.securityLevelImageView.setImageResource(R.drawable.ic_send_black_24dp);
                    holder.securityLevelImageView.setColorFilter(
                            App.getContext().getResources().getColor(R.color.colorAccent));
                    break;
                case 5:
                    //接收信息
                    holder.securityLevelImageView.setImageResource(R.drawable.ic_message_black_24dp);
                    holder.securityLevelImageView.setColorFilter(
                            App.getContext().getResources().getColor(R.color.dark_gray));
                    break;
            }
            holder.timeTextView.setText(details.get(position).getCreateDatetime());
            if (actionType < 4) {
                holder.actionTypeTextView.setText(getActionType(actionType));
            } else {
                holder.actionTypeTextView.setText(details.get(position).getContent());
            }
        }

        @Override
        public int getItemCount() {
            return details.size();
        }

        //获取相应的ActionType
        private String getActionType(Integer actionType) {
            switch (actionType) {
                case 0:
                    return App.getContext().getString(R.string.action_type_0);
                case 1:
                    return App.getContext().getString(R.string.action_type_1);
                case 2:
                    return App.getContext().getString(R.string.action_type_2);
                case 3:
                    return App.getContext().getString(R.string.action_type_3);
            }
            return "";
        }
    }
}

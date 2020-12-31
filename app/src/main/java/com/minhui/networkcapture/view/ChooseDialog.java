package com.minhui.networkcapture.view;


import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.minhui.networkcapture.MyApplication;
import com.minhui.networkcapture.R;


public class ChooseDialog extends Dialog {

    Context context;
    private ListView mListView;
    private TextView cancel;
    private String choose[];
    private OnItemClickListener itemClickListener;
    private View.OnClickListener cancelListener;
    private String title;
    private TextView etTitle;
    private int textColor = MyApplication.getContext().getResources().getColor(R.color.st_bg_common_master_orange);
    private int style = 0;

    private ChooseDialog(Context context) {
        super(context, R.style.LocationDialogStyle);

        this.context = context;
        LayoutParams params = getWindow().getAttributes();

        params.gravity = Gravity.BOTTOM;

        getWindow().setAttributes(params);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int color = context.getResources().getColor( R.color.st_bg_div_common_bottom );
        setContentView(R.layout.dialog_choose);
        findViewById(R.id.divide).setBackgroundColor(color);
        Drawable bg = context.getResources().getDrawable(R.drawable.btn_popup_black_dialog);
        findViewById(R.id.container).setBackground(bg);
        mListView = (ListView) findViewById(R.id.listView);
        etTitle = (TextView) findViewById(R.id.title);
        mListView.setDivider(new ColorDrawable(color));
        mListView.setDividerHeight(1);
        cancel = (TextView) findViewById(R.id.cancel);

        cancel.setBackgroundResource( R.drawable.view_init_no_bg_black_color_selector);
    }


    public void setAdapter(BaseAdapter adapter) {
        mListView.setAdapter(adapter);
    }

    @Override
    public void show() {
        super.show();
        ChooseAdapter adapter = new ChooseAdapter();
        mListView.setAdapter(adapter);
        mListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                dismiss();
                itemClickListener.onItemClick(parent, view, position, id);
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if (cancelListener != null) {
                    cancelListener.onClick(v);
                }
            }
        });
        if (title != null) {
            etTitle.setText(title);
        } else {
            etTitle.setVisibility(View.GONE);
        }
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        itemClickListener = listener;
    }


    private class ChooseAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            if (choose == null) {
                return 0;
            }
            return choose.length;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = View.inflate(parent.getContext(), R.layout.tv_bottom_dialog, null);
            }

            TextView tvName = (TextView) convertView.findViewById(R.id.name);
            if (title == null && position == 0) {
                tvName.setBackgroundResource( R.drawable.btn_popup_first_black_selector);
            } else {
                tvName.setBackgroundResource(R.drawable.view_init_no_bg_black_color_selector);
            }
            tvName.setTextColor(textColor);
            tvName.setText(choose[position]);
            return convertView;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }
    }

    public static final class Builder {
        private String title;
        private String[] choose;
        private OnItemClickListener itemClickListener;
        private View.OnClickListener cancelListener;
        private Context context;
        private int textColor = MyApplication.getContext().getResources().getColor(R.color.st_bg_common_master_orange);

        private Builder() {
        }

        public Builder title(String val) {
            title = val;
            return this;
        }


        public Builder textColor(int val) {
            textColor = val;
            return this;
        }

        public ChooseDialog build() {
            if (context == null) {
                return null;
            }
            ChooseDialog chooseDialog = new ChooseDialog(context);
            chooseDialog.title = title;
            chooseDialog.choose = choose;
            chooseDialog.itemClickListener = itemClickListener;
            chooseDialog.cancelListener = cancelListener;
            chooseDialog.textColor = textColor;
            return chooseDialog;
        }

        public Builder choose(String[] val) {
            choose = val;
            return this;
        }

        public Builder itemClickListener(OnItemClickListener val) {
            itemClickListener = val;
            return this;
        }

        public Builder cancelListener(View.OnClickListener val) {
            cancelListener = val;
            return this;
        }

        public Builder context(Context val) {
            context = val;
            return this;
        }
    }
}

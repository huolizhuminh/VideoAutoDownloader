package com.minhui.networkcapture.typeselect;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.minhui.networkcapture.R;
import com.minhui.vpn.parser.ShowDataType;

import java.util.ArrayList;
import java.util.List;

public class TypeSelectView extends LinearLayout {
    List<TextView> selectItemList = new ArrayList<>();
    int type = ShowDataType.ALL;
    TypeChangeListener listener;

    public TypeSelectView(Context context) {
        this(context, null);
    }

    public void setListener(TypeChangeListener listener) {
        this.listener = listener;
    }

    public TypeSelectView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        View.inflate(context, R.layout.view_type_select, this);
        initItems();
    }

    public void setType(int newType) {
        selectItemList.get(type).setTextColor(getResources().getColor(R.color.black_FF333333));
        selectItemList.get(type).setBackground(null);
        type = newType;
        selectItemList.get(type).setTextColor(Color.WHITE);
        selectItemList.get(type).setBackgroundColor(getResources().getColor(R.color.st_bg_common_master_orange));
        if (listener != null) {
            listener.onTypeChange(type);
        }
    }

    private void initItems() {
        selectItemList.add(findViewById(R.id.all));
        selectItemList.add(findViewById(R.id.text));
        selectItemList.add(findViewById(R.id.image));
        selectItemList.add(findViewById(R.id.audio));
        selectItemList.add(findViewById(R.id.video));
        selectItemList.add(findViewById(R.id.udp));
        selectItemList.add(findViewById(R.id.other));
        for (int i = 0; i < selectItemList.size(); i++) {
            int finalIndex = i;
            selectItemList.get(i).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectItemList.get(type).setTextColor(getResources().getColor(R.color.black_FF333333));
                    selectItemList.get(type).setBackground(null);
                    type = finalIndex;
                    ((TextView) v).setTextColor(Color.WHITE);
                    v.setBackgroundColor(getResources().getColor(R.color.st_bg_common_master_orange));
                    if (listener != null) {
                        listener.onTypeChange(type);
                    }
                }
            });
        }
    }

    public interface TypeChangeListener {
        void onTypeChange(int type);
    }
}

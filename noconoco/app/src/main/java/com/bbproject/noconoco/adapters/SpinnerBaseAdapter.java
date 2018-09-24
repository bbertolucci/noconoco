package com.bbproject.noconoco.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.bbproject.noconoco.R;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

@SuppressLint("ViewHolder")
public class SpinnerBaseAdapter extends BaseAdapter {

    private final String[] mData;
    private final int mCount;
    private final LayoutInflater mInflator;
    private int mSelectedPosition;

    public SpinnerBaseAdapter(Context pContext, String[] pData, int pSelectedPosition) {
        mData = pData;
        mCount = pData.length;
        mSelectedPosition = pSelectedPosition;
        mInflator = (LayoutInflater) pContext.getSystemService(LAYOUT_INFLATER_SERVICE);
    }

    public void setSelectedPosition(int pSelectedPosition) {
        mSelectedPosition = pSelectedPosition;
    }

    @Override
    public View getView(int pPosition, View pConvertView, ViewGroup pParent) {
        ViewHolder holder;
        if (null == pConvertView) {
            pConvertView = mInflator.inflate(R.layout.view_spinner, pParent, false);
            holder = new ViewHolder();
            holder.textSpinner = pConvertView.findViewById(R.id.textspinner);
            holder.relative = pConvertView.findViewById(R.id.relativespinner);
        } else {
            holder = (ViewHolder) pConvertView.getTag();
        }
        holder.textSpinner.setText(mData[pPosition]);
        if (pPosition == mSelectedPosition) {
            holder.relative.setSelected(true);
            holder.textSpinner.setTextColor(Color.WHITE);
        } else {
            holder.relative.setSelected(false);
            holder.textSpinner.setTextColor(pParent.getResources().getColor(R.color.blue_text));
        }
        pConvertView.setTag(holder);
        return pConvertView;
    }

    @Override
    public long getItemId(int pPosition) {
        return pPosition;
    }

    @Override
    public Object getItem(int pPosition) {
        return mData[pPosition];
    }

    @Override
    public int getCount() {
        return mCount;
    }

    public View getDropDownView(int pPosition, View pConvertView, ViewGroup pParent) {
        if (pConvertView == null) {
            pConvertView = mInflator.inflate(R.layout.view_spinner, pParent, false);
        }

        return pConvertView;
    }

    private class ViewHolder {
        private TextView textSpinner;
        private View relative;
    }
}
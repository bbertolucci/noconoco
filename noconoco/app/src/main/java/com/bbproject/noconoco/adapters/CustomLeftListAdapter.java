package com.bbproject.noconoco.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;

import com.bbproject.noconoco.R;
import com.bbproject.noconoco.activities.MainActivity;
import com.bbproject.noconoco.activities.TwitchActivity;
import com.bbproject.noconoco.custom.view.MyTextView;
import com.bbproject.noconoco.model.GroupItem;
import com.bbproject.noconoco.model.SubGroupItem;
import com.bbproject.noconoco.custom.view.smartimage.SmartImageView;

import java.util.ArrayList;


public class CustomLeftListAdapter extends BaseExpandableListAdapter {
    private final MainActivity mActivity;
    private final ArrayList<GroupItem> mGroupItemList;
    private final LayoutInflater mInflater;
    private final int mCount;

    public CustomLeftListAdapter(MainActivity pActivity,
                                 ArrayList<GroupItem> pGroupItemList) {
        mActivity = pActivity;
        mGroupItemList = pGroupItemList;
        mCount = pGroupItemList.size();
        mInflater = (LayoutInflater) mActivity
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (null == mInflater) {
            throw new AssertionError("LayoutInflater not found.");
        }
    }

    public SubGroupItem getChild(int pGroupPosition, int pChildPosition) {
        GroupItem groupItem = getGroup(pGroupPosition);
        if (null != groupItem) {
            ArrayList<SubGroupItem> subGroupItemList = groupItem.getSubList();
            if (null != subGroupItemList && !subGroupItemList.isEmpty()) {
                return subGroupItemList.get(pChildPosition);
            }
        }
        return null;
    }


    public long getChildId(int pGroupPosition, int pChildPosition) {
        return pChildPosition;
    }

    public View getChildView(
            final int pGroupPosition,
            final int pChildPosition,
            boolean pIsLastChild,
            View pConvertView,
            ViewGroup pParent) {
        SubGroupItem subGroupItem = getChild(pGroupPosition, pChildPosition);

        //LayoutInflater inflater = mActivity.getLayoutInflater();
        ViewHolderChild holder = null;
        if (pConvertView == null) {
            //convertView = inflater.inflate(R.layout.drawer_list, parent, false);
            pConvertView = mInflater.inflate(R.layout.drawer_list_child, pParent, false);
            holder = new ViewHolderChild();
            holder.child = pConvertView.findViewById(R.id.child);
            holder.childimage = pConvertView.findViewById(R.id.childimage);
        }
        if (null == holder) {
            holder = (ViewHolderChild)pConvertView.getTag();
        }
        holder.child.setText(subGroupItem.getName());

        if (subGroupItem.getIconId() != 0) {
            holder.childimage.setVisibility(View.VISIBLE);
            holder.childimage.setBackgroundResource(subGroupItem.getIconId());
        } else {
            holder.childimage.setVisibility(View.GONE);
        }

        pConvertView.setTag(holder);

        return pConvertView;
    }

    public int getChildrenCount(int pGroupPosition) {
        GroupItem groupItem = getGroup(pGroupPosition);
        if (null == groupItem.getSubList()) return 0;
        return groupItem.getCount();
    }

    public GroupItem getGroup(int pGroupPosition) {
        return mGroupItemList.get(pGroupPosition);
    }

    public int getGroupCount() {
        return mCount;
    }

    public long getGroupId(int pGroupPosition) {
        return pGroupPosition;
    }

    public View getGroupView(
            int pGroupPosition,
            boolean pIsExpanded,
            View pConvertView,
            ViewGroup pParent) {
        ViewHolderParent holder = null;
        GroupItem groupItem = mGroupItemList.get(pGroupPosition);
        if (pConvertView == null) {
            pConvertView = mInflater.inflate(R.layout.drawer_list_parent, pParent, false);
            holder = new ViewHolderParent();
            holder.imgRepo = pConvertView.findViewById(R.id.imgRepo);
            holder.nolifelive = pConvertView.findViewById(R.id.nolifelive);
            holder.group = pConvertView.findViewById(R.id.group);
        }
        if (null == holder) {
            holder = (ViewHolderParent) pConvertView.getTag();
        }
        switch (groupItem.getType()) {
            case GroupItem.NOICON: {
                holder.imgRepo.setVisibility(View.GONE);
                holder.nolifelive.setVisibility(View.GONE);
                break;
            }
            case GroupItem.ICON: {
                holder.imgRepo.setVisibility(View.VISIBLE);
                holder.nolifelive.setVisibility(View.GONE);
                break;
            }
            case GroupItem.TWITCH: {
                holder.imgRepo.setVisibility(View.GONE);
                holder.nolifelive.setVisibility(View.VISIBLE);
                holder.nolifelive.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.icon_twitch));
                holder.nolifelive.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View pView) {
                        Intent configIntent = new Intent(mActivity, TwitchActivity.class);
                        mActivity.startActivity(configIntent);
                    }
                });
                break;
            }
        }
        holder.group.setTypeface(null, Typeface.BOLD);
        holder.group.setText(groupItem.getName());

        pConvertView.setTag(holder);

        return pConvertView;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public boolean isChildSelectable(int pGroupPosition, int pChildPosition) {
        return true;
    }

    private static class ViewHolderParent {

        private View imgRepo;
        private SmartImageView nolifelive;
        private MyTextView group;
    }

    private static class ViewHolderChild {

        private MyTextView child;
        private ImageView childimage;
    }
}

package com.beemindz.notej.activity.navigation;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.beemindz.notej.R;

import java.util.ArrayList;

public class ItemAdapter extends BaseAdapter {
  private LayoutInflater mInflater;
  private ArrayList<ItemNavigation> arr;

  public ItemAdapter(Context mContext, ArrayList<ItemNavigation> arr) {
    super();
    this.arr = arr;
    mInflater = LayoutInflater.from(mContext);
  }

  public void add(ItemNavigation fragmentNavItem) {
    this.arr.add(fragmentNavItem);
  }

  @Override
  public int getCount() {
    int count = 0;
    if (arr != null) {
      count = arr.size();
    }
    return count;
  }

  @Override
  public Object getItem(int pos) {
    // TODO Auto-generated method stub
    return arr.get(pos);
  }

  @Override
  public long getItemId(int arg0) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    viewHolder holder;
    if (convertView == null) {
      holder = new viewHolder();
      convertView = mInflater.inflate(R.layout.navigation_item, parent, false);
      holder.itemIcon = (ImageView) convertView.findViewById(R.id.itemIcon);
      holder.itemName = (TextView) convertView.findViewById(R.id.itemName);
      convertView.setTag(holder);
    } else {
      holder = (viewHolder) convertView.getTag();
    }

    final ItemNavigation itemNavigation = arr.get(position);

    if (itemNavigation != null) {
      holder.itemIcon.setImageResource(itemNavigation.getItemIcon());
      holder.itemName.setText(itemNavigation.getItemName());
    }

    return convertView;
  }

  public class viewHolder {
    public TextView itemName;
    public ImageView itemIcon;
  }

}

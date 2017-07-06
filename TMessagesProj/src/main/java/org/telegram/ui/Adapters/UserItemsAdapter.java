package org.telegram.ui.Adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.telegram.messenger.R;
import org.telegram.ui.Components.UserItems;

import java.util.ArrayList;

/**
 * Created by oleg.svs on 22.05.2017.
 */

public class UserItemsAdapter extends BaseAdapter {

    ArrayList<Object> itemList = null; //fixed crash on restart activity

    public Activity context;
    public LayoutInflater inflater;

    public UserItemsAdapter(Activity context, ArrayList<Object> itemList) {
        super();

        this.context = context;
        this.itemList = itemList;

        this.inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        if (itemList != null)
            return itemList.size();
        else return 0;
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return itemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return 0;
    }

    public static class ViewHolder
    {
        ImageView imgViewPhoto;
        TextView txtViewName;
        TextView txtViewPhone;
        ImageView imgViewCurrent;
    }

    public void remove(int position){
        itemList.remove(itemList.get(position));;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub

        ViewHolder holder;
        if(convertView==null)
        {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.user_list_item, null);

            holder.imgViewPhoto = (ImageView) convertView.findViewById(R.id.userPhoto);
            holder.imgViewCurrent = (ImageView) convertView.findViewById(R.id.userCurrent);
            holder.txtViewName = (TextView) convertView.findViewById(R.id.userName);
            holder.txtViewPhone = (TextView) convertView.findViewById(R.id.userPhone);
            holder.imgViewCurrent.setVisibility(View.INVISIBLE);
            convertView.setTag(holder);
        }
        else
            holder=(ViewHolder)convertView.getTag();

        UserItems userItems = (UserItems) itemList.get(position);
        holder.imgViewPhoto.setImageBitmap(userItems.getPhoto());
        if ((userItems.getCurrent() != -1) && (userItems.getCurrent() == position)) {
            holder.imgViewCurrent.setColorFilter(new PorterDuffColorFilter(Color.GREEN, PorterDuff.Mode.MULTIPLY));
            holder.imgViewCurrent.setVisibility(View.VISIBLE);
        } else holder.imgViewCurrent.setVisibility(View.INVISIBLE);

        holder.txtViewName.setText(userItems.getName());
        holder.txtViewPhone.setText(userItems.getPhone());
        return convertView;
    }
}

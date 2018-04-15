package singhal.com.iit;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by Nitin on 4/15/2018.
 */

public class Adapter extends BaseAdapter {

    private ArrayList<String> arrayList;
    private Context context;

    public Adapter(ArrayList<String> arrayList, Context context) {
        this.arrayList = arrayList;
        this.context = context;
    }

    @Override
    public int getCount() {
        return arrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return arrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent)
    {
        SinglechildviewHolder singlechildviewHolder;
        if(convertView==null)
        {
            LayoutInflater layoutInflater= (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView=layoutInflater.inflate(R.layout.singleimage,null);
            singlechildviewHolder=new SinglechildviewHolder();
            singlechildviewHolder.imageView=convertView.findViewById(R.id.image);
            convertView.setTag(singlechildviewHolder);
        }


        singlechildviewHolder= (SinglechildviewHolder) convertView.getTag();
        Picasso.get().load(arrayList.get(position)).placeholder(R.mipmap.ic_launcher).into(singlechildviewHolder.imageView);
        singlechildviewHolder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(context,FullImage.class);
                intent.putExtra("IMAGEURL",arrayList.get(position));
                context.startActivity(intent);
            }
        });

        return convertView;
    }
    public class SinglechildviewHolder{

        ImageView imageView;
    }
}

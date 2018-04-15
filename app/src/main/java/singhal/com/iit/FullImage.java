package singhal.com.iit;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

public class FullImage extends AppCompatActivity {

    ImageView imageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_image);
        imageView=findViewById(R.id.fullimage);
        Intent intent=getIntent();
        if(intent!=null && intent.getStringExtra("IMAGEURL")!=null){
            Picasso.get().load(intent.getStringExtra("IMAGEURL")).into(imageView);
        }
        else {
            Toast.makeText(FullImage.this,"No Image To Show",Toast.LENGTH_SHORT).show();
        }
    }
}

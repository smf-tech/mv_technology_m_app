package com.mv.Activity;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.mv.R;
import com.mv.Utils.Constants;
import com.mv.Utils.PreferenceHelper;
import com.mv.databinding.ActivityImageViewBinding;


/**
 * Created by User on 6/20/2017.
 */

public class ActivityImageView extends AppCompatActivity {

    private ActivityImageViewBinding binding;
    private PreferenceHelper preferenceHelper;
    private String Id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_image_view);
        binding.setActivity(this);

        if (getIntent().getExtras() != null) {
            Id = getIntent().getExtras().getString(Constants.ID);
        }

        preferenceHelper = new PreferenceHelper(this);

        Glide.with(this)
                .load(getUrlWithHeaders(preferenceHelper.getString(PreferenceHelper.InstanceUrl) + "/services/data/v36.0/sobjects/Attachment/" + Id + "/Body"))
                .placeholder(getResources().getDrawable(R.drawable.app_logo))
                .into(binding.imageView);
    }

    private GlideUrl getUrlWithHeaders(String url) {
//
        return new GlideUrl(url, new LazyHeaders.Builder()
                .addHeader("Authorization", "OAuth " + preferenceHelper.getString(PreferenceHelper.AccessToken))
                .addHeader("Content-Type", "image/png")
                .build());
    }

}

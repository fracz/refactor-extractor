package com.andkulikov.transitionseverywhere;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.transitionseverywhere.ChangeBounds;
import com.transitionseverywhere.ChangeImageTransform;
import com.transitionseverywhere.TransitionManager;
import com.transitionseverywhere.TransitionSet;

/**
 * Created by Andrey Kulikov on 20/03/16.
 */
public class ImageTransformSample extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.image_transform_sample, container, false);

        final ViewGroup transitionsContainer = (ViewGroup) view.findViewById(R.id.transitions_container);
        final ImageView imageView = (ImageView) transitionsContainer.findViewById(R.id.image);

        imageView.setOnClickListener(new View.OnClickListener() {

            boolean mExpanded;

            @Override
            public void onClick(View v) {
                mExpanded = !mExpanded;

                TransitionManager.beginDelayedTransition(transitionsContainer,
                    new TransitionSet()
                        .addTransition(new ChangeImageTransform())
                        .addTransition(new ChangeBounds()));

                FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) imageView.getLayoutParams();
                if (mExpanded) {
                    params.width = ViewGroup.LayoutParams.MATCH_PARENT;
                    params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                    params.gravity = Gravity.CENTER;
                } else {
                    params.width = getResources().getDimensionPixelSize(R.dimen.image_collapsed_size);
                    params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                    params.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
                }
                imageView.setLayoutParams(params);
            }
        });

        return view;
    }
}
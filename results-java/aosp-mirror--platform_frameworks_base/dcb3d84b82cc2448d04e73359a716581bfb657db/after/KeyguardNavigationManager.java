/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.internal.policy.impl.keyguard;

import android.view.View;
import android.widget.TextView;

import com.android.internal.R;

public class KeyguardNavigationManager {

    private TextView mMessageArea;
    private KeyguardSecurityView mKeyguardSecurityView;

    public KeyguardNavigationManager(KeyguardSecurityView view) {
        mKeyguardSecurityView = view;
        mMessageArea = (TextView) ((View) view).findViewById(R.id.message_area);
        mMessageArea.setSelected(true); // Make marquee work
        mMessageArea.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mKeyguardSecurityView.getCallback().dismiss(false);
            }
        });
    }

    public void setMessage(CharSequence msg) {
        mMessageArea.setText(msg);
    }

    public void setMessage(int resId) {
        if (resId != 0) {
            mMessageArea.setText(resId);
        } else {
            mMessageArea.setText("");
        }
    }

    public void setMessage(int resId, Object... formatArgs) {
        if (resId != 0) {
            mMessageArea.setText(mMessageArea.getContext().getString(resId, formatArgs));
        } else {
            mMessageArea.setText("");
        }
    }
}
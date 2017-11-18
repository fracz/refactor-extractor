/*******************************************************************************
 * Copyright 2011, 2012 Chris Banes.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.handmark.pulltorefresh.library.extras;

import android.content.Context;
import android.media.MediaPlayer;
import android.view.View;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;

public class SoundPullEventListener<V extends View> implements PullToRefreshBase.OnPullEventListener<V> {

	private final Context mContext;
	private int mPullSoundResId, mReleaseSoundResId;

	private MediaPlayer mCurrentMediaPlayer;

	/**
	 * Constructor
	 *
	 * @param context
	 *            - Context
	 * @param pullSoundResId
	 *            - Resource Id of the sound to be played when a Pull Event
	 *            happens (e.g. <var>R.raw.pull_sound</var>)
	 * @param releaseSoundResId
	 *            - Resource Id of the sound to be played when a Release Event
	 *            happens (e.g. <var>R.raw.release_sound</var>)
	 */
	public SoundPullEventListener(Context context, int pullSoundResId, int releaseSoundResId) {
		mContext = context;
		mPullSoundResId = pullSoundResId;
		mReleaseSoundResId = releaseSoundResId;
	}

	@Override
	public void onPull(PullToRefreshBase<V> refreshView, Mode direction) {
		playSound(mPullSoundResId);
	}

	@Override
	public void onRelease(PullToRefreshBase<V> refreshView, Mode direction) {
		playSound(mReleaseSoundResId);
	}

	/**
	 * Set the Sound to be played when a Pull Event happens
	 *
	 * @param resId
	 *            - Resource Id of the sound file (e.g.
	 *            <var>R.raw.pull_sound</var>)
	 */
	public void setPullSound(int resId) {
		mPullSoundResId = resId;
	}

	/**
	 * Set the Sound to be played when a Release Event happens
	 *
	 * @param resId
	 *            - Resource Id of the sound file (e.g.
	 *            <var>R.raw.release_sound</var>)
	 */
	public void setReleaseSound(int resId) {
		mReleaseSoundResId = resId;
	}

	/**
	 * Gets the current (or last) MediaPlayer instance.
	 */
	public MediaPlayer getCurrentMediaPlayer() {
		return mCurrentMediaPlayer;
	}

	private void playSound(int resId) {
		// Stop current player, if there's one playing
		if (null != mCurrentMediaPlayer) {
			mCurrentMediaPlayer.stop();
			mCurrentMediaPlayer.release();
		}

		mCurrentMediaPlayer = MediaPlayer.create(mContext, resId);
		if (null != mCurrentMediaPlayer) {
			mCurrentMediaPlayer.start();
		}
	}

}
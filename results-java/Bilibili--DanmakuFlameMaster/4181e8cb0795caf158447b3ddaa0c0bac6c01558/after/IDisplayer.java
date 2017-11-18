/*
 * Copyright (C) 2013 Chen Hui <calmer91@gmail.com>
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

package master.flame.danmaku.danmaku.model;


public interface IDisplayer {

    int DANMAKU_STYLE_DEFAULT = -1; // 自动
    int DANMAKU_STYLE_NONE = 0; // �
    int DANMAKU_STYLE_SHADOW = 1; // 阴影
    int DANMAKU_STYLE_STROKEN = 2; // 描边
    int DANMAKU_STYLE_PROJECTION = 3; // 投影

    int getWidth();

    int getHeight();

    float getDensity();

    int getDensityDpi();

    int draw(BaseDanmaku danmaku);

    float getScaledDensity();

    int getSlopPixel();

    void measure(BaseDanmaku danmaku);

    float getStrokeWidth();

    void setHardwareAccelerated(boolean enable);

    boolean isHardwareAccelerated();

    int getMaximumCacheWidth();

    int getMaximumCacheHeight();


    ////////////////// setter ///////////////////////////

    void resetSlopPixel(float factor);

    void setDensities(float density, int densityDpi, float scaledDensity);

    void setSize(int width, int height);

    void setDanmakuStyle(int style, float[] data);

}
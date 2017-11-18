
package master.flame.danmaku.danmaku.model.android;

import android.graphics.Typeface;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import master.flame.danmaku.controller.DanmakuFilters;
import master.flame.danmaku.controller.DanmakuFilters.IDanmakuFilter;
import master.flame.danmaku.danmaku.model.AbsDisplayer;
import master.flame.danmaku.danmaku.model.AlphaValue;
import master.flame.danmaku.danmaku.model.BaseDanmaku;
import master.flame.danmaku.danmaku.model.GlobalFlagValues;
import master.flame.danmaku.danmaku.parser.DanmakuFactory;

public class DanmakuContext {

    public static DanmakuContext create() {
        return new DanmakuContext();
    }

    public enum DanmakuConfigTag {
        FT_DANMAKU_VISIBILITY, FB_DANMAKU_VISIBILITY, L2R_DANMAKU_VISIBILITY, R2L_DANMAKU_VISIBILIY, SPECIAL_DANMAKU_VISIBILITY, TYPEFACE, TRANSPARENCY, SCALE_TEXTSIZE, MAXIMUM_NUMS_IN_SCREEN, DANMAKU_STYLE, DANMAKU_BOLD, COLOR_VALUE_WHITE_LIST, USER_ID_BLACK_LIST, USER_HASH_BLACK_LIST, SCROLL_SPEED_FACTOR, BLOCK_GUEST_DANMAKU, DUPLICATE_MERGING_ENABLED, MAXIMUN_LINES, OVERLAPPING_ENABLE;

        public boolean isVisibilityRelatedTag() {
            return this.equals(FT_DANMAKU_VISIBILITY) || this.equals(FB_DANMAKU_VISIBILITY)
                    || this.equals(L2R_DANMAKU_VISIBILITY) || this.equals(R2L_DANMAKU_VISIBILIY)
                    || this.equals(SPECIAL_DANMAKU_VISIBILITY) || this.equals(COLOR_VALUE_WHITE_LIST)
                    || this.equals(USER_ID_BLACK_LIST);
        }
    }

    /**
     * 默认字体
     */
    public Typeface mFont = null;

    /**
     * paint alpha:0-255
     */
    public int transparency = AlphaValue.MAX;

    public float scaleTextSize = 1.0f;

    /**
     * 弹幕显示隐藏设置
     */
    public boolean FTDanmakuVisibility = true;

    public boolean FBDanmakuVisibility = true;

    public boolean L2RDanmakuVisibility = true;

    public boolean R2LDanmakuVisibility = true;

    public boolean SecialDanmakuVisibility = true;

    List<Integer> mFilterTypes = new ArrayList<Integer>();

    /**
     * 同屏弹幕数量 -1 按绘制效率自动调整 0 无限制 n 同屏最大显示n个弹幕
     */
    public int maximumNumsInScreen = -1;

    /**
     * 默认滚动速度系数
     */
    public float scrollSpeedFactor = 1.0f;


    /**
     * 绘制刷新率(毫秒)
     */
    public int refreshRateMS = 15;

    /**
     * 描边/阴影类型
     */
    public enum BorderType {
        NONE, SHADOW, STROKEN
    }

    public BorderType shadowType = BorderType.SHADOW;

    public int shadowRadius = 3;

    List<Integer> mColorValueWhiteList = new ArrayList<Integer>();

    List<Integer> mUserIdBlackList = new ArrayList<Integer>();

    List<String> mUserHashBlackList = new ArrayList<String>();

    private List<WeakReference<ConfigChangedCallback>> mCallbackList;

    private boolean mBlockGuestDanmaku = false;

    private boolean mDuplicateMergingEnable = false;

    private BaseCacheStuffer mCacheStuffer;

    private boolean mIsMaxLinesLimited;

    private boolean mIsPreventOverlappingEnabled;

    private final AbsDisplayer mDisplayer = new AndroidDisplayer();

    public final GlobalFlagValues mGlobalFlagValues = new GlobalFlagValues();

    public final DanmakuFilters mDanmakuFilters = new DanmakuFilters();

    public final DanmakuFactory mDanmakuFactory = new DanmakuFactory();

    public AbsDisplayer getDisplayer() {
        return mDisplayer;
    }

    /**
     * set typeface
     *
     * @param font
     */
    public DanmakuContext setTypeface(Typeface font) {
        if (mFont != font) {
            mFont = font;
            mDisplayer.clearTextHeightCache();
            mDisplayer.setTypeFace(font);
            notifyConfigureChanged(DanmakuConfigTag.TYPEFACE);
        }
        return this;
    }

    public DanmakuContext setDanmakuTransparency(float p) {
        int newTransparency = (int) (p * AlphaValue.MAX);
        if (newTransparency != transparency) {
            transparency = newTransparency;
            mDisplayer.setTransparency(newTransparency);
            notifyConfigureChanged(DanmakuConfigTag.TRANSPARENCY, p);
        }
        return this;
    }

    public DanmakuContext setScaleTextSize(float p) {
        if (scaleTextSize != p) {
            scaleTextSize = p;
            mDisplayer.clearTextHeightCache();
            mDisplayer.setScaleTextSizeFactor(p);
            mGlobalFlagValues.updateMeasureFlag();
            mGlobalFlagValues.updateVisibleFlag();
            notifyConfigureChanged(DanmakuConfigTag.SCALE_TEXTSIZE, p);
        }
        return this;
    }

    /**
     * @return 是否显示顶部弹幕
     */
    public boolean getFTDanmakuVisibility() {
        return FTDanmakuVisibility;
    }

    /**
     * 设置是否显示顶部弹幕
     *
     * @param visible
     */
    public DanmakuContext setFTDanmakuVisibility(boolean visible) {
        setDanmakuVisible(visible, BaseDanmaku.TYPE_FIX_TOP);
        setFilterData(DanmakuFilters.TAG_TYPE_DANMAKU_FILTER, mFilterTypes);
        mGlobalFlagValues.updateFilterFlag();
        if (FTDanmakuVisibility != visible) {
            FTDanmakuVisibility = visible;
            notifyConfigureChanged(DanmakuConfigTag.FT_DANMAKU_VISIBILITY, visible);
        }
        return this;
    }

    private <T> void setFilterData(String tag, T data) {
        setFilterData(tag, data, true);
    }

    private <T> void setFilterData(String tag, T data, boolean primary) {
        @SuppressWarnings("unchecked")
        IDanmakuFilter<T> filter = (IDanmakuFilter<T>) mDanmakuFilters.get(tag, primary);
        filter.setData(data);
    }

    private void setDanmakuVisible(boolean visible, int type) {
        if (visible) {
            mFilterTypes.remove(Integer.valueOf(type));
        } else if (!mFilterTypes.contains(Integer.valueOf(type))) {
            mFilterTypes.add(type);
        }
    }

    /**
     * @return 是否显示底部弹幕
     */
    public boolean getFBDanmakuVisibility() {
        return FBDanmakuVisibility;
    }

    /**
     * 设置是否显示底部弹幕
     *
     * @param visible
     */
    public DanmakuContext setFBDanmakuVisibility(boolean visible) {
        setDanmakuVisible(visible, BaseDanmaku.TYPE_FIX_BOTTOM);
        setFilterData(DanmakuFilters.TAG_TYPE_DANMAKU_FILTER, mFilterTypes);
        mGlobalFlagValues.updateFilterFlag();
        if (FBDanmakuVisibility != visible) {
            FBDanmakuVisibility = visible;
            notifyConfigureChanged(DanmakuConfigTag.FB_DANMAKU_VISIBILITY, visible);
        }
        return this;
    }

    /**
     * @return 是否显示左右滚动弹幕
     */
    public boolean getL2RDanmakuVisibility() {
        return L2RDanmakuVisibility;
    }

    /**
     * 设置是否显示左右滚动弹幕
     *
     * @param visible
     */
    public DanmakuContext setL2RDanmakuVisibility(boolean visible) {
        setDanmakuVisible(visible, BaseDanmaku.TYPE_SCROLL_LR);
        setFilterData(DanmakuFilters.TAG_TYPE_DANMAKU_FILTER, mFilterTypes);
        mGlobalFlagValues.updateFilterFlag();
        if(L2RDanmakuVisibility != visible){
            L2RDanmakuVisibility = visible;
            notifyConfigureChanged(DanmakuConfigTag.L2R_DANMAKU_VISIBILITY, visible);
        }
        return this;
    }

    /**
     * @return 是否显示右左滚动弹幕
     */
    public boolean getR2LDanmakuVisibility() {
        return R2LDanmakuVisibility;
    }

    /**
     * 设置是否显示右左滚动弹幕
     *
     * @param visible
     */
    public DanmakuContext setR2LDanmakuVisibility(boolean visible) {
        setDanmakuVisible(visible, BaseDanmaku.TYPE_SCROLL_RL);
        setFilterData(DanmakuFilters.TAG_TYPE_DANMAKU_FILTER, mFilterTypes);
        mGlobalFlagValues.updateFilterFlag();
        if (R2LDanmakuVisibility != visible) {
            R2LDanmakuVisibility = visible;
            notifyConfigureChanged(DanmakuConfigTag.R2L_DANMAKU_VISIBILIY, visible);
        }
        return this;
    }

    /**
     * @return 是否显示特殊弹幕
     */
    public boolean getSecialDanmakuVisibility() {
        return SecialDanmakuVisibility;
    }

    /**
     * 设置是否显示特殊弹幕
     *
     * @param visible
     */
    public DanmakuContext setSpecialDanmakuVisibility(boolean visible) {
        setDanmakuVisible(visible, BaseDanmaku.TYPE_SPECIAL);
        setFilterData(DanmakuFilters.TAG_TYPE_DANMAKU_FILTER, mFilterTypes);
        mGlobalFlagValues.updateFilterFlag();
        if (SecialDanmakuVisibility != visible) {
            SecialDanmakuVisibility = visible;
            notifyConfigureChanged(DanmakuConfigTag.SPECIAL_DANMAKU_VISIBILITY, visible);
        }
        return this;
    }

    /**
     * 设置同屏弹幕密度 -1自动 0无限制
     *
     * @param maxSize
     * @return
     */
    public DanmakuContext setMaximumVisibleSizeInScreen(int maxSize) {
        maximumNumsInScreen = maxSize;
        // 无限制
        if (maxSize == 0) {
            mDanmakuFilters.unregisterFilter(DanmakuFilters.TAG_QUANTITY_DANMAKU_FILTER);
            mDanmakuFilters.unregisterFilter(DanmakuFilters.TAG_ELAPSED_TIME_FILTER);
            notifyConfigureChanged(DanmakuConfigTag.MAXIMUM_NUMS_IN_SCREEN, maxSize);
            return this;
        }
        // 自动调整
        if (maxSize == -1) {
            mDanmakuFilters.unregisterFilter(DanmakuFilters.TAG_QUANTITY_DANMAKU_FILTER);
            mDanmakuFilters.registerFilter(DanmakuFilters.TAG_ELAPSED_TIME_FILTER);
            notifyConfigureChanged(DanmakuConfigTag.MAXIMUM_NUMS_IN_SCREEN, maxSize);
            return this;
        }
        setFilterData(DanmakuFilters.TAG_QUANTITY_DANMAKU_FILTER, maxSize);
        mGlobalFlagValues.updateFilterFlag();
        notifyConfigureChanged(DanmakuConfigTag.MAXIMUM_NUMS_IN_SCREEN, maxSize);
        return this;
    }

    /**
     * 设置描边样式
     *
     * @param style DANMAKU_STYLE_NONE DANMAKU_STYLE_SHADOW or
     *            DANMAKU_STYLE_STROKEN or DANMAKU_STYLE_PROJECTION
     * @param values
     *        DANMAKU_STYLE_SHADOW 阴影模式下，values传入阴影半径
     *        DANMAKU_STYLE_STROKEN 描边模式下，values传入描边宽度
     *        DANMAKU_STYLE_PROJECTION
     *            投影模式下，values传入offsetX, offsetY, alpha
     *                offsetX/offsetY: x/y 方向上的偏移量
     *                alpha: 投影透明度 [0...255]
     * @return
     */
    public DanmakuContext setDanmakuStyle(int style, float... values) {
        mDisplayer.setDanmakuStyle(style, values);
        notifyConfigureChanged(DanmakuConfigTag.DANMAKU_STYLE, style, values);
        return this;
    }

    /**
     * 设置是否粗体显示,对某些字体无效
     *
     * @param bold
     * @return
     */
    public DanmakuContext setDanmakuBold(boolean bold) {
        mDisplayer.setFakeBoldText(bold);
        notifyConfigureChanged(DanmakuConfigTag.DANMAKU_BOLD, bold);
        return this;
    }

    /**
     * 设置色彩过滤弹幕白名单
     * @param colors
     * @return
     */
    public DanmakuContext setColorValueWhiteList(Integer... colors) {
        mColorValueWhiteList.clear();
        if (colors == null || colors.length == 0) {
            mDanmakuFilters.unregisterFilter(DanmakuFilters.TAG_TEXT_COLOR_DANMAKU_FILTER);
        } else {
            Collections.addAll(mColorValueWhiteList, colors);
            setFilterData(DanmakuFilters.TAG_TEXT_COLOR_DANMAKU_FILTER, mColorValueWhiteList);
        }
        mGlobalFlagValues.updateFilterFlag();
        notifyConfigureChanged(DanmakuConfigTag.COLOR_VALUE_WHITE_LIST, mColorValueWhiteList);
        return this;
    }

    public List<Integer> getColorValueWhiteList(){
        return mColorValueWhiteList;
    }

    /**
     * 设置屏蔽弹幕用户hash
     * @param hashes
     * @return
     */
    public DanmakuContext setUserHashBlackList(String... hashes) {
        mUserHashBlackList.clear();
        if (hashes == null || hashes.length == 0) {
            mDanmakuFilters.unregisterFilter(DanmakuFilters.TAG_USER_HASH_FILTER);
        } else {
            Collections.addAll(mUserHashBlackList, hashes);
            setFilterData(DanmakuFilters.TAG_USER_HASH_FILTER, mUserHashBlackList);
        }
        mGlobalFlagValues.updateFilterFlag();
        notifyConfigureChanged(DanmakuConfigTag.USER_HASH_BLACK_LIST, mUserHashBlackList);
        return this;
    }

    public DanmakuContext removeUserHashBlackList(String... hashes){
        if(hashes == null || hashes.length == 0) {
            return this;
        }
        for (String hash : hashes) {
            mUserHashBlackList.remove(hash);
        }
        setFilterData(DanmakuFilters.TAG_USER_HASH_FILTER, mUserHashBlackList);
        mGlobalFlagValues.updateFilterFlag();
        notifyConfigureChanged(DanmakuConfigTag.USER_HASH_BLACK_LIST, mUserHashBlackList);
        return this;
    }

    /**
     * 添加屏蔽用户
     * @param hashes
     * @return
     */
    public DanmakuContext addUserHashBlackList(String... hashes){
        if(hashes == null || hashes.length == 0) {
            return this;
        }
        Collections.addAll(mUserHashBlackList, hashes);
        setFilterData(DanmakuFilters.TAG_USER_HASH_FILTER, mUserHashBlackList);
        mGlobalFlagValues.updateFilterFlag();
        notifyConfigureChanged(DanmakuConfigTag.USER_HASH_BLACK_LIST, mUserHashBlackList);
        return this;
    }

    public List<String> getUserHashBlackList(){
        return mUserHashBlackList;
    }


    /**
     * 设置屏蔽弹幕用户id , 0 表示游客弹幕
     * @param ids
     * @return
     */
    public DanmakuContext setUserIdBlackList(Integer... ids) {
        mUserIdBlackList.clear();
        if (ids == null || ids.length == 0) {
            mDanmakuFilters.unregisterFilter(DanmakuFilters.TAG_USER_ID_FILTER);
        } else {
            Collections.addAll(mUserIdBlackList, ids);
            setFilterData(DanmakuFilters.TAG_USER_ID_FILTER, mUserIdBlackList);
        }
        mGlobalFlagValues.updateFilterFlag();
        notifyConfigureChanged(DanmakuConfigTag.USER_ID_BLACK_LIST, mUserIdBlackList);
        return this;
    }

    public DanmakuContext removeUserIdBlackList(Integer... ids){
        if(ids == null || ids.length == 0) {
            return this;
        }
        for (Integer id : ids) {
            mUserIdBlackList.remove(id);
        }
        setFilterData(DanmakuFilters.TAG_USER_ID_FILTER, mUserIdBlackList);
        mGlobalFlagValues.updateFilterFlag();
        notifyConfigureChanged(DanmakuConfigTag.USER_ID_BLACK_LIST, mUserIdBlackList);
        return this;
    }

    /**
     * 添加屏蔽用户
     * @param ids
     * @return
     */
    public DanmakuContext addUserIdBlackList(Integer... ids){
        if(ids == null || ids.length == 0) {
            return this;
        }
        Collections.addAll(mUserIdBlackList, ids);
        setFilterData(DanmakuFilters.TAG_USER_ID_FILTER, mUserIdBlackList);
        mGlobalFlagValues.updateFilterFlag();
        notifyConfigureChanged(DanmakuConfigTag.USER_ID_BLACK_LIST, mUserIdBlackList);
        return this;
    }

    public List<Integer> getUserIdBlackList(){
        return mUserIdBlackList;
    }

    /**
     * 设置是否屏蔽游客弹幕
     * @param block true屏蔽，false不屏蔽
     * @return
     */
    public DanmakuContext blockGuestDanmaku(boolean block) {
        if (mBlockGuestDanmaku != block) {
            mBlockGuestDanmaku = block;
            if (block) {
                setFilterData(DanmakuFilters.TAG_GUEST_FILTER, block);
            } else {
                mDanmakuFilters.unregisterFilter(DanmakuFilters.TAG_GUEST_FILTER);
            }
            mGlobalFlagValues.updateFilterFlag();
            notifyConfigureChanged(DanmakuConfigTag.BLOCK_GUEST_DANMAKU, block);
        }
        return this;
    }

    /**
     * 设置弹幕滚动速度系数,只对滚动弹幕有效
     * @param p
     * @return
     */
    public DanmakuContext setScrollSpeedFactor(float p){
        if (scrollSpeedFactor != p) {
            scrollSpeedFactor = p;
            mDanmakuFactory.updateDurationFactor(p);
            mGlobalFlagValues.updateMeasureFlag();
            mGlobalFlagValues.updateVisibleFlag();
            notifyConfigureChanged(DanmakuConfigTag.SCROLL_SPEED_FACTOR, p);
        }
        return this;
    }

    /**
     * 设置是否启用合并重复弹幕
     * @param enable
     * @return
     */
    public DanmakuContext setDuplicateMergingEnabled(boolean enable) {
        if (mDuplicateMergingEnable != enable) {
            mDuplicateMergingEnable = enable;
            mGlobalFlagValues.updateFilterFlag();
            notifyConfigureChanged(DanmakuConfigTag.DUPLICATE_MERGING_ENABLED, enable);
        }
        return this;
    }

    public boolean isDuplicateMergingEnabled() {
        return mDuplicateMergingEnable;
    }

    /**
     * 设置最大显示行数
     * @param pairs map<K,V> 设置null取消行数限制
     * K = (BaseDanmaku.TYPE_SCROLL_RL|BaseDanmaku.TYPE_SCROLL_LR|BaseDanmaku.TYPE_FIX_TOP|BaseDanmaku.TYPE_FIX_BOTTOM)
     * V = 最大行数
     * @return
     */
    public DanmakuContext setMaximumLines(Map<Integer, Integer> pairs) {
        mIsMaxLinesLimited = (pairs != null);
        if (pairs == null) {
            mDanmakuFilters.unregisterFilter(DanmakuFilters.TAG_MAXIMUN_LINES_FILTER, false);
        } else {
            setFilterData(DanmakuFilters.TAG_MAXIMUN_LINES_FILTER, pairs, false);
        }
        mGlobalFlagValues.updateFilterFlag();
        notifyConfigureChanged(DanmakuConfigTag.MAXIMUN_LINES, pairs);
        return this;
    }

    @Deprecated
    public DanmakuContext setOverlapping(Map<Integer, Boolean> pairs) {
        return preventOverlapping(pairs);
    }

    /**
     * 设置防弹幕重�
     * @param pairs map<K,V> 设置null恢复默认设置,默认为允许重�
     * K = (BaseDanmaku.TYPE_SCROLL_RL|BaseDanmaku.TYPE_SCROLL_LR|BaseDanmaku.TYPE_FIX_TOP|BaseDanmaku.TYPE_FIX_BOTTOM)
     * V = true|false 是否重�
     * @return
     */
    public DanmakuContext preventOverlapping(Map<Integer, Boolean> pairs) {
        mIsPreventOverlappingEnabled = (pairs != null);
        if (pairs == null) {
            mDanmakuFilters.unregisterFilter(DanmakuFilters.TAG_OVERLAPPING_FILTER, false);
        } else {
            setFilterData(DanmakuFilters.TAG_OVERLAPPING_FILTER, pairs, false);
        }
        mGlobalFlagValues.updateFilterFlag();
        notifyConfigureChanged(DanmakuConfigTag.OVERLAPPING_ENABLE, pairs);
        return this;
    }

    public boolean isMaxLinesLimited() {
        return mIsMaxLinesLimited;
    }

    public boolean isPreventOverlappingEnabled() {
        return mIsPreventOverlappingEnabled;
    }

    /**
     * 设置缓存绘制填充器，默认使用{@link SimpleTextCacheStuffer}只支持纯文字显示, 如果需要图文混排请设置{@link SpannedCacheStuffer}
     * 如果需要定制其他样式请扩展{@link SimpleTextCacheStuffer}|{@link SpannedCacheStuffer}
     * @param cacheStuffer
     */
    public DanmakuContext setCacheStuffer(BaseCacheStuffer cacheStuffer) {
        this.mCacheStuffer = cacheStuffer;
        if (this.mCacheStuffer != null) {
            mDisplayer.setCacheStuffer(this.mCacheStuffer);
        }
        return this;
    }

    public interface ConfigChangedCallback {
        public boolean onDanmakuConfigChanged(DanmakuContext config, DanmakuConfigTag tag,
                Object... value);
    }

    public void registerConfigChangedCallback(ConfigChangedCallback listener) {
        if (listener == null || mCallbackList == null) {
            mCallbackList = Collections.synchronizedList(new ArrayList<WeakReference<ConfigChangedCallback>>());
        }
        for (WeakReference<ConfigChangedCallback> configReferer : mCallbackList) {
            if (listener.equals(configReferer.get())) {
                return;
            }
        }
        mCallbackList.add(new WeakReference<ConfigChangedCallback>(listener));
    }

    public void unregisterConfigChangedCallback(ConfigChangedCallback listener) {
        if (listener == null || mCallbackList == null)
            return;
        for (WeakReference<ConfigChangedCallback> configReferer : mCallbackList) {
            if (listener.equals(configReferer.get())) {
                mCallbackList.remove(listener);
                return;
            }
        }
    }

    public void unregisterAllConfigChangedCallbacks() {
        if (mCallbackList != null) {
            mCallbackList.clear();
            mCallbackList = null;
        }
    }

    private void notifyConfigureChanged(DanmakuConfigTag tag, Object... values) {
        if (mCallbackList != null) {
            for (WeakReference<ConfigChangedCallback> configReferer : mCallbackList) {
                ConfigChangedCallback cb = configReferer.get();
                if (cb != null) {
                    cb.onDanmakuConfigChanged(this, tag, values);
                }
            }
        }
    }

}
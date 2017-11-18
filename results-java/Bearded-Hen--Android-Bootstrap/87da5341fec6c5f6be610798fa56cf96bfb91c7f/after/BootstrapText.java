package com.beardedhen.androidbootstrap.support;

import android.content.Context;
import android.text.SpannableString;
import android.text.Spanned;

import com.beardedhen.androidbootstrap.font.AwesomeTypefaceSpan;
import com.beardedhen.androidbootstrap.font.FontIcon;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Bootstrap Text provides a Builder class, which allows convenient construction of SpannableStrings.
 * Currently regular text and FontAwesome icons can be added.
 */
public class BootstrapText extends SpannableString implements Serializable {

    private BootstrapText(CharSequence source) {
        super(source);
    }

    /**
     * This class should be used to construct BootstrapText instances. Text is appended to itself
     * in the order in which it was added.
     */
    public static class Builder {

        private final StringBuilder sb;
        private final Context context;

        private final Map<Integer, FontIcon> fontIndicesMap;

        public Builder(Context context) {
            fontIndicesMap = new HashMap<>();
            sb = new StringBuilder();
            this.context = context.getApplicationContext();
        }

        /**
         * Appends a regular piece of text to the BootstrapText under construction.
         *
         * @param text a regular piece of text
         * @return the updated builder instance
         */
        public Builder addText(CharSequence text) {
            sb.append(text);
            return this;
        }

        /**
         * Appends a FontIcon to the BootstrapText under construction
         * @param fontIcon a font icon
         * @return the updated builder instance
         */
        public Builder addIcon(FontIcon fontIcon) {
            sb.append(fontIcon.character());
            fontIndicesMap.put(sb.length(), fontIcon);
            return this;
        }

        /**
         * @return a new instance of BootstrapText, constructed according to Builder arguments.
         */
        public BootstrapText build() {
            BootstrapText bootstrapText = new BootstrapText(sb.toString());

            for (Map.Entry<Integer, FontIcon> entry : fontIndicesMap.entrySet()) {
                int index = entry.getKey();

                AwesomeTypefaceSpan span = new AwesomeTypefaceSpan(context, entry.getValue());
                bootstrapText.setSpan(span, index - 1, index, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            }
            return bootstrapText;
        }
    }

}
package im.actor.messenger.app.fragment.chat.messages;

import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.URLSpan;
import android.text.util.Linkify;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import im.actor.core.entity.Message;
import im.actor.core.entity.PeerType;
import im.actor.core.entity.content.TextContent;
import im.actor.core.viewmodel.UserVM;
import im.actor.messenger.R;
import im.actor.messenger.app.view.MentionSpan;
import im.actor.messenger.app.view.emoji.SmileProcessor;
import im.actor.messenger.app.view.markdown.AndroidMarkdown;
import im.actor.runtime.generic.mvvm.ListProcessor;

import static im.actor.messenger.app.core.Core.messenger;
import static im.actor.messenger.app.core.Core.myUid;
import static im.actor.messenger.app.core.Core.users;
import static im.actor.messenger.app.view.emoji.SmileProcessor.emoji;

public class ChatListProcessor implements ListProcessor<Message> {

    private HashMap<Long, PreprocessedTextData> preprocessedTexts = new HashMap<Long, PreprocessedTextData>();

    private MessagesFragment fragment;
    private boolean isGroup;
    private int[] colors;

    private Pattern peoplePattern;
    private Pattern mobileInvitePattern;
    private Pattern invitePattern;

    public ChatListProcessor(MessagesFragment fragment) {
        this.fragment = fragment;

        isGroup = fragment.getPeer().getPeerType() == PeerType.GROUP;

        colors = new int[]{
                fragment.getResources().getColor(R.color.placeholder_0),
                fragment.getResources().getColor(R.color.placeholder_1),
                fragment.getResources().getColor(R.color.placeholder_2),
                fragment.getResources().getColor(R.color.placeholder_3),
                fragment.getResources().getColor(R.color.placeholder_4),
                fragment.getResources().getColor(R.color.placeholder_5),
                fragment.getResources().getColor(R.color.placeholder_6),
        };
    }

    @Nullable
    @Override
    public Object process(@NotNull List<Message> items, @Nullable Object previous) {

        // Init tools
        if (mobileInvitePattern == null) {
            mobileInvitePattern = Pattern.compile("(actor:\\\\/\\\\/)(invite\\\\?token=)([0-9-a-z]{1,64})");
        }
        if (invitePattern == null) {
            invitePattern = Pattern.compile("(https:\\/\\/)(quit\\.email\\/join\\/)([0-9-a-z]{1,64})");
        }
        if (peoplePattern == null) {
            peoplePattern = Pattern.compile("(people:\\\\/\\\\/)([0-9]{1,20})");
        }

        ArrayList<PreprocessedData> preprocessedDatas = new ArrayList<PreprocessedData>();

        for (Message msg : items) {
            // Preprocess message

            // Assume user is cached
            messenger().getUser(msg.getSenderId());

            // Process Content
            if (msg.getContent() instanceof TextContent) {
                if (!preprocessedTexts.containsKey(msg.getRid())) {
                    TextContent text = (TextContent) msg.getContent();
                    Spannable spannableString = new SpannableString(text.getText());
                    boolean hasSpannable = false;

                    // Wait Emoji to load
                    emoji().waitForEmoji();

                    // Process markdown
                    Spannable markdown = AndroidMarkdown.processText(text.getText());
                    if (markdown != null) {
                        spannableString = markdown;
                        hasSpannable = true;
                    }

                    // Process links
                    if (Linkify.addLinks(spannableString, Linkify.EMAIL_ADDRESSES | Linkify.PHONE_NUMBERS | Linkify.WEB_URLS)) {
                        hasSpannable = true;
                    }
                    if (fixLinkifyCustomLinks(spannableString, mobileInvitePattern, false)) {
                        hasSpannable = true;
                    }
                    if (fixLinkifyCustomLinks(spannableString, invitePattern, false)) {
                        hasSpannable = true;
                    }
                    if (fixLinkifyCustomLinks(spannableString, peoplePattern, true)) {
                        hasSpannable = true;
                    }

                    // Append Sender name for groups
                    if (isGroup && msg.getSenderId() != myUid()) {

                        String name;
                        UserVM userModel = users().get(msg.getSenderId());
                        if (userModel != null) {
                            name = userModel.getName().get();
                        } else {
                            name = "???";
                        }

                        SpannableStringBuilder builder = new SpannableStringBuilder();
                        builder.append(name);
                        builder.setSpan(new ForegroundColorSpan(colors[Math.abs(msg.getSenderId()) % colors.length]), 0, name.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                        builder.append("\n");
                        spannableString = builder.append(spannableString);
                        hasSpannable = true;
                    }

                    // Process Emoji
                    if (SmileProcessor.containsEmoji(spannableString)) {
                        spannableString = emoji().processEmojiCompatMutable(spannableString, SmileProcessor.CONFIGURATION_BUBBLES);
                        hasSpannable = true;
                    }

                    preprocessedTexts.put(msg.getRid(), new PreprocessedTextData(text.getText(),
                            hasSpannable ? spannableString : null));
                }
                preprocessedDatas.add(preprocessedTexts.get(msg.getRid()));
            } else {
                // Nothing to do yet
                preprocessedDatas.add(null);
            }
        }

        return new PreprocessedList(preprocessedDatas.toArray(new PreprocessedData[preprocessedDatas.size()]));
    }

    private boolean fixLinkifyCustomLinks(Spannable spannable, Pattern p, boolean isMention) {
        Matcher m = p.matcher(spannable.toString());
        boolean res = false;
        while (m.find()) {
            URLSpan span = isMention ? new MentionSpan(m.group(), false) : new URLSpan(m.group());
            spannable.setSpan(span, m.start(), m.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            res = true;
        }
        return res;
    }
}
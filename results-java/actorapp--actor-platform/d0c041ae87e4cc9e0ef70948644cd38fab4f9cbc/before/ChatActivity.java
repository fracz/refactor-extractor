package im.actor.messenger.app.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.droidkit.mvvm.ui.Listener;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;

import im.actor.messenger.BuildConfig;
import im.actor.messenger.R;
import im.actor.messenger.app.base.BaseBarActivity;
import im.actor.messenger.app.fragment.chat.MessagesFragment;
import im.actor.messenger.app.intents.Intents;
import im.actor.messenger.app.view.AvatarDrawable;
import im.actor.messenger.app.view.AvatarView;
import im.actor.messenger.app.view.Formatter;
import im.actor.messenger.app.view.KeyboardHelper;
import im.actor.messenger.app.view.TintImageView;
import im.actor.messenger.app.view.TypingDrawable;
import im.actor.messenger.core.AppContext;
import im.actor.messenger.settings.ChatSettings;
import im.actor.messenger.storage.scheme.groups.GroupState;
import im.actor.messenger.util.RandomUtil;
import im.actor.messenger.util.io.IOUtils;
import im.actor.model.Messenger;
import im.actor.model.entity.Peer;
import im.actor.model.entity.PeerType;
import im.actor.model.viewmodel.GroupVM;
import im.actor.model.viewmodel.UserVM;

import static im.actor.messenger.app.view.ViewUtils.goneView;
import static im.actor.messenger.app.view.ViewUtils.hideView;
import static im.actor.messenger.app.view.ViewUtils.showView;
import static im.actor.messenger.core.Core.groups;
import static im.actor.messenger.core.Core.messenger;
import static im.actor.messenger.core.Core.users;

public class ChatActivity extends BaseBarActivity implements Listener<GroupState> {

    private static final int REQUEST_GALLERY = 0;
    private static final int REQUEST_PHOTO = 1;
    private static final int REQUEST_VIDEO = 2;
    private static final int REQUEST_DOC = 3;
    private static final int REQUEST_LOCATION = 4;

    private Peer peer;

    private Messenger messenger;

    private EditText messageBody;
    private TintImageView sendButton;
    private ImageButton attachButton;
    private View kicked;

    // Action bar
    private View barView;
    private AvatarView barAvatar;
    private TextView barTitle;
    private View barSubtitleContainer;
    private TextView barSubtitle;
    private View barTypingContainer;
    private ImageView barTypingIcon;
    private TextView barTyping;

    private String fileName;

    private KeyboardHelper keyboardUtils;

    private boolean isTypingDisabled = false;

    private boolean isCompose = false;

    @Override
    public void onCreate(Bundle saveInstance) {
        super.onCreate(saveInstance);

        keyboardUtils = new KeyboardHelper(this);

        peer = Peer.fromUid(getIntent().getExtras().getLong(Intents.EXTRA_CHAT_PEER));

        isCompose = saveInstance == null && getIntent().getExtras().getBoolean(Intents.EXTRA_CHAT_COMPOSE, false);

        messenger = messenger();

        // Init action bar

        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayUseLogoEnabled(false);

        // Action bar header

        barView = LayoutInflater.from(this).inflate(R.layout.bar_conversation, null);
        barTitle = (TextView) barView.findViewById(R.id.title);
        barSubtitleContainer = barView.findViewById(R.id.subtitleContainer);
        barTypingIcon = (ImageView) barView.findViewById(R.id.typingImage);
        barTypingIcon.setImageDrawable(new TypingDrawable());
        barTyping = (TextView) barView.findViewById(R.id.typing);
        barSubtitle = (TextView) barView.findViewById(R.id.subtitle);
        barTypingContainer = barView.findViewById(R.id.typingContainer);
        barTypingContainer.setVisibility(View.INVISIBLE);
        barAvatar = (AvatarView) barView.findViewById(R.id.avatarPreview);
        ActionBar.LayoutParams layout = new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT);
        getSupportActionBar().setCustomView(barView, layout);
        barView.findViewById(R.id.titleContainer).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (peer.getPeerType() == PeerType.PRIVATE) {
                    startActivity(Intents.openProfile(peer.getPeerId(), ChatActivity.this));
                } else if (peer.getPeerType() == PeerType.GROUP) {
                    // startActivity(Intents.openGroup(chatId, ChatActivity.this));
                }
            }
        });

        // Init view

        setContentView(R.layout.activity_dialog);

        getWindow().setBackgroundDrawable(null);

        getSupportFragmentManager().beginTransaction()
                .add(R.id.messagesFragment, MessagesFragment.create(peer))
                .commit();

        messageBody = (EditText) findViewById(R.id.et_message);
        messageBody.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (after > count && !isTypingDisabled) {
                    messenger.onTyping(peer);
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    sendButton.setTint(getResources().getColor(R.color.conv_send_enabled));
                    sendButton.setEnabled(true);
                } else {
                    sendButton.setTint(getResources().getColor(R.color.conv_send_disabled));
                    sendButton.setEnabled(false);
                }
            }
        });
        messageBody.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keycode, KeyEvent keyEvent) {
                if (ChatSettings.getInstance().isSendByEnter()) {
                    if (keyEvent.getAction() == KeyEvent.ACTION_DOWN && keycode == KeyEvent.KEYCODE_ENTER) {
                        sendMessage();
                        return true;
                    }
                }
                return false;
            }
        });
        messageBody.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_SEND) {
                    sendMessage();
                    return true;
                }
                if (i == EditorInfo.IME_ACTION_DONE) {
                    sendMessage();
                    return true;
                }
                if (ChatSettings.getInstance().isSendByEnter()) {
                    if (keyEvent != null && i == EditorInfo.IME_NULL && keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                        sendMessage();
                        return true;
                    }
                }
                return false;
            }
        });

        kicked = findViewById(R.id.kickedFromChat);
        kicked.setVisibility(View.GONE);

        sendButton = (TintImageView) findViewById(R.id.ib_send);
        sendButton.setResource(R.drawable.conv_send);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        attachButton = (ImageButton) findViewById(R.id.ib_attach);
        attachButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Context wrapper = new ContextThemeWrapper(ChatActivity.this, R.style.AttachPopupTheme);
                PopupMenu popup = new PopupMenu(wrapper, findViewById(R.id.attachAnchor));

                try {
                    Field[] fields = popup.getClass().getDeclaredFields();
                    for (Field field : fields) {
                        if ("mPopup".equals(field.getName())) {
                            field.setAccessible(true);
                            Object menuPopupHelper = field.get(popup);
                            Class<?> classPopupHelper = Class.forName(menuPopupHelper
                                    .getClass().getName());
                            Method setForceIcons = classPopupHelper.getMethod(
                                    "setForceShowIcon", boolean.class);
                            setForceIcons.invoke(menuPopupHelper, true);
                            break;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (BuildConfig.ENABLE_CHROME) {
                    popup.getMenuInflater().inflate(R.menu.attach_popup_chrome, popup.getMenu());
                } else {
                    popup.getMenuInflater().inflate(R.menu.attach_popup, popup.getMenu());
                }

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if (item.getItemId() == R.id.gallery) {
                            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            intent.setType("image/* video/*");
                            startActivityForResult(intent, REQUEST_GALLERY);
                            return true;
                        } else if (item.getItemId() == R.id.takePhoto) {
                            File externalFile = getExternalFilesDir(null);
                            if (externalFile == null) {
                                Toast.makeText(ChatActivity.this, R.string.toast_no_sdcard, Toast.LENGTH_LONG).show();
                                return true;
                            }
                            String externalPath = externalFile.getAbsolutePath();
                            new File(externalPath + "/actor/").mkdirs();

                            fileName = externalPath + "/actor/capture_" + RandomUtil.randomId() + ".jpg";
                            startActivityForResult(
                                    new Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                                            .putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(fileName))),
                                    REQUEST_PHOTO);
                        } else if (item.getItemId() == R.id.takeVideo) {

                            File externalFile = getExternalFilesDir(null);
                            if (externalFile == null) {
                                Toast.makeText(ChatActivity.this, R.string.toast_no_sdcard, Toast.LENGTH_LONG).show();
                                return true;
                            }
                            String externalPath = externalFile.getAbsolutePath();
                            new File(externalPath + "/actor/").mkdirs();

                            fileName = externalPath + "/actor/capture_" + RandomUtil.randomId() + ".jpg";

                            Intent i = new Intent(MediaStore.ACTION_VIDEO_CAPTURE)
                                    .putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(fileName)));
                            startActivityForResult(i, REQUEST_VIDEO);
                            return true;
                        } else if (item.getItemId() == R.id.file) {
                            startActivityForResult(Intents.pickFile(ChatActivity.this), REQUEST_DOC);
                        } else if (item.getItemId() == R.id.location) {
                            startActivityForResult(com.droidkit.pickers.Intents.pickLocation(ChatActivity.this), REQUEST_LOCATION);
                        }
                        return false;
                    }
                });
                popup.show();

            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        if (peer.getPeerType() == PeerType.PRIVATE) {
            final UserVM user = users().get(peer.getPeerId());
            if (user == null) {
                finish();
                return;
            }

            // TODO: Dynamically update avatar drawable after name change
            barAvatar.setEmptyDrawable(AvatarDrawable.create(user, 18, this));
            bind(barAvatar, user.getAvatar());
            bind(barTitle, user.getName());
            bind(barSubtitle, barSubtitleContainer, user);
            bind(barTyping, barTypingContainer, barTitle, messenger().getTyping(user.getId()));
        } else if (peer.getPeerType() == PeerType.GROUP) {
            GroupVM group = groups().get(peer.getPeerId());
            if (group == null) {
                finish();
                return;
            }

            // TODO: Dynamically update avatar drawable after title change
            barAvatar.setEmptyDrawable(AvatarDrawable.create(group, 18, this));
            bind(barAvatar, group.getAvatar());
            bind(barTitle, group.getName());

//            getBinder().bindText(barTitle, groupInfo.getTitleModel());
//
//            barSubtitle.setVisibility(View.VISIBLE);
//
//            getBinder().bind(groupInfo.getStateModel(), this);
//
//            getBinder().bind(TypingModel.groupChatTyping(chatId), new Listener<int[]>() {
//                @Override
//                public void onUpdated(int[] ints) {
//                    updateGroupStatus(groupInfo.getOnlineModel().getValue(), ints);
//                }
//            });
//            getBinder().bind(groupInfo.getOnlineModel(), new Listener<int[]>() {
//                @Override
//                public void onUpdated(int[] ints) {
//                    updateGroupStatus(ints, TypingModel.groupChatTyping(chatId).getValue());
//                }
//            });
        }

        if (isCompose) {
            messageBody.requestFocus();
            keyboardUtils.setImeVisibility(messageBody, true);
        }
        isCompose = false;

        messenger().onConversationOpen(peer);

        isTypingDisabled = true;
        String text = messenger().loadDraft(peer);
        if (text != null) {
            messageBody.setText(text);
        } else {
            messageBody.setText("");
        }
        isTypingDisabled = false;
    }

    private void updateGroupStatus(int[] onlines, int[] typings) {
        if (typings.length > 0) {
            barTyping.setText(Formatter.formatTyping(typings));
            showView(barTypingContainer);
            hideView(barSubtitle);
        } else {
            if (onlines.length == 1) {
                SpannableStringBuilder builder = new SpannableStringBuilder(
                        getString(R.string.chat_group_members).replace("{0}", onlines[0] + ""));
                builder.setSpan(new ForegroundColorSpan(0xB7ffffff), 0, builder.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                barSubtitle.setText(builder);
            } else {
                SpannableStringBuilder builder = new SpannableStringBuilder(getString(R.string.chat_group_members).replace("{0}", onlines[0] + "") + ", ");
                builder.setSpan(new ForegroundColorSpan(0xB7ffffff), 0, builder.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                builder.append(getString(R.string.chat_group_members_online).replace("{0}", onlines[1] + ""));
                barSubtitle.setText(builder);
            }
            hideView(barTypingContainer);
            showView(barSubtitle);
        }
    }

    private void sendMessage() {
        final String text = messageBody.getText().toString().trim();
        messageBody.setText("");
        if (text.length() == 0) {
            return;
        }

        // Hack for full screen mode
        if (getResources().getDisplayMetrics().heightPixels <=
                getResources().getDisplayMetrics().widthPixels) {
            keyboardUtils.setImeVisibility(messageBody, false);
        }

        messenger().sendMessage(peer, text);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_GALLERY) {
                if (data.getData() != null) {
                    sendUri(data.getData());
                }
            } else if (requestCode == REQUEST_PHOTO) {
                // MessageDeliveryActor.messageSender().sendPhoto(chatType, chatId, fileName);
            } else if (requestCode == REQUEST_VIDEO) {
                // MessageDeliveryActor.messageSender().sendVideo(chatType, chatId, fileName);
            } else if (requestCode == REQUEST_DOC) {
                if (data.getData() != null) {
                    sendUri(data.getData());
                } else if (data.hasExtra("picked")) {
                    ArrayList<String> files = data.getStringArrayListExtra("picked");
                    if (files != null) {
                        for (String s : files) {
//                            MessageDeliveryActor.messageSender().sendDocument(chatType, chatId, s,
//                                    new File(s).getName());
                        }
                    }
                }
            }
        }
    }

    private void sendUri(final Uri uri) {
        new AsyncTask<Void, Void, Void>() {

            private ProgressDialog progressDialog;

            @Override
            protected void onPreExecute() {
                progressDialog = new ProgressDialog(ChatActivity.this);
                progressDialog.setMessage(getString(R.string.pick_downloading));
                progressDialog.setCancelable(false);
                progressDialog.show();
            }

            @Override
            protected Void doInBackground(Void... params) {
                String[] filePathColumn = {MediaStore.Images.Media.DATA, MediaStore.Video.Media.MIME_TYPE,
                        MediaStore.Video.Media.TITLE};
                Cursor cursor = getContentResolver().query(uri, filePathColumn, null, null, null);
                cursor.moveToFirst();
                String picturePath = cursor.getString(cursor.getColumnIndex(filePathColumn[0]));
                String mimeType = cursor.getString(cursor.getColumnIndex(filePathColumn[1]));
                String fileName = cursor.getString(cursor.getColumnIndex(filePathColumn[2]));
                if (mimeType == null) {
                    mimeType = "?/?";
                }
                cursor.close();

                if (picturePath == null || !uri.getScheme().equals("file")) {
                    File externalFile = AppContext.getContext().getExternalFilesDir(null);
                    if (externalFile == null) {
                        return null;
                    }
                    String externalPath = externalFile.getAbsolutePath();

                    File dest = new File(externalPath + "/Actor/");
                    dest.mkdirs();

                    File outputFile = new File(dest, "upload_" + RandomUtil.randomId() + ".jpg");
                    picturePath = outputFile.getAbsolutePath();

                    try {
                        IOUtils.copy(getContentResolver().openInputStream(uri), new File(picturePath));
                    } catch (IOException e) {
                        e.printStackTrace();
                        return null;
                    }
                }

//                if (mimeType.startsWith("video/")) {
//                    MessageDeliveryActor.messageSender().sendVideo(chatType, chatId, picturePath);
//                } else if (mimeType.startsWith("image/")) {
//                    MessageDeliveryActor.messageSender().sendPhoto(chatType, chatId, picturePath);
//                } else {
//                    MessageDeliveryActor.messageSender().sendDocument(chatType, chatId, picturePath,
//                            fileName);
//                }

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                progressDialog.dismiss();
            }
        }.execute();
    }

    @Override
    public void onUpdated(GroupState groupState) {
        if (groupState == GroupState.JOINED) {
            goneView(kicked, false);
        } else if (groupState == GroupState.KICKED) {
            showView(kicked, false);
        } else {
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chat_menu, menu);

        if (peer.getPeerType() == PeerType.PRIVATE) {
            menu.findItem(R.id.contact).setVisible(true);
            TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            menu.findItem(R.id.call).setVisible(tm.getPhoneType() != TelephonyManager.PHONE_TYPE_NONE);
        } else {
            menu.findItem(R.id.contact).setVisible(false);
            menu.findItem(R.id.call).setVisible(false);
        }

        if (peer.getPeerType() == PeerType.GROUP) {
            menu.findItem(R.id.groupInfo).setVisible(true);
            menu.findItem(R.id.leaveGroup).setVisible(true);
        } else {
            menu.findItem(R.id.groupInfo).setVisible(false);
            menu.findItem(R.id.leaveGroup).setVisible(false);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.clear:
                new AlertDialog.Builder(this)
                        .setMessage(R.string.alert_delete_all_messages_text)
                        .setPositiveButton(R.string.alert_delete_all_messages_yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // ChatActionsActor.actions().clearChat(chatType, chatId);
                            }
                        })
                        .setNegativeButton(R.string.dialog_cancel, null)
                        .show()
                        .setCanceledOnTouchOutside(true);
                break;
            case R.id.leaveGroup:
                new AlertDialog.Builder(this)
                        .setMessage(R.string.alert_delete_group_title)
                        .setPositiveButton(R.string.alert_delete_group_yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog2, int which) {
                                // groupUpdates().leaveChat(chatId);
                                finish();
                            }
                        })
                        .setNegativeButton(R.string.dialog_cancel, null)
                        .show()
                        .setCanceledOnTouchOutside(true);
                break;
            case R.id.contact:
                // startActivity(Intents.openProfile(chatId, ChatActivity.this));
                break;
            case R.id.groupInfo:
                // startActivity(Intents.openGroup(chatId, ChatActivity.this));
                break;
            case R.id.files:
                // startActivity(Intents.openDocs(chatType, chatId, ChatActivity.this));
                break;
            case R.id.call:
//                UserModel user = users().get(chatId);
//                startActivity(new Intent(Intent.ACTION_DIAL).setData(Uri.parse("tel:+" + user.getPhone())));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPause() {
        super.onPause();

        messenger.saveDraft(peer, messageBody.getText().toString());
        messenger.onConversationClosed(peer);
    }
}
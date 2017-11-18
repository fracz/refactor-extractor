/*
 *  Copyright 2010 Kevin Gaudin
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.acra.sender;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import org.acra.ACRA;
import org.acra.ACRAConstants;
import org.acra.ReportField;
import org.acra.annotation.ReportsCrashes;
import org.acra.attachment.DefaultAttachmentProvider;
import org.acra.collections.ImmutableSet;
import org.acra.collector.CrashReportData;
import org.acra.config.ACRAConfiguration;
import org.acra.model.Element;
import org.acra.util.IOUtils;
import org.acra.util.InstanceCreator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.acra.ACRA.LOG_TAG;

/**
 * Send reports through an email intent.
 * <p>
 * The user will be asked to chose his preferred email client if no default is set. Included report fields can be defined using
 * {@link org.acra.annotation.ReportsCrashes#customReportContent()}. Crash receiving mailbox has to be
 * defined with {@link ReportsCrashes#mailTo()}.
 */
@SuppressWarnings("WeakerAccess")
public class EmailIntentSender implements ReportSender {

    private final ACRAConfiguration config;

    public EmailIntentSender(@NonNull ACRAConfiguration config) {
        this.config = config;
    }

    @Override
    public void send(@NonNull Context context, @NonNull CrashReportData errorContent) throws ReportSenderException {
        final PackageManager pm = context.getPackageManager();

        final String subject = buildSubject(context);
        final String body = buildBody(errorContent);
        final ArrayList<Uri> attachments = new ArrayList<Uri>();
        final boolean contentAttached = fillAttachmentList(context, errorContent, attachments);

        //we have to resolve with sendto, because send is supported by non-email apps
        final Intent resolveIntent = buildResolveIntent(subject, body);
        final ComponentName resolveActivity = resolveIntent.resolveActivity(pm);
        if (resolveActivity != null) {
            if (attachments.size() == 0) {
                //no attachments, send directly
                context.startActivity(resolveIntent);
            } else {
                final Intent attachmentIntent = buildAttachmentIntent(subject, body, attachments, contentAttached);
                final List<Intent> initialIntents = buildInitialIntents(pm, resolveIntent, attachmentIntent);
                final String packageName = getPackageName(resolveActivity, initialIntents);
                attachmentIntent.setPackage(packageName);
                if (packageName == null) {
                    //let user choose email client
                    for (Intent intent : initialIntents) {
                        grantPermission(context, intent, intent.getPackage(), attachments);
                    }
                    showChooser(context, initialIntents);
                } else if (attachmentIntent.resolveActivity(pm) != null) {
                    //use default email client
                    grantPermission(context, attachmentIntent, packageName, attachments);
                    context.startActivity(attachmentIntent);
                } else {
                    ACRA.log.w(LOG_TAG, "No email client supporting attachments found. Attachments will be ignored");
                    context.startActivity(resolveIntent);
                }
            }
        } else {
            throw new ReportSenderException("No email client found");
        }
    }

    /**
     * Finds the package name of the default email client supporting attachments
     *
     * @param resolveActivity the resolved activity
     * @param initialIntents  a list of intents to be used when
     * @return package name of the default email client, or null if more than one app match
     */
    @Nullable
    private String getPackageName(@NonNull ComponentName resolveActivity, @NonNull List<Intent> initialIntents) {
        String packageName = resolveActivity.getPackageName();
        if (packageName.equals("android")) {
            //multiple activities support the intent and no default is set
            if (initialIntents.size() > 1) {
                packageName = null;
            } else if (initialIntents.size() == 1) {
                //only one of them supports attachments, use that one
                packageName = initialIntents.get(0).getPackage();
            }
        }
        return packageName;
    }

    /**
     * Builds an email intent with attachments
     *
     * @param subject         the message subject
     * @param body            the message body
     * @param attachments     the attachments
     * @param contentAttached if the body is already contained in the attachments
     * @return email intent
     */
    @NonNull
    protected Intent buildAttachmentIntent(@NonNull String subject, @NonNull String body, @NonNull ArrayList<Uri> attachments, boolean contentAttached) {
        final Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{config.mailTo()});
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.setType("message/rfc822");
        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, attachments);
        if (!contentAttached) intent.putExtra(Intent.EXTRA_TEXT, body);
        return intent;
    }

    /**
     * Builds an intent used to resolve email clients and to send reports without attachments or as fallback if no attachments are supported
     *
     * @param subject the message subject
     * @param body    the message body
     * @return email intent
     */
    @NonNull
    protected Intent buildResolveIntent(@NonNull String subject, @NonNull String body) {
        final Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.fromParts("mailto", config.mailTo(), null));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, body);
        return intent;
    }

    @NonNull
    private List<Intent> buildInitialIntents(@NonNull PackageManager pm, @NonNull Intent resolveIntent, @NonNull Intent emailIntent) {
        final List<ResolveInfo> resolveInfoList = pm.queryIntentActivities(resolveIntent, PackageManager.MATCH_DEFAULT_ONLY);
        final List<Intent> initialIntents = new ArrayList<Intent>();
        for (ResolveInfo info : resolveInfoList) {
            final Intent packageSpecificIntent = new Intent(emailIntent);
            packageSpecificIntent.setPackage(info.activityInfo.packageName);
            if (packageSpecificIntent.resolveActivity(pm) != null) {
                initialIntents.add(packageSpecificIntent);
            }
        }
        return initialIntents;
    }

    private void showChooser(@NonNull Context context, @NonNull List<Intent> initialIntents) {
        final Intent chooser = new Intent(Intent.ACTION_CHOOSER);
        chooser.putExtra(Intent.EXTRA_INTENT, initialIntents.remove(0));
        chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, initialIntents.toArray(new Intent[initialIntents.size()]));
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(chooser);
    }

    private void grantPermission(@NonNull Context context, Intent intent, String packageName, List<Uri> attachments) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            //flags do not work on extras prior to lollipop, so we have to grant read permissions manually
            for (Uri uri : attachments) {
                context.grantUriPermission(packageName, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
        }
    }

    /**
     * Creates the message subject
     *
     * @param context a context
     * @return the message subject
     */
    @NonNull
    protected String buildSubject(@NonNull Context context) {
        return context.getPackageName() + " Crash Report";
    }

    /**
     * Creates the message body
     *
     * @param errorContent the report content
     * @return the message body
     */
    @NonNull
    protected String buildBody(@NonNull CrashReportData errorContent) {
        Set<ReportField> fields = config.reportContent();
        if (fields.isEmpty()) {
            fields = new ImmutableSet<ReportField>(ACRAConstants.DEFAULT_MAIL_REPORT_FIELDS);
        }

        final StringBuilder builder = new StringBuilder();
        for (ReportField field : fields) {
            builder.append(field.toString()).append('=');
            final Element value = errorContent.get(field);
            if (value != null) {
                builder.append(TextUtils.join("\n\t", value.flatten()));
            }
            builder.append('\n');
        }
        return builder.toString();
    }

    /**
     * Adds all attachment uris into the given list
     *
     * @param context      a context
     * @param errorContent the report content
     * @param attachments  the target list
     * @return if the attachments contain the content
     */
    protected boolean fillAttachmentList(@NonNull Context context, @NonNull CrashReportData errorContent, @NonNull List<Uri> attachments) {
        final InstanceCreator instanceCreator = new InstanceCreator();
        attachments.addAll(instanceCreator.create(config.attachmentUriProvider(), new DefaultAttachmentProvider()).getAttachments(context, config));
        if (config.reportAsFile()) {
            final Uri report = createAttachmentFromString(context, "ACRA-report" + ACRAConstants.REPORTFILE_EXTENSION, errorContent.toJSON().toString());
            if (report != null) {
                attachments.add(report);
                return true;
            }
        }
        return false;
    }

    /**
     * Creates a temporary file with the given content and name, to be used as an email attachment
     *
     * @param context a context
     * @param name    the name
     * @param content the content
     * @return a content uri for the file
     */
    @Nullable
    protected Uri createAttachmentFromString(@NonNull Context context, @NonNull String name, @NonNull String content) {
        final File cache = new File(context.getCacheDir(), name);
        try {
            IOUtils.writeStringToFile(cache, content);
            return Uri.parse("content://" + context.getPackageName() + ".acra/root" + cache.getPath());
        } catch (IOException ignored) {
        }
        return null;
    }
}
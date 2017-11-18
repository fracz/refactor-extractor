/**
 * Copyright (C) 2012 Moxie Marlinpsike
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.thoughtcrime.securesms.database.model;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;

import org.thoughtcrime.securesms.database.MmsSmsColumns;
import org.thoughtcrime.securesms.database.SmsDatabase;
import org.thoughtcrime.securesms.recipients.Recipient;
import org.thoughtcrime.securesms.recipients.Recipients;

/**
 * The base class for message record models that are displayed in
 * conversations, as opposed to models that are displayed in a thread list.
 * Encapsulates the shared data between both SMS and MMS messages.
 *
 * @author Moxie Marlinspike
 *
 */
public abstract class MessageRecord extends DisplayRecord {

  public static final int DELIVERY_STATUS_NONE     = 0;
  public static final int DELIVERY_STATUS_RECEIVED = 1;
  public static final int DELIVERY_STATUS_PENDING  = 2;
  public static final int DELIVERY_STATUS_FAILED   = 3;

  private final Recipient individualRecipient;
  private final long id;
  private final int deliveryStatus;
  private final GroupData groupData;

  public MessageRecord(Context context, long id, String body, Recipients recipients,
                       Recipient individualRecipient,
                       long dateSent, long dateReceived,
                       long threadId, int deliveryStatus,
                       long type, GroupData groupData)
  {
    super(context, body, recipients, dateSent, dateReceived, threadId, type);
    this.id                  = id;
    this.individualRecipient = individualRecipient;
    this.deliveryStatus      = deliveryStatus;
    this.groupData           = groupData;
  }

  public abstract boolean isMms();

  public boolean isFailed() {
    return
        MmsSmsColumns.Types.isFailedMessageType(type) ||
        getDeliveryStatus() == DELIVERY_STATUS_FAILED;
  }

  public boolean isOutgoing() {
    return MmsSmsColumns.Types.isOutgoingMessageType(type);
  }

  public boolean isPending() {
    return MmsSmsColumns.Types.isPendingMessageType(type) || isGroupDeliveryPending();
  }

  public boolean isSecure() {
    return MmsSmsColumns.Types.isSecureType(type);
  }

  @Override
  public SpannableString getDisplayBody() {
    return new SpannableString(getBody());
  }

  public long getId() {
    return id;
  }

  public int getDeliveryStatus() {
    return deliveryStatus;
  }

  public boolean isDelivered() {
    return getDeliveryStatus() == DELIVERY_STATUS_RECEIVED;
  }

  public boolean isStaleKeyExchange() {
    return SmsDatabase.Types.isStaleKeyExchange(type);
  }

  public boolean isProcessedKeyExchange() {
    return SmsDatabase.Types.isProcessedKeyExchange(type);
  }

  public Recipient getIndividualRecipient() {
    return individualRecipient;
  }

  public GroupData getGroupData() {
    return this.groupData;
  }

  protected boolean isGroupDeliveryPending() {
    if (this.groupData != null) {
      return groupData.groupSentCount + groupData.groupSendFailedCount < groupData.groupSize;
    }

    return false;
  }

  protected SpannableString emphasisAdded(String sequence) {
    SpannableString spannable = new SpannableString(sequence);
    spannable.setSpan(new ForegroundColorSpan(context.getResources().getColor(android.R.color.darker_gray)), 0, sequence.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    spannable.setSpan(new StyleSpan(android.graphics.Typeface.ITALIC), 0, sequence.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

    return spannable;
  }

  public static class GroupData {
    public final int groupSize;
    public final int groupSentCount;
    public final int groupSendFailedCount;

    public GroupData(int groupSize, int groupSentCount, int groupSendFailedCount) {
      this.groupSize            = groupSize;
      this.groupSentCount       = groupSentCount;
      this.groupSendFailedCount = groupSendFailedCount;
    }
  }
}
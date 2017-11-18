/**
 * Copyright (C) 2012 Moxie Marlinspike
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
import android.text.SpannableString;

import org.thoughtcrime.securesms.R;
import org.thoughtcrime.securesms.database.MmsDatabase;
import org.thoughtcrime.securesms.mms.Slide;
import org.thoughtcrime.securesms.mms.SlideDeck;
import org.thoughtcrime.securesms.recipients.Recipient;
import org.thoughtcrime.securesms.recipients.Recipients;

/**
 * Represents the message record model for MMS messages that contain
 * media (ie: they've been downloaded).
 *
 * @author Moxie Marlinspike
 *
 */

public class MediaMmsMessageRecord extends MessageRecord {

  private final Context context;
  private final SlideDeck slideDeck;

  public MediaMmsMessageRecord(Context context, long id, Recipients recipients,
                               Recipient individualRecipient, long dateSent, long dateReceived,
                               long threadId, SlideDeck slideDeck, long mailbox,
                               GroupData groupData)
  {
    super(context, id, getBodyFromSlidesIfAvailable(slideDeck), recipients,
          individualRecipient, dateSent, dateReceived,
          threadId, DELIVERY_STATUS_NONE, mailbox,
          groupData);

    this.context   = context.getApplicationContext();
    this.slideDeck = slideDeck;
  }

  public SlideDeck getSlideDeck() {
    return slideDeck;
  }

  @Override
  public boolean isMms() {
    return true;
  }

  @Override
  public SpannableString getDisplayBody() {
    if (MmsDatabase.Types.isDecryptInProgressType(type)) {
      return emphasisAdded(context.getString(R.string.MmsMessageRecord_decrypting_mms_please_wait));
    } else if (MmsDatabase.Types.isFailedDecryptType(type)) {
      return emphasisAdded(context.getString(R.string.MmsMessageRecord_bad_encrypted_mms_message));
    } else if (MmsDatabase.Types.isNoRemoteSessionType(type)) {
      return emphasisAdded(context.getString(R.string.MmsMessageRecord_mms_message_encrypted_for_non_existing_session));
    }

    return super.getDisplayBody();
  }

  private static String getBodyFromSlidesIfAvailable(SlideDeck slideDeck) {
    if (slideDeck == null)
      return "";

    for (Slide slide : slideDeck.getSlides()) {
      if (slide.hasText())
        return slide.getText();
    }

    return "";
  }

}
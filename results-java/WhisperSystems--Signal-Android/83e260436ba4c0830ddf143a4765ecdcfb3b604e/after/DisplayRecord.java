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

import org.thoughtcrime.securesms.database.SmsDatabase;
import org.thoughtcrime.securesms.recipients.Recipients;

/**
 * The base class for all message record models.  Encapsulates basic data
 * shared between ThreadRecord and MessageRecord.
 *
 * @author Moxie Marlinspike
 *
 */

public abstract class DisplayRecord {

  protected final Context context;
  private final Recipients recipients;
  private final long dateSent;
  private final long dateReceived;
  private final long threadId;
  protected final long type;

  private final String body;

  public DisplayRecord(Context context, String body, Recipients recipients, long dateSent,
                       long dateReceived, long threadId, long type)
  {
    this.context      = context.getApplicationContext();
    this.threadId     = threadId;
    this.recipients   = recipients;
    this.dateSent     = dateSent;
    this.dateReceived = dateReceived;
    this.type         = type;
    this.body         = body;
  }

  public String getBody() {
    return body;
  }

  public abstract SpannableString getDisplayBody();

  public Recipients getRecipients() {
    return recipients;
  }

  public long getDateSent() {
    return dateSent;
  }

  public long getDateReceived() {
    return dateReceived;
  }

  public long getThreadId() {
    return threadId;
  }

  public boolean isKeyExchange() {
    return SmsDatabase.Types.isKeyExchangeType(type);
  }
}
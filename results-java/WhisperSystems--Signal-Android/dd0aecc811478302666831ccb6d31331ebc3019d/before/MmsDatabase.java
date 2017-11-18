/**
 * Copyright (C) 2011 Whisper Systems
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
package org.thoughtcrime.securesms.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

import org.thoughtcrime.securesms.R;
import org.thoughtcrime.securesms.contacts.ContactPhotoFactory;
import org.thoughtcrime.securesms.crypto.MasterSecret;
import org.thoughtcrime.securesms.database.model.MediaMmsMessageRecord;
import org.thoughtcrime.securesms.database.model.MessageRecord;
import org.thoughtcrime.securesms.database.model.NotificationMmsMessageRecord;
import org.thoughtcrime.securesms.mms.SlideDeck;
import org.thoughtcrime.securesms.recipients.Recipient;
import org.thoughtcrime.securesms.recipients.RecipientFactory;
import org.thoughtcrime.securesms.recipients.RecipientFormattingException;
import org.thoughtcrime.securesms.recipients.Recipients;
import org.thoughtcrime.securesms.util.Trimmer;
import org.thoughtcrime.securesms.util.Util;

import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.Set;

import ws.com.google.android.mms.InvalidHeaderValueException;
import ws.com.google.android.mms.MmsException;
import ws.com.google.android.mms.pdu.CharacterSets;
import ws.com.google.android.mms.pdu.EncodedStringValue;
import ws.com.google.android.mms.pdu.MultimediaMessagePdu;
import ws.com.google.android.mms.pdu.NotificationInd;
import ws.com.google.android.mms.pdu.PduBody;
import ws.com.google.android.mms.pdu.PduHeaders;
import ws.com.google.android.mms.pdu.RetrieveConf;
import ws.com.google.android.mms.pdu.SendReq;

// XXXX Clean up MMS efficiency:
// 1) We need to be careful about how much memory we're using for parts. SoftRefereences.
// 2) How many queries do we make?  calling getMediaMessageForId() from within an existing query
//    seems wasteful.

public class MmsDatabase extends Database implements MmsSmsColumns {

  public  static final String TABLE_NAME         = "mms";
          static final String DATE_SENT          = "date";
          static final String DATE_RECEIVED      = "date_received";
  public  static final String MESSAGE_BOX        = "msg_box";
  private static final String MESSAGE_ID         = "m_id";
  private static final String SUBJECT            = "sub";
  private static final String SUBJECT_CHARSET    = "sub_cs";
  private static final String CONTENT_TYPE       = "ct_t";
  private static final String CONTENT_LOCATION   = "ct_l";
  private static final String EXPIRY             = "exp";
  private static final String MESSAGE_CLASS      = "m_cls";
  public  static final String MESSAGE_TYPE       = "m_type";
  private static final String MMS_VERSION        = "v";
  private static final String MESSAGE_SIZE       = "m_size";
  private static final String PRIORITY           = "pri";
  private static final String READ_REPORT        = "rr";
  private static final String REPORT_ALLOWED     = "rpt_a";
  private static final String RESPONSE_STATUS    = "resp_st";
  private static final String STATUS             = "st";
  private static final String TRANSACTION_ID     = "tr_id";
  private static final String RETRIEVE_STATUS    = "retr_st";
  private static final String RETRIEVE_TEXT      = "retr_txt";
  private static final String RETRIEVE_TEXT_CS   = "retr_txt_cs";
  private static final String READ_STATUS        = "read_status";
  private static final String CONTENT_CLASS      = "ct_cls";
  private static final String RESPONSE_TEXT      = "resp_txt";
  private static final String DELIVERY_TIME      = "d_tm";
  private static final String DELIVERY_REPORT    = "d_rpt";

  public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" + ID + " INTEGER PRIMARY KEY, "                          +
    THREAD_ID + " INTEGER, " + DATE_SENT + " INTEGER, " + DATE_RECEIVED + " INTEGER, " + MESSAGE_BOX + " INTEGER, " +
    READ + " INTEGER DEFAULT 0, " + MESSAGE_ID + " TEXT, " + SUBJECT + " TEXT, "                +
    SUBJECT_CHARSET + " INTEGER, " + CONTENT_TYPE + " TEXT, " + CONTENT_LOCATION + " TEXT, "    +
    EXPIRY + " INTEGER, " + MESSAGE_CLASS + " TEXT, " + MESSAGE_TYPE + " INTEGER, "             +
    MMS_VERSION + " INTEGER, " + MESSAGE_SIZE + " INTEGER, " + PRIORITY + " INTEGER, "          +
    READ_REPORT + " INTEGER, " + REPORT_ALLOWED + " INTEGER, " + RESPONSE_STATUS + " INTEGER, " +
    STATUS + " INTEGER, " + TRANSACTION_ID + " TEXT, " + RETRIEVE_STATUS + " INTEGER, "         +
    RETRIEVE_TEXT + " TEXT, " + RETRIEVE_TEXT_CS + " INTEGER, " + READ_STATUS + " INTEGER, "    +
    CONTENT_CLASS + " INTEGER, " + RESPONSE_TEXT + " TEXT, " + DELIVERY_TIME + " INTEGER, "     +
    DELIVERY_REPORT + " INTEGER);";

  public static final String[] CREATE_INDEXS = {
    "CREATE INDEX IF NOT EXISTS mms_thread_id_index ON " + TABLE_NAME + " (" + THREAD_ID + ");",
    "CREATE INDEX IF NOT EXISTS mms_read_index ON " + TABLE_NAME + " (" + READ + ");",
    "CREATE INDEX IF NOT EXISTS mms_read_and_thread_id_index ON " + TABLE_NAME + "(" + READ + "," + THREAD_ID + ");",
    "CREATE INDEX IF NOT EXISTS mms_message_box_index ON " + TABLE_NAME + " (" + MESSAGE_BOX + ");"
  };

  private static final String[] MMS_PROJECTION = new String[] {
      ID, THREAD_ID, DATE_SENT + " * 1000 AS " + NORMALIZED_DATE_SENT,
      DATE_RECEIVED + " * 1000 AS " + NORMALIZED_DATE_RECEIVED,
      MESSAGE_BOX, READ, MESSAGE_ID, SUBJECT, SUBJECT_CHARSET, CONTENT_TYPE,
      CONTENT_LOCATION, EXPIRY, MESSAGE_CLASS, MESSAGE_TYPE, MMS_VERSION,
      MESSAGE_SIZE, PRIORITY, REPORT_ALLOWED, STATUS, TRANSACTION_ID, RETRIEVE_STATUS,
      RETRIEVE_TEXT, RETRIEVE_TEXT_CS, READ_STATUS, CONTENT_CLASS, RESPONSE_TEXT,
      DELIVERY_TIME, DELIVERY_REPORT
  };

  public MmsDatabase(Context context, SQLiteOpenHelper databaseHelper) {
    super(context, databaseHelper);
  }

  public int getMessageCountForThread(long threadId) {
    SQLiteDatabase db = databaseHelper.getReadableDatabase();
    Cursor cursor     = null;

    try {
      cursor = db.query(TABLE_NAME, new String[] {"COUNT(*)"}, THREAD_ID + " = ?", new String[] {threadId+""}, null, null, null);

      if (cursor != null && cursor.moveToFirst())
        return cursor.getInt(0);
    } finally {
      if (cursor != null)
        cursor.close();
    }

    return 0;
  }

  public long getThreadIdForMessage(long id) {
    String sql        = "SELECT " + THREAD_ID + " FROM " + TABLE_NAME + " WHERE " + ID + " = ?";
    String[] sqlArgs  = new String[] {id+""};
    SQLiteDatabase db = databaseHelper.getReadableDatabase();

    Cursor cursor = null;

    try {
      cursor = db.rawQuery(sql, sqlArgs);
      if (cursor != null && cursor.moveToFirst())
        return cursor.getLong(0);
      else
        return -1;
    } finally {
      if (cursor != null)
        cursor.close();
    }
  }

  private long getThreadIdForHeaders(PduHeaders headers) throws RecipientFormattingException {
    try {
      EncodedStringValue encodedString = headers.getEncodedStringValue(PduHeaders.FROM);
      String fromString                = new String(encodedString.getTextString(), CharacterSets.MIMENAME_ISO_8859_1);
      Recipients recipients            = RecipientFactory.getRecipientsFromString(context, fromString, false);
      return DatabaseFactory.getThreadDatabase(context).getThreadIdFor(recipients);
    } catch (UnsupportedEncodingException e) {
      throw new AssertionError(e);
    }
  }

  public Recipient getMessageRecipient(long messageId) {
    try {
      PduHeaders headers          = new PduHeaders();
      MmsAddressDatabase database = DatabaseFactory.getMmsAddressDatabase(context);
      database.getAddressesForId(messageId, headers);

      EncodedStringValue encodedFrom = headers.getEncodedStringValue(PduHeaders.FROM);
      if (encodedFrom != null) {
        String address        = new String(encodedFrom.getTextString(), CharacterSets.MIMENAME_ISO_8859_1);
        Recipients recipients = RecipientFactory.getRecipientsFromString(context, address, false);

        if (recipients == null || recipients.isEmpty()) {
          return new Recipient("Unknown", "Unknown", null,
                               ContactPhotoFactory.getDefaultContactPhoto(context));
        }

        return recipients.getPrimaryRecipient();
      } else {
        return new Recipient("Unknown", "Unknown", null,
                             ContactPhotoFactory.getDefaultContactPhoto(context));
      }
    } catch (UnsupportedEncodingException e) {
      throw new AssertionError(e);
    } catch (RecipientFormattingException e) {
      return new Recipient("Unknown", "Unknown", null,
                           ContactPhotoFactory.getDefaultContactPhoto(context));
    }
  }

  public void updateResponseStatus(long messageId, int status) {
    SQLiteDatabase database     = databaseHelper.getWritableDatabase();
    ContentValues contentValues = new ContentValues();
    contentValues.put(RESPONSE_STATUS, status);

    database.update(TABLE_NAME, contentValues, ID_WHERE, new String[] {messageId+""});
  }

  private void updateMailboxBitmask(long id, long maskOff, long maskOn) {
    SQLiteDatabase db = databaseHelper.getWritableDatabase();
    db.execSQL("UPDATE " + TABLE_NAME +
               " SET " + MESSAGE_BOX + " = (" + MESSAGE_BOX + " & " + (Types.TOTAL_MASK - maskOff) + " | " + maskOn + " )" +
               " WHERE " + ID + " = ?", new String[] {id+""});
  }

  public void markAsSentFailed(long messageId) {
    updateMailboxBitmask(messageId, Types.BASE_TYPE_MASK, Types.BASE_SENT_FAILED_TYPE);
    notifyConversationListeners(getThreadIdForMessage(messageId));
  }

  public void markAsSent(long messageId, byte[] mmsId, long status) {
    SQLiteDatabase database     = databaseHelper.getWritableDatabase();
    ContentValues contentValues = new ContentValues();
    contentValues.put(RESPONSE_STATUS, status);
    contentValues.put(MESSAGE_ID, new String(mmsId));

    database.update(TABLE_NAME, contentValues, ID_WHERE, new String[] {messageId+""});
    updateMailboxBitmask(messageId, Types.BASE_TYPE_MASK, Types.BASE_SENT_TYPE);
    notifyConversationListeners(getThreadIdForMessage(messageId));
  }

  public void markDownloadState(long messageId, long state) {
    SQLiteDatabase database     = databaseHelper.getWritableDatabase();
    ContentValues contentValues = new ContentValues();
    contentValues.put(STATUS, state);

    database.update(TABLE_NAME, contentValues, ID_WHERE, new String[] {messageId+""});
    notifyConversationListeners(getThreadIdForMessage(messageId));
  }

  public void markAsNoSession(long messageId, long threadId) {
    updateMailboxBitmask(messageId, Types.ENCRYPTION_MASK, Types.ENCRYPTION_REMOTE_NO_SESSION_BIT);
    notifyConversationListeners(threadId);
  }

  public void markAsDecryptFailed(long messageId, long threadId) {
    updateMailboxBitmask(messageId, Types.ENCRYPTION_MASK, Types.ENCRYPTION_REMOTE_FAILED_BIT);
    notifyConversationListeners(threadId);
  }

  public void setMessagesRead(long threadId) {
    SQLiteDatabase database     = databaseHelper.getWritableDatabase();
    ContentValues contentValues = new ContentValues();
    contentValues.put(READ, 1);

    database.update(TABLE_NAME, contentValues, THREAD_ID + " = ?", new String[] {threadId+""});
  }

  public NotificationInd getNotificationMessage(long messageId) throws MmsException {
    PduHeaders headers        = getHeadersForId(messageId);
    return new NotificationInd(headers);
  }

  public MultimediaMessagePdu getMediaMessage(long messageId)
      throws MmsException
  {
    PduHeaders headers        = getHeadersForId(messageId);
    PartDatabase partDatabase = getPartDatabase(null);
    PduBody body              = partDatabase.getParts(messageId, false);

    return new MultimediaMessagePdu(headers, body);
  }

  public SendReq getSendRequest(MasterSecret masterSecret, long messageId) throws MmsException {
    PduHeaders headers        = getHeadersForId(messageId);
    PartDatabase partDatabase = getPartDatabase(masterSecret);
    PduBody body              = partDatabase.getParts(messageId, true);

    return new SendReq(headers, body, messageId, headers.getMessageBox());
  }

  public SendReq[] getOutgoingMessages(MasterSecret masterSecret) throws MmsException {
    MmsAddressDatabase addr = DatabaseFactory.getMmsAddressDatabase(context);
    PartDatabase parts      = getPartDatabase(masterSecret);
    SQLiteDatabase database = databaseHelper.getReadableDatabase();
    Cursor cursor           = null;

    try {
      cursor = database.query(TABLE_NAME, MMS_PROJECTION,
                              MESSAGE_BOX + " & " + Types.BASE_TYPE_MASK + " = ?",
                              new String[] {Types.BASE_OUTBOX_TYPE+""},
                              null, null, null);

      if (cursor == null || cursor.getCount() == 0)
        return new SendReq[0];

      SendReq[] requests = new SendReq[cursor.getCount()];
      int i = 0;

      while (cursor.moveToNext()) {
        long messageId     = cursor.getLong(cursor.getColumnIndexOrThrow(ID));
        long outboxType    = cursor.getLong(cursor.getColumnIndexOrThrow(MESSAGE_BOX));
        PduHeaders headers = getHeadersFromCursor(cursor);
        addr.getAddressesForId(messageId, headers);
        PduBody body       = parts.getParts(messageId, true);
        requests[i++]      = new SendReq(headers, body, messageId, outboxType);
      }

      return requests;
    } finally {
      if (cursor != null)
        cursor.close();
    }
  }

  private long insertMessageInbox(MasterSecret masterSecret, MultimediaMessagePdu retrieved,
                                     String contentLocation, long threadId, long mailbox)
      throws MmsException
  {
    PduHeaders headers          = retrieved.getPduHeaders();
    ContentValues contentValues = getContentValuesFromHeader(headers);

    contentValues.put(MESSAGE_BOX, mailbox);
    contentValues.put(THREAD_ID, threadId);
    contentValues.put(CONTENT_LOCATION, contentLocation);
    contentValues.put(STATUS, Status.DOWNLOAD_INITIALIZED);
    contentValues.put(DATE_RECEIVED, System.currentTimeMillis() / 1000);

    if (!contentValues.containsKey(DATE_SENT))
      contentValues.put(DATE_SENT, contentValues.getAsLong(DATE_RECEIVED));

    return insertMediaMessage(masterSecret, retrieved, contentValues);
  }

  public long insertMessageInbox(MasterSecret masterSecret, RetrieveConf retrieved,
                                 String contentLocation, long threadId)
      throws MmsException
  {
    return insertMessageInbox(masterSecret, retrieved, contentLocation, threadId,
                              Types.BASE_INBOX_TYPE | Types.ENCRYPTION_SYMMETRIC_BIT);
  }

  public long insertSecureMessageInbox(MasterSecret masterSecret, RetrieveConf retrieved,
                                       String contentLocation, long threadId)
      throws MmsException
  {
    return insertMessageInbox(masterSecret, retrieved, contentLocation, threadId,
                              Types.BASE_INBOX_TYPE | Types.SECURE_MESSAGE_BIT | Types.ENCRYPTION_REMOTE_BIT);
  }

  public long insertSecureDecryptedMessageInbox(MasterSecret masterSecret, MultimediaMessagePdu retrieved, long threadId)
      throws MmsException
  {
    return insertMessageInbox(masterSecret, retrieved, "", threadId,
                              Types.BASE_INBOX_TYPE | Types.SECURE_MESSAGE_BIT | Types.ENCRYPTION_SYMMETRIC_BIT);
  }

  public long insertMessageInbox(NotificationInd notification) {
    try {
      SQLiteDatabase db                  = databaseHelper.getWritableDatabase();
      PduHeaders headers                 = notification.getPduHeaders();
      ContentValues contentValues        = getContentValuesFromHeader(headers);
      long threadId                      = getThreadIdForHeaders(headers);
      MmsAddressDatabase addressDatabase = DatabaseFactory.getMmsAddressDatabase(context);

      Log.w("MmsDatabse", "Message received type: " + headers.getOctet(PduHeaders.MESSAGE_TYPE));

      contentValues.put(MESSAGE_BOX, Types.BASE_INBOX_TYPE);
      contentValues.put(THREAD_ID, threadId);
      contentValues.put(STATUS, Status.DOWNLOAD_INITIALIZED);
      contentValues.put(DATE_RECEIVED, System.currentTimeMillis() / 1000);

      if (!contentValues.containsKey(DATE_SENT))
        contentValues.put(DATE_SENT, contentValues.getAsLong(DATE_RECEIVED));

      long messageId = db.insert(TABLE_NAME, null, contentValues);
      addressDatabase.insertAddressesForId(messageId, headers);

      notifyConversationListeners(threadId);
      DatabaseFactory.getThreadDatabase(context).update(threadId);
      DatabaseFactory.getThreadDatabase(context).setUnread(threadId);
      Trimmer.trimThread(context, threadId);

      return messageId;
    } catch (RecipientFormattingException rfe) {
      Log.w("MmsDatabase", rfe);
      return -1;
    }
  }

  public long insertMessageOutbox(MasterSecret masterSecret, SendReq sendRequest,
                                  long threadId, boolean isSecure)
      throws MmsException
  {
    long type                   = Types.BASE_OUTBOX_TYPE;
    PduHeaders headers          = sendRequest.getPduHeaders();
    ContentValues contentValues = getContentValuesFromHeader(headers);

    if (isSecure) {
      type |= Types.SECURE_MESSAGE_BIT;
    }

    contentValues.put(MESSAGE_BOX, type);
    contentValues.put(THREAD_ID, threadId);
    contentValues.put(READ, 1);
    contentValues.put(DATE_RECEIVED, contentValues.getAsLong(DATE_SENT));

    long messageId = insertMediaMessage(masterSecret, sendRequest, contentValues);
    Trimmer.trimThread(context, threadId);

    return messageId;
  }

  private long insertMediaMessage(MasterSecret masterSecret,
                                  MultimediaMessagePdu message,
                                  ContentValues contentValues)
      throws MmsException
  {
    SQLiteDatabase db                  = databaseHelper.getWritableDatabase();
    long messageId                     = db.insert(TABLE_NAME, null, contentValues);
    PduBody body                       = message.getBody();
    PartDatabase partsDatabase         = getPartDatabase(masterSecret);
    MmsAddressDatabase addressDatabase = DatabaseFactory.getMmsAddressDatabase(context);

    addressDatabase.insertAddressesForId(messageId, message.getPduHeaders());
    partsDatabase.insertParts(messageId, body);

    notifyConversationListeners(contentValues.getAsLong(THREAD_ID));
    DatabaseFactory.getThreadDatabase(context).update(contentValues.getAsLong(THREAD_ID));

    return messageId;
  }

  public void delete(long messageId) {
    long threadId                   = getThreadIdForMessage(messageId);
    MmsAddressDatabase addrDatabase = DatabaseFactory.getMmsAddressDatabase(context);
    PartDatabase partDatabase       = DatabaseFactory.getPartDatabase(context);
    partDatabase.deleteParts(messageId);
    addrDatabase.deleteAddressesForId(messageId);

    SQLiteDatabase database = databaseHelper.getWritableDatabase();
    database.delete(TABLE_NAME, ID_WHERE, new String[] {messageId+""});
    DatabaseFactory.getThreadDatabase(context).update(threadId);
    notifyConversationListeners(threadId);
  }


  public void deleteThread(long threadId) {
    Set<Long> singleThreadSet = new HashSet<Long>();
    singleThreadSet.add(threadId);
    deleteThreads(singleThreadSet);
  }

  /*package*/ void deleteThreads(Set<Long> threadIds) {
    SQLiteDatabase db = databaseHelper.getWritableDatabase();
    String where      = "";
    Cursor cursor     = null;

    for (long threadId : threadIds) {
      where += THREAD_ID + " = '" + threadId + "' OR ";
    }

    where = where.substring(0, where.length() - 4);

    try {
      cursor = db.query(TABLE_NAME, new String[] {ID}, where, null, null, null, null);

      while (cursor != null && cursor.moveToNext()) {
        delete(cursor.getLong(0));
      }

    } finally {
      if (cursor != null)
        cursor.close();
    }
  }

  /*package*/void deleteMessagesInThreadBeforeDate(long threadId, long date) {
    date          = date / 1000;
    Cursor cursor = null;

    try {
      SQLiteDatabase db = databaseHelper.getReadableDatabase();
      String where      = THREAD_ID + " = ? AND (CASE (" + MESSAGE_BOX + " & " + Types.BASE_TYPE_MASK + ") ";

      for (long outgoingType : Types.OUTGOING_MESSAGE_TYPES) {
        where += " WHEN " + outgoingType + " THEN " + DATE_SENT + " < " + date;
      }

      where += (" ELSE " + DATE_RECEIVED + " < " + date + " END)");

      Log.w("MmsDatabase", "Executing trim query: " + where);
      cursor = db.query(TABLE_NAME, new String[] {ID}, where, new String[] {threadId+""}, null, null, null);

      while (cursor != null && cursor.moveToNext()) {
        Log.w("MmsDatabase", "Trimming: " + cursor.getLong(0));
        delete(cursor.getLong(0));
      }

    } finally {
      if (cursor != null)
        cursor.close();
    }
  }


  public void deleteAllThreads() {
    DatabaseFactory.getPartDatabase(context).deleteAllParts();
    DatabaseFactory.getMmsAddressDatabase(context).deleteAllAddresses();

    SQLiteDatabase database = databaseHelper.getWritableDatabase();
    database.delete(TABLE_NAME, null, null);
  }

  public Cursor getCarrierMmsInformation(String apn) {
    Uri uri                = Uri.withAppendedPath(Uri.parse("content://telephony/carriers"), "current");
    String selection       = Util.isEmpty(apn) ? null : "apn = ?";
    String[] selectionArgs = Util.isEmpty(apn) ? null : new String[] {apn.trim()};

    try {
      return context.getContentResolver().query(uri, null, selection, selectionArgs, null);
    } catch (NullPointerException npe) {
      // NOTE - This is dumb, but on some devices there's an NPE in the Android framework
      // for the provider of this call, which gets rethrown back to here through a binder
      // call.
      throw new IllegalArgumentException(npe);
    }
  }

  private PduHeaders getHeadersForId(long messageId) throws MmsException {
    SQLiteDatabase database = databaseHelper.getReadableDatabase();
    Cursor cursor           = null;

    try {
      cursor = database.query(TABLE_NAME, MMS_PROJECTION, ID_WHERE, new String[] {messageId+""},
                              null, null, null);

      if (cursor == null || !cursor.moveToFirst())
        throw new MmsException("No headers available at ID: " + messageId);

      PduHeaders headers      = getHeadersFromCursor(cursor);
      long messageBox         = cursor.getLong(cursor.getColumnIndexOrThrow(MESSAGE_BOX));
      MmsAddressDatabase addr = DatabaseFactory.getMmsAddressDatabase(context);

      addr.getAddressesForId(messageId, headers);
      headers.setMessageBox(messageBox);

      return headers;
    } finally {
      if (cursor != null)
        cursor.close();
    }
  }

  private PduHeaders getHeadersFromCursor(Cursor cursor) throws InvalidHeaderValueException {
    PduHeaders headers    = new PduHeaders();
    PduHeadersBuilder phb = new PduHeadersBuilder(headers, cursor);

    phb.add(RETRIEVE_TEXT, RETRIEVE_TEXT_CS, PduHeaders.RETRIEVE_TEXT);
    phb.add(SUBJECT, SUBJECT_CHARSET, PduHeaders.SUBJECT);
    phb.addText(CONTENT_LOCATION, PduHeaders.CONTENT_LOCATION);
    phb.addText(CONTENT_TYPE, PduHeaders.CONTENT_TYPE);
    phb.addText(MESSAGE_CLASS, PduHeaders.MESSAGE_CLASS);
    phb.addText(MESSAGE_ID, PduHeaders.MESSAGE_ID);
    phb.addText(RESPONSE_TEXT, PduHeaders.RESPONSE_TEXT);
    phb.addText(TRANSACTION_ID, PduHeaders.TRANSACTION_ID);
    phb.addOctet(CONTENT_CLASS, PduHeaders.CONTENT_CLASS);
    phb.addOctet(DELIVERY_REPORT, PduHeaders.DELIVERY_REPORT);
    phb.addOctet(MESSAGE_TYPE, PduHeaders.MESSAGE_TYPE);
    phb.addOctet(MMS_VERSION, PduHeaders.MMS_VERSION);
    phb.addOctet(PRIORITY, PduHeaders.PRIORITY);
    phb.addOctet(READ_STATUS, PduHeaders.READ_STATUS);
    phb.addOctet(REPORT_ALLOWED, PduHeaders.REPORT_ALLOWED);
    phb.addOctet(RETRIEVE_STATUS, PduHeaders.RETRIEVE_STATUS);
    phb.addOctet(STATUS, PduHeaders.STATUS);
    phb.addLong(NORMALIZED_DATE_SENT, PduHeaders.DATE);
    phb.addLong(DELIVERY_TIME, PduHeaders.DELIVERY_TIME);
    phb.addLong(EXPIRY, PduHeaders.EXPIRY);
    phb.addLong(MESSAGE_SIZE, PduHeaders.MESSAGE_SIZE);

    headers.setLongInteger(headers.getLongInteger(PduHeaders.DATE) / 1000L, PduHeaders.DATE);

    return headers;
  }

  private ContentValues getContentValuesFromHeader(PduHeaders headers) {
    ContentValues contentValues = new ContentValues();
    ContentValuesBuilder cvb    = new ContentValuesBuilder(contentValues);

    cvb.add(RETRIEVE_TEXT, RETRIEVE_TEXT_CS, headers.getEncodedStringValue(PduHeaders.RETRIEVE_TEXT));
    cvb.add(SUBJECT, SUBJECT_CHARSET, headers.getEncodedStringValue(PduHeaders.SUBJECT));
    cvb.add(CONTENT_LOCATION, headers.getTextString(PduHeaders.CONTENT_LOCATION));
    cvb.add(CONTENT_TYPE, headers.getTextString(PduHeaders.CONTENT_TYPE));
    cvb.add(MESSAGE_CLASS, headers.getTextString(PduHeaders.MESSAGE_CLASS));
    cvb.add(MESSAGE_ID, headers.getTextString(PduHeaders.MESSAGE_ID));
    cvb.add(RESPONSE_TEXT, headers.getTextString(PduHeaders.RESPONSE_TEXT));
    cvb.add(TRANSACTION_ID, headers.getTextString(PduHeaders.TRANSACTION_ID));
    cvb.add(CONTENT_CLASS, headers.getOctet(PduHeaders.CONTENT_CLASS));
    cvb.add(DELIVERY_REPORT, headers.getOctet(PduHeaders.DELIVERY_REPORT));
    cvb.add(MESSAGE_TYPE, headers.getOctet(PduHeaders.MESSAGE_TYPE));
    cvb.add(MMS_VERSION, headers.getOctet(PduHeaders.MMS_VERSION));
    cvb.add(PRIORITY, headers.getOctet(PduHeaders.PRIORITY));
    cvb.add(READ_REPORT, headers.getOctet(PduHeaders.READ_REPORT));
    cvb.add(READ_STATUS, headers.getOctet(PduHeaders.READ_STATUS));
    cvb.add(REPORT_ALLOWED, headers.getOctet(PduHeaders.REPORT_ALLOWED));
    cvb.add(RETRIEVE_STATUS, headers.getOctet(PduHeaders.RETRIEVE_STATUS));
    cvb.add(STATUS, headers.getOctet(PduHeaders.STATUS));
    cvb.add(DATE_SENT, headers.getLongInteger(PduHeaders.DATE));
    cvb.add(DELIVERY_TIME, headers.getLongInteger(PduHeaders.DELIVERY_TIME));
    cvb.add(EXPIRY, headers.getLongInteger(PduHeaders.EXPIRY));
    cvb.add(MESSAGE_SIZE, headers.getLongInteger(PduHeaders.MESSAGE_SIZE));

    return cvb.getContentValues();
  }


  protected PartDatabase getPartDatabase(MasterSecret masterSecret) {
    if (masterSecret == null)
      return DatabaseFactory.getPartDatabase(context);
    else
      return DatabaseFactory.getEncryptingPartDatabase(context, masterSecret);
  }

  public Reader readerFor(MasterSecret masterSecret, Cursor cursor) {
    return new Reader(masterSecret, cursor);
  }

  public static class Status {
    public static final int DOWNLOAD_INITIALIZED     = 1;
    public static final int DOWNLOAD_NO_CONNECTIVITY = 2;
    public static final int DOWNLOAD_CONNECTING      = 3;
    public static final int DOWNLOAD_SOFT_FAILURE    = 4;
    public static final int DOWNLOAD_HARD_FAILURE    = 5;

    public static boolean isDisplayDownloadButton(int status) {
      return
          status == DOWNLOAD_INITIALIZED     ||
          status == DOWNLOAD_NO_CONNECTIVITY ||
          status == DOWNLOAD_SOFT_FAILURE;
    }

    public static String getLabelForStatus(Context context, int status) {
      switch (status) {
        case DOWNLOAD_CONNECTING:   return context.getString(R.string.MmsDatabase_connecting_to_mms_server);
        case DOWNLOAD_INITIALIZED:  return context.getString(R.string.MmsDatabase_downloading_mms);
        case DOWNLOAD_HARD_FAILURE: return context.getString(R.string.MmsDatabase_mms_download_failed);
      }

      return context.getString(R.string.MmsDatabase_downloading);
    }

    public static boolean isHardError(int status) {
      return status == DOWNLOAD_HARD_FAILURE;
    }
  }

  public class Reader {

    private final Cursor cursor;
    private final MasterSecret masterSecret;

    public Reader(MasterSecret masterSecret, Cursor cursor) {
      this.cursor       = cursor;
      this.masterSecret = masterSecret;
    }

    public MessageRecord getNext() {
      if (cursor == null || !cursor.moveToNext())
        return null;

      return getCurrent();
    }

    public MessageRecord getCurrent() {
      long mmsType = cursor.getLong(cursor.getColumnIndexOrThrow(MmsDatabase.MESSAGE_TYPE));

      if (mmsType == PduHeaders.MESSAGE_TYPE_NOTIFICATION_IND) {
        return getNotificationMmsMessageRecord(cursor);
      } else {
        return getMediaMmsMessageRecord(cursor);
      }
    }

    private MediaMmsMessageRecord getMediaMmsMessageRecord(Cursor cursor) {
      long id             = cursor.getLong(cursor.getColumnIndexOrThrow(MmsDatabase.ID));
      long dateSent       = cursor.getLong(cursor.getColumnIndexOrThrow(MmsDatabase.NORMALIZED_DATE_SENT));
      long dateReceived   = cursor.getLong(cursor.getColumnIndexOrThrow(MmsDatabase.NORMALIZED_DATE_RECEIVED));
      long box            = cursor.getLong(cursor.getColumnIndexOrThrow(MmsDatabase.MESSAGE_BOX));
      long threadId       = cursor.getLong(cursor.getColumnIndexOrThrow(MmsDatabase.THREAD_ID));
      Recipient recipient = getMessageRecipient(id);
      MessageRecord.GroupData groupData = null;

      SlideDeck slideDeck;

      try {
        MultimediaMessagePdu pdu = getMediaMessage(id);
        slideDeck                = getSlideDeck(masterSecret, pdu);

        if (cursor.getColumnIndex(MmsSmsDatabase.GROUP_SIZE) != -1) {
          int groupSize       = pdu.getTo().length;
          int groupSent       = MmsDatabase.Types.isFailedMessageType(box) ? 0 : groupSize;
          int groupSendFailed = groupSize - groupSent;

          if (groupSize <= 1) {
            groupSize       = cursor.getInt(cursor.getColumnIndexOrThrow(MmsSmsDatabase.GROUP_SIZE));
            groupSent       = cursor.getInt(cursor.getColumnIndexOrThrow(MmsSmsDatabase.MMS_GROUP_SENT_COUNT));
            groupSendFailed = cursor.getInt(cursor.getColumnIndexOrThrow(MmsSmsDatabase.MMS_GROUP_SEND_FAILED_COUNT));
          }

          Log.w("ConversationAdapter", "MMS GroupSize: " + groupSize + " , GroupSent: " + groupSent + " , GroupSendFailed: " + groupSendFailed);

          groupData = new MessageRecord.GroupData(groupSize, groupSent, groupSendFailed);
        }
      } catch (MmsException me) {
        Log.w("ConversationAdapter", me);
        slideDeck = null;
      }

      return new MediaMmsMessageRecord(context, id, new Recipients(recipient), recipient,
                                       dateSent, dateReceived, threadId,
                                       slideDeck, box, groupData);
    }

    protected SlideDeck getSlideDeck(MasterSecret masterSecret, MultimediaMessagePdu pdu) {
      if (masterSecret == null)
        return null;

      return new SlideDeck(context, masterSecret, pdu.getBody());
    }

    private NotificationMmsMessageRecord getNotificationMmsMessageRecord(Cursor cursor) {
      long id             = cursor.getLong(cursor.getColumnIndexOrThrow(MmsDatabase.ID));
      long dateSent       = cursor.getLong(cursor.getColumnIndexOrThrow(MmsDatabase.NORMALIZED_DATE_SENT));
      long dateReceived   = cursor.getLong(cursor.getColumnIndexOrThrow(MmsDatabase.NORMALIZED_DATE_RECEIVED));
      long threadId       = cursor.getLong(cursor.getColumnIndexOrThrow(MmsDatabase.THREAD_ID));
      long mailbox        = cursor.getLong(cursor.getColumnIndexOrThrow(MmsDatabase.MESSAGE_BOX));
      Recipient recipient = getMessageRecipient(id);

      NotificationInd notification;

      try {
        notification = getNotificationMessage(id);
      } catch (MmsException me) {
        Log.w("ConversationAdapter", me);
        notification = new NotificationInd(new PduHeaders());
      }

      return new NotificationMmsMessageRecord(context, id, new Recipients(recipient), recipient,
                                              dateSent, dateReceived, threadId,
                                              notification.getContentLocation(),
                                              notification.getMessageSize(),
                                              notification.getExpiry(),
                                              notification.getStatus(),
                                              notification.getTransactionId(),
                                              mailbox);

    }

    public void close() {
      cursor.close();
    }
  }

}
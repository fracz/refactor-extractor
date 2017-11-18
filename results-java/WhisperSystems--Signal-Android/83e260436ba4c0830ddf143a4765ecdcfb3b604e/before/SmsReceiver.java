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
package org.thoughtcrime.securesms.service;

import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.util.Log;

import org.thoughtcrime.securesms.ApplicationPreferencesActivity;
import org.thoughtcrime.securesms.crypto.DecryptingQueue;
import org.thoughtcrime.securesms.crypto.InvalidKeyException;
import org.thoughtcrime.securesms.crypto.InvalidVersionException;
import org.thoughtcrime.securesms.crypto.KeyExchangeMessage;
import org.thoughtcrime.securesms.crypto.KeyExchangeProcessor;
import org.thoughtcrime.securesms.crypto.MasterSecret;
import org.thoughtcrime.securesms.crypto.MasterSecretUtil;
import org.thoughtcrime.securesms.database.DatabaseFactory;
import org.thoughtcrime.securesms.notifications.MessageNotifier;
import org.thoughtcrime.securesms.protocol.Prefix;
import org.thoughtcrime.securesms.protocol.WirePrefix;
import org.thoughtcrime.securesms.recipients.Recipient;
import org.thoughtcrime.securesms.sms.MultipartMessageHandler;
import org.thoughtcrime.securesms.sms.TextMessage;

import java.util.ArrayList;

public class SmsReceiver {

  private MultipartMessageHandler multipartMessageHandler = new MultipartMessageHandler();

  private final Context context;

  public SmsReceiver(Context context) {
    this.context      = context;
  }

  private String assembleSecureMessageFragments(String sender, String messageBody) {
    String localPrefix;

    if (WirePrefix.isEncryptedMessage(messageBody)) {
      localPrefix = Prefix.ASYMMETRIC_ENCRYPT;
    } else {
      localPrefix = Prefix.KEY_EXCHANGE;
    }

    Log.w("SMSReceiverService", "Calculated local prefix for message: " + messageBody + " - Local Prefix: " + localPrefix);

    messageBody = messageBody.substring(WirePrefix.PREFIX_SIZE);

    Log.w("SMSReceiverService", "Parsed off wire prefix: " + messageBody);

    if (!multipartMessageHandler.isManualTransport(messageBody))
      return localPrefix + messageBody;
    else
      return multipartMessageHandler.processPotentialMultipartMessage(localPrefix, sender, messageBody);

  }

  private String assembleMessageFragments(TextMessage[] messages) {
    StringBuilder body = new StringBuilder();

    for (TextMessage message : messages) {
      body.append(message.getMessage());
    }

    String messageBody = body.toString();

    if (WirePrefix.isEncryptedMessage(messageBody) || WirePrefix.isKeyExchange(messageBody)) {
      return assembleSecureMessageFragments(messages[0].getSender(), messageBody);
    } else {
      return messageBody;
    }
  }

  private long storeSecureMessage(MasterSecret masterSecret, TextMessage message, String messageBody) {
    long messageId = DatabaseFactory.getSmsDatabase(context).insertSecureMessageReceived(message, messageBody);
    Log.w("SmsReceiver", "Inserted secure message received: " + messageId);

    if (masterSecret != null)
      DecryptingQueue.scheduleDecryption(context, masterSecret, messageId, message.getSender(), messageBody);

    return messageId;
  }

  private long storeStandardMessage(MasterSecret masterSecret, TextMessage message, String messageBody) {
    if      (masterSecret != null)                               return DatabaseFactory.getEncryptingSmsDatabase(context).insertMessageReceived(masterSecret, message, messageBody);
    else if (MasterSecretUtil.hasAsymmericMasterSecret(context)) return DatabaseFactory.getEncryptingSmsDatabase(context).insertMessageReceived(MasterSecretUtil.getAsymmetricMasterSecret(context, null), message, messageBody);
    else                                                         return DatabaseFactory.getSmsDatabase(context).insertMessageReceived(message, messageBody);
  }

  private long storeKeyExchangeMessage(MasterSecret masterSecret, TextMessage message, String messageBody) {
    if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean(ApplicationPreferencesActivity.AUTO_KEY_EXCHANGE_PREF, true)) {
      try {
        Recipient recipient                   = new Recipient(null, message.getSender(), null, null);
        KeyExchangeMessage keyExchangeMessage = new KeyExchangeMessage(messageBody);
        KeyExchangeProcessor processor        = new KeyExchangeProcessor(context, masterSecret, recipient);

        Log.w("SmsReceiver", "Received key with fingerprint: " + keyExchangeMessage.getPublicKey().getFingerprint());

        if (processor.isStale(keyExchangeMessage)) {
          messageBody    = messageBody.substring(Prefix.KEY_EXCHANGE.length());
          messageBody    = Prefix.STALE_KEY_EXCHANGE + messageBody;
        } else if (!processor.hasCompletedSession() || processor.hasSameSessionIdentity(keyExchangeMessage)) {
          messageBody    = messageBody.substring(Prefix.KEY_EXCHANGE.length());
          messageBody    = Prefix.PROCESSED_KEY_EXCHANGE + messageBody;
          long messageId = storeStandardMessage(masterSecret, message, messageBody);
          long threadId  = DatabaseFactory.getSmsDatabase(context).getThreadIdForMessage(messageId);

          processor.processKeyExchangeMessage(keyExchangeMessage, threadId);
          return messageId;
        }
      } catch (InvalidVersionException e) {
        Log.w("SmsReceiver", e);
      } catch (InvalidKeyException e) {
        Log.w("SmsReceiver", e);
      }
    }

    return storeStandardMessage(masterSecret, message, messageBody);
  }

  private long storeMessage(MasterSecret masterSecret, TextMessage message, String messageBody) {
    if (messageBody.startsWith(Prefix.ASYMMETRIC_ENCRYPT)) {
      return storeSecureMessage(masterSecret, message, messageBody);
    } else if (messageBody.startsWith(Prefix.KEY_EXCHANGE)) {
      return storeKeyExchangeMessage(masterSecret, message, messageBody);
    } else {
      return storeStandardMessage(masterSecret, message, messageBody);
    }
  }

//  private SmsMessage[] parseMessages(Bundle bundle) {
//    Object[] pdus         = (Object[])bundle.get("pdus");
//    SmsMessage[] messages = new SmsMessage[pdus.length];
//
//    for (int i=0;i<pdus.length;i++)
//      messages[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
//
//    return messages;
//  }

  private void handleReceiveMessage(MasterSecret masterSecret, Intent intent) {
    ArrayList<TextMessage> messagesList = intent.getExtras().getParcelableArrayList("text_messages");
    TextMessage[] messages              = messagesList.toArray(new TextMessage[0]);
//    Bundle bundle         = intent.getExtras();
//    SmsMessage[] messages = parseMessages(bundle);
    String message        = assembleMessageFragments(messages);

    if (message != null) {
      long messageId = storeMessage(masterSecret, messages[0], message);
      long threadId = DatabaseFactory.getSmsDatabase(context).getThreadIdForMessage(messageId);

      MessageNotifier.updateNotification(context, masterSecret, threadId);
    }
  }

  public void process(MasterSecret masterSecret, Intent intent) {
    if (intent.getAction().equals(SendReceiveService.RECEIVE_SMS_ACTION)) {
      handleReceiveMessage(masterSecret, intent);
    }
  }
}
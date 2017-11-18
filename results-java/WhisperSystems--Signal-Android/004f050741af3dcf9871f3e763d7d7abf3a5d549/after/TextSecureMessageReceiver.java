/**
 * Copyright (C) 2014 Open Whisper Systems
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
package org.whispersystems.textsecure.api;

import org.whispersystems.libaxolotl.InvalidMessageException;
import org.whispersystems.textsecure.api.crypto.AttachmentCipherInputStream;
import org.whispersystems.textsecure.api.messages.TextSecureAttachmentPointer;
import org.whispersystems.textsecure.api.push.TrustStore;
import org.whispersystems.textsecure.api.util.CredentialsProvider;
import org.whispersystems.textsecure.internal.push.PushServiceSocket;
import org.whispersystems.textsecure.internal.util.StaticCredentialsProvider;
import org.whispersystems.textsecure.internal.websocket.WebSocketConnection;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * The primary interface for receiving TextSecure messages.
 *
 * @author Moxie Marlinspike
 */
public class TextSecureMessageReceiver {

  private final PushServiceSocket   socket;
  private final TrustStore          trustStore;
  private final String              url;
  private final CredentialsProvider credentialsProvider;

  /**
   * Construct a TextSecureMessageReceiver.
   *
   * @param url The URL of the TextSecure server.
   * @param trustStore The {@link org.whispersystems.textsecure.api.push.TrustStore} containing
   *                   the server's TLS signing certificate.
   * @param user The TextSecure user's username (eg. phone number).
   * @param password The TextSecure user's password.
   * @param signalingKey The 52 byte signaling key assigned to this user at registration.
   */
  public TextSecureMessageReceiver(String url, TrustStore trustStore,
                                   String user, String password, String signalingKey)
  {
    this(url, trustStore, new StaticCredentialsProvider(user, password, signalingKey));
  }

  /**
   * Construct a TextSecureMessageReceiver.
   *
   * @param url The URL of the TextSecure server.
   * @param trustStore The {@link org.whispersystems.textsecure.api.push.TrustStore} containing
   *                   the server's TLS signing certificate.
   * @param credentials The TextSecure user's credentials.
   */
  public TextSecureMessageReceiver(String url, TrustStore trustStore, CredentialsProvider credentials) {
    this.url                 = url;
    this.trustStore          = trustStore;
    this.credentialsProvider = credentials;
    this.socket              = new PushServiceSocket(url, trustStore, credentials);
  }

  /**
   * Retrieves a TextSecure attachment.
   *
   * @param pointer The {@link org.whispersystems.textsecure.api.messages.TextSecureAttachmentPointer}
   *                received in a {@link org.whispersystems.textsecure.api.messages.TextSecureMessage}.
   * @param destination The download destination for this attachment.
   *
   * @return An InputStream that streams the plaintext attachment contents.
   * @throws IOException
   * @throws InvalidMessageException
   */
  public InputStream retrieveAttachment(TextSecureAttachmentPointer pointer, File destination)
      throws IOException, InvalidMessageException
  {
    socket.retrieveAttachment(pointer.getRelay().orNull(), pointer.getId(), destination);
    return new AttachmentCipherInputStream(destination, pointer.getKey());
  }

  /**
   * Creates a pipe for receiving TextSecure messages.
   *
   * Callers must call {@link TextSecureMessagePipe#shutdown()} when finished with the pipe.
   *
   * @return A TextSecureMessagePipe for receiving TextSecure messages.
   */
  public TextSecureMessagePipe createMessagePipe() {
    WebSocketConnection webSocket = new WebSocketConnection(url, trustStore, credentialsProvider);
    return new TextSecureMessagePipe(webSocket, credentialsProvider);
  }

}
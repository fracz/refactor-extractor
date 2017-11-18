commit 83e260436ba4c0830ddf143a4765ecdcfb3b604e
Author: Moxie Marlinspike <moxie@thoughtcrime.org>
Date:   Sat Apr 20 12:22:04 2013 -0700

    Major storage layer refactoring to set the stage for clean GCM.

    1) We now try to hand out cursors at a minimum.  There has always been
       a fairly clean insertion layer that handles encrypting message bodies,
       but the process of decrypting message bodies has always been less than
       ideal.  Here we introduce a "Reader" interface that will decrypt message
       bodies when appropriate and return objects that encapsulate record state.

       No more MessageDisplayHelper.  The MmsSmsDatabase interface is also more
       sane.

    2) We finally rid ourselves of the technical debt associated with TextSecure's
       initial usage of the default SMS DB.  In that world, we weren't able to use
       anything other than the default "Inbox, Outbox, Sent" types to describe a
       message, and had to overload the message content itself with a set of
       local "prefixes" to describe what it was (encrypted, asymetric encrypted,
       remote encrypted, a key exchange, procssed key exchange), and so on.

       This includes a major schema update that transforms the "type" field into
       a bitmask that describes everything that used to be encoded in a prefix,
       and prefixes have been completely eliminated from the system.

       No more Prefix.java

    3) Refactoring of the MultipartMessageHandler code.  It's less of a mess, and
       hopefully more clear as to what's going on.

    The next step is to remove what we can from SmsTransportDetails and genericize
    that interface for a GCM equivalent.
commit 1149f2d9164c7f8aabab9fecb66c6cc320ee8ca0
Author: Jesse Vincent <jesse@fsck.com>
Date:   Fri Nov 26 03:53:15 2010 +0000

    refactor getHeaders and removeHeaders to use a common method and an
    iterator to address a Market FC:

    java.util.ConcurrentModificationException
    at java.util.ArrayList$ArrayListIterator.next(ArrayList.java:573)
    at com.fsck.k9.mail.internet.MimeHeader.getHeader(MimeHeader.java:87)
    at
    com.fsck.k9.mail.internet.MimeHeader.getFirstHeader(MimeHeader.java:51)
    at
    com.fsck.k9.mail.internet.MimeMessage.getFirstHeader(MimeMessage.java:437)
    at
    com.fsck.k9.mail.internet.MimeMessage.getContentType(MimeMessage.java:130)
    at
    com.fsck.k9.mail.internet.MimeMessage.getMimeType(MimeMessage.java:159)
    at
    com.fsck.k9.mail.internet.MimeUtility.findFirstPartByMimeType(MimeUtility.java:971)
    at com.fsck.k9.crypto.Apg.isEncrypted(Apg.java:464)
    at
    com.fsck.k9.activity.MessageView.updateDecryptLayout(MessageView.java:2702)
    at
    com.fsck.k9.activity.MessageView$Listener$6.run(MessageView.java:2466)
    at android.os.Handler.handleCallback(Handler.java:587)
    at android.os.Handler.dispatchMessage(Handler.java:92)
    at android.os.Looper.loop(Looper.java:144)
    at android.app.ActivityThread.main(ActivityThread.java:4937)
    at java.lang.reflect.Method.invokeNative(Native Method)
    at java.lang.reflect.Method.invoke(Method.java:521)
    at
    com.android.internal.os.ZygoteInit$MethodAndArgsCaller.run(ZygoteInit.java:868)
    at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:626)
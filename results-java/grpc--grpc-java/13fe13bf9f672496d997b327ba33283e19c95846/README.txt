commit 13fe13bf9f672496d997b327ba33283e19c95846
Author: Eric Anderson <ejona@google.com>
Date:   Thu Feb 11 23:10:26 2016 -0800

    okhttp: Enable transport test

    OkHttpClientTransport has a fix for shutdown during start which
    prevented transportTerminated from being called. It also no longer fails
    pending streams during shutdown. Lifecycle management in general was
    revamped to be hopefully simpler and more precise. In the process GOAWAY
    handling (both sending and receiving) was improved.

    With some of the changes, the log spam generated was immense and
    unhelpful (since many exceptions are part of normal operation on
    shutdown). This change reduces the amount of log spam to nothing.
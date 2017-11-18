commit 09e1ba548d9555ea71a5e90513e0e7114460ca45
Author: Jacksgong <igzhenjie@gmail.com>
Date:   Thu Apr 28 14:04:17 2016 +0800

    refactor(send-to-listener-architecture); refactor the architecture which is used to handle the event send to FileDownloadListener, the new architecture just like a messenger and message-staion, each tasks would write snapshot messages to message-station
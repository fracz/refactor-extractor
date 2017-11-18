commit 2ea71ad6254c4094d0d34a39d9988c9d75b038ed
Author: Sergey Poromov <poromov@google.com>
Date:   Tue Feb 9 16:24:46 2016 +0100

    Fix that backupFinished() callback is not called sometimes.

    Before this in case of TRANSPORT_ERROR backup pass was aborted before backupFinished() call.
    Now this happens in 'finally' block so that there is no way to avoid it.
    Also, now backup pass doesn't break in case of QUOTA_EXCEEDED result for single package.
    And some refactoring around 'currentPackage' variable.

    Bug: 27094847
    Change-Id: I18df3f500b427381f32bd11ed1aa87ab9577bc91
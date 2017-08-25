commit d7a9852f7b1ea7999f730fde8bd9816da68dad8b
Author: Björn Schießle <schiessle@owncloud.com>
Date:   Thu Jun 6 13:32:02 2013 +0200

    use pre_setPassword hook to update the encryption keys if the back-end doesn't support password change; improved output to let the admin know what happened
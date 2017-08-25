commit e78040d2502aba01b5dd92497078ac71afab771a
Author: Bjoern Schiessle <bjoern@schiessle.org>
Date:   Fri Jul 21 12:07:32 2017 +0200

    improved error handling

    check if table was updated successfully and only then send a notification
    mail and return "true".

    Signed-off-by: Bjoern Schiessle <bjoern@schiessle.org>
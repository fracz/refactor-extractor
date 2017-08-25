commit 0a4bd86d2fa030a64e92a242421ef44e34993774
Author: Sem <sembrestels@riseup.net>
Date:   Sun Feb 9 06:42:35 2014 +0100

    chore(users): refactoring password reset action and function

    Password reset action is using force_user_password_reset function.
    The execute_new_password_request function sends the password by email
    when it's generated randomly.
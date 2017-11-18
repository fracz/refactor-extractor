commit d7483bfffcbbd4715a9e3cc2c704fb702ec529bf
Author: Gilles Debunne <debunne@google.com>
Date:   Wed Nov 10 10:47:45 2010 -0800

    Lock screen should not display password.

    Bug 3179062

    Problem was introduced in CL 78854: changing the keyListener should not
    change the transformation method.

    mInputType changes are brittle. It is often reset to TYPE_CLASS_TEXT or to
    mInput.getInputType(). The TYPE_TEXT_FLAG_MULTI_LINE and TYPE_TEXT_VARIATION_PASSWORD
    variation are then added back based on the previous state. But this is not consistent,
    and sometimes, only one of those is set.

    This should be refactored if a bug is found in what seems to work correclty at the moment.

    Change-Id: Ie251ec7db0ce0af4a07564b0dbb53465e6f361c6
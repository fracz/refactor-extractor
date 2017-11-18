commit aa89847f4e808548edfdb949c217683206ab19ce
Author: Jim Miller <jaggies@google.com>
Date:   Tue Nov 12 18:14:26 2013 -0800

    Reduce camera launch time by about 250ms.

    This reduces the amount of time available to the user to cancel
    launching the camera in order to improve average launch time.

    It also increases the threshhold for flings and motions to prevent
    unintentional launches which are costly in terms of falsing.

    Fixes bug b/11657355

    Change-Id: I852abbe3ce8ddddcb65f3a48a2f8111d20126189
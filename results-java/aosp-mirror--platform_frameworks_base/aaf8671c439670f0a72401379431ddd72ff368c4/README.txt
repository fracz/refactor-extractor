commit aaf8671c439670f0a72401379431ddd72ff368c4
Author: Jean Chalard <jchalard@google.com>
Date:   Fri Mar 1 15:08:14 2013 -0800

    Final small cleanup.

    This does not change anything in the practice since the only
    time where timing of the endBatchEdit() call matters is when
    sendCurrentText is a no-op. Still, it's theoretically correct
    to do it in this order.
    This concludes a four-step refactoring moving where the
    editor calls updateSelection to warn the IME of a cursor move.

    Change-Id: I3109a482ec1d4cd9b4ffb33cc363a4ce5128861a
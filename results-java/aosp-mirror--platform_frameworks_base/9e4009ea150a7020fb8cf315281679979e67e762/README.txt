commit 9e4009ea150a7020fb8cf315281679979e67e762
Author: Fabrice Di Meglio <fdimeglio@google.com>
Date:   Mon Aug 19 13:16:46 2013 -0700

    Decouple TimePicker code

    - introduce a TimePickerDelegate interface to be able to have several
    TimePicker implementations
    - code refactoring

    Change-Id: I0d8bdfdb4c2723a51311c01fbd485e34983bb423
commit 039a784ea3c24625b74084be18530f81dabd4bbb
Author: Fabrice Di Meglio <fdimeglio@google.com>
Date:   Wed Aug 28 17:41:26 2013 -0700

    Decouple DatePicker code

    - introduce a DatePickerDelegate interface to be able to have several
    DatePicker implementations
    - code refactoring

    Change-Id: I3a9453b1c87dea9046216cc501b0b5daf3d75d78
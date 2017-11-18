commit c6afe76dfe641afa32dab6e0b4740d780072d2c6
Author: Fabrice Di Meglio <fdimeglio@google.com>
Date:   Thu Aug 29 15:24:19 2013 -0700

    Decouple CalendarView code

    - introduce a CalendarViewDelegate interface to be able to have several
    CalendarView implementations
    - code refactoring

    Change-Id: Ib98fc51471c33a86ef58210e06176a4b4d253f01
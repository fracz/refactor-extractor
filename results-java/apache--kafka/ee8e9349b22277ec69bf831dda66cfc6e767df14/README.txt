commit ee8e9349b22277ec69bf831dda66cfc6e767df14
Author: Damian Guy <damian.guy@gmail.com>
Date:   Tue Aug 22 11:21:32 2017 +0100

    KAFKA-5689; Add MeteredWindowStore and refactor store hierarchy

    Add MeteredWindowStore and ChangeLoggingWindowBytesStore.
    Refactor Store hierarchy such that Metered is always the outermost store
    Do serialization in MeteredWindowStore

    Author: Damian Guy <damian.guy@gmail.com>

    Reviewers: Guozhang Wang <wangguoz@gmail.com>

    Closes #3692 from dguy/kafka-5689
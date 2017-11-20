commit c2e6d0bf7acd27be1aec31a21a71625ef439cf1d
Author: Pei Sun <peis@alluxio.com>
Date:   Fri Apr 7 15:25:33 2017 -0700

    [ALLUXIO-2689] New journal to improve master's availability [PR1] (#5031)

    * Faster master recovery

    * Improve logging and some fixes

    * Use default visibility for some methods in UfsJournal

    * Minor fix

    * Minor fix. do not use ArrayQueue, use ArrayDequeu

    * Address comments

    * Rename Journal{Reader,Writer}CreateOptions to Journal{Reader,Writer}Options

    * Address Jiri's comments

    * Address calvin's comments.

    * Address comments

    * Address Gene's comments

    * Add a new interface JournalFileParser

    * no binary search

    * style fix

    * minor fix

    * Address comments
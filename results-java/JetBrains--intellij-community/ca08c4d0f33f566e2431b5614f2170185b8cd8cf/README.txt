commit ca08c4d0f33f566e2431b5614f2170185b8cd8cf
Author: Pavel Fatin <pavel.fatin@jetbrains.com>
Date:   Wed Oct 21 14:11:13 2015 +0200

    Using JNA direct mapping in JnaUnixMediatorImpl

    JNA's FAQ says that "you might expect a speedup of about an order of magnitude moving to JNA direct mapping".
    More real-life example: total time of enumerating attributes within /usr is reduced from 7.3s to 5.7s (about 20% improvement).
    The improvement is basically "for free", and, in theory, can speedup IDEA's indexing.
commit 2c671fdb0f2ff9b2c3ac331aac3a098f60e5d63a
Author: Manfred Bergmann <mb@software-by-mabe.com>
Date:   Thu Jun 26 21:45:52 2014 +0200

    some refactorings. considering varchar uses 4 byte if empty made me switch to a one entity approach having a simgle value column which can take upto 200k.
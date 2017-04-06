commit 35709f62b65cde39f8ba420559d091e247a6adce
Author: Adrien Grand <jpountz@gmail.com>
Date:   Thu Jan 21 14:22:20 2016 +0100

    Be stricter about parsing boolean values in mappings.

    Parsing is currently very lenient, which has the bad side-effect that if you
    have a typo and pass eg. `store: fasle` this will actually be interpreted as
    `store: true`. Since mappings can't be changed after the fact, it is quite bad
    if it happens on an index that already contains data.

    Note that this does not cover all settings that accept a boolean, but since the
    PR was quite hard to build and already covers some main settirgs like `store`
    or `doc_values` this would already be a good incremental improvement.
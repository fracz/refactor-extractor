commit 04d72abde1efc963ebb2f145f859552e83b92d13
Author: Clara Bayarri <clarabayarri@google.com>
Date:   Tue Jan 10 09:31:51 2017 -0800

    Expose fonts.xml via public API with a service

    This change creates a new FontManagerService, in charge of providing
    font management data. It exposes a public API to retrieve the
    information in fonts.xml without accessing it directly. To do this,
    it also refactors FontListParser's internal classes into a new public
    FontConfig class holding all the font data.

    getSystemFonts() returns all the available information in fonts.xml
    as well as file descriptors for all the fonts. This allows us to
    share the memory consumed by these files between all clients.

    Bug: 34190490
    Test: See attached CTS change in topic
    Change-Id: I0e922f8bcc9a197a1988d04071eb485328d66fb7
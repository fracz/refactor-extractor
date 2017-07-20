commit c26b38146922617bc017238a2d1f8f23a9435c99
Author: mark_story <mark@mark-story.com>
Date:   Sat Aug 30 20:27:37 2014 -0400

    Make words ending in data uninflected.

    Since both metadata and word ending in metadata have caused issues in
    the past, uninflecting them seems like the best option. This will also
    cover cases like ProfileData not being inflected to ProfileDatum which
    seems like an improvement to me.

    Fixes #4419
commit c8f42ef92dd419f99c8eaebdc94f24c8feb1c4db
Author: Havoc Pennington <hp@pobox.com>
Date:   Mon Oct 13 11:23:07 2014 -0400

    Make ResolveContext immutable

    This is a mechanical refactoring not intended to change behavior.
    Not very efficiently implemented but that's fine for now.
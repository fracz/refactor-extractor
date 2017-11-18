commit ea863751776c4b0e24becd848d0bcf9db49d9837
Author: Davide Grohmann <davide.grohmann@neotechnology.com>
Date:   Wed Jan 28 17:17:05 2015 +0100

    Make indexes platform independent

    The previous Base64 encoding could introduce new lines if the encoded
    string was longer than a given number of bytes.  Since new line
    encodings are platform dependent so were our indexes.  This change
    fixes that by removing the new lines from the Base64 encoding and
    provides a migration path from older versions of the database, i.e.,
    indexes that might contain the problematic encoding as values will be
    forcely rebuilt.

    Bump store version up to '0.A.5' in order to fail when trying to use
    stores from 2.2-M0{1,2,3}.

    Small refactor around UpgredableDatabase: added method
    hasCurrentVersion for checking if the store has the current and
    avoiding using the NeoStoreUtil directly.
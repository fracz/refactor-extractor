commit 3ed32e022efe12e5cfbaecc45ef68a00b160a01a
Author: Simon Willnauer <simonw@apache.org>
Date:   Tue Sep 16 14:15:00 2014 +0200

    [SNAPSHOT] Trigger retry logic also if we hit a JsonException

    We rely on retry logic when reading a snapshot since it's concurrently
    serialized. We should move to a better logic here but the refactoring
    of the blobstore change the semantics and this now throws Json
    exceptions rather than returning an unexpected Token
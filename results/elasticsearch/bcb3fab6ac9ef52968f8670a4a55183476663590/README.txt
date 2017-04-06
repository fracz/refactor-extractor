commit bcb3fab6ac9ef52968f8670a4a55183476663590
Author: Boaz Leskes <b.leskes@gmail.com>
Date:   Fri Oct 2 14:08:10 2015 +0200

    Engine: Remove Engine.Create

    The `_create` API is handy way to specify an index operation should only be done if the document doesn't exist. This is currently implemented in explicit code paths all the way down to the engine. However, conceptually this is no different than any other versioned operation - instead of requiring a document is on a specific version, we require it to be deleted (or non-existent). This PR removes Engine.Create in favor of a slight extension in the VersionType logic.

    There are however a couple of side effects:
    - DocumentAlreadyExistsException is removed and VersionConflictException is used instead (with an improved error message)
    - Update will reject version parameters if the upsert option is used (it doesn't compute anyway).
    - Translog.Create is also removed infavor of Translog.Index (that's OK because their binary format was the same, so we can just read Translog.Index of the translog file)

    Closes #13955
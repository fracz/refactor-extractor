commit be5f49fb6e17e0b9588d3b94022b7e3eb6d47317
Author: Gilles Debunne <debunne@google.com>
Date:   Tue Oct 25 15:05:16 2011 -0700

    Performance improvements for long text edition.

    Limit each parse to batches of a few words, to keep the UI thread
    responsive.

    Possible optimizations for the future:
    - SpellCheck in a thread, but that requires some locking mecanism
    - Only spell check what is visible on screen. Will require additional
      spans to tag the pieces of text.

    This is a cherry pick of 145656 into ICS-MR1

    Patch Set 2: Make the Runnable shared and stop it when detached.

    Change-Id: Ibf8e98274bda84b7176aac181ff267fc1f1fa4cb
commit 35199f5ce7cfc433628a1beda84d80fcaa475e41
Author: Gilles Debunne <debunne@google.com>
Date:   Tue Oct 25 15:05:16 2011 -0700

    Performance improvements for long text edition.

    Limit each parse to batches of a few words, to keep the UI thread
    responsive.

    Possible optimizations for the future:
    - SpellCheck in a thread, but that requires some locking mecanism
    - Only spell check what is visible on screen. Will require additional
      spans to tag the pieces of text.

    Change-Id: Ibf8e98274bda84b7176aac181ff267fc1f1fa4cb
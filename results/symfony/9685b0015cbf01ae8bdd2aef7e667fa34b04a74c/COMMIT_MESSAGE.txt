commit 9685b0015cbf01ae8bdd2aef7e667fa34b04a74c
Merge: 769b71f 34494b3
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Tue Sep 6 08:57:16 2011 +0200

    merged branch brki/mimetype-extension-guesser-refactor (PR #1386)

    Commits
    -------

    34494b3 whitespace fixes
    1a86a4a Refactor mime-type to file extension guessing
    e7481a3 Decouple mime-type to extension logic from File class

    Discussion
    ----------

    [2.1] Decouple mimetype-to-extension logic from File class

    This allows guessing the extension from a given mime type
    without requiring the existence of a local file.

    If a file's meta information (mime-type, etc.) is already available (i.e. it's
    been extracted once and stored in some persistent data store), it would be
    nice to be able to make a best-guess on the extension based on the known mime-type.

    A concrete use case of this is for the symfony-cmf, where a file has been stored
    in the jackrabbit data store.  When delivering this file or saving it to disk, we'd like to
    use an extension that's created based on the known mime type of the file.

    ---------------------------------------------------------------------------

    by brki at 2011/06/21 04:35:13 -0700

    Now implemented similarly to the existing MimeTypeGuesser.

    ---------------------------------------------------------------------------

    by brki at 2011/06/21 07:51:22 -0700

    whitespace removed

    ---------------------------------------------------------------------------

    by stof at 2011/09/04 05:04:54 -0700

    @fabpot @brki what is the status of this PR ?
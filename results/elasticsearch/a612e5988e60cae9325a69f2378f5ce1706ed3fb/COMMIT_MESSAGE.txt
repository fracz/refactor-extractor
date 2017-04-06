commit a612e5988e60cae9325a69f2378f5ce1706ed3fb
Author: Nik Everett <nik9000@gmail.com>
Date:   Mon Oct 31 17:03:06 2016 -0400

    Bump reindex-from-remote's buffer to 200mb

    It was 10mb and that was causing trouble when folks reindex-from-remoted
    with large documents.

    We also improve the error reporting so it tells folks to use a smaller
    batch size if they hit a buffer size exception. Finally, adds some docs
    to reindex-from-remote mentioning the buffer and giving an example of
    lowering the size.

    Closes #21185
commit fcb54d8be7777b899d4d2da31419591b12a765d6
Author: Ben Kwa <kenobi@google.com>
Date:   Thu Dec 10 15:21:18 2015 -0800

    Wrap up the stable ID refactor.

    - Rationalize band selection: make it internally a range selection
      operation, that translates positions to IDs only when updating the
      Selection.
    - Clean up TODOs and comments.
    - Fix selection adjustment when things are removed from the view.

    Change-Id: If917eb9dd18e755c5a0ce83c84409902c4ef3d2e
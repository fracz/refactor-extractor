commit 64c5a1430f76eaa9e9f06ae7d09729172b1763ab
Author: David Persson <davidpersson@gmx.de>
Date:   Sat May 19 12:45:59 2012 +0200

    QA

    - Minor formatting.
    - Improve Database::_formatters docblock.
    - Split db value tests.
    - Document bypass feature in database value.
    - Do not import Closure class when used for doc purposes.

      Fully namespaced class names should be used for documentation
      purposes. The use block at the top is for "real" dependencies.

      Partially reverts 798ac5d9eeea1bc0c0480e76e4f2b68f8d82ad1f.

    - Set nested list doc type for closures from closure to \Closure.
    - Fix type in net\http\Media::scope().
    - Document test error to exception conversion.
    - Document filter methods.
    - Remove 'This method can be filtered.', default message are added in
      li3_docs.
    - Mini refactor Locale::lookup().
    - Refactor Locale::_preferredAction.
    - Better deprecation message.
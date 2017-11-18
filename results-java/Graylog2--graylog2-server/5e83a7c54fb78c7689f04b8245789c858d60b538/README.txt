commit 5e83a7c54fb78c7689f04b8245789c858d60b538
Author: Bernd Ahlers <bernd@graylog.com>
Date:   Wed Jun 17 13:49:15 2015 +0200

    Some refactorings for DocsHelper.

    - Move back "HELP_*" constants to UI.
    - Make DOCS_URL final and private.
    - Replace usage of StringBuffer with string concat.
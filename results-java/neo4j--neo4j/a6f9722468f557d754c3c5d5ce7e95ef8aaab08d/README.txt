commit a6f9722468f557d754c3c5d5ce7e95ef8aaab08d
Author: Mattias Persson <mattias@neotechnology.com>
Date:   Tue Aug 16 15:38:52 2016 +0200

    Defaults REST traversal expander to all relationships

    Fixes #7497

    which is the case in previous versions. There was a recent mistake in
    a refactoring making the default be empty instead.
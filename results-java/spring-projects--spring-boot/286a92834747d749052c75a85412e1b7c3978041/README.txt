commit 286a92834747d749052c75a85412e1b7c3978041
Author: Vedran Pavic <vedran.pavic@gmail.com>
Date:   Wed Aug 3 10:00:27 2016 +0200

    Improve database initializers

    This commit improves database initializers for Spring Batch and Spring
    Session by introducing `AbstractDatabaseInitializer` which eliminates
    duplicated logic in existing initializers. Additionally, database
    platform resolution now relies on `DatabaseDriver`.

    See gh-6543
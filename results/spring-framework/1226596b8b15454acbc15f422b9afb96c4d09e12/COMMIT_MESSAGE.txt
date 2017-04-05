commit 1226596b8b15454acbc15f422b9afb96c4d09e12
Author: Sam Brannen <sam@sambrannen.com>
Date:   Tue Apr 14 23:00:43 2015 +0200

    Document connection handling in ScriptUtils

    This commit improves the documentation for ScriptUtils by explicitly
    mentioning that a JDBC Connection supplied to the one of the
    executeSqlScript() methods will not be released automatically.

    Issue: SPR-12908
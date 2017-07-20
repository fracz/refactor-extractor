commit 34e238be6261fac5d3af54f11cf8639016910a1a
Author: Richard Fath <richard67@users.noreply.github.com>
Date:   Sat May 7 18:33:50 2016 +0200

    Utf8mb4 refactoring - remove duplicate code and fix utf8mb4 to utf8 statement downgrade (#9847)

    * Correct sql statement downgrading of db driver

    Correct the sql statement utf8mb4 to utf8 downgrade function of the db
    driver to be tolerant regarding spaces or case in alter table statement
    and not replace utf8mb4 by utf8 in qouted text or table or column names.

    * Remove local duplicates of db driver functions

    Remove the local implementations of db driver functions for utf8mb4
    stuff which once were added to support updating Joomla! Core with
    extension installer, and use db driver functions instead, since we can
    rely on the new db driver being available when using Joomla! Update
    component.

    * Let db schema manager use installer script

    The schema manager uses script.php anyway just a few lines above calling
    the local conversion function, so it can use also the conversion
    function of script.php instead of a local one. Both functions are equal
    anyway except of the special message text used in script.php, and this
    also can be used for the schema manager, the message text might have to
    be changed maybe for that.

    * Remove obsolete call to prepareUtf8mb4StatusTable

    This call to prepareUtf8mb4StatusTable() is not necessary anymore since
    recently the same call to this function had been added to populateStatem
    which runs before the changeset is created and so does all what is
    needed.

    * Remove local duplicates also from installer lib

    Remove the local implementations of db driver functions  for utf8mb4
    stuff which once were added to support  updating Joomla! Core with
    extension installer, and use db driver functions instead, since we can
    rely on the new db driver being available when using Joomla! Update
    component.

    * Optimize error message

    Show message that db fixer should be checked only if requested by a new
    parameter flag, e.g. not show it if already in the database fixer.
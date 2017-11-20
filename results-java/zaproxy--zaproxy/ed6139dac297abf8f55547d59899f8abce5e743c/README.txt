commit ed6139dac297abf8f55547d59899f8abce5e743c
Author: thc202 <thc202@gmail.com>
Date:   Fri Jun 10 01:43:25 2016 +0100

    Do not clean up unsaved (file based) sessions

    Do not clean up the database (that is, remove temporary messages) of
    unsaved sessions if the database implementation is HSQLDB (file based),
    the files of that database are deleted if the session is not saved, just
    before creating or opening a new one.
    The change improves the time it takes to create or open a session when
    the previous session was not (effectively) saved.

    More detailed changes:
     - Session, add a method that tells if the session requires a clean up
     or not and change it to use that method when closing the database (to
     clean or not the database), before opening a session;
     - Model, change to check if the session requires clean up when closing
     the database, before creating a new session.

    Related to #2506 - Do not discard the session for file based database
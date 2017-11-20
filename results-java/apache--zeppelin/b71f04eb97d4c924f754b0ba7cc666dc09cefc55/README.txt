commit b71f04eb97d4c924f754b0ba7cc666dc09cefc55
Author: Khalid Huseynov <khalidhnv@nflabs.com>
Date:   Tue Jul 21 11:17:35 2015 +0900

    Sync functionality with another storage system

    This PR is to handle the synchronization of notebooks between local Zeppelin fs and remote storage system.

    Corresponding issue: https://issues.apache.org/jira/browse/ZEPPELIN-133

    TODO:
    - [x] sync non existing files at start
    - [x] sync modified files at start
    - [x] save to both storages on save()
    - [x] delete from both storages on remove()
    - [x] pull new/updated notes from secondary storage
    - [x] add comma separated storage class definitions
    - [x] corresponding tests
    - [x] handle failure cases of secondary storage
    - [x] handle updated notes on Paragraph level instead of Note class itself

    Author: Khalid Huseynov <khalidhnv@nflabs.com>

    Closes #123 from khalidhuseynov/sync-with-remote and squashes the following commits:

    14c87ff [Khalid Huseynov] Merge branch 'master' into sync-with-remote Latest updates
    cbe167c [Khalid Huseynov] minor test change + log removal
    d33ffc7 [Khalid Huseynov] check latest modified from Paragraph fields
    c2827ff [Khalid Huseynov] delete modTime field from Note and NoteInfo
    4293723 [Khalid Huseynov] add initial tests
    a441f9d [Khalid Huseynov] catch write to secondary storage failed exception, write to main storage
    5a967f1 [Khalid Huseynov] logic ramification; add comma-separated storage class names; use list of repos
    3314971 [Khalid Huseynov] remove more comments
    b6a3590 [Khalid Huseynov] minor refactoring of getting storage class
    7c4366d [Khalid Huseynov] remove commented code section and redundant type conversion
    d001ebf [Khalid Huseynov] function name/description change
    3899f8e [Khalid Huseynov] minor changes to log comments
    359a41e [Khalid Huseynov] minor fix of default classname
    51e6271 [Khalid Huseynov] change modTime of Note object on save()
    5f7ff18 [Khalid Huseynov] use modTime field for uploading modified notes
    5b8d994 [Khalid Huseynov] modify containsID() function to return object instead of boolean
    032e59b [Khalid Huseynov] add modification time field for Note and NoteInfo
    e778c4e [Khalid Huseynov] initial sync implementation
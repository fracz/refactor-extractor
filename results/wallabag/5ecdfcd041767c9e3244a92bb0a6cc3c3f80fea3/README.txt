commit 5ecdfcd041767c9e3244a92bb0a6cc3c3f80fea3
Author: Thomas Citharel <tcit@tcit.fr>
Date:   Tue Mar 8 17:02:34 2016 +0100

    manage assets through npm

    first draft

    remote assetic totally

    work

    nearly there

    use at least nodejs > 0.12

    use proper version of grunt

    bump nodejs version for travis

    update npm

    workaround for materialize

    install node 5.0

    add grunt-cli

    baggy theme & cache node modules

    cache bower & npm

    make travis build assets on php7 only

    exclude installing node & npm if not needed & use bash

    clean & try to make icomoon work on baggy

    ready

    config for travis

    rebase

    make travis work

    more travis work

    impove travis & update deps

    add missing pixrem deps

    add module through oddly lost

    ui updates

    install latest nodejs

    add install_dev.sh, link local binaries for npm/bower/grunt

    ui improvements (mostly baggy)

    fix travis build

    no need to install on travis

    Add unread filter to entries pages

    Add the ability to filter for unread pages in the filters menu.

    Add unread filter test to EntryControllerTest

    Add a new test to the EntryControllerTest collection which checks that
    only entries which have not been archived (and are treated as "unread")
    are retrieved.

    Improve English translation

    Update FAQ

    -Fix grammar
    -Add notes about MTA, firewall, and SELinux

    Update installation instructions

    -Fix grammar
    -Add SELinux section

    add screenshots of android docu in English

    Fix the deletion of Tags/Entries relation when delete an entry
    Fix #2121

    Move fixtures to the right place

    Display a message when saving an entry failed

    When saving an entry fail because of database error we previously just returned `false`.
    Now we got an error in the log and the displayed notice to the user is updated too.

    Change ManyToMany between entry & tag

    Following https://gist.github.com/Ocramius/3121916

    Be sure to remove the related entity when removing an entity.

    Let say you have Entry -> EntryTag -> Tag.
    If you remove the entry:

     - before that commit, the EntryTag will stay (at least using SQLite).
     - with that commit, the related entity is removed

    Prepare wallabag 2.0.5

    enforce older materialize version
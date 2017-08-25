commit 521a6ab04d06e5ffdb444ea766ccbf352e622aab
Author: Penny Leach <penny@liip.ch>
Date:   Tue Dec 15 12:36:49 2009 +0000

    portfolio MDL-20896 added the ability to set mime info for "intended" files

    This means for places in Moodle that are going to write a file, like a
    CSV file, they can set the intended mimetype of the generated file.

    Previously you had to use a stored_file object.

    This also gets rid of portfolio_fake_add_url function and replaces the
    data module implementation with a button.  I also refactored
    portfolio_add_button::to_html to use moodle_url so it's easy to return
    the same parameters to hidden form fields, an escaped url (for a link),
    and a non escaped url (to redirect to, which is what the data module
    does)
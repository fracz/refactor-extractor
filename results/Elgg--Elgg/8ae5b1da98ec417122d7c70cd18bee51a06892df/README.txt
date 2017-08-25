commit 8ae5b1da98ec417122d7c70cd18bee51a06892df
Author: Ismayil Khayredinov <ismayil.khayredinov@gmail.com>
Date:   Fri Jul 8 10:52:34 2016 +0200

    feature(views): improves usability of object listing views

    Improves usability of summary and full view listings by wrapping
    listing components in their own views, which should allow plugins
    to target listing elements via hooks and override individual parts
    without having to hijack an entire view.

    Adds class selectors to make it easier to style object listings.
commit e300ae5a06d2b8555d4790ba885d7189b13ede32
Author: Francis Morissette <morissette.francis@gmail.com>
Date:   Tue May 31 20:16:39 2016 -0400

    Dates improved
    Invoice items now have Carbon versions for start and end dates in subscriptions (allows formatting and localization), also overridden the default template for receipts.
    Invoice date now uses UTC for instantiation since Stripe's API uses UTC for all dates (see https://support.stripe.com/questions/what-timezone-does-the-dashboard-and-api-use)

    Non breaking changes.
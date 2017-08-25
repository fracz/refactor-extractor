commit 11dd562cd625ffca797563b4c2b411537e66681a
Author: Ismayil Khayredinov <ismayil.khayredinov@gmail.com>
Date:   Fri Jul 8 11:32:26 2016 +0200

    feature(notifications): refactor notification system for improved
    usability

    Improves usability of instant and subscription notifications:

    - Adds "origin" to notification object parameters making it easier to distinguish between
    instant and subscription notifications

    - Adds a high level "prepare, notification" plugin hook that can be used to filter
    both subscription and instant notifications.
    Normalizes parameters sent to the hook for both subscription types.

    - Adds "format, notification:$method" hook, which can be used to format notification subject
    and/or body, to make it suitable for the delivery method. For example, plugins may strip
    HTML tags from notifcation body for plaintext emails, or wrap the body in an HTML template
    for HTML emails.

    - Instant notifications are now sent by the notifications service triggering
    a set of hooks similar to subscriptions notifications.

    - Help developers debug notifications by logging notifications and notification
    event results

    - Notification event serialization is now more reliable and tested.

    - Adds API to retrieve registered notification methods

    - Adds ElggUser::getNotificationSettings() and
      ElggUser::setNotificationSetting()
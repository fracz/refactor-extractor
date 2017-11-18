commit e40451a89dc91dfd636af7cb32a23b4a4cc93fdc
Author: Daniel Sandler <dsandler@google.com>
Date:   Thu Feb 3 14:51:35 2011 -0500

    Ongoing notification for GPS use.

    This change improves upon the notification priority API
    introduced in change I9e738cc4, allowing privileged clients
    to set the priority of a notification when posting it
    directly to INotificationManager. StatusBarTest is updated
    to test this new feature.

    The new LocationController in SystemUI uses this facility to
    post a high-priority ongoing notification whenever GPS is in
    use (replacing the functionality of the legacy GPS status
    bar icon).

    Also happens to fix http://b/3325472 (adding a log message
    when notifications are dropped because of a missing icon).

    Bug: 3412807

    Change-Id: I523016ffa53bf979be98ddc4a2deb55a6270c68a
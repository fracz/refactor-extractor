commit af951b0627902cce4bbc1120eeb6d0e442b4ec00
Author: Johannes Edmeier <johannes.edmeier@gmail.com>
Date:   Fri Sep 16 18:40:07 2016 +0200

    Make the AbstractNotifier work for any event.

    With this change the AbstractNotifier isn't specialized on status changes, but
    the default behaviour for the existing notifiers isn't changed.
    In addition the existing notifiers have been improved so that subclassing has
    become easier.
    To have existing notifiers acting on other events you need to override
    shouldNotify().

    fixes #270
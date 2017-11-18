commit baf3094ecaa073e1cb3e807fa17d096f826c3968
Author: Jean Chalard <jchalard@google.com>
Date:   Thu Feb 28 16:01:51 2013 -0800

    Add a SpanWatcher to catch programmatic selection changes

    When the selection is set via Selection#setSelection, the
    Editor needs to know so that it can call back to a bound
    IME, if any. This adds a watcher to catch these events.
    This is step 2 of a four-step refactoring.

    Bug: 8000119
    Change-Id: Ia01aee853d5cafb4820fd234bc24b587ad3f7dd0
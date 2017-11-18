commit 8b8a6fd1936aa5acd50191108f4a02670c1f8900
Author: Steve McKay <smckay@google.com>
Date:   Tue Mar 1 11:26:00 2016 -0800

    Ensure directory names are aligned correctly...

    In list view...and don't show dir dates, even when present.
    In fact, no details on folders at all when in list view.
    Also improve layout of files without details (if they exist).

    Bug: 27406730
    Change-Id: Ida4b5f99666c4a145ca98a3c15c480e59a76b4a6
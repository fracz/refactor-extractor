commit b858dfda5012a1040927ed62c3bb856c3294d882
Author: Dianne Hackborn <hackbod@google.com>
Date:   Tue Feb 2 10:49:14 2010 -0800

    Implement system data migration support.

    This adds three new features:

    - <original-package android:name="com.foo" /> manifest tag.
      This allows an .apk to specify another package it originally came from,
      propagating all state and data from the old to new package.

    - <adopt-permissions android:name="com.foo" /> manifest tag.
      In some more complicated cases, a new .apk may be a combination
      of multiple older .apks that each declared their own permissions.
      This allows you to propagate the permissions from these other
      .apks into the new one.

    - A new system/etc/updatecmds directory.
      You can place files here which describe data files to move from
      one package to another.  (See below for details.)

    Also in this change: we now clean up the data directories of
    .apks that disappear from the system image, and some improvements
    to logging and reporting error messages.

    A typical file in the updatecmds directory looks like this:

    -------
    com.google.android.gsf:com.google.android.providers.talk
        databases/talk.db
    com.google.android.gsf:com.google.android.googleapps
        databases/gls.db
    -------

    This says that for com.google.android.sfs, there are two packages to
    move files from:

    From com.google.android.providers.talk, the file databases/talk.db.
    From com.google.android.googleapps, the file databases/gls.db

    As part of moving the file, its owner will be changed from the old
    package to whoever is the owner of the new package's data directory.

    If those two files had existed, after booting you would now have the
    files:

    /data/data/com.google.android.gsf/databases/talk.db
    /data/data/com.google.android.gsf/databases/gls.db

    Note that all three of these facilities assume that the older .apk
    is completely removed from the newer system.  The WILL NOT work
    correctly if the older .apk still remains.
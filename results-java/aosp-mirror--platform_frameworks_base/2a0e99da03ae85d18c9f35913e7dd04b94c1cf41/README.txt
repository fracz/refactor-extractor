commit 2a0e99da03ae85d18c9f35913e7dd04b94c1cf41
Author: Adam Powell <adamp@google.com>
Date:   Thu Aug 4 10:55:03 2011 -0700

    Fix bug 5118908 - ImageView.setImageDrawable always requests layout

    Make ImageView a bit more conservative about when it requests a
    layout. This improves performance for ListViews where apps
    asynchronously load images and replace placeholders outside of the
    optimized getView path.

    Change-Id: I564a4a343ab9c8c2d5baf907b5f573b5ee02c87a
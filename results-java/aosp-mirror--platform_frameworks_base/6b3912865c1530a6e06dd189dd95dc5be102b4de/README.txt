commit 6b3912865c1530a6e06dd189dd95dc5be102b4de
Author: Chris Tate <ctate@google.com>
Date:   Thu Oct 14 15:48:59 2010 -0700

    Drag thumbnail fixes / improvements

    * Properly wipe the thumbnail canvas before handing it to the app for
      its contents to be drawn

    * Provide a getView() method in DragThumbnailBuilder that allows
      clients or subclasses to get at the associated view.  This is
      especially for clients that want to draw entire (sub)layouts as
      the drag thumbnail, by overriding onDrawThumbnail(Canvas c)
      like this:

      // Override specifically for drawing a whole ViewGroup into
      // the drag thumbnail canvas
      @Override
      public void onDrawThumbnail(Canvas c) {
          getView().dispatchDraw(c);
      }

    Change-Id: Ib43ddd7cf1d44faf2d7f6ba79f102bc3c7f14596
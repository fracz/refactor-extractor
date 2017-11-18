commit 9279d45b8d26c88c69fa917816ea396142627020
Author: Daniel Sandler <dsandler@android.com>
Date:   Thu Oct 17 13:05:53 2013 -0400

    DessertCase memory improvements

      - reuse intermediate bitmaps when loading resources
      - only use hardware layers on views that are animating

    Bug: 11269977
    Change-Id: I39ad7aff16468632da47448404416404f3b54cc4
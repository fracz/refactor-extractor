commit 931d094c2449082e9d4b51e8ab1113a4bca9719e
Author: daz <darrell.deboer@gradleware.com>
Date:   Wed Dec 18 14:06:26 2013 -0700

    More native model refactoring:
    - Inlined NativeComponent into ProjectNativeComponent
      - Removed NativeComponent altogether
      - Executable/Library now extend ProjectNativeComponent
      - PrebuiltLibrary is independent of ProjectNativeComponent
      - Remove utility interfaces joining Executable/Library to ProjectNativeComponent

    This is effectively reverting earlier changes to the component hierarchy, by simply
    renaming NativeComponent -> ProjectNativeComponent. This is now possible due to the
    unlinking of LibraryBinary and PrebuiltLibrary from NativeComponent
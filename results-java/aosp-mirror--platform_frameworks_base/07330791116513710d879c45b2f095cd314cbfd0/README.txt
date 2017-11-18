commit 07330791116513710d879c45b2f095cd314cbfd0
Author: Jeff Brown <jeffbrown@google.com>
Date:   Tue Mar 30 19:57:08 2010 -0700

    Show SD unavailable icon for apps on SD when ejected.

    This change include a minor refactoring of PackageItemInfo and related
    classes to eliminate code duplication and to avoid redundant work
    searching for an ApplicationInfo instance we already have.

    Bug: b/2537578
    Change-Id: Id0794c3f055ea58b943028f7a84abc7dec9d0aac
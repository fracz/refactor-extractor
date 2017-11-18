commit 32dc265d4909540312c1f70d61c836c5a33a1de0
Author: Lari Hotari <lari.hotari@gradle.com>
Date:   Tue Apr 12 17:59:46 2016 -0400

    Add test coverage for OutputFilesCollectionSnapshotter

    - OutputFilesCollectionSnapshotter.createOutputSnapshot replaces
      FileCollectionSnapshot's applyAllChangesSince and updateFrom methods
      - refactored previous tests from DefaultFileCollectionSnapshotterTest

    +review REVIEW-5911
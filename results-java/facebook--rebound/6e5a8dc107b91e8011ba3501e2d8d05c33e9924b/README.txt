commit 6e5a8dc107b91e8011ba3501e2d8d05c33e9924b
Author: Will Bailey <wbailey@fb.com>
Date:   Thu Jan 23 08:46:33 2014 -0800

    Project Restructure

    - reorganize around a standard gradle directory structure
    - create gradle build files
    - separate Android specific utilities from the physics core
      to allow use on other java platforms (processing)
    - create a continuous integration build script for travis CI
    - move to proper semver versioning. The API for rebound is not fully
      finalized we should be at version 0.3
    - API is unchanged

    TODO:
    - maven hosting for build artifacts to allow easy integration
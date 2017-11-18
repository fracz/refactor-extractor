commit 3cd658e1a598e59738bc129c35eecf4cd0f20680
Author: Todd Kennedy <toddke@google.com>
Date:   Tue Aug 16 15:00:31 2016 -0700

    More refactoring

    * Break apart getSettingsWithBenefits() into separate methods for creating
      new settings and updating settings, etc...
    * Add more hooks to test equality of setting clones
    * Create tests for each subset of functionality

    Bug: 30219944
    Change-Id: I1fb3d07bb9279f93ba81ada2ff802989ec2c5965
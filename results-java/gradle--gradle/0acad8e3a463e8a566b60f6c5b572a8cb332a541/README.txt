commit 0acad8e3a463e8a566b60f6c5b572a8cb332a541
Author: Cedric Champeau <cedric.champeau@gradleware.com>
Date:   Thu Jul 9 12:44:52 2015 +0200

    First implementation of variant aware dependency resolution for custom components
       * resolves JavaPlatform in a special case
       * custom variant dimensions are compared by string equality
       * Missing variants are NOT included in error messages yet
       * Test coverage needs improvement

    +review REVIEW-5554
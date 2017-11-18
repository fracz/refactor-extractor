commit 4cc9dfdd8a69748ebab2d054b6f38f393a401cdf
Author: Davide Grohmann <davide.grohmann@neotechnology.com>
Date:   Thu Sep 18 17:03:48 2014 +0200

    ParallelBatchImporterTest should have a test for the writer factory enabled by default

    Details:
     - make sure we test the default configuration in ParallelBatchImporterTest
     - refactor to allow to test all the possible configuration
     - disable the test with IoQueue due to a known issue in parallelizing I/O writes
     - make package protected the constructor used only for testing
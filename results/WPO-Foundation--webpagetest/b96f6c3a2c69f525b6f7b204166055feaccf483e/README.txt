commit b96f6c3a2c69f525b6f7b204166055feaccf483e
Author: pmeenan <pmeenan@webpagetest.org>
Date:   Thu Jul 14 21:40:05 2011 +0000

    Changed the work queues to use job files with serialized arrays that are stored in the temp directory (preferably in RAM) to guarantee test ordering and significantly improve the performance of managing the work queue (dropped the processing time of getwork.php by an order of magnitude).  Eventually want to move this into an APC-backed buffer.
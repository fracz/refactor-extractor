commit e04b8be52e297cee8122817bdf5491415e1fb36e
Author: dgolitsyn <golitsyn.dima@gmail.com>
Date:   Wed Jun 28 07:58:36 2017 +0400

    maxSegmentsInQueue in CoordinatorDinamicConfig (#4445)

    * Add maxSegmentsInQueue parameter to CoordinatorDinamicConfig and use it in LoadRule to improve segments loading and replication time

    * Rename maxSegmentsInQueue to maxSegmentsInNodeLoadingQueue

    * Make CoordinatorDynamicConfig constructor private; add/fix tests; set default maxSegmentsInNodeLoadingQueue to 0 (unbounded)

    * Docs added for maxSegmentsInNodeLoadingQueue parameter in CoordinatorDynamicConfig

    * More docs for maxSegmentsInNodeLoadingQueue and style fixes
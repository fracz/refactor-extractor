commit a03860970b36d01bf183a655aef547ee9d9bb5ad
Author: javanna <cavannaluca@gmail.com>
Date:   Sat Aug 9 03:16:05 2014 +0200

    Internal: refactored TransportSingleCustomOperationAction, subclasses and requests

    TransportSingleCustomOperationAction is subclassed by two similar, yet different transport action: TransportAnalyzeAction and TransportGetFieldMappingsAction. Made their difference and similarities more explicit by sharing common code and moving specific code to subclasses:
    - moved index field to the parent SingleCustomOperationAction class
    - moved the common check blocks code to the parent transport action class
    - moved the main transport handler to the TransportAnalyzeAction subclass as it is only used to receive external requests through clients. In the case of the TransportGetFieldMappingsIndexAction instead, the action is internal and executed only locally as part of the user facing TransportGetFieldMappingsAction. The corresponding request gets sent over the transport though as part of the related shard request
    - removed the get field mappings index action from the action names mapping as it is not a transport handler anymore. It was before although never used.

    Closes #7214
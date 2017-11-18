commit 16f286afa6146c03d326cc766af0082d519d72e7
Author: Irina.Chernushina <Irina.Chernushina@jetbrains.com>
Date:   Wed Apr 19 11:53:03 2017 +0200

    json schema refactoring: refactor settings:
    - remove unneeded inheritance of mappings configuration
    - mappings configuration accepts information about refactored paths
    - get rid of JsonSchemaServiceEx
    - since schema files and their mapping to providers is cached already in JsonSchemaServiceImpl, no need to have schema files cache in configuration
    - other minor changes to greenify

    to be done: refactor external schemas resolve, currently not working. get rid of separate definitions class, do everything inside JsonSchemaServiceImpl
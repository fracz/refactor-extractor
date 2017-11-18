commit 2f121f6f94654071ebe6f98d66b1f730aeefa0d3
Author: Irina.Chernushina <Irina.Chernushina@jetbrains.com>
Date:   Thu Apr 20 11:36:13 2017 +0200

    json schema refactoring:
    - json schema service: have methods to get schema object by file/schema file
    - get rid of CodeInsightProviders etc, and keeping annotator, completion contributor, document provider for schema file in cache: now we just cache schema descriptor object as cached value on a file, and json schema-based annotator, completion contributor and documentation provider directly get this descriptor.
    This all is possible only after we only suggest code assistance for files with not duplicated schema mappings (as exception, it is allowed to override the system schema with the user schema)
    - inner state of json schema service is simplified now: we only keep the cache of schema files and corresponding schema providers
    - for support of json schema in javascript, we define the new extension point JsonSchemaInJavaScriptProvider. Implementers would provide information on whether to apply code assistance for the context, and schema file. All other things are implemented in common JavaScript-Json Schema part
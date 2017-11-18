commit 525101b64d9a22ffff9a29c0309444f721ebb763
Author: Jim Ferenczi <jim.ferenczi@elastic.co>
Date:   Thu Apr 20 22:12:20 2017 +0200

    Query string default field (#24214)

    Currently any `query_string` query that use a wildcard field with no matching field is rewritten with the `_all` field.

    For instance:
    ````
    #creating test doc
    PUT testing/t/1
    {
      "test": {
        "field_one": "hello",
        "field_two": "world"
      }
    }
    #searching abc.* (does not exist) -> hit
    GET testing/t/_search
    {
      "query": {
        "query_string": {
          "fields": [
            "abc.*"
          ],
          "query": "hello"
        }
      }
    }
    ````

    This bug first appeared in 5.0 after the query refactoring and impacts only users that use `_all` as default field.
    Indices created in 6.x will not have this problem since `_all` is deactivated in this version.

    This change fixes this bug by returning a MatchNoDocsQuery for any term that expand to an empty list of field.
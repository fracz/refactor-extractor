commit 1cb3068737d2516a614382c1e80def36c60cd194
Author: Nik Everett <nik9000@gmail.com>
Date:   Tue Oct 6 16:02:17 2015 -0400

    Make root_cause of field conflicts more obvious

    Does so by improving the error message passed to MapperParsingException.

    The error messages for mapping conflicts now look like:
    ```
    {
      "error" : {
        "root_cause" : [ {
          "type" : "mapper_parsing_exception",
          "reason" : "Failed to parse mapping [type_one]: Mapper for [text] conflicts with existing mapping in other types:\n[mapper [text] has different [analyzer], mapper [text] is used by multiple types. Set update_all_types to true to update [search_analyzer] across all types., mapper [text] is used by multiple types. Set update_all_types to true to update [search_quote_analyzer] across all types.]"
        } ],
        "type" : "mapper_parsing_exception",
        "reason" : "Failed to parse mapping [type_one]: Mapper for [text] conflicts with existing mapping in other types:\n[mapper [text] has different [analyzer], mapper [text] is used by multiple types. Set update_all_types to true to update [search_analyzer] across all types., mapper [text] is used by multiple types. Set update_all_types to true to update [search_quote_analyzer] across all types.]",
        "caused_by" : {
          "type" : "illegal_argument_exception",
          "reason" : "Mapper for [text] conflicts with existing mapping in other types:\n[mapper [text] has different [analyzer], mapper [text] is used by multiple types. Set update_all_types to true to update [search_analyzer] across all types., mapper [text] is used by multiple types. Set update_all_types to true to update [search_quote_analyzer] across all types.]"
        }
      },
      "status" : 400
    }
    ```

    Closes #12839

    Change implementation

    Rather than make a new exception this improves the error message of the old
    exception.
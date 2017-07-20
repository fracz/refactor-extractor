commit c77fdcb2dc8ce5943e5fcd50147d7d7b6a7346b8
Author: Christian Flothmann <christian.flothmann@gmail.com>
Date:   Sat Nov 1 13:21:10 2014 +0100

    improve error message for multiple documents

    The YAML parser doesn't support multiple documents. This pull requests
    improves the error message when the parser detects multiple YAML
    documents.
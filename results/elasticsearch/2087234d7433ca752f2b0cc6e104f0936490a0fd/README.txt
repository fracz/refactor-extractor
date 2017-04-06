commit 2087234d7433ca752f2b0cc6e104f0936490a0fd
Author: Nik Everett <nik9000@gmail.com>
Date:   Mon Dec 5 10:54:51 2016 -0500

    Timeout improvements for rest client and reindex (#21741)

    Changes the default socket and connection timeouts for the rest
    client from 10 seconds to the more generous 30 seconds.

    Defaults reindex-from-remote to those timeouts and make the
    timeouts configurable like so:
    ```
    POST _reindex
    {
      "source": {
        "remote": {
          "host": "http://otherhost:9200",
          "socket_timeout": "1m",
          "connect_timeout": "10s"
        },
        "index": "source",
        "query": {
          "match": {
            "test": "data"
          }
        }
      },
      "dest": {
        "index": "dest"
      }
    }
    ```

    Closes #21707
commit a306eb773442d26849bfd7d6543bf5749a1e04b5
Author: Marvin S. Addison <marvin.addison@gmail.com>
Date:   Mon Aug 19 14:09:10 2013 -0400

    CAS-1325 Serialization bug fixes/improvements.

    - Added default private constructor to many components to facilitate
      serialization.
    - Store normal collections and provide immutable wrapper on demand.
    - Store null collections and provide empty wrapper on demand.
    - Improve equals support (required for test coverage, but arguably needed
      for correct application behavior).
    - Remove custom serializers (default constructor allows use of defaults in
      many cases).
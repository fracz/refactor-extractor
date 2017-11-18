commit 17406168f0de171b6cdb67e9936561e0eec08e79
Author: Artem Prigoda <arteamon@gmail.com>
Date:   Sun Jul 13 19:20:48 2014 +0400

    Code-review improvements of proxy configuration

    - Use default values for the port and the scheme instead constants and methods getPresentPort and getPresentScheme
    - Use @PortRange with min=-1 instead @Range
    - Use ignoreCase in @OneOf instead enumeration possible schemas in both cases.
    - Reuse constructors
commit 20e06f7f4c9b2811082513a9621729756e10ee42
Author: Lissachenko Alexander <lisachenko.it@gmail.com>
Date:   Wed May 15 12:01:38 2013 +0300

    Small optimization: do not serialize default values for interceptor instances. This will improve speed of unserialization and reduce the data size for serialized content
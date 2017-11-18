commit 847cf342c956eac3dec03b7b29fcb188ffe8804f
Author: Jesse Wilson <jessewilson@google.com>
Date:   Thu Apr 21 11:28:31 2011 -0700

    Pool strings in JsonReader.

    This yields a 10% improvement in parsing both Twitter and Google Reader
    streams.

    Change-Id: Ifc238777e929b5b3e9a4480098b315e418378f8b
    http://b/3201883
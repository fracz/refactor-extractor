commit c4c72ab3d978f5a5a8515365ecc429fe70215b02
Author: Matthias Broecheler <me@matthiasb.com>
Date:   Thu Jan 23 18:38:28 2014 -0800

    Replaced Kryo with custom serializer and added multiple special purpose serializers for common types. Added compressed string data type. Fixes #490.
    Tests not yet refactored.
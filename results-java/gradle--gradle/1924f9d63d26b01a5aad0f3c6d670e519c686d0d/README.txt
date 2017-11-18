commit 1924f9d63d26b01a5aad0f3c6d670e519c686d0d
Author: Adam Murdoch <adam@gradle.com>
Date:   Mon Nov 16 15:24:47 2015 +1100

    `ManagedModelProjection`no longer requires a `ModelSchemaStore` as it can reference the property schemas directly from the struct schema.

    This simplifies the setup for these types and improves performance by removing a schema lookup for each property _instance_.
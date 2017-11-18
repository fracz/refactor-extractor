commit 78bb53b672e87f4448519a17acebcd079e1b8344
Author: Max Sumrall <max.sumrall@neotechnology.com>
Date:   Thu Mar 10 11:55:06 2016 +0100

    Always migrate using batch importer.

    Migration times did not seem to improve by doing a direct migration.
     However we still use the direct migration for migrating token stores.
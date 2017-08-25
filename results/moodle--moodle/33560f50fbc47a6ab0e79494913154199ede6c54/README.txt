commit 33560f50fbc47a6ab0e79494913154199ede6c54
Author: David Mudrak <david@moodle.com>
Date:   Tue Jun 7 18:25:36 2011 +0200

    MDL-22414 Added new inforef manager

    This helper class keeps the referenced ids to be dumped into the
    inforef.xml files. For now, it uses a in-memory storage structures but
    in the future we may refactor it so that it can use some persistent
    storage (like converter's stashes).
commit 1bfdac6a171caab4a5b33c5756476e63878bc7dc
Author: Nileema Shingte <nileema.shingte@gmail.com>
Date:   Tue Aug 11 10:54:20 2015 -0700

    Minor refactoring for MetadataDao

    Change metadataDao.updateTemporalColumnId(columnId, tableId) to
    metadataDao.updateTemporalColumnId(tableId, columnId)
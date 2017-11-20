commit b17df331b478ed0320840056f8dd0e151b59beec
Author: l.garulli <l.garulli@3625ad7b-9c83-922f-a72b-73d79161f2ea>
Date:   Fri May 14 16:49:44 2010 +0000

    Huge refactoring on:
    - serialization format (added " even in storage for strings), better support for floats
    - new db configuration settings such as: localeLanguage, localeCountry, schemaRecordId, securityRecordId, dictionaryRecordId
    - User management. Now all the settings reside in the new ORole class
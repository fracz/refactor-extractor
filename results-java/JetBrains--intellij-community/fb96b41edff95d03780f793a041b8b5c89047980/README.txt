commit fb96b41edff95d03780f793a041b8b5c89047980
Author: irengrig <Irina.Chernushina@jetbrains.com>
Date:   Mon Jan 16 13:38:55 2017 +0100

    json schema support major refactoring:
    1 read schema files through psi -> this allows to have psi references to immer schema objects and easy navigation
    2 simplify access by schema id -> remove additional cache of all definitions in the schema that can be referenced, have just id -> file cache
    3 do schema resolve mainly by climbing the in-memory schema object, considering variants with definition addresses, accessing additional schema files as needed -> and use psi references for navigation
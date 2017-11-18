commit 95cf95b8f79cf5513cefad9b6192d2b8af244d70
Author: irengrig <Irina.Chernushina@jetbrains.com>
Date:   Fri Nov 11 17:30:12 2016 +0100

    Json schema: improve highlighting speed (invalidate all json files corresponding to the schema on schema change). fixes WEB-21418 JSON Schema: if file under the schema is placed in the split Editor tab update comes later than should be
    key: "json.schema.fast.annotation.update" (turned on by default)
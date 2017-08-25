commit 6972c07a61c374fc3782da7be5f97df5abb1fbba
Author: poltawski <poltawski>
Date:   Sun Dec 23 16:18:25 2007 +0000

    MDL-10241 - unenrolling self wasn't working properly from user profile
    because unenrol.php was doing wrong capability check when $userid set.o
    Also improves the lanaguage used when unenrolling self.
    merged from MOODLE_18_STABLE
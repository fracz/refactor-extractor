commit bce7279df247a3330bd177b29420eed3c29bd633
Author: Dan LaRocque <dalaro@hopcount.org>
Date:   Tue Jul 29 09:18:07 2014 -0400

    Temporary fix for relative ES directory, conf-file

    Relative paths for ES's directory and conf-file options used to be
    interpreted relative to the config file in which they appeared, same
    as storage.directory and storage.conf-file, but this broke a while
    back.  It only broke for indices though -- not storage backends.

    This commit is a short-term fix that restores the old interpretation
    for indices.  The code this commit touches is a relic from the days of
    hard-coded string config keys and should be refactored out of
    existince by extending ConfigOption.
commit b669e1a3c13dc6fabc8929a4abc4fd13fb8a4450
Author: Arthur Schiwon <blizzz@owncloud.com>
Date:   Wed Jan 8 12:07:57 2014 +0100

    LDAP: implement userCount action. This required to (finally) clean up and refactor the search method, which will have a positive performance impact on paged search.
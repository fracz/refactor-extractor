commit 6c92a85d49822dba180fe949d211db60c5b40d51
Author: Arthur Schiwon <blizzz@owncloud.com>
Date:   Thu Jul 26 16:11:23 2012 +0200

    LDAP: use OC_Cache to cache results from LDAP. Default is set to 10 min. Should improve performance especially when LDAP users use the sync client, because userExists checks with the LDAP server are reduced.
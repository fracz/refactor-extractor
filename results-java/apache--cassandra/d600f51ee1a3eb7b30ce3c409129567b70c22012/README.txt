commit d600f51ee1a3eb7b30ce3c409129567b70c22012
Author: Stefania Alborghetti <stefania.alborghetti@datastax.com>
Date:   Tue Aug 30 16:08:09 2016 +0800

    Handle composite prefixes with final EOC=0 as in 2.x and refactor LegacyLayout.decodeBound

    patch by Stefania Alborghetti and Sylvain Lebresne; reviewed by Tyler Hobbs and
    Sylvain Lebresne for CASSANDRA-12423
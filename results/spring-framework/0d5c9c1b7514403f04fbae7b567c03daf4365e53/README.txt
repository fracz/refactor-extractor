commit 0d5c9c1b7514403f04fbae7b567c03daf4365e53
Author: Thomas Risberg <trisberg@vmware.com>
Date:   Wed Jun 3 19:26:24 2009 +0000

    improved integration; added delegation to NamedParameterJdbcTemplate for batchUpdate from SimpleJdbcTemplate; added call to proteced getParsedSql method; fixed bugs (SPR-3322, SPR-5162)
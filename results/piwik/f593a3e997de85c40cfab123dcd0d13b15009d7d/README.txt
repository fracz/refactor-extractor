commit f593a3e997de85c40cfab123dcd0d13b15009d7d
Author: mattpiwik <matthieu.aubry@gmail.com>
Date:   Tue Jun 1 13:40:05 2010 +0000

    Adding ts_archived to the archive_numeric table INDEX which improves the explain plan significantly.
    Example query:
    explain SELECT idarchive, value, name, date1 as startDate FROM piwik_archive_numeric_2010_05 WHERE idsite = 1 AND date1 = '2010-05-03' AND date2 = '2010-05-03' AND period = 1 AND ( (name = 'done' AND value = 1) OR (name = 'done' AND value = 3) OR name = 'nb_visits') AND ts_archived >= '2010-05-03 18:15:00' ORDER BY ts_archived DESC

    Test URL: index.php?module=Goals&action=getEvolutionGraph&idSite=1&period=day&date=2010-05-03,2010-06-01&viewDataTable=sparkline&columns[]=Goal_nb_conversions

    git-svn-id: http://dev.piwik.org/svn/trunk@2250 59fd770c-687e-43c8-a1e3-f5a4ff64c105
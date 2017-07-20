commit 3dd4ae105c70d3588f4b069d3fbd5721f314e963
Author: mattpiwik <matthieu.aubry@gmail.com>
Date:   Tue Aug 9 07:25:23 2011 +0000

    Refs #2327
     * Adding new archive.php optimized rewrite of archive.sh - see description below
     * Adding new API to return only active website ID with visits since $timestamp (which is used to get all sites with visits since last archive execution)
    {{{
     * Description
     * This script is a much optimized rewrite of archive.sh in PHP
     * allowing for more flexibility and better performance when Piwik tracks thousands of websites.
     *
     * What this script does:
     * - Fetches Super User token_auth from config file
     * - Calls API to get the list of all websites Ids with new visits since the last archive.php succesful run
     * - Calls API to get the list of segments to pre-process
     * The script then loops over these websites & segments and calls the API to pre-process these reports.
     * It does try to achieve Near real time for "daily" reports, processing them as often as possible.
     *
     * Notes about the algorithm:
     * - To improve performance, API is called with date=last1 whenever possible, instead of last52
     * - The script will only process (or re-process) reports for Current week / Current month
     *       or Current year at most once per hour.
     *   You can change this timeout as a parameter of the archive.php script.
     *   The concept is to archive daily report as often as possible, to stay near real time on "daily" reports,
     *   while allowing less real time weekly/monthly/yearly reporting.
     */

    /**
     * TODO/Ideas
     * - Process first all period=day, then all other periods (less important)
     * - Ensure script can only run once at a time
     * - Add "report last processed X s ago" in UI grey box "About"
     * - piwik.org update FAQ / online doc
     * - check that when ran as crontab, it will email errors when there is any
    }}}

    git-svn-id: http://dev.piwik.org/svn/trunk@5087 59fd770c-687e-43c8-a1e3-f5a4ff64c105
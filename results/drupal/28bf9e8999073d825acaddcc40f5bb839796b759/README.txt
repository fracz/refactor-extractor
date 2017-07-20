commit 28bf9e8999073d825acaddcc40f5bb839796b759
Author: Dries Buytaert <dries@buytaert.net>
Date:   Mon Oct 30 16:18:39 2000 +0000

    - fixed a bug in check_input: html stripping was not 100% correct.
    - fixed a bug in account.php: the confirmation url is now correct.
    - improved error checking + security in diary.php.
    - fixed a bug in the html code of theme zaphod.
    - improved the date handling: always call format_date().
    - expanded account information in administration pages.
    - added a new variable $siteurl to ./includes/config.inc.
    - added comment moderation to theme zaphod.
    - "alter table users add timezone varchar(8);"

    - !!! added new timezone feature !!! :o)
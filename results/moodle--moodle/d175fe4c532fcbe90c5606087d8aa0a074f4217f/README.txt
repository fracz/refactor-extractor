commit d175fe4c532fcbe90c5606087d8aa0a074f4217f
Author: poltawski <poltawski>
Date:   Thu Jul 30 18:57:19 2009 +0000

    lib/simplepie MDL-7946 - improve simplepie defaults

    - Specify default low connect timeout in order that 'interactive' pages are
      not slowed down by slow feeds
    - Set default cache feed duration of 1 hour

    In RSS feed block:
    - Try really hard in cron to retreive the feed
    - Set the cache duration low in order to help cron refresh the cache
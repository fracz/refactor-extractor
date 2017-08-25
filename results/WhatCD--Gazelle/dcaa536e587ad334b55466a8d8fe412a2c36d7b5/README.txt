commit dcaa536e587ad334b55466a8d8fe412a2c36d7b5
Author: What.CD <committer@what.cd>
Date:   Thu May 12 10:24:03 2011 +0000

    Update sphinxapi for v2

    Allow indexing by column 0 in ->to_array()

    Only need numeric indices in 'ip_bans' key

    Cache keys were completely broken on the debug pages

    Add bbcode missing functions to staffpms

    Add border to improve layout of torrent pages with postmod

    Fix a bug where users could download anyone's snatch/upload/seedlist regardless of paranoia settings

    Don't escape thread titles before caching

    Better BBCode URL matching

    Fix user search by tracker IP for ocelot data

    Fix bug where the same thread would show up twice in the announcements forum

    Update tables before sending invite email to prevent sending multiple invites
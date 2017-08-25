commit b3aefe3cc85bc9c941449c1f10ff9fce02b57a5d
Author: Petr Škoda <commits@skodak.org>
Date:   Tue Aug 21 10:07:03 2012 +0200

    MDL-34990 improve custom toolbar setting parsing

    It is probably better to parse the setting every time because somebody may put unsupported values directly into config.php, performance should not be an issue because we do not have editors on every page.
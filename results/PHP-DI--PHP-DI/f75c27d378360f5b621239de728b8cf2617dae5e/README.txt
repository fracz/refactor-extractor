commit f75c27d378360f5b621239de728b8cf2617dae5e
Author: Matthieu Napoli <matthieu@mnapoli.fr>
Date:   Sun Sep 6 14:45:18 2015 +0200

    Optimization: remove useless checks

    Those checks were useless: for them to fail, it would mean there was a bug in ResolverDispatcher. Removing them improves performances between 4-6% in the time needed to resolve an entry.
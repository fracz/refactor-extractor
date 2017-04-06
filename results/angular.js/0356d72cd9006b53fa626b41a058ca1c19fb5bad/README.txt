commit 0356d72cd9006b53fa626b41a058ca1c19fb5bad
Author: Igor Minar <igor@angularjs.org>
Date:   Sat Feb 21 17:40:33 2015 -0800

    chore(travis,grunt): extract the npm install and cache busting logic into install-dependencies.sh

    So now we are DRY.

    Added extra error checking and improved the grunt file init setup so that stdio is visible in console.

    Closes #11110
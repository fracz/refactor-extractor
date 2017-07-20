commit 4f9cf232f8d4d750d39aca83406b8a6d5e17a6c9
Author: Jeremy Benoist <jeremy.benoist@gmail.com>
Date:   Fri Mar 11 09:42:08 2016 +0100

    Improve test failure readability

    If the response content isn't the one expected, instead of checking into the whole DOM (with node tag, etc ..) we only check the text.
    So if it fails, phpunit will display only the text, not all node tag. It'll be easier to read.
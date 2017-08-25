commit bfc65a68a70a8b098ff2a3e3287915499f84e8b5
Author: Jerome Bakker <jeabakker@coldtrick.com>
Date:   Wed Feb 18 15:31:36 2015 +0100

    fix(views): improved check on non existing array keys

    This prevents your PHP errorlog from overflowing with warnings about
    undefined indexes
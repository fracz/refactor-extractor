commit 1838ddb95d40c57eb0ba3909f6ea3d67cab7640d
Author: Stephane Nicoll <snicoll@pivotal.io>
Date:   Sun Nov 5 13:14:45 2017 +0200

    Support Ant-style package name with component index

    This commit improves the component index so that it supports ant-style
    package name (i.e. com.example.**.foo).

    Issue: SPR-16152
commit 4c1b0740a924c381f9b87bd35e782375daf322b5
Author: Steve Clay <steve@mrclay.org>
Date:   Thu Mar 12 15:06:54 2015 -0400

    feature(security): Adds component to create and validate HMAC tokens

    HMAC uses a binary encoding of the site key instead of the Base64 or hex encodings, and improves the docs of the site secret component.

    Fixes #7824
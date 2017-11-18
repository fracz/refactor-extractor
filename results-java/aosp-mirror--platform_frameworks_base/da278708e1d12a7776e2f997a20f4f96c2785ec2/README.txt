commit da278708e1d12a7776e2f997a20f4f96c2785ec2
Author: Raph Levien <raph@google.com>
Date:   Wed Mar 11 14:09:26 2015 -0700

    Fix XML parsing crash in SettingsProvider

    A previous change added more whitespace to settings_global.xml to
    improve human readability, but the parser is overly picky in ignoring
    whitespace. This patch makes it accept all whitespace strings.

    Bug: 19696812
    Change-Id: I3ebb8f6df2e25f4e6b6841da743be3f3a91e2442
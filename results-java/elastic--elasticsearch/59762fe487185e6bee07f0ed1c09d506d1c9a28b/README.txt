commit 59762fe487185e6bee07f0ed1c09d506d1c9a28b
Author: Tanguy Leroux <tlrx.dev@gmail.com>
Date:   Mon Jun 27 12:04:59 2016 +0200

    Register group setting for repository-azure accounts

    Since the Settings infrastructure has been improved, a group setting must be registered by the repository-azure plugin to allow settings like "cloud.azure.storage.my_account.account" to be coherent with Azure plugin documentation.


package com.donnfelker.android.bootstrap.authenticator;

import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.accounts.AccountsException;
import android.app.Activity;
import android.os.Bundle;

import java.io.IOException;

import javax.inject.Inject;

import static android.accounts.AccountManager.KEY_AUTHTOKEN;
import static com.donnfelker.android.bootstrap.core.Constants.Auth.AUTHTOKEN_TYPE;
import static com.donnfelker.android.bootstrap.core.Constants.Auth.BOOTSTRAP_ACCOUNT_TYPE;

/**
 * Bridge class that obtains a API key for the currently configured account
 */
public class ApiKeyProvider {

    @Inject protected AccountManager mAccountManager;

    /**
     * This call blocks, so shouldn't be called on the UI thread
     *
     * @return API key to be used for authorization with a
     *      {@link com.donnfelker.android.bootstrap.core.BootstrapService} instance
     * @throws AccountsException
     * @throws IOException
     */
    public String getAuthKey(final Activity activity) throws AccountsException, IOException {
        final AccountManagerFuture<Bundle> accountManagerFuture
                = mAccountManager.getAuthTokenByFeatures(BOOTSTRAP_ACCOUNT_TYPE,
                        AUTHTOKEN_TYPE, new String[0], activity, null, null, null, null);

        return accountManagerFuture.getResult().getString(KEY_AUTHTOKEN);
    }
}
package im.actor.model.modules.utils;

import im.actor.model.network.AuthKeyStorage;
import im.actor.model.droidkit.engine.PreferencesStorage;

/**
 * Created by ex3ndr on 08.02.15.
 */
public class PreferenceApiStorage implements AuthKeyStorage {

    private PreferencesStorage preferencesStorage;

    public PreferenceApiStorage(PreferencesStorage preferencesStorage) {
        this.preferencesStorage = preferencesStorage;
    }

    @Override
    public long getAuthKey() {
        return preferencesStorage.getLong("auth_id", 0);
    }

    @Override
    public void saveAuthKey(long key) {
        preferencesStorage.putLong("auth_id", key);
    }
}
package im.actor.core.runtime.generic;

import im.actor.core.runtime.generic.storage.AsyncListEngine;
import im.actor.runtime.EnginesRuntime;
import im.actor.runtime.bser.BserCreator;
import im.actor.runtime.bser.BserObject;
import im.actor.runtime.storage.ListEngine;
import im.actor.runtime.storage.ListEngineItem;
import im.actor.runtime.storage.ListStorage;
import im.actor.runtime.storage.ListStorageDisplayEx;

public class GenericEnginesProvider implements EnginesRuntime {

    @Override
    public <T extends BserObject & ListEngineItem> ListEngine<T> createListEngine(ListStorage storage, BserCreator<T> creator) {
        return new AsyncListEngine<T>((ListStorageDisplayEx) storage, creator);
    }

    @Override
    public boolean isDisplayListSupported() {
        return true;
    }
}
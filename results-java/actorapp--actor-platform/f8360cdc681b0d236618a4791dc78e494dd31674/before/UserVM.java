package im.actor.model.viewmodel;

import java.util.ArrayList;
import java.util.List;

import im.actor.model.entity.Avatar;
import im.actor.model.entity.ContactRecord;
import im.actor.model.entity.Sex;
import im.actor.model.entity.User;
import im.actor.model.modules.Modules;
import im.actor.model.mvvm.BaseValueModel;
import im.actor.model.mvvm.MVVMEngine;
import im.actor.model.mvvm.ModelChangedListener;
import im.actor.model.mvvm.ValueModel;

/**
 * Created by ex3ndr on 19.02.15.
 */
public class UserVM extends BaseValueModel<User> {
    private int id;
    private long hash;
    private ValueModel<String> name;
    private ValueModel<Avatar> avatar;
    private Sex sex;
    private ValueModel<Boolean> isContact;
    private ValueModel<UserPresence> presence;
    private ArrayList<ModelChangedListener<UserVM>> listeners = new ArrayList<ModelChangedListener<UserVM>>();
    private ValueModel<ArrayList<UserPhone>> phones;

    public UserVM(User user, Modules modules) {
        super(user);

        id = user.getUid();
        hash = user.getAccessHash();
        sex = user.getSex();
        name = new ValueModel<String>("user." + id + ".name", user.getName());
        avatar = new ValueModel<Avatar>("user." + id + ".avatar", user.getAvatar());
        isContact = new ValueModel<Boolean>("user." + id + ".contact", modules.getContactsModule().isUserContact(id));
        presence = new ValueModel<UserPresence>("user." + id + ".presence", new UserPresence(UserPresence.State.UNKNOWN));
        phones = new ValueModel<ArrayList<UserPhone>>("user." + id + ".phones", buildPhones(user.getRecords()));
    }

    @Override
    protected void updateValues(User rawObj) {
        boolean isChanged = false;
        isChanged |= name.change(rawObj.getName());
        isChanged |= avatar.change(rawObj.getAvatar());
        isChanged |= phones.change(buildPhones(rawObj.getRecords()));

        if (isChanged) {
            notifyChange();
        }
    }

    public int getId() {
        return id;
    }

    public long getHash() {
        return hash;
    }

    public ValueModel<String> getName() {
        return name;
    }

    public ValueModel<Avatar> getAvatar() {
        return avatar;
    }

    public Sex getSex() {
        return sex;
    }

    public ValueModel<Boolean> isContact() {
        return isContact;
    }

    public ValueModel<UserPresence> getPresence() {
        return presence;
    }

    public ValueModel<ArrayList<UserPhone>> getPhones() {
        return phones;
    }

    private ArrayList<UserPhone> buildPhones(List<ContactRecord> records) {
        ArrayList<UserPhone> res = new ArrayList<UserPhone>();
        for (ContactRecord r : records) {
            if (r.getRecordType() == ContactRecord.TYPE_PHONE) {
                res.add(new UserPhone(Long.parseLong(r.getRecordData()), r.getRecordTitle()));
            }
        }
        return res;
    }

    // We expect that subscribe will be called only on UI Thread
    public void subscribe(ModelChangedListener<UserVM> listener) {
        if (listeners.contains(listener)) {
            return;
        }
        listeners.add(listener);
        listener.onChanged(this);
    }

    // We expect that subscribe will be called only on UI Thread
    public void unsubscribe(ModelChangedListener<UserVM> listener) {
        listeners.remove(listener);
    }

    private void notifyChange() {
        MVVMEngine.getMainThreadProvider().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (ModelChangedListener<UserVM> l : listeners.toArray(new ModelChangedListener[0])) {
                    l.onChanged(UserVM.this);
                }
            }
        });
    }
}
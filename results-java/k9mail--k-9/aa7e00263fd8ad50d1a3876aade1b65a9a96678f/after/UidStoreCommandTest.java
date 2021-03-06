package com.fsck.k9.mail.store.imap;


import java.util.Collections;

import com.fsck.k9.mail.Flag;
import com.fsck.k9.mail.store.imap.UidStoreCommand;
import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class UidStoreCommandTest {

    @Test
    public void createCommandString_withDeletedFlagAndPositiveValue_shouldCreateRespectiveString() {
        UidStoreCommand command = createUidStoreCommand(1L, true, Flag.DELETED);

        String commandString = command.createCommandString();

        assertEquals(commandString, "UID STORE 1 +FLAGS.SILENT (\\Deleted)");
    }

    @Test
    public void createCommandString_withDeletedFlagAndNegativeValue_shouldCreateRespectiveString() {
        UidStoreCommand command = createUidStoreCommand(1L, false, Flag.DELETED);

        String commandString = command.createCommandString();

        assertEquals(commandString, "UID STORE 1 -FLAGS.SILENT (\\Deleted)");
    }

    @Test
    public void createCommandString_withSeenFlagAndPositiveValue_shouldCreateRespectiveString() {
        UidStoreCommand command = createUidStoreCommand(1L, true, Flag.SEEN);

        String commandString = command.createCommandString();

        assertEquals(commandString, "UID STORE 1 +FLAGS.SILENT (\\Seen)");
    }

    @Test
    public void createCommandString_withSeenFlagAndNegativeValue_shouldCreateRespectiveString() {
        UidStoreCommand command = createUidStoreCommand(1L, false, Flag.SEEN);

        String commandString = command.createCommandString();

        assertEquals(commandString, "UID STORE 1 -FLAGS.SILENT (\\Seen)");
    }

    private UidStoreCommand createUidStoreCommand(Long uid, boolean value, Flag flag) {
        return UidStoreCommand.createWithUids(Collections.singleton(uid), value,
                Collections.singleton(flag), true);
    }
}
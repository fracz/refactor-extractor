/**
 * Copyright 2014 Nikita Koksharov, Nickolay Borbit
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.redisson.cluster;

import org.redisson.MasterSlaveServersConfig;
import org.redisson.client.RedisException;
import org.redisson.client.protocol.RedisCommands;
import org.redisson.connection.ConnectionEntry.Mode;
import org.redisson.connection.DefaultConnectionListener;
import org.redisson.connection.FutureConnectionListener;

public class ClusterConnectionListener extends DefaultConnectionListener {

    private final boolean readFromSlaves;

    public ClusterConnectionListener(boolean readFromSlaves) {
        this.readFromSlaves = readFromSlaves;
    }

    @Override
    public void onConnect(MasterSlaveServersConfig config, Mode serverMode, FutureConnectionListener connectionListener) throws RedisException {
        super.onConnect(config, serverMode, connectionListener);
        if (serverMode == Mode.SLAVE && readFromSlaves) {
            connectionListener.addCommand(RedisCommands.READONLY);
        }
    }

}
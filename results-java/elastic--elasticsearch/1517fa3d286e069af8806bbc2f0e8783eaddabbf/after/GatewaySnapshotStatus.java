/*
 * Licensed to Elastic Search and Shay Banon under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. Elastic Search licenses this
 * file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.elasticsearch.action.admin.indices.status;

import org.elasticsearch.ElasticSearchIllegalArgumentException;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;

/**
 * @author kimchy (shay.banon)
 */
public class GatewaySnapshotStatus {

    public static enum Stage {
        NONE((byte) 0),
        INDEX((byte) 1),
        TRANSLOG((byte) 2),
        FINALIZE((byte) 3),
        DONE((byte) 4),
        FAILURE((byte) 5);

        private final byte value;

        Stage(byte value) {
            this.value = value;
        }

        public byte value() {
            return this.value;
        }

        public static Stage fromValue(byte value) {
            if (value == 0) {
                return Stage.NONE;
            } else if (value == 1) {
                return Stage.INDEX;
            } else if (value == 2) {
                return Stage.TRANSLOG;
            } else if (value == 3) {
                return Stage.FINALIZE;
            } else if (value == 4) {
                return Stage.DONE;
            } else if (value == 5) {
                return Stage.FAILURE;
            }
            throw new ElasticSearchIllegalArgumentException("No stage found for [" + value + "]");
        }
    }

    final Stage stage;

    final long startTime;

    final long time;

    final long indexSize;

    public GatewaySnapshotStatus(Stage stage, long startTime, long time, long indexSize) {
        this.stage = stage;
        this.startTime = startTime;
        this.time = time;
        this.indexSize = indexSize;
    }

    public Stage stage() {
        return this.stage;
    }

    public Stage getStage() {
        return stage();
    }

    public long startTime() {
        return this.startTime;
    }

    public long getStartTime() {
        return startTime();
    }

    public TimeValue time() {
        return TimeValue.timeValueMillis(time);
    }

    public TimeValue getTime() {
        return time();
    }

    public ByteSizeValue indexSize() {
        return new ByteSizeValue(indexSize);
    }

    public ByteSizeValue getIndexSize() {
        return indexSize();
    }
}
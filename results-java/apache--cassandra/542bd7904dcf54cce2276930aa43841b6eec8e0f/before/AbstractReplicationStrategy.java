/*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements.  See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership.  The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
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
package org.apache.cassandra.locator;

import java.net.InetAddress;
import java.util.*;

import org.apache.log4j.Logger;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.apache.cassandra.dht.Range;
import org.apache.cassandra.dht.Token;
import org.apache.cassandra.gms.FailureDetector;
import org.apache.cassandra.service.ConsistencyLevel;
import org.apache.cassandra.gms.Gossiper;
import org.apache.cassandra.service.WriteResponseHandler;
import org.apache.cassandra.service.StorageService;
import org.apache.cassandra.utils.FBUtilities;

/**
 * This class contains a helper method that will be used by
 * all abstraction that implement the IReplicaPlacementStrategy
 * interface.
*/
public abstract class AbstractReplicationStrategy
{
    protected static final Logger logger_ = Logger.getLogger(AbstractReplicationStrategy.class);

    protected TokenMetadata tokenMetadata_;
    protected int replicas_;

    AbstractReplicationStrategy(TokenMetadata tokenMetadata, int replicas)
    {
        tokenMetadata_ = tokenMetadata;
        replicas_ = replicas;
    }

    public abstract ArrayList<InetAddress> getNaturalEndpoints(Token token, TokenMetadata metadata);

    public WriteResponseHandler getWriteResponseHandler(int blockFor, ConsistencyLevel consistency_level)
    {
        return new WriteResponseHandler(blockFor);
    }

    public ArrayList<InetAddress> getNaturalEndpoints(Token token)
    {
        return getNaturalEndpoints(token, tokenMetadata_);
    }

    /*
     * This method returns the hint map. The key is the endpoint
     * on which the data is being placed and the value is the
     * endpoint to which it should be forwarded.
     */
    public Map<InetAddress, InetAddress> getHintedEndpoints(Token token, Collection<InetAddress> naturalEndpoints)
    {
        return getHintedMapForEndpoints(getWriteEndpoints(token, naturalEndpoints));
    }

    /**
     * write endpoints may be different from read endpoints, because read endpoints only need care about the
     * "natural" nodes for a token, but write endpoints also need to account for nodes that are bootstrapping
     * into the ring, and write data there too so that they stay up to date during the bootstrap process.
     * Thus, this method may return more nodes than the Replication Factor.
     *
     * Only ReplicationStrategy should care about this method (higher level users should only ask for Hinted).
     */
    public Collection<InetAddress> getWriteEndpoints(Token token, Collection<InetAddress> naturalEndpoints)
    {
        if (tokenMetadata_.getPendingRanges().isEmpty())
            return naturalEndpoints;

        List<InetAddress> endpoints = new ArrayList<InetAddress>(naturalEndpoints);

        for (Map.Entry<Range, Collection<InetAddress>> entry : tokenMetadata_.getPendingRanges().entrySet())
        {
            if (entry.getKey().contains(token))
            {
                endpoints.addAll(entry.getValue());
            }
        }

        return endpoints;
    }

    /**
     * returns map of {ultimate target: destination}, where if destination is not the same
     * as the ultimate target, it is a "hinted" node, a node that will deliver the data to
     * the ultimate target when it becomes alive again.
     *
     * A destination node may be the destination for multiple targets.
     */
    private Map<InetAddress, InetAddress> getHintedMapForEndpoints(Collection<InetAddress> targets)
    {
        Set<InetAddress> usedEndpoints = new HashSet<InetAddress>();
        Map<InetAddress, InetAddress> map = new HashMap<InetAddress, InetAddress>();

        IEndPointSnitch endPointSnitch = StorageService.instance.getEndPointSnitch();
        Set<InetAddress> liveNodes = Gossiper.instance.getLiveMembers();

        for (InetAddress ep : targets)
        {
            if (FailureDetector.instance.isAlive(ep))
            {
                map.put(ep, ep);
                usedEndpoints.add(ep);
            }
            else
            {
                // Ignore targets that have died when bootstrapping
                if (!tokenMetadata_.isMember(ep))
                    continue;

                // find another endpoint to store a hint on.  prefer endpoints that aren't already in use
                InetAddress hintLocation = null;
                List<InetAddress> preferred = endPointSnitch.getSortedListByProximity(ep, liveNodes);

                for (InetAddress hintCandidate : preferred)
                {
                    if (!targets.contains(hintCandidate)
                        && !usedEndpoints.contains(hintCandidate)
                        && tokenMetadata_.isMember(hintCandidate))
                    {
                        hintLocation = hintCandidate;
                        break;
                    }
                }
                // if all endpoints are already in use, might as well store it locally to save the network trip
                if (hintLocation == null)
                    hintLocation = FBUtilities.getLocalAddress();

                map.put(ep, hintLocation);
                usedEndpoints.add(hintLocation);
            }
        }

        assert map.size() == targets.size();
        return map;
    }

    /*
     NOTE: this is pretty inefficient. also the inverse (getRangeAddresses) below.
     this is fine as long as we don't use this on any critical path.
     (fixing this would probably require merging tokenmetadata into replicationstrategy, so we could cache/invalidate cleanly.)
     */
    public Multimap<InetAddress, Range> getAddressRanges(TokenMetadata metadata)
    {
        Multimap<InetAddress, Range> map = HashMultimap.create();

        for (Token token : metadata.sortedTokens())
        {
            Range range = metadata.getPrimaryRangeFor(token);
            for (InetAddress ep : getNaturalEndpoints(token, metadata))
            {
                map.put(ep, range);
            }
        }

        return map;
    }

    public Multimap<Range, InetAddress> getRangeAddresses(TokenMetadata metadata)
    {
        Multimap<Range, InetAddress> map = HashMultimap.create();

        for (Token token : metadata.sortedTokens())
        {
            Range range = metadata.getPrimaryRangeFor(token);
            for (InetAddress ep : getNaturalEndpoints(token, metadata))
            {
                map.put(range, ep);
            }
        }

        return map;
    }

    public Multimap<InetAddress, Range> getAddressRanges()
    {
        return getAddressRanges(tokenMetadata_);
    }

    public Collection<Range> getPendingAddressRanges(TokenMetadata metadata, Token pendingToken, InetAddress pendingAddress)
    {
        TokenMetadata temp = metadata.cloneOnlyTokenMap();
        temp.updateNormalToken(pendingToken, pendingAddress);
        return getAddressRanges(temp).get(pendingAddress);
    }

}
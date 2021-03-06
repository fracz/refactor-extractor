package com.profiler.server.handler;

import com.profiler.common.dto.thrift.ApiMetaData;
import com.profiler.server.dao.ApiMetaDataDao;
import org.apache.thrift.TBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.DatagramPacket;

/**
 *
 */
public class ApiMetaDataHandler implements Handler {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private ApiMetaDataDao sqlMetaDataDao;

    @Override
    public void handler(TBase<?, ?> tbase, DatagramPacket datagramPacket) {
        if (!(tbase instanceof ApiMetaData)) {
            logger.warn("invalid tbase:{}", tbase);
            return;
        }
        ApiMetaData apiMetaData = (ApiMetaData) tbase;
        if (logger.isInfoEnabled()) {
            logger.info("Received ApiMetaData{}", apiMetaData);
        }
        sqlMetaDataDao.insert(apiMetaData);

    }
}
/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.web.service;

import java.util.ArrayList;
import java.util.List;

import com.navercorp.pinpoint.common.bo.AnnotationBo;
import com.navercorp.pinpoint.common.bo.ApiMetaDataBo;
import com.navercorp.pinpoint.common.bo.Span;
import com.navercorp.pinpoint.common.bo.SpanBo;
import com.navercorp.pinpoint.common.bo.SpanEventBo;
import com.navercorp.pinpoint.common.service.AnnotationKeyRegistryService;
import com.navercorp.pinpoint.common.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.trace.AnnotationKeyMatcher;
import com.navercorp.pinpoint.common.util.AnnotationUtils;
import com.navercorp.pinpoint.common.util.ApiDescription;
import com.navercorp.pinpoint.common.util.ApiDescriptionParser;
import com.navercorp.pinpoint.web.calltree.span.CallTreeIterator;
import com.navercorp.pinpoint.web.calltree.span.CallTreeNode;
import com.navercorp.pinpoint.web.calltree.span.SpanAlign;
import com.navercorp.pinpoint.web.dao.TraceDao;
import com.navercorp.pinpoint.web.filter.Filter;
import com.navercorp.pinpoint.web.vo.BusinessTransactions;
import com.navercorp.pinpoint.web.vo.Range;
import com.navercorp.pinpoint.web.vo.TransactionId;
import com.navercorp.pinpoint.web.vo.callstacks.Record;
import com.navercorp.pinpoint.web.vo.callstacks.RecordSet;

import org.apache.commons.lang.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author jaehong.kim
 */
@Service
public class TransactionInfoServiceImpl implements TransactionInfoService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private TraceDao traceDao;

    @Autowired
    private AnnotationKeyMatcherService annotationKeyMatcherService;

    @Autowired
    private ServiceTypeRegistryService registry;

    @Autowired
    private AnnotationKeyRegistryService annotationKeyRegistryService;

    // Temporarily disabled Becuase We need to slove authentication problem inter system.
    // @Value("#{pinpointWebProps['log.enable'] ?: false}")
    // private boolean logLinkEnable;

    // @Value("#{pinpointWebProps['log.button.name'] ?: ''}")
    // private String logButtonName;

    // @Value("#{pinpointWebProps['log.page.url'] ?: ''}")
    // private String logPageUrl;

    @Override
    public BusinessTransactions selectBusinessTransactions(List<TransactionId> transactionIdList, String applicationName, Range range, Filter filter) {
        if (transactionIdList == null) {
            throw new NullPointerException("transactionIdList must not be null");
        }
        if (applicationName == null) {
            throw new NullPointerException("applicationName must not be null");
        }
        if (filter == null) {
            throw new NullPointerException("filter must not be null");
        }
        if (range == null) {
            // TODO range is not used - check the logic again
            throw new NullPointerException("range must not be null");
        }

        List<List<SpanBo>> traceList;

        if (filter == Filter.NONE) {
            traceList = this.traceDao.selectSpans(transactionIdList);
        } else {
            traceList = this.traceDao.selectAllSpans(transactionIdList);
        }

        BusinessTransactions businessTransactions = new BusinessTransactions();
        for (List<SpanBo> trace : traceList) {
            if (!filter.include(trace)) {
                continue;
            }

            for (SpanBo spanBo : trace) {
                // show application's incoming requests
                if (applicationName.equals(spanBo.getApplicationId())) {
                    businessTransactions.add(spanBo);
                }
            }
        }

        return businessTransactions;
    }

    @Override
    public RecordSet createRecordSet(CallTreeIterator callTreeIterator, long focusTimestamp) {
        if (callTreeIterator == null) {
            throw new NullPointerException("callTreeIterator must not be null");
        }

        RecordSet recordSet = new RecordSet();
        final List<SpanAlign> spanAlignList = callTreeIterator.values();

        // finds and marks the focusTimestamp.
        // focusTimestamp is needed to determine which span to use as reference when there are more than 2 spans making up a transaction.
        // for cases where focus cannot be found due to an error, a separate marker is needed.
        // TODO potential error - because server time is used, there may be more than 2 focusTime due to differences in server times.
        SpanBo focusTimeSpanBo = findFocusTimeSpanBo(spanAlignList, focusTimestamp);
        // FIXME patched temporarily for cases where focusTimeSpanBo is not found. Need a more complete solution.
        if (focusTimeSpanBo != null) {
            recordSet.setAgentId(focusTimeSpanBo.getAgentId());
            recordSet.setApplicationId(focusTimeSpanBo.getApplicationId());

            final String applicationName = getRpcArgument(focusTimeSpanBo);
            recordSet.setApplicationName(applicationName);
        }

        // find the startTime to use as reference
        long startTime = getStartTime(spanAlignList);
        recordSet.setStartTime(startTime);

        // find the endTime to use as reference
        long endTime = getEndTime(spanAlignList);
        recordSet.setEndTime(endTime);

        final SpanAlignPopulate spanAlignPopulate = new SpanAlignPopulate();
        List<Record> recordList = spanAlignPopulate.populateSpanRecord(callTreeIterator);
        logger.debug("RecordList:{}", recordList);

        if (focusTimeSpanBo != null) {
            // mark the record to be used as focus
            long beginTimeStamp = focusTimeSpanBo.getStartTime();
            markFocusRecord(recordList, beginTimeStamp);
            recordSet.setBeginTimestamp(beginTimeStamp);
        }

        recordSet.setRecordList(recordList);

        // if (logLinkEnable) {
        // addlogLink(recordSet);
        // }

        return recordSet;
    }

    private void markFocusRecord(List<Record> recordList, long beginTimeStamp) {
        for (Record record : recordList) {
            if (record.getBegin() == beginTimeStamp) {
                record.setFocused(true);
                break;
            }
        }
    }

    // private void addlogLink(RecordSet recordSet) {
    // List<Record> records = recordSet.getRecordList();
    // List<TransactionInfo> transactionInfoes = new LinkedList<TransactionInfo>();
    //
    // for (Iterator<Record> iterator = records.iterator(); iterator.hasNext();) {
    // Record record = (Record) iterator.next();
    //
    // if(record.getTransactionId() == null) {
    // continue;
    // }
    //
    // TransactionInfo transactionInfo = new TransactionInfo(record.getTransactionId(), record.getSpanId());
    //
    // if (transactionInfoes.contains(transactionInfo)) {
    // continue;
    // };
    //
    // record.setLogPageUrl(logPageUrl);
    // record.setLogButtonName(logButtonName);
    //
    // transactionInfoes.add(transactionInfo);
    // }
    // }

    private class TransactionInfo {

        private final String transactionId;
        private final long spanId;

        public TransactionInfo(String transactionId, long spanId) {
            this.transactionId = transactionId;
            this.spanId = spanId;
        }

        public String getTransactionId() {
            return transactionId;
        }

        public long getSpanId() {
            return spanId;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof TransactionInfo == false) {
                return false;
            }

            TransactionInfo transactionInfo = (TransactionInfo) obj;

            if (!transactionId.equals(transactionInfo.getTransactionId())) {
                return false;
            }
            if (spanId != transactionInfo.getSpanId()) {
                return false;
            }

            return true;
        }
    }

    private long getStartTime(List<SpanAlign> spanAlignList) {
        if (spanAlignList == null || spanAlignList.size() == 0) {
            return 0;
        }
        SpanAlign spanAlign = spanAlignList.get(0);
        if (spanAlign.isSpan()) {
            SpanBo spanBo = spanAlign.getSpanBo();
            return spanBo.getStartTime();
        } else {
            SpanEventBo spanEventBo = spanAlign.getSpanEventBo();
            return spanAlign.getSpanBo().getStartTime() + spanEventBo.getStartElapsed();
        }
    }

    private long getEndTime(List<SpanAlign> spanAlignList) {
        if (spanAlignList == null || spanAlignList.size() == 0) {
            return 0;
        }
        SpanAlign spanAlign = spanAlignList.get(0);
        if (spanAlign.isSpan()) {
            SpanBo spanBo = spanAlign.getSpanBo();
            return spanBo.getElapsed();
        } else {
            SpanEventBo spanEventBo = spanAlign.getSpanEventBo();
            long begin = spanAlign.getSpanBo().getStartTime() + spanEventBo.getStartElapsed();
            long elapsed = spanEventBo.getEndElapsed();
            return begin + elapsed;
        }
    }

    private SpanBo findFocusTimeSpanBo(List<SpanAlign> spanAlignList, long focusTimestamp) {
        SpanBo firstSpan = null;
        for (SpanAlign spanAlign : spanAlignList) {
            if (spanAlign.isSpan()) {
                SpanBo spanBo = spanAlign.getSpanBo();
                if (spanBo.getCollectorAcceptTime() == focusTimestamp) {
                    return spanBo;
                }
                if (firstSpan == null) {
                    firstSpan = spanBo;
                }
            }
        }
        // return firstSpan when focus Span could not be found.
        return firstSpan;
    }

    private class SpanAlignPopulate {
        private final Logger logger = LoggerFactory.getLogger(this.getClass());

        private final ApiDescriptionParser apiDescriptionParser = new ApiDescriptionParser();

        // spans with id = 0 are regarded as root - start at 1
        private int idGen = 1;

        // private final Stack<SpanDepth> stack = new Stack<SpanDepth>();

        private int getNextId() {
            return idGen++;
        }

        private List<Record> populateSpanRecord(CallTreeIterator callTreeIterator) {
            if (callTreeIterator == null) {
                throw new NullPointerException("callTreeIterator must not be null");
            }

            final List<Record> recordList = new ArrayList<Record>(callTreeIterator.size() * 2);

            // annotation id has nothing to do with spanAlign's seq and thus may be incremented as long as they don't overlap.
            while (callTreeIterator.hasNext()) {
                final CallTreeNode node = callTreeIterator.next();
                final SpanAlign spanAlign = node.getValue();
                // set record next id.
                spanAlign.setId(getNextId());

                if (spanAlign.isSpan()) {
                    SpanBo spanBo = spanAlign.getSpanBo();
                    String argument = getRpcArgument(spanBo);

                    final long begin = spanBo.getStartTime();
                    final long elapsed = spanBo.getElapsed();
                    final int spanBoSequence = spanAlign.getId();
                    int parentSequence;
                    final CallTreeNode prev = node.getParent();
                    if (prev == null) {
                        // root span
                        parentSequence = 0;
                    } else {
                        parentSequence = prev.getValue().getId();
                    }
                    logger.debug("apiId={}", spanBo.getApiId());
                    logger.debug("spanBoSequence:{}, parentSequence:{}", spanBoSequence, parentSequence);

                    String method = AnnotationUtils.findApiAnnotation(spanBo.getAnnotationBoList());
                    if (method != null) {
                        ApiDescription apiDescription = apiDescriptionParser.parse(method);
                        Record record = new Record(spanAlign.getDepth(),
                                spanBoSequence,
                                parentSequence,
                                true,
                                apiDescription.getSimpleMethodDescription(),
                                argument, begin, elapsed,
                                spanAlign.getGap(),
                                spanBo.getAgentId(),
                                spanBo.getApplicationId(),
                                registry.findServiceType(spanBo.getServiceType()),
                                null,
                                spanAlign.isHasChild(),
                                false,
                                spanBo.getTransactionId(),
                                spanBo.getSpanId(),
                                spanAlign.getExecutionTime());
                        record.setSimpleClassName(apiDescription.getSimpleClassName());
                        record.setFullApiDescription(method);
                        recordList.add(record);
                    } else {
                        String apiTag = AnnotationUtils.findApiTagAnnotation(spanBo.getAnnotationBoList());
                        if (apiTag != null) {
                            Record record = new Record(spanAlign.getDepth(),
                                    spanBoSequence,
                                    parentSequence,
                                    true,
                                    apiTag,
                                    argument,
                                    begin,
                                    elapsed,
                                    spanAlign.getGap(),
                                    spanBo.getAgentId(),
                                    spanBo.getApplicationId(),
                                    registry.findServiceType(spanBo.getServiceType()),
                                    null,
                                    spanAlign.isHasChild(),
                                    false,
                                    spanBo.getTransactionId(),
                                    spanBo.getSpanId(),
                                    spanAlign.getExecutionTime());
                            record.setSimpleClassName("");
                            record.setFullApiDescription("");
                            recordList.add(record);
                        } else {
                            AnnotationKey apiMetaDataError = getApiMetaDataError(spanBo.getAnnotationBoList());
                            Record record = new Record(spanAlign.getDepth(),
                                    spanBoSequence,
                                    parentSequence,
                                    true,
                                    apiMetaDataError.getName(),
                                    argument,
                                    begin,
                                    elapsed,
                                    spanAlign.getGap(),
                                    spanBo.getAgentId(),
                                    spanBo.getApplicationId(),
                                    registry.findServiceType(spanBo.getServiceType()),
                                    null,
                                    spanAlign.isHasChild(),
                                    false,
                                    spanBo.getTransactionId(),
                                    spanBo.getSpanId(),
                                    spanAlign.getExecutionTime());
                            record.setSimpleClassName("");
                            record.setFullApiDescription("");
                            recordList.add(record);
                        }
                    }
                    // add exception record
                    final Record exceptionRecord = getExceptionRecord(spanAlign.getDepth(), spanAlign, spanBoSequence);
                    if (exceptionRecord != null) {
                        recordList.add(exceptionRecord);
                    }

                    List<Record> annotationRecord = createAnnotationRecord(spanAlign.getDepth() + 1, spanBoSequence, spanBo.getAnnotationBoList());
                    recordList.addAll(annotationRecord);
                    if (spanBo.getRemoteAddr() != null) {
                        Record remoteAddress = createParameterRecord(spanAlign.getDepth() + 1, spanBoSequence, "REMOTE_ADDRESS", spanBo.getRemoteAddr());
                        recordList.add(remoteAddress);
                    }
                } else {
                    SpanEventBo spanEventBo = spanAlign.getSpanEventBo();
                    SpanBo spanBo = spanAlign.getSpanBo();

                    String argument = getDisplayArgument(spanEventBo);
                    final int spanBoEventSequence = spanAlign.getId();
                    final CallTreeNode parent = node.getParent();
                    if (parent == null) {
                        throw new IllegalStateException("prev is null. nodes=" + callTreeIterator);
                    }
                    final int parentSequence = parent.getValue().getId();
                    logger.debug("spanBoEventSequence:{}, parentSequence:{}", spanBoEventSequence, parentSequence);

                    final AnnotationBo annotation = AnnotationUtils.findAnnotationBo(spanEventBo.getAnnotationBoList(), AnnotationKey.API_METADATA);
                    // final String method = AnnotationUtils.findApiAnnotation(spanEventBo.getAnnotationBoList());
                    if (annotation != null) {
                        ApiMetaDataBo apiMetaData = (ApiMetaDataBo) annotation.getValue();
                        final String apiInfo = getApiInfo(apiMetaData);
                        String title = apiInfo;
                        String className = "";
                        if (apiMetaData.getType() == 0) {
                            ApiDescription apiDescription = apiDescriptionParser.parse(apiInfo);
                            title = apiDescription.getSimpleMethodDescription();
                            className = apiDescription.getSimpleClassName();
                        }
                        String destinationId = spanEventBo.getDestinationId();

                        long begin = spanAlign.getSpanBo().getStartTime() + spanEventBo.getStartElapsed();
                        long elapsed = spanEventBo.getEndElapsed();

                        // use spanBo's applicationId instead of spanEventBo's destinationId to display the name of the calling application on the call stack.
                        Record record = new Record(spanAlign.getDepth(),
                                spanBoEventSequence,
                                parentSequence,
                                true,
                                title,
                                argument,
                                begin,
                                elapsed,
                                spanAlign.getGap(),
                                spanEventBo.getAgentId(),
                                spanBo.getApplicationId(),
                                registry.findServiceType(spanEventBo.getServiceType()),
                                /* spanEventBo.getDestinationId(), spanEventBo.getServiceTypeCode(), */
                                destinationId,
                                spanAlign.isHasChild(),
                                false,
                                spanBo.getTransactionId(),
                                spanBo.getSpanId(),
                                spanAlign.getExecutionTime());
                        record.setSimpleClassName(className);
                        record.setFullApiDescription(apiInfo);

                        recordList.add(record);
                    } else {
                        AnnotationKey apiMetaDataError = getApiMetaDataError(spanEventBo.getAnnotationBoList());
                        String destinationId = spanEventBo.getDestinationId();

                        long begin = spanAlign.getSpanBo().getStartTime() + spanEventBo.getStartElapsed();
                        long elapsed = spanEventBo.getEndElapsed();

                        // use spanBo's applicationId instead of spanEventBo's destinationId to display the name of the calling application on the call stack.
                        Record record = new Record(spanAlign.getDepth(),
                                spanBoEventSequence,
                                parentSequence,
                                true,
                                apiMetaDataError.getName(),
                                argument,
                                begin,
                                elapsed,
                                spanAlign.getGap(),
                                spanEventBo.getAgentId(),
                                spanBo.getApplicationId(),
                                registry.findServiceType(spanEventBo.getServiceType()),
                                /* spanEventBo.getDestinationId(), spanEventBo.getServiceTypeCode(), */
                                destinationId,
                                spanAlign.isHasChild(),
                                false,
                                spanBo.getTransactionId(),
                                spanBo.getSpanId(),
                                spanAlign.getExecutionTime());
                        record.setSimpleClassName("");
                        record.setFullApiDescription("");
                        recordList.add(record);
                    }
                    // add exception record
                    final Record exceptionRecord = getExceptionRecord(spanAlign.getDepth(), spanAlign, spanBoEventSequence);
                    if (exceptionRecord != null) {
                        recordList.add(exceptionRecord);
                    }

                    List<Record> annotationRecord = createAnnotationRecord(spanAlign.getDepth() + 1, spanBoEventSequence, spanEventBo.getAnnotationBoList());
                    recordList.addAll(annotationRecord);
                }
            }
            return recordList;
        }

        private Record getExceptionRecord(int depth, SpanAlign spanAlign, int parentSequence) {
            if (spanAlign.isSpan()) {
                final SpanBo spanBo = spanAlign.getSpanBo();
                if (spanBo.hasException()) {
                    String simpleExceptionClass = getSimpleExceptionName(spanBo.getExceptionClass());
                    return new Record(depth + 1, getNextId(), parentSequence, false, simpleExceptionClass, spanBo.getExceptionMessage(), 0L, 0L, 0, null, null, null, null, false, false, spanBo.getTransactionId(), spanBo.getSpanId(), spanAlign.getExecutionTime());
                }
            } else {
                final SpanEventBo spanEventBo = spanAlign.getSpanEventBo();
                if (spanEventBo.hasException()) {
                    String simpleExceptionClass = getSimpleExceptionName(spanEventBo.getExceptionClass());
                    return new Record(depth + 1, getNextId(), parentSequence, false, simpleExceptionClass, spanEventBo.getExceptionMessage(), 0L, 0L, 0, null, null, null, null, false, true, null, 0, spanAlign.getExecutionTime());
                }
            }
            return null;
        }

        private String getSimpleExceptionName(String exceptionClass) {
            if (exceptionClass == null) {
                return "";
            }
            final int index = exceptionClass.lastIndexOf('.');
            if (index != -1) {
                exceptionClass = exceptionClass.substring(index + 1, exceptionClass.length());
            }
            return exceptionClass;
        }

        private List<Record> createAnnotationRecord(int depth, int parentId, List<AnnotationBo> annotationBoList) {
            List<Record> recordList = new ArrayList<Record>(annotationBoList.size());

            for (AnnotationBo ann : annotationBoList) {
                AnnotationKey annotation = findAnnotationKey(ann.getKey());
                if (annotation.isViewInRecordSet()) {
                    Record record = new Record(depth, getNextId(), parentId, false, annotation.getName(), ann.getValue().toString(), 0L, 0L, 0, null, null, null, null, false, false, null, 0, 0);
                    recordList.add(record);
                }
            }

            return recordList;
        }

        private Record createParameterRecord(int depth, int parentId, String method, String argument) {
            return new Record(depth, getNextId(), parentId, false, method, argument, 0L, 0L, 0, null, null, null, null, false, false, null, 0, 0);
        }

        private String getApiInfo(ApiMetaDataBo apiMetaDataBo) {
            if (apiMetaDataBo.getLineNumber() != -1) {
                return apiMetaDataBo.getApiInfo() + ":" + apiMetaDataBo.getLineNumber();
            } else {
                return apiMetaDataBo.getApiInfo();
            }
        }
    }

    private String getDisplayArgument(Span span) {
        AnnotationBo displayArgument = getDisplayArgument0(span);
        if (displayArgument == null) {
            return "";
        }
        return ObjectUtils.toString(displayArgument.getValue());
    }

    private String getRpcArgument(SpanBo spanBo) {
        String rpc = spanBo.getRpc();
        if (rpc != null) {
            return rpc;
        }
        return getDisplayArgument(spanBo);
    }

    public AnnotationBo getDisplayArgument0(Span span) {
        // TODO needs a more generalized implementation for Arcus
        List<AnnotationBo> list = span.getAnnotationBoList();
        if (list == null) {
            return null;
        }

        final AnnotationKeyMatcher matcher = annotationKeyMatcherService.findAnnotationKeyMatcher(span.getServiceType());
        if (matcher == null) {
            return null;
        }

        for (AnnotationBo annotation : list) {
            int key = annotation.getKey();

            if (matcher.matches(key)) {
                return annotation;
            }
        }
        return null;
    }

    public AnnotationKey getApiMetaDataError(List<AnnotationBo> annotationBoList) {
        for (AnnotationBo bo : annotationBoList) {
            AnnotationKey apiErrorCode = annotationKeyRegistryService.findApiErrorCode(bo.getKey());
            if (apiErrorCode != null) {
                return apiErrorCode;
            }
        }
        // could not find a more specific error - returns generalized error
        return AnnotationKey.ERROR_API_METADATA_ERROR;
    }

    private AnnotationKey findAnnotationKey(int key) {
        return annotationKeyRegistryService.findAnnotationKey(key);
    }
}
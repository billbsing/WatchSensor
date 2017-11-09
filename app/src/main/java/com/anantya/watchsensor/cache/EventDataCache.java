package com.anantya.watchsensor.cache;

import android.util.Log;

import com.anantya.watchsensor.data.EventDataItem;
import com.anantya.watchsensor.data.EventDataList;
import com.anantya.watchsensor.data.EventDataStatItem;

import java.io.File;

/**
 * Created by bill on 10/16/17.
 */

// cache class to use a random access file to read/write event data items

public class EventDataCache {

    private static final String FILENAME = "event_data_cache.dat";
    private File mFolder;


    private static final String TAG = "EventDataCache";

    public EventDataCache(File folder) {
        mFolder = folder;
    }

    static public File getFile(File folder) {
        return new File(folder, FILENAME);
    }

    public File getFile() {
        return new File(mFolder, FILENAME);
    }

    public void writeEventDataList(EventDataList eventDataList) {
        CacheFile file = new CacheFile(getFile());

        if ( file.open()) {
            CacheHeader header = file.getHeader();
            for (int i = 0; i < eventDataList.getItems().size(); i++) {
                EventDataItem eventDataItem = eventDataList.getItems().get(i);
                CacheData data = new CacheData();
                data.assign(eventDataItem);
                long id = header.getLastId();
                if (file.appendData(data, id)) {
                    header.addRecord();
                    file.writeHeader(header);
                }
            }
            file.close();
        }
    }

    public EventDataList getDataForUpload(int maxSize, long timeout) {
        EventDataList eventDataList = new EventDataList();
        CacheFile file = new CacheFile(getFile());
        if ( file.open()) {
            long maxTimeoutTime = System.currentTimeMillis();
            long id;
//            int scanCount = 0;
            boolean isFirstSendFound = true;
            CacheHeader header = file.getHeader();
            for (id = header.getStartSendId(); id < header.getRecordCount() && eventDataList.getItems().size() < maxSize; id++) {
                long[] timeData = file.getDataHeader(id);
//                scanCount ++;
                if ( timeData[CacheData.INDEX_HEADER_UPLOAD_TIME] == 0) {
                    if ( isFirstSendFound ) {
//                        Log.d(TAG, "Scan count = " + String.valueOf(scanCount));
                        header.setStartSendId(id);
                        file.writeHeader(header);
                        isFirstSendFound = false;
                    }
                    if (timeData[CacheData.INDEX_HEADER_UPLOAD_TIMEOUT_TIME] < maxTimeoutTime) {
                        // this record can be uploaded
                        CacheData data = file.getData(id);
                        EventDataItem eventDataItem = data.getEventDataItem(id);
                        eventDataList.add(eventDataItem);

                        if ( timeData[CacheData.INDEX_HEADER_UPLOAD_TIMEOUT_TIME] == 0) {
                            header.updateToRetry();
                            file.writeHeader(header);
                        }
                        long uploadTimeoutTime = maxTimeoutTime + timeout;
                        timeData[CacheData.INDEX_HEADER_UPLOAD_TIME] = 0;
                        timeData[CacheData.INDEX_HEADER_UPLOAD_TIMEOUT_TIME] = uploadTimeoutTime;
                        file.writeDataHeader(timeData, id);
                    }
                    else {
//                        Log.d(TAG, "timeout = " + String.valueOf((timeData[CacheData.INDEX_HEADER_UPLOAD_TIMEOUT_TIME] - maxTimeoutTime) / 1000 ));
                    }
                }
            }
            // at the end of all records, we now stop re scanning the data all the time
            // place the startSendId to the last record
            if (isFirstSendFound && header.getRecordCount() > 0) {
                header.setStartSendId(header.getRecordCount() - 1);
            }
            file.writeHeader(header);
            file.close();
        }
        return eventDataList;
    }

    public void updateUploadIdList(long[] idList, long timestamp) {

        CacheFile file = new CacheFile(getFile());

        if ( file.open()) {
            CacheHeader header = file.getHeader();
            for (int i = 0; i < idList.length; i++) {
                long id = idList[i];
                long[] timeData = file.getDataHeader(id);
                if ( timeData[CacheData.INDEX_HEADER_UPLOAD_TIME] == 0) {
                    timeData[CacheData.INDEX_HEADER_UPLOAD_TIME] = timestamp;
                    file.writeDataHeader(timeData, id);
                    header.updateToSend();
                    file.writeHeader(header);
                }
            }
            file.close();
        }
    }


    public EventDataStatItem getRecordCountStats() {
        EventDataStatItem eventDataStatItem = new EventDataStatItem();
        CacheFile file = new CacheFile(getFile());
        if ( file.openReadOnly()) {
            CacheHeader header = file.getHeader();
            eventDataStatItem.setTotal(header.getRecordCount());
            eventDataStatItem.setUploadDone(header.getSendCount());
            eventDataStatItem.setUploadWait(header.getWaitCount());
            eventDataStatItem.setUploadProcessing(header.getRetryCount());
            file.close();
        }
        return eventDataStatItem;
    }


    public void purge() {
        EventDataList eventDataList = new EventDataList();
        CacheFile file = new CacheFile(getFile());

        Log.d(TAG, "Purge started");
        if ( file.openReadOnly()) {
            CacheHeader header = file.getHeader();
            for (long id = 0; id < header.getRecordCount(); id++) {
                long[] timeData = file.getDataHeader(id);
                if ( timeData[CacheData.INDEX_HEADER_UPLOAD_TIME] == 0) {
                    CacheData data = file.getData(id);
                    eventDataList.add(data.getEventDataItem(id));
                }
            }
            file.close();
            if ( getFile().delete() ) {
                if ( eventDataList.getItems().size() > 0) {
                    writeEventDataList(eventDataList);
                }
            }
        }
        Log.d(TAG, "Purge end");
    }

    public EventDataStatItem rebuildRecordStats() {
        EventDataStatItem eventDataStatItem = new EventDataStatItem();
        CacheFile file = new CacheFile(getFile());

        int waitCount = 0;
        int retryCount = 0;
        int sendCount = 0;

//        Log.d(TAG, "Starting rebuild record stats" );
//        long startTime = System.currentTimeMillis();
        if ( file.open()) {
            CacheHeader header = file.getHeader();
            for (long id = 0; id < header.getRecordCount(); id++) {
                long[] timeData = file.getDataHeader(id);
                if (timeData[CacheData.INDEX_HEADER_UPLOAD_TIME] == 0) {
                    if (timeData[CacheData.INDEX_HEADER_UPLOAD_TIMEOUT_TIME] == 0) {
                        waitCount ++;
                    } else {
                        retryCount ++;
                    }
                } else {
                    sendCount++;
                }
            }
            eventDataStatItem.setTotal(header.getRecordCount());
            eventDataStatItem.setUploadWait(waitCount);
            eventDataStatItem.setUploadProcessing(retryCount);
            eventDataStatItem.setUploadDone(sendCount);
            header.setWaitCount(waitCount);
            header.setRetryCount(retryCount);
            header.setSendCount(sendCount);
            header.setStartSendId(0);
            header.setLastCheckTime(System.currentTimeMillis());
            file.writeHeader(header);
            file.close();
        }
//        Log.d(TAG, "Duration = " + String.valueOf((System.currentTimeMillis() - startTime) / 1000));
        return eventDataStatItem;
    }



}

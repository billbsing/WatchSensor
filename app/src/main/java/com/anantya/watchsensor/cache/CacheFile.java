package com.anantya.watchsensor.cache;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Created by bill on 10/16/17.
 */

class CacheFile {

    private RandomAccessFile mHandle;
    private File mFile;
    private boolean mIsReadOnly;
    private String mErrorMessage;

    public CacheFile(File file) {
        mHandle = null;
        mFile = file;
        mIsReadOnly = false;
        mErrorMessage = "";
    }

    public boolean open() {
        return open(mFile, "rw");
    }

    public boolean openReadOnly() {
        mIsReadOnly = true;
        return open(mFile, "r");
    }

    public boolean open(File file, String mode) {
        mErrorMessage = "";
        try {
            if ( mHandle != null) {
                close();
            }
            mHandle = new RandomAccessFile(file.getAbsoluteFile(), "rw");
        } catch (FileNotFoundException e) {
            mErrorMessage = e.getMessage();
        }
        return mHandle != null;
    }

    public boolean writeHeader(CacheHeader header) {
        boolean result = false;
        mErrorMessage = "";
        try {
            if ( mHandle != null) {
                header.write(mHandle);
                result = true;
            }
            else {
                mErrorMessage = "file not open";
            }
        } catch (IOException e) {
            mErrorMessage = e.getMessage();
        }
        return result;
    }

    public boolean appendData(CacheData data, long id) {
        boolean result = false;
        mErrorMessage = "";
        if ( mHandle != null) {
            try {
                data.write(mHandle, id);
                result = true;
            } catch (IOException e) {
                mErrorMessage = e.getMessage();
            }
        }
        else {
            mErrorMessage = "file not open";
        }
        return result;
    }

    public boolean close() {
        mErrorMessage = "";
        boolean result = false;
        try {
            if ( mHandle != null) {
                mHandle.close();
                mHandle = null;
                result = true;
            }
            else {
                mErrorMessage = "file not open";
            }
        } catch (IOException e) {
            mErrorMessage = e.getMessage();
        }
        return result;
    }

    public CacheHeader getHeader() {
        CacheHeader header = new CacheHeader();
        try {
            if ( mHandle != null) {
                header.read(mHandle);
            }
        } catch (IOException e) {
            mErrorMessage = e.getMessage();
        }
        return header;
    }

    public boolean isOpen() { return mHandle != null; }


    public CacheData getData(long id) {
        CacheData data = new CacheData();
        try {
            data.read(mHandle, id);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return data;
    }

    public long[] getDataHeader(long id) {
        CacheData data = new CacheData();
        long[] timeData = new long[2];
        try {
            timeData = data.getHeader(mHandle, id);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return timeData;
    }

    public void writeData(CacheData data, long id) {
        try {
            data.write(mHandle, id);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void writeDataHeader(long[] values, long id) {
        CacheData data = new CacheData();
        try {
            data.writeHeader(mHandle, values, id);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public File getFile() {
        return mFile;
    }

    public RandomAccessFile getHandle() {
        return mHandle;
    }
    public String getErrorMessage() {
        return mErrorMessage;

    }
}

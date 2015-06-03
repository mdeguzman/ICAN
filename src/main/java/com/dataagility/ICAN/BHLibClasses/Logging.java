package com.dataagility.ICAN.BHLibClasses;

//Authors: Ray Fillingham and Norman Soh
//Organisation: The Alfred
//Year: 2005

import java.io.*;
import java.util.*;
import java.text.*;

/** This class contains methods used to log HL7 messages sourced from or sent to an HL7eway
 */

public class Logging {

    BHConstants k = new BHConstants();

    public Format mDateFormat = new SimpleDateFormat("yyyyMMdd");
    public Format mTimeFormat = new SimpleDateFormat("HH:mm:ss:SSS");
    public Format mSubDirMonthYearDateFormat = new SimpleDateFormat("MMM-yyyy");
    public String mFileDate;
    public String mFileTime;
    public Date mDate;
    public String mFilename = "Out.log";
    public String mFileExt = ".log";
    public String mFilePath;
    public boolean mHexOn;
    public boolean mDateSubDirOn = false;
    public String mEnvironment = "";

    public Logging(String pFilePath, String pEnvironment, boolean pHexOn) {
        mFilePath = pFilePath.concat(pEnvironment).concat("\\");
        mHexOn = pHexOn;
    }

    public Logging(String pFilePath, String pEnvironment) {
        mFilePath = pFilePath.concat(pEnvironment).concat("\\");
        mHexOn = false;
    }

    public Logging(String pEnvironment, boolean pHexOn) {
        mFilePath = k.LOG_FILE_PATH.concat(pEnvironment).concat("\\");
        mHexOn = pHexOn;
    }

    public Logging(String pEnvironment) {
        mFilePath = k.LOG_FILE_PATH.concat(pEnvironment).concat("\\");
        mHexOn = false;
    }

    public Logging(boolean pDateSubDirOn, String pEnvironment, boolean pHexOn ) {
        if (pDateSubDirOn) {
            mDateSubDirOn = true;
            mEnvironment = pEnvironment;
            mDate = new Date();
            String aDirMonthYear = mSubDirMonthYearDateFormat.format(mDate);
            String aDirDate = mDateFormat.format(mDate);
            mFilePath = k.LOG_FILE_PATH.concat(pEnvironment).concat("\\").concat(aDirMonthYear).concat("\\").concat(aDirDate).concat("\\");
        } else {
            mDateSubDirOn = false;
            mFilePath = k.LOG_FILE_PATH.concat(pEnvironment).concat("\\");
        }
        mHexOn = pHexOn;
    }

    public Logging(boolean pDateSubDirOn, String pEnvironment) {
        if (pDateSubDirOn) {
            mDateSubDirOn = true;
            mEnvironment = pEnvironment;
            mDate = new Date();
            String aDirMonthYear = mSubDirMonthYearDateFormat.format(mDate);
            String aDirDate = mDateFormat.format(mDate);
            mFilePath = k.LOG_FILE_PATH.concat(pEnvironment).concat("\\").concat(aDirMonthYear).concat("\\").concat(aDirDate).concat("\\");
        } else {
            mDateSubDirOn = false;
            mFilePath = k.LOG_FILE_PATH.concat(pEnvironment).concat("\\");
        }
        mHexOn = false;
    }

    /** This method sets the name of file formatted as
     * [From | To]-[Sending Fac]-[Recieving Fac]_YYYYMMDD.log
     *  Example ... pFromTo = "From", pSendFac = "CSC_ALF", pRecFac = "CERNERALF" ....
     *  ........... returns "FromCSC_ALF_CERNERALF_20050923.log".
     */
    public void constructFileName(String pFromTo, String pSendFac, String pRecFac) {
        mDate = new Date();
        mFileDate = mDateFormat.format(mDate);
        mFilename = pFromTo.concat(k.HYPHEN).concat(pSendFac).concat(k.HYPHEN).concat(pRecFac).concat(k.UNDERSCORE).concat(mFileDate).concat(mFileExt);
    }

    /** This method sets the name of file formatted as
     * [Filename]_YYYYMMDD.log
     *  Example ... pCollabname = "FromCSC-ALF"
     *  ........... returns "FromCSC_ALF_20050923.log".
     */
    public void constructFileName(String pFilename) {
        // This method gets the name of file formatted as
        // <Filename>_YYYYMMDD.log
        mDate = new Date();
        mFileDate = mDateFormat.format(mDate);
        mFilename = pFilename.concat(k.UNDERSCORE).concat(mFileDate).concat(mFileExt);
    }

    /** This method WRITES pMessage into a log file with additional header information depending on pMsgTypeID.
     *  Typically :- pFileName will be of the form  .... [Filename]_YYYYMMDD.log   ... eg "FromCSC_ALF_20050923.log"
     *  ......... :- pMessage will be an HL7 message<p>
     *  ......... :- pMsgTypeID will be 0=Inbound, 1 = Outbound, 2=Information, 3=Error or 4=Undefined.
     */
    public int writeToLog(String pMessage, int pMsgTypeID) {
        // This method writes a line of text into the log file
        // The text will have a prefix of datetime
        // intMsgType passed is as follows:
        // 0 = inbound HL7Message
        // 1 = outbound HL7Message (ACK)
        // 2 = info
        // 3 = error

        String aMsgType = k.NULL;
        File aFileLog;
        BufferedWriter aBWOut;
        boolean aPathCreate = false;
        mDate = new Date();
        mFileTime = mTimeFormat.format(mDate);

        if (mDateSubDirOn) {
            String aDirMonthYear = mSubDirMonthYearDateFormat.format(mDate);
            String aDirDate = mDateFormat.format(mDate);
            mFilePath = k.LOG_FILE_PATH.concat(mEnvironment).concat("\\").concat(aDirMonthYear).concat("\\").concat(aDirDate).concat("\\");
        }

        // Check to see if file exists by creating it if none exists
        try {
            aPathCreate = (new File(mFilePath)).mkdirs();
            aFileLog = new File(mFilePath.concat(mFilename));
            aFileLog.createNewFile();

            // Format the aMsgType string
            switch (pMsgTypeID) {
                case 0:
                    aMsgType = " <<< Inbound <<<------------------------------------------------------------";
                    break;
                case 1:
                    aMsgType = " ----------------------------------------------------------->>> Outbound >>>";
                    break;
                case 2:
                    aMsgType = " ***************************** Information *********************************";
                    break;
                case 3:
                    aMsgType = " !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! Error !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!";
                    break;
                default:
                    aMsgType = " ---------------------------- Type undefined -------------------------------";
                    break;
            }
            try {
                aBWOut = new BufferedWriter(new FileWriter(mFilePath.concat(mFilename), true));
                aBWOut.write(mFileTime.concat(aMsgType).concat(k.NEW_LINE).concat(pMessage).concat(k.NEW_LINE));
                if (mHexOn == true) {
                    aBWOut.write(getHexFormat(pMessage).concat(k.NEW_LINE));
                }
                aBWOut.close();
            } catch ( IOException e ) {
                // Error writing to file
                return 3;
            }
            // Success
            return 1;
        } catch ( IOException e ) {
            // Hardware error - Directory or file cannot be created
            return 2;
        }
    }

    public int writeToLog(String pMessage) {
        // This method simply writes a line of text into a log file with date subdir

        File aFileLog;
        BufferedWriter aBWOut;
        boolean aPathCreate = false;

        if (mDateSubDirOn) {
            String aDirMonthYear = mSubDirMonthYearDateFormat.format(mDate);
            String aDirDate = mDateFormat.format(mDate);
            mFilePath = k.LOG_FILE_PATH.concat(mEnvironment).concat("\\").concat(aDirMonthYear).concat("\\").concat(aDirDate).concat("\\");
        }

        // Check to see if file exists by creating it if none exists
        try {
            aPathCreate = (new File(mFilePath)).mkdirs();
            aFileLog = new File(mFilePath.concat(mFilename));
            aFileLog.createNewFile();
            try {
                aBWOut = new BufferedWriter(new FileWriter(mFilePath.concat(mFilename), true));
                aBWOut.write(pMessage.concat(k.NEW_LINE).concat(k.NEW_LINE));
                if (mHexOn == true) {
                    aBWOut.write(getHexFormat(pMessage).concat(k.NEW_LINE));
                }
                aBWOut.close();
            } catch ( IOException e ) {
                // Error writing to file
                return 3;
            }
            // Success
            return 1;
        } catch ( IOException e ) {
            // Hardware error - Directory or file cannot be created
            return 2;
        }
    }

    public String getHexFormat(String pMessage) {
        //getHexFormat(string) formats an input string into <str><HEX> format.

        String aMessage[];
        String aResult = k.NULL;
        String aStrChar;
        char aChar;
        int aCount;
        int aCount2 = 0;
        int aDecNum;

        aMessage = new String[pMessage.length() * 2];
        for (aCount = 0; aCount <= pMessage.length() - 1; aCount++) {
            aStrChar = pMessage.substring(aCount, aCount + 1);
            aChar = aStrChar.charAt(0);
            aMessage[aCount2++] = aStrChar;
            aMessage[aCount2++] = "[".concat(Integer.toHexString(aChar)).concat("]");
        }

        //Convert string array to string
        for (aCount = 0; aCount <= aMessage.length - 1; aCount++) {
            aResult = aResult.concat(aMessage[aCount]);
        }
        return aResult;
    }
}

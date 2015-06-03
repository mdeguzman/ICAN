package com.dataagility.ICAN.BHLibSLEEPSTUDY;

import com.dataagility.ICAN.BHLibClasses.*;
import jcifs.smb.SmbFile;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * @author mdeguzman
 */
public class SLEEPSTUDYProcessFromUFD extends ProcessSegmentsFromUFD {
    public String mEnvironment = "";

    /**
     * Generic Segment Processing Methods
     *
     * @param pHL7Message Initialized with the HL7 message to be processed.
     */
    public SLEEPSTUDYProcessFromUFD(String pHL7Message, String pEnvironment) throws ICANException {
        super(pHL7Message);
        mEnvironment = pEnvironment;
    }

    public byte[] processMessage() throws ICANException {

        byte[] aResultByteArr = null;

        HL7Message aInMess = new HL7Message(mHL7Message);
        HL7Segment aMSHSegment = new HL7Segment(aInMess.getSegment(HL7_23.MSH));

        //R01 = Result event
        if (aInMess.isEvent("R01")) {
            HL7Segment OBR = new HL7Segment(k.NULL);
            OBR.setSegment(aInMess.getSegment(HL7_23.OBR));
            String aResultFile = OBR.getField(HL7_23.OBR_18_Placers_Field_1);

            // if result file is in an ftp or http location
            // (ie. "ftp://username:password@127.0.0.1/folder/file.txt" or "http://127.0.0.1/file.txt")
            if (StringUtils.containsIgnoreCase(aResultFile, "ftp://") || StringUtils.containsIgnoreCase(aResultFile, "http://")) {
                aResultByteArr = getResultFromFTPorHTTP(aResultFile);

            // if result file is in an smb location (ie. "smb://127.0.0.1/folder/file.txt")
            } else if (StringUtils.containsIgnoreCase(aResultFile, "smb://")) {
                aResultByteArr = getResultFromSMB(aResultFile);

            // if result file is in a local or shared folder location (ie "c:\folder\file.txt" or "\\sharedfolder\file.txt")
            } else {
                aResultByteArr = getResultFromFile(aResultFile);
            }
        }

        return aResultByteArr;
    }

    /**
     * Retrieve byte[] from a file located on a FTP or HTTP server.
     * @param pResultFile file location string
     * @return byte[] file byte array
     * @throws ICANException exception
     */
    private byte[] getResultFromFTPorHTTP(String pResultFile) throws ICANException {
        String aResultFile = pResultFile;
        InputStream aInputStream = null;
        byte[] aResultFileByteArr = null;

        try {
            URL aUrl = new URL(aResultFile);
            URLConnection aUrlConnection = aUrl.openConnection();
            aInputStream = aUrlConnection.getInputStream();
            aResultFileByteArr = IOUtils.toByteArray(aInputStream);

            // alternate implementation
            // File file = File.createTempFile("", "");
            // FileUtils.copyURLToFile(url, file);
            // fileByteArray = FileUtils.readFileToByteArray(file);
        } catch (Exception e) {
            throw new ICANException(mEnvironment, e.getMessage());
        } finally {
            if (aInputStream != null) {
                try {
                    aInputStream.close();
                } catch (Exception e) {
                    throw new ICANException(mEnvironment, e.getMessage());
                }
            }
        }
        return aResultFileByteArr;
    }

    /**
     * Retrieve byte[] from a file located on a SMB server.
     * @param pResultFile file location string
     * @return byte[] file byte array
     * @throws ICANException exception
     */
    private byte[] getResultFromSMB(String pResultFile) throws ICANException {
        String aResultFile = pResultFile;
        InputStream aInputStream = null;
        byte[] aResultFileByteArr = null;

        try {
            // if authentication needed
            // String username = "username";
            // String password = "password";
            // NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication("", username, password);
            // SmbFile smbFile = new SmbFile(resultFileLocation, auth);

            SmbFile smbFile = new SmbFile(aResultFile);
            aInputStream = smbFile.getInputStream();
            aResultFileByteArr = IOUtils.toByteArray(aInputStream);
        } catch (Exception e) {
            throw new ICANException(mEnvironment, e.getMessage());
        } finally {
            if (aInputStream != null) {
                try {
                    aInputStream.close();
                } catch (Exception e) {
                    throw new ICANException(mEnvironment, e.getMessage());
                }
            }
        }
        return aResultFileByteArr;
    }

    /**
     * Retrieve byte[] from a file located on a local or shared network folder.
     * @param pResultFile file location string
     * @return byte[] file byte array
     * @throws ICANException exception
     */
    private byte[] getResultFromFile(String pResultFile) throws ICANException {
        String aResultFile = pResultFile;
        InputStream aInputStream = null;
        byte[] aResultFileByteArr = null;

        try {
            File resultFile = new File(aResultFile);
            aResultFileByteArr = FileUtils.readFileToByteArray(resultFile);
        } catch (Exception e) {
            throw new ICANException(mEnvironment, e.getMessage());
        } finally {
            if (aInputStream != null) {
                try {
                    aInputStream.close();
                } catch (Exception e) {
                    throw new ICANException(mEnvironment, e.getMessage());
                }
            }
        }
        return aResultFileByteArr;
    }
}

package com.dataagility.ICAN.BHLibSLEEPSTUDY;

import com.dataagility.ICAN.BHLibClasses.ICANException;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * @author mdeguzman
 */
public class SLEEPSTUDYProcessFromUFDTest {

    private String ORU = "";

    @Before
    public void setup() {
        ORU = "MSH|^~\\&|PFTLAB|ALF|SMARTHEALTH|SHS|20150505182325||ORU^R01|0505182325000000001|P|2.3.1|||AL|NE|AUS\r" +
                "PID|||0999995^^^ALF^MR||BROWN^Terence^||19330926|M|||^^^^\r" +
                "PV1||O|^^^ALF^^\r" +
                "OBR|1||94405^LOCAL|ANTH^Anthropometry^SH||20150224112505|20150224112000|||||||||^WILSON^JOHN^^^PROF.||FILENAME||||20150224112000||PF|F\r" +
                "OBX|1|NM|8302-2^Height^LN||159|cm|||||F\r" +
                "OBX|2|NM|3141-9^Weight^LN||66.2|kg|||||F\r" +
                "OBR|2||94405^LOCAL|PRSP^PreBD Spirometry^SH||20150224112505|20150224112000|||||||||^WILSON^JOHN^^^PROF.||||||20150224112000||PF|F\r" +
                "OBX|1|NM|20157-4^PreBD FEV1^LN||1.09|L|> 1.30||||F\r" +
                "OBX|2|NM|20151-7^PreBD %Predicted FEV1^LN||56|%|||||F\r" +
                "OBX|3|NM|19876-2^PreBD FVC^LN||2.2|L|> 2.06||||F\r" +
                "OBX|4|NM|19871-3^PreBD %Predicted FVC^LN||78|%|||||F\r" +
                "OBX|5|NM|19927-3^PreBD MMEF^LN||0.6|L/sec|> 0.0||||F\r" +
                "OBX|6|NM|19944-8^PreBD % Predicted MMEF^LN||46|%|||||F\r" +
                "OBX|7|NM|33452-4^PreBD PEF^LN||3.7|L/sec|> 3.7||||F\r" +
                "OBX|8|NM|SH2091^PreBD % Predicted PEF^LN||67|%|||||F\r" +
                "OBX|9|TX|15427-8^Report Comment^LN||Test performance was good.||||||F";
    }

    @Test()
    public void testLocalFile() throws ICANException {
        String message = StringUtils.replace(ORU, "FILENAME", "C:\\temp\\test.txt");
        SLEEPSTUDYProcessFromUFD test = new SLEEPSTUDYProcessFromUFD(message, "TEST");
        byte[] result = test.processMessage();
        assertNotNull(result);
    }

    @Test()
    public void testLocalFileNotExisting() throws ICANException {
        String message = StringUtils.replace(ORU, "FILENAME", "C:\\temp\\test2.txt");
        byte[] result = null;
        SLEEPSTUDYProcessFromUFD test = new SLEEPSTUDYProcessFromUFD(message, "TEST");
        try {
            result = test.processMessage();
        } catch (Exception e) {
            assertNull(result);
        }
    }
}

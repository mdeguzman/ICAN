/*
 * VIDRLProcessFromUFD.java
 *
 * Created on 11 October 2005, 15:25
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */
package BHLibOlinqua;

import BHLibClasses.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 *
 * @author normansoh
 */
public class OlinquaProcessFromUFD extends ProcessSegmentsFromUFD {

    public String mEnvironment = "";

    /**
     * Creates a new instance of VIDRLProcessFromUFD
     */
    public OlinquaProcessFromUFD(String pHL7Message, String pEnvironment) throws ICANException {
        super(pHL7Message);
        mVersion = "A";
        mEnvironment = pEnvironment;
    }

    public String[] processMessage() throws ICANException {

        String aOlinquaMessageArray[] = {k.NULL, k.NULL, k.NULL};
        HL7Message aInMess = new HL7Message(mHL7Message);

        HL7Segment aMSHSegment = new HL7Segment(aInMess.getSegment(HL7_23.MSH));
        //O01 = Oh Zero One
        if (aInMess.isEvent("O01")) {
            HL7Segment OBR = new HL7Segment(k.NULL);
            OBR.setSegment(aInMess.getSegment(HL7_23.OBR));
            String obr4 = OBR.getField(HL7_23.OBR_4_Universal_Service_ID);

            String regex = "NOMATCH";
            try {
                regex = getRegexPattern();
            } catch (Exception e) {
            }
            if (obr4.matches(regex)) {
                //Send HL7 message
                aMSHSegment.set(HL7_23.MSH_5_receiving_application, "Olinqua");
                aInMess.setSegment(aMSHSegment.getSegment(), 1);
                aOlinquaMessageArray[0] = aMSHSegment.get(HL7_23.MSH_3_sending_application);
                aOlinquaMessageArray[1] = aMSHSegment.get(HL7_23.MSH_4_sending_facility);
                aOlinquaMessageArray[2] = aInMess.getMessage();
            }
        }
        return aOlinquaMessageArray;
    }

    public String replaceInStr(String pSrc, String pFrom, String pTo) {
        int aPos;

        aPos = pSrc.indexOf(pFrom);
        if (aPos >= 0) {
            pSrc = pSrc.substring(0, aPos) + pTo + pSrc.substring(aPos + pFrom.length());
        }
        return pSrc;

    }

    public String getRegexPattern() throws IOException {
        String pattern = "";

        BufferedReader br = new BufferedReader(new FileReader("c:\\ICANLookUpTables\\" + mEnvironment + "\\OlinquaFilter.table"));
        try {

            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {

                sb.append(".*");

                sb.append(line);
                sb.append(".*|");
                line = br.readLine();
            }
            pattern = sb.toString();

        } catch (Exception e) {
        } finally {
            br.close();
        }
        return pattern;
    }
}

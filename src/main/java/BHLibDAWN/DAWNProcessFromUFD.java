/*
 * DAWNProcessFromUFD.java
 *
 * Created on 11 October 2005, 15:25
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package BHLibDAWN;
import BHLibClasses.*;

/**
 *
 * @author norman soh
 */
public class DAWNProcessFromUFD extends ProcessSegmentsFromUFD {

    public String mEnvironment = "";
    /**
     * Creates a new instance of DAWNProcessFromUFD
     */
    public DAWNProcessFromUFD(String pHL7Message, String pEnvironment) throws ICANException {
        super(pHL7Message);
        mVersion = "A";
        mEnvironment = pEnvironment;
    }

    public String[] processMessage() throws ICANException {
        String aHL7MessageArray[] = {k.NULL, k.NULL, k.NULL};
        String aSegment;
        HL7Group aGroup;
        HL7Message aInMess = new HL7Message(mHL7Message);
        HL7Message aOutMess = new HL7Message(k.NULL);
        HL7Segment aMSHSegment = new HL7Segment(aInMess.getSegment(HL7_23.MSH));

        if (aInMess.isEvent("R01")) {
            HL7Segment aInOBRSegment = new HL7Segment(aInMess.getSegment("OBR"));
            String aOBR4_1TestCode = aInOBRSegment.get(HL7_23.OBR_4_Universal_Service_ID, HL7_23.CE_ID_code);
            if (aOBR4_1TestCode.matches("5508040") ||
                    aOBR4_1TestCode.matches("5508010") ||
                    aOBR4_1TestCode.matches("5508020")) {
                aOutMess = aInMess;
                aOutMess.setSegment(processMSHFromUFD("DAWN").getSegment());
                aHL7MessageArray[0] = aMSHSegment.getField(HL7_23.MSH_3_sending_application);
                aHL7MessageArray[1] = aMSHSegment.getField(HL7_23.MSH_4_sending_facility);
                aHL7MessageArray[2] = aOutMess.getMessage();
            }
        }
        return aHL7MessageArray;
    }
}

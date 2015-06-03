/*
 * XCELERAProcessToUFD.java
 *
 * Created on 5 January 2007, 15:57
 *
 */

package BHLibXCELERA;

import BHLibClasses.*;

/**
 *
 * @author sohn
 */
public class XCELERAProcessToUFD extends ProcessSegmentsToUFD {

    public HL7Message mInHL7Message;
    public String mEnvironment = "";

    /** Creates a new instance of XCELERAProcessToUFD */
    public XCELERAProcessToUFD(String pHL7Message, String pEnvironment) throws ICANException {
        super(pHL7Message);
        mVersion = "a";    // Release Version Number
        mHL7Message = pHL7Message;
        mEnvironment = pEnvironment;
    }

    public String processMessage() throws ICANException {
        HL7Message aOutHL7Message = new HL7Message(k.NULL);
        HL7Segment aInMSHSegment = new HL7Segment(mInHL7Message.getSegment("MSH"));

        if (mInHL7Message.isEvent("R01")) {
            HL7Segment aMSHSegment = processMSHToUFD();
            HL7Segment aPIDSegment = processPIDToUFD();
            HL7Segment aPV1Segment = processPV1ToUFD();
            HL7Group aOrderObservationsGroup = processOrderObservations_ToUFD();
            HL7Segment aZBXSegment = processZDSToUFD();

            aOutHL7Message.append(aMSHSegment);
            aOutHL7Message.append(aPIDSegment);
            aOutHL7Message.append(aPV1Segment);
            aOutHL7Message.append(aOrderObservationsGroup);
            aOutHL7Message.append(aZBXSegment);
        } else if (mInHL7Message.isEvent("O01")) {
            HL7Segment aMSHSegment = processMSHToUFD();
            HL7Segment aPIDSegment = processPIDToUFD();
            HL7Segment aPV1Segment = processPV1ToUFD();
            HL7Group aOrderObservationsGroup = processOrderObservations_ToUFD();

            aOutHL7Message.append(aMSHSegment);
            aOutHL7Message.append(aPIDSegment);
            aOutHL7Message.append(aPV1Segment);
            aOutHL7Message.append(aOrderObservationsGroup);
        }
        if (aOutHL7Message.getMessage().length() > 0) {
            aOutHL7Message.append(setupZBX("MESSAGE", "SOURCE_ID", aInMSHSegment.get(HL7_23.MSH_10_message_control_ID)));
        }

        return aOutHL7Message.getMessage();
    }

    public HL7Group processOrderObservations_ToUFD() throws ICANException {
        HL7Group aOutOrderObservationsGroup = new HL7Group("");
        int aGroupCount;
        int aCount = 1;
        String aGroupID[] = HL7_23.Group_Orders;
        aGroupCount = mInHL7Message.countGroups(aGroupID);

        for (aCount = 1; aCount <= aGroupCount; aCount++) {
            aOutOrderObservationsGroup.append(mInHL7Message.getGroup(aGroupID, aCount));
        }
        return aOutOrderObservationsGroup;
    }

    public HL7Segment processZDSToUFD() throws ICANException {
        HL7Segment aOutSegment = new HL7Segment("ZBX");
        HL7Segment aInZDSSegment = new HL7Segment(mInHL7Message.getSegment("ZDS"));
        aOutSegment = setupZBX("ORDER", "STUDY_ID", aInZDSSegment.get(XCELERA_23.ZDS_1_study_instance_id));
        return aOutSegment;
    }
}

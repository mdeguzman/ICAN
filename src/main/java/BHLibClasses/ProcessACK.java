/*
 * ProcessACK.java
 *
 * Created on 23 December 2005, 12:51
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package BHLibClasses;

/**
 *
 * @author sohn
 */
public class ProcessACK {

    public HL7Message mHL7Message;

    /** Creates a new instance of ProcessACK */
    public ProcessACK(HL7Message pHL7Message) {
        mHL7Message = pHL7Message;
    }

    public String getACK() {
        HL7Message aOutMessage = new HL7Message("");
        HL7Segment aOutMSHSegment = new HL7Segment("MSH");
        HL7Segment aOutMSASegment = new HL7Segment("MSA");
        HL7Segment aInMSHSegment = new HL7Segment(mHL7Message.getSegment("MSH"));
        aOutMSHSegment.linkTo(aInMSHSegment);
        aOutMSHSegment.copy(HL7_23.MSH_2_encoding_characters);

        String aMSH3SendApp = aInMSHSegment.get(HL7_23.MSH_3_sending_application);
        String aMSH4SendFac = aInMSHSegment.get(HL7_23.MSH_4_sending_facility);
        String aMSH5RecApp = aInMSHSegment.get(HL7_23.MSH_5_receiving_application);
        String aMSH6RecFac = aInMSHSegment.get(HL7_23.MSH_6_receiving_facility);

        aOutMSHSegment.set(HL7_23.MSH_3_sending_application, aMSH5RecApp);
        aOutMSHSegment.set(HL7_23.MSH_4_sending_facility, aMSH6RecFac);

        aOutMSHSegment.set(HL7_23.MSH_5_receiving_application, aMSH3SendApp);
        aOutMSHSegment.set(HL7_23.MSH_6_receiving_facility, aMSH4SendFac);

        aOutMSHSegment.set(HL7_23.MSH_7_message_date_time, aOutMSHSegment.getDateTime());
        aOutMSHSegment.set(HL7_23.MSH_9_message_type, "ACK");
        aOutMSHSegment.copy(HL7_23.MSH_10_message_control_ID);
        aOutMSHSegment.copy(HL7_23.MSH_11_processing_ID);
        aOutMSHSegment.copy(HL7_23.MSH_12_version_ID);

        aOutMSASegment.set("MSA_1", "AA");
        aOutMSASegment.set("MSA_2", aOutMSHSegment.get(HL7_23.MSH_10_message_control_ID));

        aOutMessage.append(aOutMSHSegment);
        aOutMessage.append(aOutMSASegment);

        return aOutMessage.getMessage();
    }
}

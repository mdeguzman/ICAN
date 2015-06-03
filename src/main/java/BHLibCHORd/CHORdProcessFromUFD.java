/*
 * OPASProcessFromUFD.java
 *
 * Created on 11 October 2005, 17:04
 *
 */
package BHLibCHORd;

import BHLibClasses.*;
import java.sql.SQLException;

/**
 * OPASProcessFromUFD contains the methods required to build a OPAS message from
 * a UFD HL7 message structure
 *
 * @author Ray Fillingham and Norman Soh
 */
public class CHORdProcessFromUFD extends ProcessSegmentsFromUFD {

    /**
     * Constant class
     */
    public BHConstants k = new BHConstants();
    public String mEnvironment = "";
    public HL7Message mInHL7Message = new HL7Message("");
    //--------------------------------------------------------------------------

    /**
     * This constructor creates a new instance of OPASProcessFromUFD passing a
     * HL7 UFD message structure
     *
     * @param pHL7Message HL7 message text string
     * @param pEnvironment the environment this is running on
     * @throws BHLibClasses.ICANException ICANException
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public CHORdProcessFromUFD(String pHL7Message, String pEnvironment) throws ICANException, ClassNotFoundException, SQLException {
        super(pHL7Message);
        mHL7Message = pHL7Message;
        mEnvironment = pEnvironment;

    }
    //--------------------------------------------------------------------------

    /**
     * This method contains the methods required to build a OPAS HL7 message
     *
     * @return OPAS HL7 message text string
     * @throws BHLibClasses.ICANException ICANException
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public String processMessage() throws ICANException, ClassNotFoundException, SQLException {

        mVersion = "B";
        mInHL7Message = new HL7Message(mHL7Message, k.TRIM_LAST);
        //List<String> output = new ArrayList<String>();
        HL7Segment msh = new HL7Segment(mInHL7Message.getSegment(HL7_23.MSH));
        String hospCode = msh.get(HL7_23.MSH_4_sending_facility);
        if (mInHL7Message.isEvent("A01, A02, A03, A04, A08, A12, A13, A21, A22, A28, A31, O01, R01")) {

            PatientLookup patientLookup = new PatientLookup();
            patientLookup.setHospCode(hospCode);
            HL7Segment pPIDSegment = new HL7Segment(mInHL7Message.getSegment(HL7_23.PID));
            String aPID3Array[] = pPIDSegment.getRepeatFields(HL7_23.PID_3_patient_ID_internal);
            HL7Field aPID3Field = new HL7Field();
            int aPID3ArrayCount = aPID3Array.length;

            for (int i = 0; i < aPID3ArrayCount; i++) {
                aPID3Field.setField(aPID3Array[i]);
                if (aPID3Field.getSubField(HL7_23.CX_ID_type_code).equalsIgnoreCase("PI")) {
                    String urNumber = aPID3Field.getSubField(HL7_23.CX_ID_number);
                    aPID3Field.setSubField(mFacility, HL7_23.CX_assigning_authority);
                    if (mFacility.equalsIgnoreCase("SDMH") && Integer.parseInt(urNumber) < 899546) {
                        CodeLookUp aLookUp = new CodeLookUp("SDMH_6To7Digit_UR.table", mEnvironment);
                        urNumber = aLookUp.getValue(urNumber);
                    }
                    patientLookup.setPatientId(urNumber);
                } else if (aPID3Field.getSubField(HL7_23.CX_ID_type_code).equalsIgnoreCase("MC")) {
                    String medicareNumber = aPID3Field.getSubField(HL7_23.CX_ID_number);
                    patientLookup.setMedicare(medicareNumber);

                } else if (aPID3Field.getSubField(HL7_23.CX_ID_type_code).equalsIgnoreCase("VA")) {
                    String dvaNumber = aPID3Field.getSubField(HL7_23.CX_ID_number);
                    patientLookup.setDvaNumber(dvaNumber);

                } else if (aPID3Field.getSubField(HL7_23.CX_ID_type_code).equalsIgnoreCase("PEN")) {
                    String pensionNumber = aPID3Field.getSubField(HL7_23.CX_ID_number);
                    patientLookup.setPensionId(pensionNumber);

                }
            }

            String dvaNumber = pPIDSegment.get(HL7_23.PID_27_veterans_military_status);
            if (dvaNumber.length() > 0) {
                patientLookup.setDvaNumber(dvaNumber);
            }

            patientLookup.setPatientDob(pPIDSegment.get(HL7_23.PID_7_date_of_birth));
            //process first address in PID-11 Patient address
            String aPID5Array[] = pPIDSegment.getRepeatFields(HL7_23.PID_5_patient_name);
            int aPID5ArrayCount = aPID5Array.length;
            HL7Field aPID5Field = new HL7Field();
            for (int i = 0; i < aPID5ArrayCount; i++) {
                aPID5Field.setField(aPID5Array[i]);
                if (aPID5Field.getSubField(HL7_23.XPN_name_type).equalsIgnoreCase("L")) {
                    patientLookup.setPatientName(aPID5Array[0]);
                    i = aPID5ArrayCount;
                }
            }

            int chordUID = patientLookup.execute();
            if (chordUID>0){
                OPASProcessFromUFD opasProcess = new OPASProcessFromUFD(mHL7Message, mEnvironment);
                String opasMessage = opasProcess.processMessage()[2] ;
                System.out.println("++++ OPAS Message:" + opasMessage);
                if (opasMessage.length()>0){
                  opasMessage = opasMessage+"ZUI|"+chordUID;
                  OPASProcessToCHORD toCHORD = new OPASProcessToCHORD(opasMessage, mEnvironment);
                  return toCHORD.processMessage();
                }
            }

        }
        return "";

    }


}

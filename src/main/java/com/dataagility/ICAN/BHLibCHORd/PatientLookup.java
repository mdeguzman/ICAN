/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dataagility.ICAN.BHLibCHORd;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;


/**
 *
 * @author ashaw
 */
public class PatientLookup {

    private String patientId;
    private String hospCode;
    private String patientFamilyName;
    private String patientGivenName;
    private String patientTitleCode;
    private String patientDob;
    private String medicareNo;
    private String medicareLineNo;
    private String pensionId;
    private String dvaId;

    private final Connection conn;
    private final CallableStatement patientLookup;

    public PatientLookup() throws ClassNotFoundException, SQLException {
        CHORdWrapper opas = new CHORdWrapper();
        this.conn = opas.getConnection();

        patientLookup = conn.prepareCall("{? = call sp_lookupPatient ?,?,?,?,?,"
                + "?,?,?,?}");
        patientLookup.registerOutParameter(1, Types.INTEGER);
    }

    public int execute() {
        try {

            patientLookup.setString("inPatientId", this.patientId);
            patientLookup.setString("inHospCode", this.hospCode);
            patientLookup.setString("inMedicareNo", this.medicareNo );
            patientLookup.setString("inMedicareLineNo", this.medicareLineNo);
            patientLookup.setString("inPatientFamilyName", this.patientFamilyName);
            patientLookup.setString("inPensionId", this.pensionId);
            patientLookup.setString("inPatientGivenName", this.patientGivenName);
            patientLookup.setString("inDVAId", this.dvaId);
            patientLookup.setString("inBirthDate", this.patientDob);
            //patientLookup.execute();

            //int returnValue = patientLookup.getInt(1);
            ResultSet rs = patientLookup.executeQuery();
           //System.out.println(rs.getMetaData().getColumnCount());
           while (rs.next()){
               if (rs.getBoolean("consented")){
                   return rs.getInt("UID");
               }

           }

            return -1;
            //ResultSet rs = pmiUpdate.execute();
            //rs.getString("Return Value");
        } catch (SQLException s) {
            s.printStackTrace();
            return -1;
        }
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId.trim();
    }

    public String getHospCode() {
        return hospCode;
    }

    public void setHospCode(String hospCode) {
        hospCode = hospCode.trim();
        if (hospCode.length() > 5) {
            hospCode = hospCode.substring(0, 5);
        }
        this.hospCode = hospCode;
    }

    public void setPatientName(String hl7Name) {
        String[] namePart = hl7Name.split("\\^", -1);
        this.patientFamilyName = namePart[0];
        this.patientGivenName = namePart[1];
        this.patientTitleCode = namePart[4];
    }

    public String getPatientFamilyName() {
        return patientFamilyName;
    }

    public void setPatientFamilyName(String patientFamilyName) {
        this.patientFamilyName = patientFamilyName.toUpperCase();
    }

    public String getPatientGivenName() {
        return patientGivenName;
    }

    public void setPatientGivenName(String patientGivenName) {
        this.patientGivenName = patientGivenName;
    }

    public String getPatientTitleCode() {
        return patientTitleCode;
    }

    public void setPatientTitleCode(String patientTitleCode) {
        this.patientTitleCode = patientTitleCode;
    }

    public String getMedicareNo() {
        return medicareNo;
    }

    public void setMedicare(String hl7Medicare) {
        if (hl7Medicare.equalsIgnoreCase("C-U")) {
            this.medicareNo = hl7Medicare;
        } else {
            String[] medicareNoPart = hl7Medicare.split(" ", -1);
            try{
            this.medicareNo = medicareNoPart[0].substring(0, 10);

            this.medicareLineNo = medicareNoPart[0].substring(10, 11);
            } catch (Exception e) {
                this.medicareNo = medicareNoPart[0];
            }
        }
    }

    public void setMedicareNo(String medicareNo) {
        this.medicareLineNo = medicareNo;
    }

    public String getMedicareLineNo() {
        return medicareLineNo;
    }

    public void setMedicareLineNo(String medicareLineNo) {
        this.medicareLineNo = medicareLineNo;
    }

    public String getPatientDob() {
        return Utilities.FormatDateTimeSQLtoHL7(patientDob);
    }

    public void setPatientDob(String patientDob) {
        patientDob = patientDob + "00000000";
        patientDob = patientDob.substring(0, 8);
        patientDob = Utilities.FormatDateTimeHL7toSQL(patientDob);
        this.patientDob = patientDob;
    }

    public String getPensionId() {
        return pensionId;
    }

    public void setPensionId(String pensionId) {
        this.pensionId = pensionId;
    }

    public String getDvaNumber() {
        return dvaId;
    }

    public void setDvaNumber(String dvaNumber) {
        this.dvaId = dvaNumber;
    }

}

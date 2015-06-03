package BHLibClasses;
//Authors: Ray Fillingham and Norman Soh
//Organisation: The Alfred
//Year: 2005
/**
 * This is a container Class carrying Segment, Field and SubField positioning details.
 */
public class HL7FieldDescriptor {

    private String mData[];
    public String ID;
    public int fieldNum;
    public int subFieldNum;
    public int subSubFieldNum;
    public int numOfLevels;
    BHConstants k = new BHConstants();

    /** Creates a new instance of HL7FieldDescriptor */
    public HL7FieldDescriptor(String pFieldString) {
       ID = k.NULL;
       fieldNum = 0;
       subFieldNum = 0;
       subSubFieldNum = 0;
       mData = pFieldString.split(k.UNDERSCORE);
       ID = mData[0];
       numOfLevels = mData.length;
       if (mData.length > 1) fieldNum = GetInteger(mData[1]);
       if (mData.length > 2) subFieldNum = GetInteger(mData[2]);
       if (mData.length > 3) subSubFieldNum = GetInteger(mData[3]);
    }

    private int GetInteger(String pString) {
        int aInt;
        try
        {
            aInt = Integer.parseInt(pString);
        }
        catch(NumberFormatException e)
        {
            aInt = 0;
        }
        return aInt;
    }

}

package tp1.util;

public class GoogleReturnValues {
	
	String range;
	String majorDimension;
	String[][] values;
	
	public GoogleReturnValues(String range,String majorDimension,String[][] values)
	{
		this.range = range;
		this.majorDimension = majorDimension;
		this.values = values;
	}
	
	public String getRange()
	{
		return this.range;
	}
	
	public String getMajorDimension()
	{
		return this.majorDimension;
	}
	
	public String[][] getValues()
	{
		return this.values;
	}
	
	public void setRange(String range)
	{
		 this.range = range;
	}
	
	public void setMajorDimension(String majorDimension)
	{
		this.majorDimension = majorDimension;
	}
	
	public void setValues(String[][] values)
	{
	    this.values = values;
	}
	
	

}

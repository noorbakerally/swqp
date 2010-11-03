package edu.rpi.tw.eScience.WaterQualityPortal.usgs;

import java.util.ArrayList;
import java.util.Date;

public class MeasurementSite {
	private String loc_id;
	double lat,lon;
	String country_code;
	Integer state_code;
	Integer county_code;
	ArrayList<Measurement> data = new ArrayList<Measurement>(); 
	
	    class Measurement {
	    	String ID; 
	    	Date date;
	    	String time;
	    	String chemical;
	    	double value;
	    	String unit;  //g/ml
	    	
	    }

		public void setId(String loc_id) {
			this.loc_id = loc_id;
		}

		public String getId() {
			return loc_id;
		}
		   


}

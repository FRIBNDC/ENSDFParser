package ensdfparser.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtil {

	Calendar cal=null;
	boolean isValid=false,isLenient=false;
	
	public DateUtil() {//current date
		cal=Calendar.getInstance();
	}
	
	   public DateUtil(String dateS,String format) {
	        cal=Calendar.getInstance();
	        isLenient=false;
	        parseDate(dateS,format);
	    }
	   
	public DateUtil(String dateS,String format,boolean isLenient) {
		cal=Calendar.getInstance();
		this.isLenient=isLenient;
		parseDate(dateS,format);
	}

	public static DateUtil currentDate() {
		return new DateUtil();
	}
	
	private void parseDate(String dateS,String formatS) {
		try {
			SimpleDateFormat  format= new SimpleDateFormat(formatS);
			
			//if isLenient=true, it will accept invalid date like "30-FEB-2021", which will be interpreted as "2-MAR-2021" after parsing
			//if isLenient false, the above date will not be able to be parsed
			format.setLenient(isLenient);
			
			Date date = format.parse(dateS);

			cal.clear();
			cal.setTime(date);

			isValid=true;
			
			//Calendar cal1=cal;
			//System.out.println(" "+dateS+" format="+formatS+" first day of week="+cal1.getFirstDayOfWeek()+" monday="+Calendar.MONDAY+"  "+cal1.get(Calendar.DAY_OF_WEEK));
			//System.out.println(cal1.get(Calendar.WEEK_OF_MONTH)+"  "+cal1.get(Calendar.DAY_OF_WEEK_IN_MONTH));
			//System.out.println(cal1.get(Calendar.YEAR)+"  "+cal1.get(Calendar.MONTH)+" "+cal1.get(Calendar.DAY_OF_MONTH)+" "+cal1.get(Calendar.DAY_OF_WEEK)+"  "+cal1.get(Calendar.DAY_OF_YEAR));
		}catch(Exception e) {
		    isValid=false;
		    cal=null;
//			e.printStackTrace();
		}
	}
	
	public Calendar getCalendar() {return cal;}
	
	public int year() {try{return cal.get(Calendar.YEAR);}catch(Exception e) {return -1;}}
	public int month() {try{return cal.get(Calendar.MONTH)+1;}catch(Exception e) {return -1;}}//in Calendar, month=0 for January; this function returns actual index of month, with 1 for Jan
	public int day() {try{return cal.get(Calendar.DAY_OF_MONTH);}catch(Exception e) {return -1;}}//day of month, first day=1	
	public int dayOfYear() {try{return cal.get(Calendar.DAY_OF_YEAR);}catch(Exception e) {return -1;}}//start from 1 for the first day of a year
	public int dayOfWeek() {try{return cal.get(Calendar.DAY_OF_WEEK);}catch(Exception e) {return -1;}}//SUNDAY=1, the first day of week by default
	
	//get(Calendar.DAY_OF_WEEK_IN_MONTH) return the index of the week in a month, index starting from 1, first week in month starting from day 1 of the month
	//so it returns 1 for day 1-7, 2 for day 8-14, 3 for day 15-21, 4 for day 22-28, 5 for rest
	//get(Calendar.DAY_OF_WEEK) return the index of the actual week for the day, starting from 1, the first week not necessarily staring from day 1 of the month
	
	public int weekOfYear() {try{return cal.get(Calendar.WEEK_OF_YEAR);}catch(Exception e) {return -1;}}
	public int weekOfMonth() {try{return cal.get(Calendar.WEEK_OF_MONTH);}catch(Exception e) {return -1;}}
	
	public String monthName(String option) {
		String name="";
		try {
			option=option.toUpperCase();
			
			if((option.startsWith("M")&&option.length()<4) || option.equals("SHORT") || option.equals("S")) {
				SimpleDateFormat  format= new SimpleDateFormat("MMM");			
				name=format.format(cal.getTime());
				
				//name=getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault());
			}else {
				SimpleDateFormat  format= new SimpleDateFormat("MMMM");
				name=format.format(cal.getTime());
				
				//name=getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault());
			}
		}catch(Exception e) {
			
		}
		
		return name;
				
	}
	public String monthName() {
		return monthName("");
	}
	public String monthNameUpper(String option) {
		return monthName(option).toUpperCase();
	}
	public String monthNameUpper() {
		return monthNameUpper("");
	}
	
	public String dayName(String option) {
		String name="";
		try {
			option=option.toUpperCase();
			
			if( (option.startsWith("E")&&option.length()<4) || option.equals("SHORT") || option.equals("S")) {
				SimpleDateFormat  format= new SimpleDateFormat("EEE");			
				name=format.format(cal.getTime());
				
				//name=getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault());
			}else {
				SimpleDateFormat  format= new SimpleDateFormat("EEEE");
				name=format.format(cal.getTime());
				
				//name=getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault());
			}
		}catch(Exception e) {
			
		}
		
		return name;
				
	}
	public String dayName() {
		return dayName("");
	}
	public String dayNameUpper(String option) {
		return dayName(option).toUpperCase();
	}
	public String dayNameUpper() {
		return dayNameUpper("");
	}
	
	public boolean isValid() {return isValid;}
	
	public boolean isLenient() {return isLenient;}

}

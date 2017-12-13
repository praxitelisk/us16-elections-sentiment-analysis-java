package webmining.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/*
 * A class to change a date into a timestamp in milliseconds
 */
public class TimeStampUtils {
	private final long ONE_MINUTE_IN_MILLIS = 60000;// millisecs
	public int year;
	public int month;
	public int day;
	public int hour;
	public int minute;
	public long initialDateInMills;

	

	// constructor
	public TimeStampUtils(int year, int month, int day, int hour, int minute) {
		this.year = year;
		this.month = month;
		this.day = day;
		this.hour = hour;
		this.minute = minute;
		initialDateInMills = getInitialDate().getTime();
		
	}

	// return the current date
	private Date getInitialDate() {
		try {
			return new SimpleDateFormat("dd/MM/yyyy/HH:mm")
					.parse(day + "/" + month + "/" + year + "/" + hour + ":" + minute);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return new Date();
	}

	// add one hour and return it in milliseconds to existing timestamp in
	// milliseconds
	public long addMinutedToDateInMillis(long time) {

		return initialDateInMills = initialDateInMills + (time * ONE_MINUTE_IN_MILLIS);

	}

}

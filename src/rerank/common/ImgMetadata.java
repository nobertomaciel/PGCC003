package rerank.common;

import java.util.GregorianCalendar;

public class ImgMetadata {
	private String user, id;
	private double lat, lon;
	private int numViews;
	private GregorianCalendar date;
	private String time;
	
	
	public ImgMetadata(String metadataLine){
		String tokens[] = metadataLine.split(" ");
		
		//tokens[0] is not used. It is just a sequence number
		this.user = tokens[1];
		this.id = tokens[2];
		this.lat = Double.parseDouble(tokens[3]);
		this.lon = Double.parseDouble(tokens[4]);
		this.numViews = Integer.parseInt(tokens[5]);
		
		String dateInfo[] = tokens[6].split("-");
		
		int year = Integer.parseInt(dateInfo[0]);
		int month = Integer.parseInt(dateInfo[1]);
		int day = Integer.parseInt(dateInfo[2]);
		this.date = new GregorianCalendar(year, month, day);
		this.time = tokens[7];
	}
	
	public String getUser() {return user;}
	public String getId() {return id;}
	public double getLat() {return lat;}
	public double getLon() {return lon;}
	public GregorianCalendar getDate() {return date;}
	public String getTime() {return time;}		
	public int getNumViews(){return numViews;}
}

package backup;
/*
 * https://www.bitstamp.net/api/ticker/
 * {"high": "569.22", "last": "515.01", "timestamp": "1395984968", 
 * 	"bid": "515.01", "vwap": "516.12", "volume": "55785.07253245", 
 * 	"low": "466.10", "ask": "518.00"}
 */
public class Ticker{
	private String high;
	private String low;
	private String last;
	private String timestamp;
	private String bid;
	private String ask;
	private String vwap;
	private String volume;
	public Ticker(){}
	public String getHigh(){
    	return high;
    }
    public String getLow(){
    	return low;
    }
    public String getLast(){
    	return last;
    }
    public String getTimestamp(){
    	return timestamp;
    }
    public String getBid(){
    	return bid;
    }
    public String getAsk(){
    	return ask;
    }
    public String getVwap(){
    	return vwap;
    }
    public String getVolume(){
    	return volume;
    }
}
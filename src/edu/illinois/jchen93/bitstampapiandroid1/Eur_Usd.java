package edu.illinois.jchen93.bitstampapiandroid1;
/*
 * https://www.bitstamp.net/api/eur_usd/
 * {"sell": "1.3711", "buy": "1.3823"}
 */

public class Eur_Usd{
	private String sell;
	private String buy;
	public Eur_Usd(){};
	public Eur_Usd(String sell, String buy){
		this.sell = sell;
		this.buy = buy;
	}
	public String getBuy(){
    	return buy;
    }
    public String getSell(){
    	return sell;
    }
}
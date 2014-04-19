package backup;
/*
 * Transaction class used to bind JSON data from Transaction API
 * https://www.bitstamp.net/api/transactions/
 * example JSON data:
	{"date": "1395551812", "tid": 4151239, "price": "564.23", "amount": "0.05000000"}
*/
public class Transaction {
    private String date;
    private int tid;
    private String price;
    private String amount;
    public Transaction() {
    }
    public Transaction(String date, int tid, String price, String amount) {
      this.date = date;
      this.tid = tid;
      this.price = price;
      this.amount = amount;
    }
    @Override
    public String toString() {
      return String.format("(date=%s, tid=%d, price=%s, amount=%s)", date, tid, price, amount);
    }
    
    public String getDate(){
    	return date;
    }
    public int getTid(){
    	return tid;
    }
    public String getPrice(){
    	return price;
    }
    public String getAmount(){
    	return amount;
    }
}
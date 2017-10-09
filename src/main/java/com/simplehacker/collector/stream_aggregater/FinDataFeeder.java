package com.simplehacker.collector.stream_aggregater;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

public class FinDataFeeder {
	
	private Producer<String, String> producer;
	
	public FinDataFeeder(String brokerList) {
		this.createProducer(brokerList);
	}
	
	public static class DataSource {
		private static String URLBase = "http://download.finance.yahoo.com/d/quotes.csv?e=.csv&";
		private static String stockSymbols = "s=";
		private static String stockFields = "f=";
		private static String URL;
		
		public DataSource(ArrayList<String> symbols, String fields) {
			makeUrl(symbols, fields);
		}
		
		private void makeUrl(ArrayList<String> symbols, String fields) {
			StringBuffer url = new StringBuffer(URLBase);
			stockSymbols += String.join("+", symbols);
			stockFields += fields;
			url.append(stockSymbols).append('&').append(stockFields);
			URL = url.toString();
			
			// test
			System.out.println(URL);
		}
		
		public BufferedReader getData() throws IOException {
			//TODO: connecting and get response part can be in one function with data source
			URL url = new URL(URL);
			
			URLConnection con = url.openConnection();
			con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 4.10; rv:52.0) Gecko/20100101 Firefox/52.0");
			BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
			return br;
		}
	}
	
	public void createProducer(String brokerList) {
		Properties props = new Properties();
		props.put("bootstrap.servers", brokerList);
		props.put("acks", "all");
		props.put("retries", 3);
		props.put("linger.ms", 5);
		
		props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
		props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
		
		producer = new KafkaProducer<>(props);
	}
	
	public void feedsGo(BufferedReader br) throws IOException {
		
		String line;
		ProducerRecord<String, String> record;
		while((line=br.readLine()) != null){
			record = new ProducerRecord<String, String>("quotes", null, line);
		}
		br.close();
	}
	
	public static void main(String args[]) throws ExecutionException, InterruptedException, IOException {
		//String brokerList = args[0];
		//FinDataFeeder fdf = new FinDataFeeder(brokerList);

		ArrayList<String> symbols = new ArrayList<String>(Arrays.asList("DIA","AAPL","AXP","BA","CAT","CSCO","CVX","DD","DIS","GE","GS","HD","IBM","INTC","JNJ"));
		String fields = "lsnpd1oml1vq";
		
		FinDataFeeder.DataSource ds = new FinDataFeeder.DataSource(symbols, fields);
		
		// test:
		BufferedReader br;
		String line;
		for(int i=0;i<10;++i) {
			br = ds.getData();
			while((line=br.readLine()) != null){
				System.out.println(line);
			}
			//Pause for 5 seconds
            Thread.sleep(5000);
		}
		
		
		/* frequency to be 10 first
		for(int i=0;i<10;++i) {
			fdf.feedsGo(ds.getData());
			//Pause for 5 seconds
            Thread.sleep(5000);
		}*/
	}
	
	
	
}

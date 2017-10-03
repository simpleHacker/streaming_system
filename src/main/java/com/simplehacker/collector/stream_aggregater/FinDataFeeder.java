package com.simplehacker.collector.stream_aggregater;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

public class FinDataFeeder {
	private static String URLBase = "http://finance.yahoo.com/d/quotes.csv?";
	private static String stockSymbols = "s=";
	private static String stockFields = "f=";
	private Producer<String, String> producer;
	
	public FinDataFeeder(String brokerList) {
		this.createProducer(brokerList);
	}
	
	public String makeUrl(ArrayList<String> symbols, String fields) {
		StringBuffer url = new StringBuffer(URLBase);
		stockSymbols += String.join("+", symbols);
		stockFields += fields;
		url.append(stockSymbols).append('&').append(stockFields);
		return url.toString();
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
	
	public void feedsGo(ArrayList<String> symbols, String fields) {
		URL url = new URL(makeUrl(symbols, fields));
		
		URLConnection con = url.openConnection();
		con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 4.10; rv:52.0) Gecko/20100101 Firefox/52.0");
		BufferedReader br = new BufferedReader(new InputStreamReader(hc.getInputStream()));
		
		String line;
		ProducerRecord<String, String> record;
		while((line=br.readLine()) != null){
			record = new ProducerRecord<String, String>("quotes", null, line);
		}
		br.close();
	}
	
	public static void main(String args[]) throws ExecutionException, InterruptedException {
		String brokerList = args[0];
		FinDataFeeder fdf = new FinDataFeeder(brokerList);
		
		// for a certain period, keep record fin data from yahoo
	}
	
	
	
}

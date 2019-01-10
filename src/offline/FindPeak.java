package offline;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.Document;

import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.client.MapReduceIterable;
import com.mongodb.client.MongoDatabase;

import db.mongodb.MongoDBUtil;

public class FindPeak {
	private static List<LocalTime> buckets = initBuckets();

	public static void main(String[] args) {
		// Init
		MongoClient mongoClient = new MongoClient();
		MongoDatabase db = mongoClient.getDatabase(MongoDBUtil.DB_NAME);

		// Construct mapper function
		// function() {
		// if (this.url.startswith("/Jupiter")) {
		// emit(this.time.substring(0, 5), 1);
		// }
		// }

		StringBuilder sb = new StringBuilder();
		sb.append("function() {");
		sb.append(" if (this.url.startsWith(\"/Jupiter\")) {");
		sb.append(" emit(this.time.substring(0, 5), 1); }");
		sb.append("}");
		String map = sb.toString();

		// Construct a reducer function
		String reduce = "function(key, values) {return Array.sum(values)} ";

		// MapReduce
		MapReduceIterable<Document> results = db.getCollection("logs").mapReduce(map, reduce);

		// reduce result 10:10 -> 2, 10:11 -> 4
		// 10:10 -> [10:00, 10:15]
		// 10:11 -> [10:00, 10:15]
		// [10:00, 10:15] -> 2 + 4 = 6
		
		// Save total count to each bucket
		Map<String, Double> timeMap = new HashMap<>();
		results.forEach(new Block<Document>() {
			@Override
			public void apply(final Document document) {
				String time = findBucket(document.getString("_id"));
				Double count = document.getDouble("value");
				if (timeMap.containsKey(time)) {
					timeMap.put(time, timeMap.get(time) + count);
				} else {
					timeMap.put(time, count);
				}
			}
		});

		// Need a sorting here
		List<Map.Entry<String, Double>> timeList = new ArrayList<Map.Entry<String, Double>>(timeMap.entrySet());
		Collections.sort(timeList, new Comparator<Map.Entry<String, Double>>() {
			public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2) {
				return Double.compare(o2.getValue(), o1.getValue());
			}
		});

		printList(timeList);
		mongoClient.close();
	}

	private static void printList(List<Map.Entry<String, Double>> timeList) {
		for (Map.Entry<String, Double> entry : timeList) {
			System.out.println("time: " + entry.getKey() + " count: " + entry.getValue());
		}
	}

	private static List<LocalTime> initBuckets() {
		List<LocalTime> buckets = new ArrayList<>();
		LocalTime time = LocalTime.parse("00:00");
		for (int i = 0; i < 96; ++i) {
			buckets.add(time);
			time = time.plusMinutes(15);
		}
		return buckets;
	}

	// Use LocalTime.isAfter/isBefore to compare to objects
	private static String findBucket(String currentTime) {
		LocalTime curr = LocalTime.parse(currentTime);
		int left = 0, right = buckets.size() - 1;
		while (left < right - 1) {
			int mid = (left + right) / 2;
			if (buckets.get(mid).isAfter(curr)) {
				right = mid - 1;
			} else {
				left = mid;
			}
		}
		if (buckets.get(right).isAfter(curr)) {
			return buckets.get(left).toString();
		}
		return buckets.get(right).toString();
	}
}


import java.io.IOException;
import java.util.Iterator;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;

public class ReducerStage2 extends MapReduceBase implements
		Reducer<Text, Text, Text, Text> {

	public void reduce(Text key, Iterator<Text> values,
			OutputCollector<Text, Text> output, Reporter reporter)
			throws IOException {

		StringBuilder linkList = new StringBuilder();

		// Create the adjacency graph for each title and its associated
		// wikilinks
		while (values.hasNext()) {
			linkList.append(values.next().toString() + "\t");
		}
		if (linkList.length() > 0)
			linkList.deleteCharAt(linkList.length() - 1);
		if (linkList.toString().equals("ZERO"))
			output.collect(key, new Text("\t"));
		else
			output.collect(key, new Text(linkList.toString()));
	}
}
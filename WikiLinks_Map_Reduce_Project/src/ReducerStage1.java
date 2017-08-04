

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;

public class ReducerStage1 extends MapReduceBase implements
		Reducer<Text, Text, Text, Text> {

	public void reduce(Text key, Iterator<Text> values,
			OutputCollector<Text, Text> output, Reporter reporter)
			throws IOException {

		Set<String> linkSet = new HashSet<String>();

		while (values.hasNext()) {
			linkSet.add(values.next().toString());
		}
		// Ignore redlinks
		if (linkSet.contains("#")) {
			linkSet.remove("#"); // Remove the unwanted values #
			output.collect(key, new Text(" "));
			Iterator<String> i = linkSet.iterator();
			while (i.hasNext()) {
				String val = i.next();
				if (!val.equals("ZERO") && !val.equals(""+key))
					output.collect(new Text(val), key);
				else
					output.collect(key, new Text(" "));
			}
		}
	}
}
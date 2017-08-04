

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class MapperStage1 extends MapReduceBase implements
		Mapper<LongWritable, Text, Text, Text> {

	// First mapper method to generate key value pairs
	public void map(LongWritable key, Text value,
			OutputCollector<Text, Text> output, Reporter reporter)
			throws IOException {

		try {
			// XML parser
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(new ByteArrayInputStream(("<xmlns>"
					+ value + "</xmlns>").getBytes()));

			// Extract each page from the wikilinks file
			NodeList nList = doc.getElementsByTagName("page");

			for (int temp = 0; temp < nList.getLength(); temp++) {
				Node nNode = nList.item(temp);
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) nNode;

					// Extract title from each page
					String title = eElement.getElementsByTagName("title")
							.item(0).getTextContent().trim()
							.replaceAll(" ", "_");

					// Extract text from each page
					String textString = eElement.getElementsByTagName("text")
							.item(0).getTextContent();
					Pattern pattern = Pattern
							.compile("\\[\\[(.*?)(\\||\\]\\])");
					Matcher m = pattern.matcher(textString);
					Text textTitle = new Text(title);
					Text dummyText = new Text("#");
					output.collect(textTitle, dummyText);
					boolean flag = true;
					// Extract and collect wikilinks from each page
					while (m.find()) {
						flag = false;
						String link = m.group(1).trim().replaceAll(" ", "_");
						Text textLink = new Text(link);
						output.collect(textLink, textTitle);
					}
					if (flag) {
						output.collect(textTitle, new Text("ZERO"));
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
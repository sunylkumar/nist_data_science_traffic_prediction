

import java.io.IOException;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.log4j.PropertyConfigurator;

public class WikiLinks {

	public static void main(String[] args) throws Exception {
		PropertyConfigurator.configure("./src/logj4.properties");
		WikiLinks wikiObj = new WikiLinks();
		String outputGraph = args[1] + "/graph/";
		String tempFiles = args[1] + "/temp/";
		String OutputStage1 = "WikiLinks.stage1.out";
		wikiObj.OutlinkGenrationJob1(args[0], tempFiles + OutputStage1);
		wikiObj.OutlinkGenrationJob2(tempFiles + OutputStage1, outputGraph);

	}

	public void OutlinkGenrationJob1(String input, String output)
			throws IOException {

		JobConf conf = new JobConf(WikiLinks.class);
		conf.set(XmlInputFormat.START_TAG_KEY, "<page>");
		conf.set(XmlInputFormat.END_TAG_KEY, "</page>");
		conf.setJarByClass(WikiLinks.class);
		FileInputFormat.setInputPaths(conf, new Path(input));
		conf.setInputFormat(XmlInputFormat.class);
		conf.setMapperClass(MapperStage1.class);
		FileOutputFormat.setOutputPath(conf, new Path(output));
		conf.setReducerClass(ReducerStage1.class);
		conf.setOutputKeyClass(Text.class);
		conf.setOutputValueClass(Text.class);
		JobClient.runJob(conf);
	}

	public void OutlinkGenrationJob2(String input, String output)
			throws IOException {

		JobConf conf = new JobConf(WikiLinks.class);
		conf.setJarByClass(WikiLinks.class);
		FileInputFormat.setInputPaths(conf, new Path(input));
		conf.setMapperClass(MapperStage2.class);
		FileOutputFormat.setOutputPath(conf, new Path(output));
		conf.setReducerClass(ReducerStage2.class);
		conf.setOutputKeyClass(Text.class);
		conf.setOutputValueClass(Text.class);
		JobClient.runJob(conf);
	}

}
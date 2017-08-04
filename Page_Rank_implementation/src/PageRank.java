

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.NumberFormat;
import java.text.DecimalFormat;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.Partitioner;


public class PageRank {

    public static long count = 0;

    public static void main(String[] args) throws Exception {
        PageRank mainObject = new PageRank();

        String input=args[0];
        String output_path=args[1];

        int noOfIterations = 8;
        String result = output_path+"/" ;
        String tmpLoc = output_path+ "/temp/";

        
        String LinkCounterOuput="num_nodes";

        String Iter1Sorted = "iter1.sorted.out";
        String Iter8Sorted = "iter8.sorted.out";

        String[] iterations = new String[noOfIterations+1];

        for ( int i =0; i <= noOfIterations ; i++){
            iterations[i] = "iter" + Integer.toString(i) +".out";
        }


        //To count the total number of pages
        mainObject.InlinkCountGenerationJob(input, tmpLoc + LinkCounterOuput);

        //Calculating the rank using out-links
        mainObject.RunCalculatorStage1(input, tmpLoc + iterations[0], tmpLoc + LinkCounterOuput);

        //calculate the Rank for #noOfIteration that is 8 times
        
        for ( int i =1; i <= noOfIterations ; i++){
            mainObject.RankCalculatorJob(tmpLoc + iterations[i-1], tmpLoc +iterations[i], tmpLoc + LinkCounterOuput);
        }

        //Sort the pages according to their page rank for iteration 1 and 8 and write to results
        mainObject.SortJob(tmpLoc + iterations[1], tmpLoc + Iter1Sorted, tmpLoc + LinkCounterOuput);
        mainObject.SortJob(tmpLoc + iterations[8], tmpLoc + Iter8Sorted, tmpLoc + LinkCounterOuput);


        //Merge the files of the outputs into one file
        
        mainObject.MergeFiles(tmpLoc + LinkCounterOuput, result+LinkCounterOuput);
        mainObject.MergeFiles(tmpLoc + Iter1Sorted, result+iterations[1]);
        mainObject.MergeFiles(tmpLoc + Iter8Sorted,  result+iterations[8]);

    }


    public void InlinkCountGenerationJob(String input, String output_path) throws IOException{
        JobConf conf = new JobConf(PageRank.class);
        conf.setJarByClass(PageRank.class);

        //Configure the in-link generation mapper
        
        FileInputFormat.setInputPaths(conf, new Path(input));
        conf.setMapperClass(LinkCountMapper.class);
        conf.setMapOutputKeyClass(LongWritable.class);
        conf.setMapOutputValueClass(NullWritable.class);

        //Configure the in-link generation reducer
        FileOutputFormat.setOutputPath(conf, new Path(output_path));
        conf.setReducerClass(LinkCounterReducer.class);

        //Define the output key and value classes
        
        conf.setOutputKeyClass(Text.class);
        conf.setOutputValueClass(NullWritable.class);
        JobClient.runJob(conf);
    }
    

    public void RunCalculatorStage1 (String input, String output, String linkcountfile) throws IOException {
        JobConf conf = new JobConf(PageRank.class);
        conf.setJarByClass(PageRank.class);

        //Configure the in-link generation mapper
        FileInputFormat.setInputPaths(conf, new Path(input));
        conf.setMapperClass(RankCalculateMapperStage1.class);

        //Configure the inl-ink generation reducer
        
        FileOutputFormat.setOutputPath(conf, new Path(output));
        conf.setReducerClass(RankCalculateReducerStage1.class);

        //Define the output key and value classes
        
        conf.setOutputKeyClass(Text.class);
        conf.setOutputValueClass(Text.class);

        Integer count = readCountFromFile(linkcountfile, conf);
        conf.set("count", Integer.toString(count));
        JobClient.runJob(conf);
    }


    public void RankCalculatorJob(String input, String output, String linkcountfile) throws IOException{
        JobConf conf = new JobConf(PageRank.class);
        conf.setJarByClass(PageRank.class);

        //Configure the in-link generation mapper
        FileInputFormat.setInputPaths(conf, new Path(input));
        conf.setMapperClass(RankCalculateMapper.class);

        //Configure the in-link generation reducer
        FileOutputFormat.setOutputPath(conf, new Path(output));
        conf.setReducerClass(RankCalculateReducer.class);

        //Define the output key and value classes
        conf.setOutputKeyClass(Text.class);
        conf.setOutputValueClass(Text.class);

        Integer count = readCountFromFile(linkcountfile, conf);
        conf.set("count", Integer.toString(count));
        JobClient.runJob(conf);
    }

    public void SortJob(String input, String output, String linkcountfile) throws IOException{
        JobConf conf = new JobConf(PageRank.class);
        conf.setJarByClass(PageRank.class);

        //Configure the in-link generation mapper
        FileInputFormat.setInputPaths(conf, new Path(input));
        conf.setMapperClass(SortMapper.class);

        //Configure the in-link generation reducer
        FileOutputFormat.setOutputPath(conf, new Path(output));
        conf.setReducerClass(SortReducer.class);

        //define the output key and value from mapper
        conf.setMapOutputKeyClass(DoubleWritable.class);
        conf.setMapOutputValueClass(Text.class);

        //Define the output key and value classes from reducer
        conf.setOutputKeyClass(Text.class);
        conf.setOutputValueClass(DoubleWritable.class);

        //sort the keys of reducer
        conf.setOutputKeyComparatorClass(KeyComparator.class);
        conf.setOutputValueGroupingComparator(GroupComparator.class);
        conf.setPartitionerClass(FirstPartitioner.class);

        //Get the count value and set the configuration
        Integer count = readCountFromFile(linkcountfile, conf);
        conf.set("count", Integer.toString(count));

        JobClient.runJob(conf);
    }

    public static class FirstPartitioner implements Partitioner<DoubleWritable, Text> {

        @Override
        public void configure(JobConf job) {}

        @Override
        public int getPartition(DoubleWritable key, Text value, int numPartitions) {
            double d = (Double.parseDouble(key.toString()));
            int n =(int) d * 100;
            return (int)(n / numPartitions) ;
        }
    }

    public static class GroupComparator extends WritableComparator {
        protected GroupComparator() {
            super(DoubleWritable.class, true);
        }
        @Override
        public int compare(WritableComparable w1, WritableComparable w2) {
            DoubleWritable ip1 = (DoubleWritable) w1;
            DoubleWritable ip2 = (DoubleWritable) w2;
            return ip1.compareTo(ip2);
        }
    }

    public static class KeyComparator extends WritableComparator {
        protected KeyComparator() {
            super(DoubleWritable.class, true);
        }
        @Override
        public int compare(WritableComparable w1, WritableComparable w2) {
            DoubleWritable d1 = (DoubleWritable) w1;
            DoubleWritable d2 = (DoubleWritable) w2;
            int cmp = d1.compareTo(d2);
            return cmp * -1; //reverse
        }
    }

    private Integer readCountFromFile (String filepath, Configuration conf) throws IOException
    {
        String fileName = filepath + "/part-r-00";
        Integer count = 1;
        BufferedReader br = null;
        FileSystem fs = null;
        Path path ;//= new Path(fileName);
        NumberFormat nf = new DecimalFormat("000");
        Configuration config = new Configuration();

        try {

            path = new Path (fileName + nf.format(0));
            fs = path.getFileSystem(new Configuration());
            String line = "";

            if (!fs.isFile(path)){
                fileName = filepath + "/part-00";
            }

            //This will generate file names -00001 to -00999, I hope this is sufficient
            for (int i=0; i<=999; i++){
                path = new Path (fileName + nf.format(i));
                fs = path.getFileSystem(config);
                if (fs.isFile(path)){
                    br = new BufferedReader(new InputStreamReader(fs.open(path)));
                    line = br.readLine();

                    if (line!= null && !line.isEmpty() && line.length() >= 2)
                        break;
                }
            }

            if (line != null && !line.isEmpty())
            {
                //String[] splits = line.split("=");
            	String splits=line;
                count = Integer.parseInt(splits);
                return count;
            }

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            try {
                br.close();
                fs.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return count;
    }

    private void MergeFiles(String input, String output) throws  IOException {
        String fileName = input + "/part-r-00";
        NumberFormat nf = new DecimalFormat("000");

        Configuration conf = new Configuration();
        FileSystem outFS = null;

        try {

            Path outFile = new Path(output);
            outFS = outFile.getFileSystem(new Configuration());
            if (outFS.exists(outFile)){
                System.out.println(outFile + " already exists");
                System.exit(1);
            }

            FSDataOutputStream out = outFS.create(outFile);

            Path inFile = new Path (fileName + nf.format(0));
            FileSystem inFS = inFile.getFileSystem(new Configuration());

            if (!inFS.exists(inFile)){
                fileName = input + "/part-00";
            }

            //This will generate file names -00001 to -00999, I hope this is sufficient
            for (int i=0; i<=999; i++){

                inFile = new Path (fileName + nf.format(i));
                inFS = inFile.getFileSystem(new Configuration());

                if (inFS.isFile(inFile)){

                    int bytesRead=0;
                    byte[] buffer = new byte[4096];

                    FSDataInputStream in = inFS.open(inFile);

                    while ((bytesRead = in.read(buffer)) > 0) {
                        out.write(buffer, 0, bytesRead);
                    }

                    in.close();
                }else{
                    break;
                }

                inFS.close();
            }

            out.close();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                outFS.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
  }
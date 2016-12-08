package jsonserver.alikeaudience.com;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by AlikeAudience on 23/9/2016.
 */
public final class FileWriteHelper {

    private static final int MAX_COUNT = 100;

    private static volatile FileWriteHelper instance;

    private static final String FILE_A_NAME = "outfilename";
    private static final String FILE_B_NAME = "outfilename2";
    private static final int SAVE_TO_FILE_A = 1;
    private static final int SAVE_TO_FILE_B = 2;

    private static final String LOG_DIRECTORY = "netty_logs";

    private File logDir;

    private static volatile int fileIndicator = 0;

    private FileWriter fw;
    private BufferedWriter bw;
    private PrintWriter out;

//    private StringBuffer stringBuffer;


    private FileWriteHelper() {
        File rootDir = new File("srv");

        checkDirExist(rootDir);

        logDir = new File(rootDir.getAbsolutePath() + File.separator + LOG_DIRECTORY);

        checkDirExist(logDir);

        String fileName = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());

        File secondDir = new File(logDir.getAbsolutePath() + File.separator + fileName.substring(0, 8));

        checkDirExist(secondDir);

        try {
            fw = new FileWriter(secondDir.getAbsolutePath() + File.separator + fileName.substring(8), true);
            bw = new BufferedWriter(fw);
            out = new PrintWriter(bw);
//            System.out.println(fileIndicator);

        } catch (IOException e) {
            e.printStackTrace();
        }

//        stringBuffer = new StringBuffer();



    }

    /**
     * Get the only instance of this class.
     *
     * @return the single instance.
     */
    public static FileWriteHelper getInstance() {
        if (instance == null) {
            synchronized (FileWriteHelper.class) {
                if (instance == null) {
                    instance = new FileWriteHelper();
                }
            }
        }
        return instance;
    }

    public void writeToFile(String data) {
        out.println(data);
        out.flush();
//        try {
//            bw.write(data);
//            bw.newLine();
//            bw.flush();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

//        stringBuffer.append(data);

//        System.out.println("write data");




    }

    public void writeNewLine() {
        try {
            bw.newLine();
            bw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

//        JsonKafkaProducer.getInstance().sendToKafka(stringBuffer.toString());
//        stringBuffer = new StringBuffer();

//        System.out.println("write new line");
    }

    public void closeAll() {

        if(out != null)
            out.close();
        try {
            if(bw != null)
                bw.close();
        } catch (IOException e) {
            //exception handling left as an exercise for the reader
        }
        try {
            if(fw != null)
                fw.close();
        } catch (IOException e) {
            //exception handling left as an exercise for the reader
        }
    }

    public void swap() {
        //no need to swap after the first call of swap that creates the first instance
        if(fileIndicator == 0) {
            fileIndicator = SAVE_TO_FILE_A;
            return;
        }

        if (fileIndicator == SAVE_TO_FILE_A) fileIndicator = SAVE_TO_FILE_B;
        else fileIndicator = SAVE_TO_FILE_A;

        switch (fileIndicator) {
            case SAVE_TO_FILE_A:
                try {
                    fw = new FileWriter(FILE_A_NAME, true);
                    bw = new BufferedWriter(fw);
                    out = new PrintWriter(bw);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case SAVE_TO_FILE_B:
                try {
                    fw = new FileWriter(FILE_B_NAME, true);
                    bw = new BufferedWriter(fw);
                    out = new PrintWriter(bw);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;

        }

//        System.out.println(new Date().toString() + " " + fileIndicator);

    }

    public void writeToNewFile() {
        //no need to write to new file  after the first call of writeToNewFile that creates the first instance
        if(fileIndicator == 0) {
            fileIndicator = SAVE_TO_FILE_A;
            return;
        }


        String fileName = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());

        File secondDir = new File(logDir.getAbsolutePath() + File.separator + fileName.substring(0, 8));

        checkDirExist(secondDir);

        try {
            fw = new FileWriter(secondDir.getAbsolutePath() + File.separator + fileName.substring(8), true);
            bw = new BufferedWriter(fw);
            out = new PrintWriter(bw);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void checkDirExist(File theDir) {
        // if the directory does not exist, create it
        if (!theDir.exists()) {
            System.out.println("creating directory: " + theDir.getName() );
            boolean result = false;

            try{
                theDir.mkdir();
                result = true;
            }
            catch(SecurityException se){
                //handle it
            }
            if(result) {
                System.out.println("DIR created");
            }
        }
    }


}

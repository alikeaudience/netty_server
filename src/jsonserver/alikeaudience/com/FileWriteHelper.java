package jsonserver.alikeaudience.com;

import io.netty.util.CharsetUtil;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
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

    private static volatile int fileIndicator = 0;

    private FileWriter fw;
    private BufferedWriter bw;
    private PrintWriter out;

    private int count;



    private FileWriteHelper() {

        try {
            fw = new FileWriter(FILE_A_NAME, true);
            bw = new BufferedWriter(fw);
            out = new PrintWriter(bw);
//            System.out.println(fileIndicator);

        } catch (IOException e) {
            e.printStackTrace();
        }



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


}

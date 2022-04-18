package ja;

import jdk.jfr.EventType;
import jdk.jfr.consumer.*;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {

    static final String CLASS_METHOD_SPLITER="#";

    public static void main(String[] args) throws Exception {
        Path path = Paths.get(args[0]);
        String classFilter = "";
        String methodFilter="";

        if (args.length > 1) {
            String[] ss = args[1].split(CLASS_METHOD_SPLITER);
            classFilter= ss[0];
            methodFilter= ss[1];
            System.out.println("class filter: " + classFilter);
            System.out.println("method filter: "+ methodFilter);
            if (ss.length>2) {
                System.out.println("Error: split filter error "+args[1]);
                System.exit(0);
            }
            if (classFilter.isEmpty()) {
                System.out.println("Error: class filter empty");
            }

        } else {
            System.out.println("Info: no filter");
            System.exit(0);
        }


        int totalCount = 0;
        int count = 0;

        RecordingFile rf = new RecordingFile(path);
        while (rf.hasMoreEvents()) {

            RecordedEvent event = rf.readEvent();
            EventType et = event.getEventType();
            if (et.getName().equals("jdk.ExecutionSample")) {
                totalCount++;
                // System.out.println(event.getStartTime());
                // System.out.println(event.getEndTime());
                RecordedStackTrace st = event.getStackTrace();
                //System.out.println("is trucated: "+st.isTruncated());
                for (RecordedFrame frame : st.getFrames()) {
                    RecordedMethod method = frame.getMethod();
                    RecordedClass claz = method.getType();
                    //if (claz.getName().contains(classFilter)) {
                        if (!methodFilter.isEmpty() && method.getName().contains(methodFilter)) {
                            count++;
                        }/*else {
                            count++;
                        }*/
                        //System.out.println("frame: " + frame);
                        // System.out.print("method: "+method.getName());
                        // System.out.println("line number: "+frame.getLineNumber());
                    //}
                }
            }
        }
        rf.close();
        System.out.println("count " + count);
        System.out.println("total " + totalCount);
        float p = (float) count / (float) totalCount * 100;
        System.out.println("percent: " + p);
    }
}


package ja;

import jdk.jfr.EventType;
import jdk.jfr.consumer.*;
import picocli.CommandLine;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "find", description = "find calling stacktrace by 'className#method")
public class Find implements Callable {

    private Queue<StackNode> stack = new LinkedList<>();
    private Map<StackNode, Integer> cpus = new HashMap<>();

    private int counts;

    @CommandLine.Parameters(paramLabel = "jfr file", description = "captured jfr file to analyze")
    private String jfrFileName;

    @CommandLine.Option(names = {"-c", "--class"}, required = true, description = "class filter")
    private String classFilter;

    @CommandLine.Option(names = {"-m", "--method"}, required = true, description = "method filter")
    private String methodFilter;

    @Override
    public Object call() throws Exception {
        System.out.println("class filter: " + classFilter);
        if (methodFilter != null) {
            System.out.println("method filter: " + methodFilter);
        }

        Path path = Paths.get(jfrFileName);
        RecordingFile rf = new RecordingFile(path);

        while (rf.hasMoreEvents()) {
            boolean found = false;
            RecordedEvent event = rf.readEvent();
            EventType et = event.getEventType();
            if (et.getName().equals("jdk.ExecutionSample")) {
                RecordedStackTrace st = event.getStackTrace();

                for (RecordedFrame frame : st.getFrames()) {
                    RecordedMethod method = frame.getMethod();
                    RecordedClass claz = method.getType();

                    if (found) {
                        // up the stack
                        inputStack(claz.getName(), method.getName(), frame.getLineNumber());
                    } else {
                        if (claz.getName().contains(classFilter) && method.getName().contains(methodFilter)) {
                                found = true;
                                // no line, maybe in different line of the method
                                inputStack(claz.getName(), method.getName(), 0);
                        }
                    }
                }

                if (found) {
                    break;
                }
            }
        }
        rf.close();

        calculateCpu();

        printStack();

        System.out.println(counts);
        return null;
    }


    private void inputStack(String claz, String method, int lineNumber) {
        StackNode node = new StackNode(claz, method, lineNumber);
        stack.add(node);
        cpus.put(node, 0);
    }


    private void printStack() {
        StackNode node = stack.poll();
        while (node != null) {
            int cpu = (int) ((float) cpus.get(node) / (float) counts * 100);
            System.out.print("cpu: " + cpu + " ");
            //System.out.print("cpu: " + cpus.get(node) + " ");
            System.out.println(node);
            node = stack.poll();
        }
    }

    private void calculateCpu() throws Exception {
        Path path = Paths.get(jfrFileName);
        RecordingFile rf = new RecordingFile(path);

        //int cc= 0;

        while (rf.hasMoreEvents()) {
            RecordedEvent event = rf.readEvent();
            EventType et = event.getEventType();
            if (et.getName().equals("jdk.ExecutionSample")) {
                RecordedStackTrace st = event.getStackTrace();
                for (RecordedFrame frame : st.getFrames()) {
                    RecordedMethod method = frame.getMethod();
                    RecordedClass claz = method.getType();

                    StackNode n = null;
                    if (claz.getName().contains(classFilter) && method.getName().contains(methodFilter) ) {
                        //System.out.println(claz.getName()+method.getName()+frame.getLineNumber());
                        n= new StackNode(claz.getName(), method.getName(), 0);
                        //cc++;
                    }else {
                        n= new StackNode(claz.getName(), method.getName(), frame.getLineNumber());
                    }

                    Integer c = cpus.get(n);
                    if (c != null) {
                        c++;
                        cpus.put(n, c);
                    }
                }
                counts++;
            }
        }

        //System.out.println("cc: "+cc);

        rf.close();
    }

    public static void main(String... args) {
        int exitCode = new CommandLine(new Find()).execute(args);
        System.exit(exitCode);
    }
}

class StackNode {
    public String claz;
    public String method;
    public int lineNumber;

    public StackNode(String claz, String method, int lineNumber) {
        this.claz = claz;
        this.method = method;
        this.lineNumber = lineNumber;
    }

    public String toString() {
        return "class: " + claz + " method: " + method + " , line number: " + lineNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StackNode stackNode = (StackNode) o;
        return lineNumber == stackNode.lineNumber && Objects.equals(claz, stackNode.claz) && Objects.equals(method, stackNode.method);
    }

    @Override
    public int hashCode() {
        return Objects.hash(claz, method, lineNumber);
    }
}

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

    private Queue<StackNode> upStack = new LinkedList<>();
    private Map<StackNode, Integer> cpus = new HashMap<>();

    private Queue<StackNode> downStack = new LinkedList<>();

    private int counts;

    @CommandLine.Parameters(paramLabel = "jfr file", description = "captured jfr file to analyze")
    private String jfrFileName;

    @CommandLine.Option(names = {"-d", "--downStack"}, description = "analyze down the stack")
    private boolean isDownStack = false;

    @CommandLine.Option(names = {"-c", "--class"}, required = true, description = "class filter")
    private String classFilter;

    @CommandLine.Option(names = {"-m", "--method"}, required = true, description = "method filter")
    private String methodFilter;

    @CommandLine.Option(names = {"-l", "--lineNumber"}, description = "line number")
    private int lineNumberFilter;

    @CommandLine.Option(names={"-mp", "--methodPrecise"}, description = "method match precisely")
    private boolean isMethodPrecise;

    @Override
    public Object call() throws Exception {
        System.out.println("class filter: " + classFilter);
        if (methodFilter != null) {
            System.out.println("method filter: " + methodFilter);
        }
        System.out.println("line number filter: "+lineNumberFilter);

        if (isDownStack) {
            goDownStack();
            printStack(downStack);
        } else {
            goUpStack();
            printStack(upStack);
        }

        System.out.println("total sample: " + counts);
        return null;
    }

    private void goUpStack() throws Exception {
        System.out.println("up stack");

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
                    String className = claz.getName();
                    String methodName = method.getName();

                    // up stack ignore linenumber filter?

                    if (found) {
                        // up the stack
                        insertUpStack(className, methodName, frame.getLineNumber());

                    } else {
                        if (className.contains(classFilter) && methodName.contains(methodFilter)) {
                            found = true;
                            insertUpStack(claz.getName(), method.getName(), 0);
                        }
                    }

                }

                if (found) {
                    break; //up stack, no need going on
                }

            } // in execution sample
        }// while all events
        rf.close();

        calculateCpu();
    }

    private void goDownStack() throws Exception {
        System.out.println("down stack");

        Path path = Paths.get(jfrFileName);
        RecordingFile rf = new RecordingFile(path);

        // find root
        while (rf.hasMoreEvents()) {
            boolean found = false;
            RecordedEvent event = rf.readEvent();
            EventType et = event.getEventType();
            if (et.getName().equals("jdk.ExecutionSample")) {
                RecordedStackTrace st = event.getStackTrace();

                RecordedFrame[] frames = st.getFrames().toArray(new RecordedFrame[0]);
                int i = 0;
                while (i < frames.length) {
                    RecordedMethod method = frames[i].getMethod();
                    RecordedClass claz = method.getType();
                    String className = claz.getName();
                    String methodName = method.getName();

                    int lineNumber= 0;
                    if (lineNumberFilter!=0) {
                        lineNumber = frames[i].getLineNumber();
                    }

                    if (className.contains(classFilter) && lineNumber==lineNumberFilter) {
                        if (isMethodPrecise) {
                            if (methodName.equals(methodFilter)) found=true;
                        }else {
                             if ( methodName.contains(methodFilter)) found=true;
                        }

                        if (found) {
                            if (downStack.size() == 0) {  // newly found
                                //System.out.println("insert root");
                                insertDownStack(className, methodName, lineNumber);
                            }
                            break;  // no need up
                        }
                    }
                    i++;
                } // while frame

                if (found && (i != 0)) {
                    RecordedMethod method = frames[i - 1].getMethod();  // only 1 level
                    RecordedClass claz = method.getType();
                    String className = claz.getName();
                    String methodName = method.getName();
                    StackNode n= new StackNode(className, methodName, frames[i - 1].getLineNumber());
                    if (!downStack.contains(n)) {
                        //System.out.println(className+methodName);
                        insertDownStack(claz.getName(), method.getName(), frames[i - 1].getLineNumber());
                    }
                }

            } // in execution sample

        }// while all events
        rf.close();


        calculateCpu();
    }


    private void insertUpStack(String claz, String method, int lineNumber) {
        StackNode node = new StackNode(claz, method, lineNumber);
        upStack.add(node);
        cpus.put(node, 0);
    }

    private void insertDownStack(String claz, String method, int lineNumber) {
        StackNode node = new StackNode(claz, method, lineNumber);
        downStack.add(node);
        cpus.put(node, 0);
    }


    private void printStack(Queue<StackNode> stack) {
        StackNode node = stack.poll();
        while (node != null) {
            float cpu =  ((float) cpus.get(node) / (float) counts)*100;
            String s= String.format("%5.2f", cpu);
            System.out.print("cpu: " + s + "%,  ");
            System.out.print("sample " + cpus.get(node) + " ");
            System.out.println(node);
            node = stack.poll();
        }
    }

    private void calculateCpu() throws Exception {
        Path path = Paths.get(jfrFileName);
        RecordingFile rf = new RecordingFile(path);

        while (rf.hasMoreEvents()) {
            RecordedEvent event = rf.readEvent();
            EventType et = event.getEventType();
            if (et.getName().equals("jdk.ExecutionSample")) {
                RecordedStackTrace st = event.getStackTrace();
                for (RecordedFrame frame : st.getFrames()) {
                    RecordedMethod method = frame.getMethod();
                    RecordedClass claz = method.getType();

                    int lineNumber = frame.getLineNumber();

                    StackNode  n = new StackNode(claz.getName(), method.getName(), lineNumber);
                    StackNode  n1 = new StackNode(claz.getName(), method.getName(), 0);

                    Integer c = cpus.get(n);
                    if (c != null) {
                        c++;
                        cpus.put(n, c);
                    }

                    c = cpus.get(n1);
                    if (c != null) {
                        c++;
                        cpus.put(n1, c);
                    }
                }
                counts++;
            }
        }

        rf.close();
    }

    public static void main(String... args) {
        int exitCode = new CommandLine(new Find()).execute(args);
        System.exit(exitCode);
    }
}

class DownStackNode {
    StackNode node;
    List<DownStackNode> children;

    public DownStackNode(StackNode node) {
        this.node = node;
        this.children = new LinkedList<>();
    }

    public DownStackNode findDownStackNode(StackNode node) {
        if (this.node == node) {
            return this;
        } else {
            for (DownStackNode n : children) {
                DownStackNode nn = n.findDownStackNode(node);
                if (nn != null) {
                    return nn;
                }
            }
        }
        return null;
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

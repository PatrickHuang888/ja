

## command line
```console
java -cp "/home/hxm/.m2/repository/info/picocli/picocli/4.6.3/picocli-4.6.3.jar:target/ja-1.0-SNAPSHOT.jar" ja.Find -c=TestServlet -m=doPost /opt/Work/form_loongson_0402_01.jfr

```

## sample output
```console
class filter: TestServlet
method filter: doPost
cpu: 54 class: com.test.servlet.TestServlet method: doPost , line number: 0
cpu: 54 class: javax.servlet.http.HttpServlet method: service , line number: 665
cpu: 54 class: javax.servlet.http.HttpServlet method: service , line number: 750
cpu: 54 class: org.apache.catalina.core.StandardWrapper method: service , line number: 1579
cpu: 60 class: org.apache.catalina.core.StandardWrapperValve method: invoke , line number: 217
cpu: 61 class: org.apache.catalina.core.StandardContextValve method: invoke , line number: 119
cpu: 61 class: org.apache.catalina.core.StandardPipeline method: doInvoke , line number: 611
cpu: 66 class: org.apache.catalina.core.StandardPipeline method: invoke , line number: 550
cpu: 66 class: com.sun.enterprise.web.WebPipeline method: invoke , line number: 75
cpu: 69 class: org.apache.catalina.core.StandardHostValve method: invoke , line number: 114
cpu: 70 class: org.apache.catalina.connector.CoyoteAdapter method: doService , line number: 337
cpu: 81 class: org.apache.catalina.connector.CoyoteAdapter method: service , line number: 202
cpu: 81 class: com.apusic.enterprise.v10.services.impl.ContainerMapper$HttpHandlerCallable method: call , line number: 439
cpu: 81 class: com.apusic.enterprise.v10.services.impl.ContainerMapper method: service , line number: 144
cpu: 82 class: org.glassfish.grizzly.http.server.HttpHandler method: runService , line number: 195
cpu: 82 class: org.glassfish.grizzly.http.server.HttpHandler method: doHandle , line number: 162
cpu: 82 class: org.glassfish.grizzly.http.server.HttpServerFilter method: handleRead , line number: 218
cpu: 91 class: org.glassfish.grizzly.filterchain.ExecutorResolver$9 method: execute , line number: 95
cpu: 106 class: org.glassfish.grizzly.filterchain.DefaultFilterChain method: executeFilter , line number: 261
cpu: 106 class: org.glassfish.grizzly.filterchain.DefaultFilterChain method: executeChainPart , line number: 178
cpu: 107 class: org.glassfish.grizzly.filterchain.DefaultFilterChain method: execute , line number: 110
cpu: 107 class: org.glassfish.grizzly.filterchain.DefaultFilterChain method: process , line number: 89
cpu: 107 class: org.glassfish.grizzly.ProcessorExecutor method: execute , line number: 53
cpu: 94 class: org.glassfish.grizzly.nio.transport.TCPNIOTransport method: fireIOEvent , line number: 549
cpu: 95 class: org.glassfish.grizzly.strategies.AbstractIOStrategy method: fireIOEvent , line number: 89
cpu: 95 class: org.glassfish.grizzly.strategies.WorkerThreadIOStrategy method: run0 , line number: 94
cpu: 95 class: org.glassfish.grizzly.strategies.WorkerThreadIOStrategy method: access$100 , line number: 33
cpu: 95 class: org.glassfish.grizzly.strategies.WorkerThreadIOStrategy$WorkerThreadRunnable method: run , line number: 114
cpu: 98 class: org.glassfish.grizzly.threadpool.AbstractThreadPool$Worker method: doWork , line number: 569
cpu: 99 class: org.glassfish.grizzly.threadpool.AbstractThreadPool$Worker method: run , line number: 549
cpu: 99 class: java.lang.Thread method: run , line number: 750

```
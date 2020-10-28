学习笔记

一、设置GC为串行GC，观察结果

执行命令：
java -XX:+UseSerialGC -Xms512m -Xmx512m -Xloggc:gc.demo.log -XX:+PrintGCDetails -XX:+PrintGCDateStamps GCLogAnalysis

观察结果一共进行了9次Minor GC，7次Full GC
本初取一次Minor GC分析
2020-10-28T19:05:31.938-0800: 1.148: [GC (Allocation Failure) 2020-10-28T19:05:31.938-0800: 1.148: [DefNew: 139776K->17471K(157248K), 0.0294415 secs] 139776K->46524K(506816K), 0.0295208 secs] [Times: user=0.03 sys=0.01, real=0.03 secs]

[GC (Allocation Failure)：表示为Minor GC触发的原因。本次GC事件, 是由于年轻代中没有空间来存放新的数据结构引起的。
DefNew – 垃圾收集器，用于回收年轻代 单线程, 算法标记-复制(mark-copy),
[DefNew: 139776K->17471K(157248K), 0.0294415 secs]:回收年轻代从139M回收到17M，用时29ms
139776K->46524K(506816K)：整个堆从139M回收至46M，17M和46M相差29M，说明有部分对象被回收至老年代

取一次full GC观察
2020-10-28T19:05:32.337-0800: 1.547: [GC (Allocation Failure) 2020-10-28T19:05:32.337-0800: 1.547: [DefNew: 156985K->156985K(157248K), 0.0000225 secs]2020-10-28T19:05:32.337-0800: 1.547: [Tenured: 304542K->273980K(349568K), 0.0582599 secs] 461527K->273980K(506816K), [Metaspace: 2689K->2689K(1056768K)], 0.0583694 secs] [Times: user=0.06 sys=0.00, real=0.06 secs]

[DefNew: 156985K->156985K(157248K), 0.0000225 secs]：此处先进行了一次Minor GC，年轻代没有任何对象被回收，耗时0.02ms

[Tenured: 304542K->273980K(349568K), 0.0582599 secs] 461527K->273980K(506816K):Tenured用于清理老年代空间的垃圾收集器，采用算法标记-清除-整理(mark-sweep-compact)。
老年代从304M降至273M
[Metaspace: 2689K->2689K(1056768K)], 0.0583694 secs]：Metaspace 空间没有回收任何垃圾
[Times: user=0.06 sys=0.00, real=0.06 secs] ：GC事件的持续时间, 通过三个部分来衡量:
                                              user – 在此次垃圾回收过程中, 所有 GC线程所消耗的CPU时间之和。
                                              sys – GC过程中中操作系统调用和系统等待事件所消耗的时间。
                                              real – 应用程序暂停的时间。因为串行垃圾收集器(Serial Garbage Collector)只使用单线程, 因此 real time 等于 user 和 system 时间的总和。
                                              
二、设置GC为并行GC
执行命令：java -XX:+UseParallelGC -Xms512m -Xmx512m -Xloggc:gc.demo.log -XX:+PrintGCDetails -XX:+PrintGCDateStamps GCLogAnalysis
取一次YoungGC 
2020-10-28T22:22:03.679-0800: 1.599: [GC (Allocation Failure) [PSYoungGen: 131185K->21498K(153088K)] 131185K->42805K(502784K), 0.0200174 secs] [Times: user=0.03 sys=0.10, real=0.02 secs]
Allocation Failure – 触发垃圾收集的原因。本次GC事件, 是由于年轻代中没有适当的空间存放新的数据结构引起的。
PSYoungGen – 垃圾收集器的名称。这个名字表示的是在年轻代中使用的: 并行的 标记-复制(mark-copy), 全线暂停(STW) 垃圾收集器。
取一次FullGC 
2020-10-28T22:22:04.032-0800: 1.951: [Full GC (Ergonomics) [PSYoungGen: 17495K->0K(116736K)] [ParOldGen: 321709K->241370K(349696K)] 339205K->241370K(466432K), [Metaspace: 2689K->2689K(1056768K)], 0.0344248 secs] [Times: user=0.20 sys=0.01, real=0.03 secs]
Full GC – 用来表示此次是 Full GC 的标志。Full GC表明本次清理的是年轻代和老年代。
Ergonomics – 触发垃圾收集的原因。Ergonomics 表示JVM内部环境认为此时可以进行一次垃圾收集。

三、设置为CMS GC
执行命令：java -XX:+UseConcMarkSweepGC -Xms512m -Xmx512m -Xloggc:gc.demo.log -XX:+PrintGCDetails -XX:+PrintGCDateStamps GCLogAnalysis
CMS的设计目标是避免在老年代垃圾收集时出现长时间的卡顿。主要通过两种手段来达成此目标。
第一, 不对老年代进行整理, 而是使用空闲列表(free-lists)来管理内存空间的回收。
第二, 在 mark-and-sweep (标记-清除) 阶段的大部分工作和应用线程一起并发执行。

取一次Minor GC
2020-10-28T23:07:52.906-0800: 1.278: [GC (Allocation Failure) 2020-10-28T23:07:52.906-0800: 1.278: [ParNew: 139776K->17471K(157248K), 0.0203139 secs] 139776K->43776K(506816K), 0.0204659 secs] [Times: user=0.04 sys=0.10, real=0.02 secs]
Allocation Failure – 触发垃圾收集的原因。本次GC事件, 是由于年轻代中没有适当的空间存放新的数据结构引起的。
ParNew – 垃圾收集器的名称。这个名字表示的是在年轻代中使用的: 并行的 标记-复制(mark-copy), 全线暂停(STW)垃圾收集器, 专门设计了用来配合老年代使用的 Concurrent Mark & Sweep 垃圾收集器。
0.0204659 secs - 垃圾收集器在标记和复制年轻代存活对象时所消耗的时间。包括和ConcurrentMarkSweep收集器的通信开销, 提升存活时间达标的对象到老年代,以及垃圾收集后期的一些最终清理。

取一次full GC
2020-10-28T23:07:53.140-0800: 1.512: [GC (CMS Initial Mark) [1 CMS-initial-mark: 204858K(349568K)] 222477K(506816K), 0.0002340 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
2020-10-28T23:07:53.141-0800: 1.513: [CMS-concurrent-mark-start]
2020-10-28T23:07:53.143-0800: 1.515: [CMS-concurrent-mark: 0.002/0.002 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
2020-10-28T23:07:53.143-0800: 1.515: [CMS-concurrent-preclean-start]
2020-10-28T23:07:53.143-0800: 1.515: [CMS-concurrent-preclean: 0.001/0.001 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
2020-10-28T23:07:53.143-0800: 1.515: [CMS-concurrent-abortable-preclean-start]
2020-10-28T23:07:53.161-0800: 1.533: [GC (Allocation Failure) 2020-10-28T23:07:53.161-0800: 1.533: [ParNew: 156756K->17465K(157248K), 0.0328046 secs] 361614K->270413K(506816K), 0.0328997 secs] [Times: user=0.24 sys=0.02, real=0.03 secs]
2020-10-28T23:07:53.216-0800: 1.588: [GC (Allocation Failure) 2020-10-28T23:07:53.216-0800: 1.588: [ParNew: 157241K->17471K(157248K), 0.0302165 secs] 410189K->315675K(506816K), 0.0303155 secs] [Times: user=0.22 sys=0.02, real=0.03 secs]
2020-10-28T23:07:53.269-0800: 1.640: [GC (Allocation Failure) 2020-10-28T23:07:53.269-0800: 1.640: [ParNew: 157247K->17471K(157248K), 0.0329572 secs] 455451K->364311K(506816K), 0.0330567 secs] [Times: user=0.24 sys=0.02, real=0.04 secs]
2020-10-28T23:07:53.302-0800: 1.674: [CMS-concurrent-abortable-preclean: 0.003/0.158 secs] [Times: user=0.76 sys=0.06, real=0.16 secs]
2020-10-28T23:07:53.302-0800: 1.674: [GC (CMS Final Remark) [YG occupancy: 20502 K (157248 K)]2020-10-28T23:07:53.302-0800: 1.674: [Rescan (parallel) , 0.0002698 secs]2020-10-28T23:07:53.302-0800: 1.674: [weak refs processing, 0.0000250 secs]2020-10-28T23:07:53.302-0800: 1.674: [class unloading, 0.0002433 secs]2020-10-28T23:07:53.302-0800: 1.674: [scrub symbol table, 0.0003695 secs]2020-10-28T23:07:53.303-0800: 1.675: [scrub string table, 0.0001298 secs][1 CMS-remark: 346840K(349568K)] 367343K(506816K), 0.0011429 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
2020-10-28T23:07:53.303-0800: 1.675: [CMS-concurrent-sweep-start]
2020-10-28T23:07:53.304-0800: 1.676: [CMS-concurrent-sweep: 0.001/0.001 secs] [Times: user=0.01 sys=0.00, real=0.00 secs]
2020-10-28T23:07:53.304-0800: 1.676: [CMS-concurrent-reset-start]
2020-10-28T23:07:53.305-0800: 1.676: [CMS-concurrent-reset: 0.001/0.001 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]

从上面的数据可以看出, 进行老年代的并发回收时, 可能会伴随着多次年轻代的小型GC. 在这种情况下, 大型GC的日志中就会掺杂着多次小型GC事件
阶段 1: Initial Mark(初始标记). 这是第一次STW事件。 此阶段的目标是标记老年代中所有存活的对象, 包括 GC ROOR 的直接引用, 以及由年轻代中存活对象所引用的对象。 后者也非常重要, 因为老年代是独立进行回收的。
阶段 2: Concurrent Mark(并发标记). 在此阶段, 垃圾收集器遍历老年代, 标记所有的存活对象, 从前一阶段 “Initial Mark” 找到的 root 根开始算起。 顾名思义, “并发标记”阶段, 就是与应用程序同时运行,不用暂停的阶段。 请注意, 并非所有老年代中存活的对象都在此阶段被标记, 因为在标记过程中对象的引用关系还在发生变化。
阶段 3: Concurrent Preclean(并发预清理). 此阶段同样是与应用线程并行执行的, 不需要停止应用线程。 因为前一阶段是与程序并发进行的,可能有一些引用已经改变。如果在并发标记过程中发生了引用关系变化,JVM会(通过“Card”)将发生了改变的区域标记为“脏”区(这就是所谓的卡片标记,Card Marking)。
在预清理阶段,这些脏对象会被统计出来,从他们可达的对象也被标记下来。此阶段完成后, 用以标记的 card 也就被清空了。
阶段 4: Concurrent Abortable Preclean(并发可取消的预清理). 此阶段也不停止应用线程. 本阶段尝试在 STW 的 Final Remark 之前尽可能地多做一些工作。本阶段的具体时间取决于多种因素, 因为它循环做同样的事情,直到满足某个退出条件( 如迭代次数, 有用工作量, 消耗的系统时间,等等)。
阶段 5: Final Remark(最终标记). 这是此次GC事件中第二次(也是最后一次)STW阶段。本阶段的目标是完成老年代中所有存活对象的标记. 因为之前的 preclean 阶段是并发的, 有可能无法跟上应用程序的变化速度。所以需要 STW暂停来处理复杂情况。
阶段 6: Concurrent Sweep(并发清除). 此阶段与应用程序并发执行,不需要STW停顿。目的是删除未使用的对象,并收回他们占用的空间。
阶段 7: Concurrent Reset(并发重置). 此阶段与应用程序并发执行,重置CMS算法相关的内部数据, 为下一次GC循环做准备。

三、设置为G1
执行命令：java -XX:+UseG1GC -Xms512m -Xmx512m -Xloggc:gc.demo.log -XX:+PrintGCDetails -XX:+PrintGCDateStamps GCLogAnalysis
0.125: [GC pause (G1 Evacuation Pause) (young), 0.0064386 secs]– G1转移暂停,只清理年轻代空间。暂停在JVM启动之后 134 ms 开始, 持续的系统时间为 0.0064386秒 。
[Parallel Time: 5.9 ms, GC Workers: 8] – 表明后面的活动由8个 Worker 线程并行执行, 消耗时间为5.9毫秒(real time)。
[GC Worker Start (ms): Min: 125.5, Avg: 125.6, Max: 125.8, Diff: 0.2] -  GC的worker线程开始启动时,相对于 pause 开始的时间戳。如果 Min 和 Max 差别很大,则表明本机其他进程所使用的线程数量过多, 挤占了GC的CPU时间。
[Ext Root Scanning (ms): Min: 0.2, Avg: 0.4, Max: 1.0, Diff: 0.7, Sum: 3.2]-  用了多长时间来扫描堆外(non-heap)的root, 如 classloaders, JNI引用, JVM的系统root等。后面显示了运行时间, “Sum” 指的是CPU时间。
[Code Root Scanning (ms) – 用了多长时间来扫描实际代码中的 root: 例如局部变量等等(local vars)。
[Object Copy (ms) – 用了多长时间来拷贝收集区内的存活对象。
[Termination (ms) – GC的worker线程用了多长时间来确保自身可以安全地停止, 这段时间什么也不用做, stop 之后该线程就终止运行了。
[Termination Attempts – GC的worker 线程尝试多少次 try 和 teminate。如果worker发现还有一些任务没处理完,则这一次尝试就是失败的, 暂时还不能终止。
[GC Worker Other (ms) – 一些琐碎的小活动,在GC日志中不值得单独列出来。
GC Worker Total (ms) – GC的worker 线程的工作时间总计。
[GC Worker End (ms) – GC的worker 线程完成作业的时间戳。通常来说这部分数字应该大致相等, 否则就说明有太多的线程被挂起, 很可能是因为坏邻居效应(noisy neighbor) 所导致的。



第一次压测：
使用wrk，启动命令：java  -jar -Xms512m -Xmx512m gateway-server-0.0.1-SNAPSHOT.jar
压测命令：wrk -t 16 -c 200 -d60s http://localhost:8088/api/hello 
压测结果：
Running 1m test @ http://localhost:8088/api/hello
  16 threads and 200 connections（共8个测试线程，200个连接）
  Thread Stats   Avg      Stdev     Max   +/- Stdev
                （平均值） （标准差）（最大值）（正负一个标准差所占比例）
    Latency     28.86ms   73.42ms   1.15s    91.03%
    （延迟）
    Req/Sec     1.88k   738.33     7.51k    73.42%
  （处理中的请求数）
  1694648 requests in 1.00m, 202.32MB read（60秒内共处理完成了1694648个请求，读取了202.32MB数据）
  Socket errors: connect 0, read 110, write 0, timeout 0
Requests/sec:  28203.43 （平均每秒处理完成28203.43个请求）
Transfer/sec:      3.37MB （平均每秒读取数据3.37MB MB）

第二次压测：
使用wrk，启动命令：java  -jar -Xms1g -Xmx1g gateway-server-0.0.1-SNAPSHOT.jar

$wrk -t 16 -c 200 -d60s  --latency http://localhost:8088/api/hello
Running 1m test @ http://localhost:8088/api/hello
  16 threads and 200 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency    36.95ms   90.99ms   1.19s    90.82%
    Req/Sec     1.53k   783.98     4.37k    55.77%
  Latency Distribution
     50%    5.45ms
     75%   13.28ms
     90%  116.05ms
     99%  456.09ms
  1381583 requests in 1.00m, 164.95MB read
  Socket errors: connect 0, read 115, write 0, timeout 0
Requests/sec:  22988.57
Transfer/sec:      2.74MB

分析同上，增大了内存，QPS反而边小了
                                    
                                              

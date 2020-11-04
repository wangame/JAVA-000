学习笔记
第三周主要学习内容：
周四学习了netty原理与实践：

学习了五种nio模型：
1、阻塞式I/O：blocking IO
2、非阻塞式I/O： nonblocking IO
3、I/O复用（select，poll，epoll...）：IO multiplexing
4、信号驱动式I/O（SIGIO）：signal driven IO
5、异步I/O（POSIX的aio_系列函数）：asynchronous IO

Channel:
通道(Channel)可以理解为数据传输的管道。通道与流不同的是，流只是在一个方向上移动(一个流必须是inputStream或者outputStream的子类)，而通道可以用于读、写或者同时用于读写。

netty实现reactor线程模型:
单线程：
 EventLoopGroup bossGroup = new NioEventLoopGroup();
多线程：
 EventLoopGroup bossGroup = new NioEventLoopGroup(n);
混合型reactor线程模型：
 EventLoopGroup bossGroup = new NioEventLoopGroup(1);
EventLoopGroup workerGroup = new NioEventLoopGroup(16);

netty处理流程可简记为：bech
b:Bootstrap创建服务绑定端口
e:Eventloop绑定处理请求线程池
c:Channel
h:处理任务handle

周六学习了多线程基础知识：
线程的状态：RUNNABLE，RUNNING，BLOCKED，WAITING，TIMED_WAITING，TERMINATED
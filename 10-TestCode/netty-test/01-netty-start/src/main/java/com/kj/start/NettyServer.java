package com.kj.start;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * 1. `Netty` 服务器在 `6668` 端⼝监听，客户端能发送消息给服务器"hello,服务器~"
 * 2. 服务器可以回复消息给客户端"hello"
 * 3. ⽬的：对 `Netty` 线程模型有⼀个初步认识，便于理解 `Netty` 模型理论
 */
public class NettyServer {
    public static void main(String[] args) {
        /*
        * 创建 BossGroup和WorkerGroup
        * 说明：
        * 1、创建两个线程组 bossGroup 和 workerGroup
        * 2、bossGroup用来处理连接请求，workerGroup完成业务处理
        * 3、两个都是无限循环
        * 4、 bossGroup 和 workerGroup 含有的⼦线程(NioEventLoop)的个数， 默认实际 cpu核数 * 2
        *
        */

        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup(); //16
        try {
            //创建服务器端的启动对象，配置参数
            ServerBootstrap bootstrap = new ServerBootstrap();
            //使⽤链式编程来进⾏设置
            bootstrap.group(bossGroup, workerGroup) //设置两个线程组
                     .channel(NioServerSocketChannel.class) //使⽤ NioSocketChannel 作为服务器的通道实现
                     .option(ChannelOption.SO_BACKLOG, 128) // 设置线程队列得到连个数
                     .childOption(ChannelOption.SO_KEEPALIVE, true) //设置保持活动连接状态
                                  // .handler(null) // 该 handler对应 bossGroup ,childHandler 对应 workerGroup
                     .childHandler(new ChannelInitializer<SocketChannel>() {//创建⼀个通道初始化对象(匿名对象)
                        //给pipeline 设置处理器
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            System.out.println("客户socketchannel hashcode=" +
                                    ch.hashCode()); //可以使⽤⼀个集合管理 SocketChannel， 再推送消息时，可以将业务加⼊到各个channel 对应的 NIOEventLoop 的 taskQueue 或者 scheduleTaskQueue
                            ch.pipeline().addLast(new NettyServerHandler());
                        }
                    }); // 给我们的workerGroup 的 EventLoop 对应的管道设置处理器
            System.out.println(".....服务器 is ready...");
            //绑定⼀个端⼝并且同步, ⽣成了⼀个 ChannelFuture 对象
            //启动服务器(并绑定端⼝)  同步方法阻塞直到绑定成功
            final ChannelFuture cf = bootstrap.bind(6668).sync();
            //给cf 注册监听器，监控我们关⼼的事件
            cf.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws
            Exception {
                if (cf.isSuccess()) {
                    System.out.println("监听端⼝ 6668 成功");
                } else {
                    System.out.println("监听端⼝ 6668 失败");
                }
            }
        });
        //对关闭通道进⾏监听
        cf.channel().closeFuture().sync();
    } catch (InterruptedException e) {
        e.printStackTrace();
    } finally {
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }
  }
}

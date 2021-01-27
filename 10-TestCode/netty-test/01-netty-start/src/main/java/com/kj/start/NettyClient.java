package com.kj.start;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class NettyClient {
    public static void main(String[] args) {
        //客户端需要⼀个事件循环组
        EventLoopGroup group = new NioEventLoopGroup();
        //创建客户端启动对象
        //注意客户端使⽤的不是 ServerBootstrap ⽽是 Bootstrap
        Bootstrap bootstrap = new Bootstrap();
        try{
            //设置相关参数
            bootstrap.group(group) //设置线程组
                    .channel(NioSocketChannel.class) // 设置客户端通道的实现类(反射)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws
                                Exception {
                            ch.pipeline().addLast(new com.kj.start.NettyClientHandler());//加⼊⾃⼰的处理器
                        }
                    });
            System.out.println("客户端 ok..");
            //启动客户端去连接服务器端
            //关于 ChannelFuture 要分析，涉及到netty的异步模型
            ChannelFuture channelFuture = null;
            try {
                channelFuture = bootstrap.connect("127.0.0.1", 6668).sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //给关闭通道进⾏监听
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
        }
    }
}

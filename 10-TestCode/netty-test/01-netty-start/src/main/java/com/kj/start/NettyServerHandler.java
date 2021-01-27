package com.kj.start;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.util.CharsetUtil;

import java.util.concurrent.TimeUnit;

public class NettyServerHandler extends ChannelInboundHandlerAdapter{
    //读取数据实际(这⾥我们可以读取客户端发送的消息)
    /**
     * 1. ChannelHandlerContext ctx:上下⽂对象, 含有 管道pipeline , 通道channel,地址
     * 2. Object msg: 就是客户端发送的数据 默认Object
     */
    @Override
    public void channelRead(final ChannelHandlerContext ctx, Object msg) throws
            Exception {
        // ⽐如这⾥我们有⼀个⾮常耗时⻓的业务-> 异步执⾏ -> 提交该channel 对应的NIOEventLoop 的 taskQueue中,
        // 解决⽅案1 ⽤户程序⾃定义的普通任务
        ctx.channel().eventLoop().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(5 * 1000);
                    ctx.writeAndFlush(Unpooled.copiedBuffer("hello, Client No 2", CharsetUtil.UTF_8));
                            System.out.println("channel code=" + ctx.channel().hashCode());
                } catch (Exception ex) {
                    System.out.println("发⽣异常" + ex.getMessage());
                }
            }
        });
        ctx.channel().eventLoop().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(5 * 1000);
                    ctx.writeAndFlush(Unpooled.copiedBuffer("hello, Client No 3", CharsetUtil.UTF_8));
                            System.out.println("channel code=" + ctx.channel().hashCode());
                } catch (Exception ex) {
                    System.out.println("发⽣异常" + ex.getMessage());
                }
            }
        });
        //解决⽅案2 : ⽤户⾃定义定时任务 -》 该任务是提交到 scheduleTaskQueue中
        ctx.channel().eventLoop().schedule(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(5 * 1000);
                    ctx.writeAndFlush(Unpooled.copiedBuffer("hello, Client No 4", CharsetUtil.UTF_8));
                            System.out.println("channel code=" +ctx.channel().hashCode());
                } catch (Exception ex) {
                    System.out.println("发⽣异常" + ex.getMessage());
                }
            }
        }, 5, TimeUnit.SECONDS);
        System.out.println("go on ...");
        // System.out.println("服务器读取线程 " + Thread.currentThread().getName()+ " channle =" + ctx.channel());
        // System.out.println("server ctx =" + ctx);
        // System.out.println("看看channel 和 pipeline的关系");
        // Channel channel = ctx.channel();
        // ChannelPipeline pipeline = ctx.pipeline(); //本质是⼀个双向链接, 出站⼊站
        //
        // //将 msg 转成⼀个 ByteBuf
        // //ByteBuf 是 Netty 提供的，不是 NIO 的 ByteBuffer.
        // ByteBuf buf = (ByteBuf) msg;
        // System.out.println("客户端发送消息是:" +buf.toString(CharsetUtil.UTF_8));
        // System.out.println("客户端地址:" + channel.remoteAddress());
    }
    public void channelRead2(ChannelHandlerContext ctx, Object msg) throws
            Exception {
        System.out.println("服务器读取线程 " + Thread.currentThread().getName() +
                " channle =" + ctx.channel());
        System.out.println("server ctx =" + ctx);
        System.out.println("看看channel 和 pipeline的关系");
        Channel channel = ctx.channel();
        ChannelPipeline pipeline = ctx.pipeline(); //本质是⼀个双向链接, 出站⼊站
        //将 msg 转成⼀个 ByteBuf
        //ByteBuf 是 Netty 提供的，不是 NIO 的 ByteBuffer.
        ByteBuf buf = (ByteBuf) msg;
        System.out.println("客户端发送消息是:" +
                buf.toString(CharsetUtil.UTF_8));
        System.out.println("客户端地址:" + channel.remoteAddress());
    }
    //数据读取完毕
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws
            Exception {
        //writeAndFlush 是 write + flush
        //将数据写⼊到缓存，并刷新
        //⼀般讲，我们对这个发送的数据进⾏编码
        ctx.writeAndFlush(Unpooled.copiedBuffer("hello, Client No 1",
                CharsetUtil.UTF_8));
    }
    //处理异常, ⼀般是需要关闭通道
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        ctx.close();
    }

}

package transfer;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.oio.OioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.oio.OioServerSocketChannel;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;

/**
 * Netty阻塞网络传输处理
 */
public class NettyOioServer {
    public void server(int port) throws Exception{
        final ByteBuf buf = Unpooled.unreleasableBuffer(
                Unpooled.copiedBuffer("Hi!\r\n", Charset.forName("UTF-8")));
        //创建一个NioEventLoopGroup;
        EventLoopGroup group = new OioEventLoopGroup();
        try{
            //创建ServerBootStrap
            ServerBootstrap b = new ServerBootstrap();
            b.group(group)
                    .channel(OioServerSocketChannel.class)  //使用OioEventLoopGrop允许阻塞模式
                    .localAddress(new InetSocketAddress(port))
                    //指定一个ChannelInitializer，对于每个已经接受的连接都调用它
                    .childHandler(new ChannelInitializer<SocketChannel>() {

                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ch.pipeline().addLast(
                                    //添加一个ChannelInboundHandler Adapter 以拦截和 处理事件
                                    new ChannelInboundHandlerAdapter(){
                                        @Override
                                        public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                            //将消息添加到客户端，并添加ChannelFutrueListener,以便消息一被写完就关闭
                                            ctx.writeAndFlush(buf.duplicate()).addListener(
                                                    ChannelFutureListener.CLOSE
                                            );
                                        }
                                    }
                            );
                        }
                    });
            //绑定服务器 以接收连接
            ChannelFuture f = b.bind().sync();
            f.channel().closeFuture().sync();
        }finally {
            //最后就是释放所有资源
            group.shutdownGracefully();
        }
    }


}

package transfer;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;

/**
 * Netty非阻塞网络传输demo 异步网络传输
 */
public class NettyNioServer {
    public void server(int port) throws Exception{
        final ByteBuf buf = Unpooled.copiedBuffer("Hi!\\r\\n",Charset.forName("UTF-8"));
        //使用非阻塞模式
        EventLoopGroup group = new NioEventLoopGroup();
        try{
            ServerBootstrap b = new ServerBootstrap();
            b.group(group).channel(NioServerSocketChannel.class)
                    .localAddress(new InetSocketAddress(port))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            //添加channleInbountHandlerAdapter以接收和处理事件
                            ch.pipeline().addLast(new ChannelInboundHandlerAdapter(){
                                @Override
                                public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                    //将消息写到客户端，并添加channleFutureListener,以便消息一被写完就关闭连接
                                    ctx.writeAndFlush(buf.duplicate()).addListener(ChannelFutureListener.CLOSE);
                                }
                            });
                        }
                    });
            ChannelFuture f = b.bind().sync();  //绑定服务器 以接收连接
            f.channel().closeFuture().sync();
        }finally {
            //释放所有资源
            group.shutdownGracefully();
        }
    }
}

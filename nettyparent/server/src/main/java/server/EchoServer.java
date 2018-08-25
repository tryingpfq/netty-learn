package server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.InetSocketAddress;

/**
 * 引导服务器
 * 绑定到服务器，将在其上监听并接受传入连接请求的端口
 */
public class EchoServer {

    private final int port;
    public EchoServer(int port){
        this.port = port;
    }

    public static void main(String[] args) throws Exception{
        if(args.length != 1){
            System.err.println("Usage: "+ EchoServer.class.getSimpleName()+"<port>");
            return;
        }
        int port = Integer.parseInt(args[0]);
        new EchoServer(port).start();//调用服务器的start方法
    }

    public void start() throws Exception{
        final EchoServerHandler serverHandler = new EchoServerHandler();
        //创建Even Loop Group
        //配置服务器的NIO线程组
        //两个Reactor 一个用于服务器接收客户端的连接  一个用于经行SocketChannel的网络读写
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try{
            //创建ServerBootStrap
            ServerBootstrap b = new ServerBootstrap();
            //指定所使用的NIO传输Channle
            b.group(bossGroup,workerGroup).channel(NioServerSocketChannel.class)
                    .localAddress(new InetSocketAddress(port))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) {
                            //如果EchoServerHandler被注为@Shareable的时候，则可以总是使用同样的实例
                            socketChannel.pipeline().addLast(serverHandler);
                        }
                    });
            //异步的绑定服务器，调用sync方法阻塞，直到绑定完成
            ChannelFuture f = b.bind().sync();
            //获取Channel的CloseFuture,并阻塞当前线程直到它完成
            f.channel().closeFuture().sync();
        }finally {
            //关闭EvenLoopGroup,释放所有资源
            bossGroup.shutdownGracefully().sync();
            workerGroup.shutdownGracefully().sync();
        }
    }


}

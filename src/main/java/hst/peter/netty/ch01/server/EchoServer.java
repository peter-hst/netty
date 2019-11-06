package hst.peter.netty.ch01.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.InetSocketAddress;

public class EchoServer {
    private final int port;

    public EchoServer(int port) {
        this.port = port;
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.out.println("Usage: " + EchoServer.class.getSimpleName() + " <port>");
        }
        int port = Integer.parseInt(args[0]); // 设置端口值(如果端口参数的格式不正确，则抛出一个NumberFormatException)
        new EchoServer(port).start(); // 调用服务器的 start() 方法
    }

    public void start() throws Exception {
        final EchoServerHandler serverHandler = new EchoServerHandler();
        EventLoopGroup group = new NioEventLoopGroup(); // 1.创建EventLoopGroup
        try {
            ServerBootstrap b = new ServerBootstrap(); // 2.创建ServerBootstrap辅助类
            b.group(group)
                    .channel(NioServerSocketChannel.class) // 3.指定所使用的NIO传输Channel
                    .localAddress(new InetSocketAddress(port)) // 4. 使用指定的端口设置套接字地址
                    .childHandler(new ChannelInitializer<SocketChannel>() { // 5. 添加一个EchoServerHandler到子Channel的ChannelPipeline
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(serverHandler); // 6. EchoServerHandler被标注为@Shareable，所以我们可以总是使用同样的实例
                            //这里对多有的客户端链接来说，都会使用同一个EchoServerHandler，因为其被标注为@Sharable
                        }
                    });
            ChannelFuture f = b.bind().sync(); // 7.异步地绑定服务器，调用 sync() 方法阻塞等待知道绑定完成
            f.channel().closeFuture().sync(); // 8. 获取Channel的CloseFuture，并阻塞当前线程知道它完成
        } finally {
            group.shutdownGracefully().sync(); // 9. 关闭EventLoopGroup，释放所有的资源
        }
        /**
         * ??EchoServerHandler 实现了业务逻辑；
         * ??main()方法引导了服务器；
         * 引导过程中所需要的步骤如下：
         * ??创建一个 ServerBootstrap 的实例以引导和绑定服务器；
         * ??创建并分配一个 NioEventLoopGroup 实例以进行事件的处理，如接受新连接以及读/
         * 写数据；
         * ??指定服务器绑定的本地的 InetSocketAddress；
         * ??使用一个 EchoServerHandler 的实例初始化每一个新的 Channel；
         * ??调用 ServerBootstrap.bind()方法以绑定服务器。
         */
    }
}

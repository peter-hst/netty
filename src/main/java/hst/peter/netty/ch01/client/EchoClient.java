package hst.peter.netty.ch01.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;

public class EchoClient {
    private final String host;
    private final int port;

    public EchoClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void start() throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap(); // 1. 创建bootstrap
            bootstrap.group(group) // 2. 指定 EventLoopGroup 以处理客户端事件；需要适用于 NIO 的实现
                    .channel(NioSocketChannel.class) // 3. 适用于 NIO 传输的 Channel 类型
                    .remoteAddress(new InetSocketAddress(host, port)) // 4. 设置连接服务器的 InetSocketAddress
                    .handler(new ChannelInitializer<SocketChannel>() { // 5. 在创建Channel时，向 ChannelPipeline 中添加一个 EchoClientHandler 实例
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new EchoClientHandler());
                        }
                    });
            ChannelFuture channelFuture = bootstrap.connect().sync(); // 6. 连接到远程节点，阻塞等待直到连接完成
            channelFuture.channel().closeFuture().sync(); // 7. 阻塞，直到 Channel 关闭
        } finally {
            group.shutdownGracefully().sync(); // 8. 关闭线程池并且释放所有的资源
        }

        // 和之前一样，使用了 NIO 传输。注意，你可以在客户端和服务器上分别使用不同的传输。例如，在服务器端使用 NIO 传输，而在客户端使用 OIO 传输

        /**
         * ??为初始化客户端，创建了一个 Bootstrap 实例；
         * ??为进行事件处理分配了一个 NioEventLoopGroup 实例，其中事件处理包括创建新的
         * 连接以及处理入站和出站数据；
         * ??为服务器连接创建了一个 InetSocketAddress 实例；
         * ??当连接被建立时，一个 EchoClientHandler 实例会被安装到（该 Channel 的）
         * ChannelPipeline 中；
         * ??在一切都设置完成后，调用 Bootstrap.connect()方法连接到远程节点；
         */
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println("Usage: " + EchoClient.class.getSimpleName() + " <Host> <port>");
            return;
        }
        String host = args[0];
        int port = Integer.parseInt(args[1]);
        new EchoClient(host, port).start();
    }
}

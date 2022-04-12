package xyz.nowinski.udptester;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
public class UDPConnector {
    final int port;
    final String host;

    Channel channel;
    EventLoopGroup workGroup = new NioEventLoopGroup();

    /**
     * Constructor
     *
     * @param port {@link Integer} port of server
     * @param host
     */
    public UDPConnector(int port, String host) {
        this.port = port;
        this.host = host;
    }

    /**
     * Startup the client
     *
     * @return {@link ChannelFuture}
     * @throws Exception
     */
    public ChannelFuture startup() throws Exception {
        try {
            Bootstrap b = new Bootstrap();
            b.group(workGroup);
            b.channel(NioDatagramChannel.class);
            b.handler(new ChannelInitializer<DatagramChannel>() {
                protected void initChannel(DatagramChannel datagramChannel) throws Exception {
                    datagramChannel.pipeline().addLast(new NettyHandler());
                    setupSender(datagramChannel);
                }
            });
            ChannelFuture channelFuture = b.connect(host, this.port).sync();
            this.channel = channelFuture.channel();

            return channelFuture;
        } finally {
        }
    }

    private void setupSender(DatagramChannel datagramChannel) {
        ScheduledExecutorService se = new ScheduledThreadPoolExecutor(10);
        se.scheduleAtFixedRate(() -> {
            log.info("Sending datagram...");
            datagramChannel.writeAndFlush(Unpooled.wrappedBuffer("Hello".getBytes()));
        }, 1l, 1l, TimeUnit.SECONDS);
    }

    /**
     * Shutdown a client
     */
    public void shutdown() {
        workGroup.shutdownGracefully();
    }

    public void startServer() {

        try {
            // Create a client
            System.out.println("Creating new UDP Client");

            UDPConnector client = new UDPConnector(port, host);
            ChannelFuture channelFuture = client.startup();

            System.out.println("New Client is created");

            // wait for 5 seconds
            Thread.sleep(5000);
            // check the connection is successful
            if (channelFuture.isSuccess()) {
                // send message to server
                channelFuture.channel().writeAndFlush(Unpooled.wrappedBuffer("Hello".getBytes()))
                        .addListener(new ChannelFutureListener() {
                            @Override
                            public void operationComplete(ChannelFuture future) throws Exception {
                                System.out.println(future.isSuccess() ? "Message sent to server : Hello" :
                                        "Message sending failed");
                            }
                        });
            }
            // timeout before closing client
            Thread.sleep(5000);
            // close the client
            client.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Try Starting Server First !!");
        } finally {

        }
    }
}

package xyz.nowinski.udptester;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;
import java.util.Optional;

@Slf4j
public class UDPConnector {
    final int port;
    final String host;

    Channel channel;
    final EventLoopGroup workGroup = new NioEventLoopGroup();

    @Getter @Setter
    long messageDelay = 1000L;
    boolean running=false;
    PackageListener listener=null;


    public UDPConnector(int port, String host) {
        this.port = port;
        this.host = host;
    }

    public void addPackageListener(PackageListener l) {
        this.listener = l;
    }

    public ChannelFuture startup() throws Exception {
        running=true;
        Bootstrap b = new Bootstrap();
        b.group(workGroup);
        b.channel(NioDatagramChannel.class);
        b.handler(new ChannelInitializer<DatagramChannel>() {
            protected void initChannel(DatagramChannel datagramChannel) {
                datagramChannel.pipeline().addLast(new ResponseHandler());
                setupSender(datagramChannel);
            }
        });
        ChannelFuture channelFuture = b.connect(host, this.port).sync();
        this.channel = channelFuture.channel();

        return channelFuture;
    }
    private void setupSender(DatagramChannel datagramChannel) {
        new Thread(new DatagramSender(datagramChannel)).start();
    }


    /**
     * Shutdown a client
     */
    public void shutdown() {
        running=false;
        workGroup.shutdownGracefully();

    }

    private class ResponseHandler extends SimpleChannelInboundHandler<Object> {

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
            super.channelReadComplete(ctx);
        }



        @Override
        public void channelRead0(ChannelHandlerContext ctx, Object msg) {
            DatagramPacket packet = (DatagramPacket) msg;
            String message = packet.content().toString(Charset.defaultCharset());
            try {
                long sourceTimestamp = Long.parseLong(message);
                Optional.ofNullable(listener).ifPresent(l->l.packageReceived(sourceTimestamp, System.currentTimeMillis()));
            } catch (NumberFormatException nfe) {
                log.warn("Failed to parse message as timestamp, message: {}", message);
            }
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            super.channelActive(ctx);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            log.warn("Got exception on transmission.", cause);
        }
    }


    private class DatagramSender implements Runnable {
        final DatagramChannel channel;

        public DatagramSender(DatagramChannel channel) {
            this.channel = channel;
        }

        @Override
        public void run() {
            try {
                while (running) {
                    long timestamp = System.currentTimeMillis();
                    String content = "" + timestamp;

                    log.info("Sending datagram, content={}", content);
                    channel.writeAndFlush(Unpooled.wrappedBuffer(content.getBytes()));
                    Optional.ofNullable(listener).ifPresent(l->l.packageSent(timestamp));
                    Thread.sleep(messageDelay);

                }
            } catch (InterruptedException e) {
                log.warn("Unexpected interruption", e);
                Thread.currentThread().interrupt();
            }
        }
    }
}

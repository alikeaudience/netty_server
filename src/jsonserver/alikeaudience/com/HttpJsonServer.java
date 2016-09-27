package jsonserver.alikeaudience.com;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created by AlikeAudience on 22/9/2016.
 */
public final class HttpJsonServer {
    private static final boolean SSL = System.getProperty("ssl") != null;
    private static final int PORT = Integer.parseInt(System.getProperty("port", SSL? "8443" : "8080"));

    private static final int SWAP_INTERVAL_IN_MINUTES = 5; //default: 5 minutes for saving file swap interval

    static String kafkaConfigFile;



    public static void main(String[] args) throws Exception {
        // Configure SSL.
        final SslContext sslCtx;

        final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        final ScheduledFuture<?> fileNameSwapHandler;

        final Runnable fileNameSwap = new Runnable() {
            public void run() {
                FileWriteHelper.getInstance().writeToNewFile();
            }
        };

        fileNameSwapHandler = scheduler.scheduleAtFixedRate(fileNameSwap, SWAP_INTERVAL_IN_MINUTES, SWAP_INTERVAL_IN_MINUTES, TimeUnit.MINUTES);

        if(args.length > 0) kafkaConfigFile = args[0];
        else kafkaConfigFile = null;

        //Instantiate filewrite and kafkaproducer here to further reduce initialization delay
        FileWriteHelper.getInstance();
        JsonKafkaProducer.getInstance();

        if (SSL) {
            SelfSignedCertificate ssc = new SelfSignedCertificate();
            sslCtx = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
        } else {
            sslCtx = null;
        }

        // Configure the server.
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.option(ChannelOption.SO_BACKLOG, 1024);
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new HttpJsonServerInitializer(sslCtx));

            Channel ch = b.bind(PORT).sync().channel();

            System.err.println("Open your web browser and navigate to " +
                    (SSL? "https" : "http") + "://127.0.0.1:" + PORT + '/');

            ch.closeFuture().sync();



        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();

            scheduler.schedule(new Runnable() {
                public void run() { fileNameSwapHandler.cancel(true); System.out.println("close");}
            }, 0,  TimeUnit.SECONDS);
        }
    }
}

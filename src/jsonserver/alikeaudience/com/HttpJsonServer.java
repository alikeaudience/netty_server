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



    static String kafkaConfigFile;

    static String topicName; //The topic name that must be specified for Kafka

    static String topicKey; //The key is used to map to different partitions



    public static void main(String[] args) throws Exception {
        final int SWAP_INTERVAL_IN_MINUTES; //default: 5 minutes for saving file swap interval

        if(args.length > 0) SWAP_INTERVAL_IN_MINUTES = Integer.parseInt(args[0]);
        else SWAP_INTERVAL_IN_MINUTES = 5;

        System.out.println("Set the log file swap in interval to " + SWAP_INTERVAL_IN_MINUTES + " minutes");

        if(args.length > 1) kafkaConfigFile = args[1];
        else kafkaConfigFile = null;

        System.out.println((kafkaConfigFile != null) ? ("Use the specified Kafka producer properties: " + HttpJsonServer.kafkaConfigFile) : "Default Kafka producer properties used");

        if(args.length > 2) topicName = args[2];
        else topicName = null;

        System.out.println((topicName != null) ? ("Set the topic name to: " + HttpJsonServer.topicName) : "Use the default topic name");

        if(args.length > 3) topicKey = args[3];
        else topicKey = null;

        System.out.println((topicKey != null) ? ("Set the topic key to: " + HttpJsonServer.topicKey) : "Use the default topic key");

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

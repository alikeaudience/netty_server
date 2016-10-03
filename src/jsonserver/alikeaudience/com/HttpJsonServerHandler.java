package jsonserver.alikeaudience.com;


import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.AbstractDiskHttpData;
import io.netty.handler.codec.http.multipart.DiskFileUpload;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;


import java.io.*;

import static io.netty.handler.codec.http.HttpHeaders.Names.*;
import static io.netty.handler.codec.http.HttpHeaders.Values;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.*;

/**
 * Created by AlikeAudience on 22/9/2016.
 */
public class HttpJsonServerHandler extends ChannelInboundHandlerAdapter {
    private static final byte[] CONTENT = { 'H', 'e', 'l', 'l', 'o', ' ', 'W', 'o', 'r', 'l', 'd' };


    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
//        System.out.println(msg);

        String uri = null;

        if (msg instanceof HttpRequest) {
            HttpRequest req = (HttpRequest) msg;

            uri = req.uri();



            if (HttpHeaders.is100ContinueExpected(req)) {
                ctx.write(new DefaultFullHttpResponse(HTTP_1_1, CONTINUE));
            }






            boolean keepAlive = HttpHeaders.isKeepAlive(req);

            FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK);
            response.headers().set(CONTENT_TYPE, "text/plain");
            response.headers().set(CONTENT_LENGTH, response.content().readableBytes());

            if (!keepAlive) {
                ctx.write(response).addListener(ChannelFutureListener.CLOSE);
            } else {
                response.headers().set(CONNECTION, Values.KEEP_ALIVE);
                ctx.write(response);

            }
        }

        if (msg instanceof HttpContent) {


            HttpContent httpContent = (HttpContent)msg;
            ByteBuf buf = httpContent.content();




            try {


                while(buf.isReadable()) {
//                System.out.println(buf.isReadable());
//                    System.out.println(buf.toString(CharsetUtil.UTF_8));
                    //Saving data to local files
                    String bufData = buf.toString(CharsetUtil.UTF_8);
                    FileWriteHelper.getInstance().writeToFile(bufData);
                    if(uri != null) JsonKafkaProducer.getInstance().sendToKafka(bufData, uri.substring(1));
                    else JsonKafkaProducer.getInstance().sendToKafka(bufData, null);
                    break;
                }

                //Sending data to Kafka server
//                JsonKafkaProducer.getInstance().sendToKafka(buf.toString(CharsetUtil.UTF_8));

            } finally {
//                System.out.println(buf.capacity()+" finish");
////                ReferenceCountUtil.release(msg);
//                if (msg instanceof LastHttpContent) {
////                System.out.println("last httpcontent");
//                    FileWriteHelper.getInstance().writeNewLine();
//
//                }
            }


        }

        ReferenceCountUtil.release(msg);
    }





    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

}


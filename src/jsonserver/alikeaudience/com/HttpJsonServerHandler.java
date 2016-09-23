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
        if (msg instanceof HttpRequest) {
            HttpRequest req = (HttpRequest) msg;


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



//            try(FileWriter fw = new FileWriter("outfilename", true);
//                BufferedWriter bw = new BufferedWriter(fw))
//            {
//                while (buf.isReadable()){
////                    out.println(buf.toString(CharsetUtil.UTF_8));
//                    bw.write(buf.toString(CharsetUtil.UTF_8));
//                    bw.newLine();
//                    break;
//                }
//
//                bw.close();
//
//
//            } catch (IOException e) {
//                e.printStackTrace();
//            }

            try {
                while (buf.isReadable()) {
                    //Saving data to local files
                    FileWriteHelper.getInstance().writeToFile(buf.toString(CharsetUtil.UTF_8));

                    //Sending data to Kafka server
//                    JsonKafkaProducer.getInstance().sendToKafka(buf.toString(CharsetUtil.UTF_8));

                    break;
                }
//                if(buf.isReadable()) FileWriteHelper.getInstance().writeToFile(buf.toString(CharsetUtil.UTF_8));


            } finally {
                ReferenceCountUtil.release(msg);
            }



//            while (buf.isReadable()){
//                FileWriteHelper.getInstance().writeToFile(buf.toString(CharsetUtil.UTF_8));
//                break;
//            }
//
//            buf.release();




        }

    }





    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

}


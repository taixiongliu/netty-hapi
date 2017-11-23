package cn.liutaixiong.hapi.netty;

import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.util.Map;

import cn.liutaixiong.hapi.http.BaseHapiHttpRequestImpl;
import cn.liutaixiong.hapi.http.DefaultHapiHttpResponseImpl;
import cn.liutaixiong.hapi.http.HttpUrlErrorException;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;

/**
 * <b>Netty read handler, modify from netty example demo</b>
 * @author taixiong.liu
 * 
 */
public class NettyHttpServerHandler extends ChannelInboundHandlerAdapter{
	private HttpRequest request;
	private Boolean isPost = null;
	private String ip;
	private String postContent="";
	private HttpRequestHandler handler;
	private SocketChannel channel;
	private BaseHapiHttpRequestImpl requestImpl;
	
	public NettyHttpServerHandler(SocketChannel channel,HttpRequestHandler handler, BaseHapiHttpRequestImpl requestImpl){
		this.channel = channel;
		this.handler = handler;
		this.requestImpl = requestImpl;
	}
	
	private FullHttpResponse setResponse(DefaultHapiHttpResponseImpl nettyResponse) throws UnsupportedEncodingException{
		if(nettyResponse.getStatus() == null){
			nettyResponse.setStatus(HttpResponseStatus.NOT_FOUND);
		}
		if(nettyResponse.getContent() == null){
			nettyResponse.setContent("404, not found...");
		}
		
		FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
				nettyResponse.getStatus(), Unpooled.wrappedBuffer(nettyResponse.getContent().getBytes("UTF-8")));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain");
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH,
              response.content().readableBytes());
        return response;
	}
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		// TODO Auto-generated method stub
		if (msg instanceof HttpRequest) {
			request = (HttpRequest) msg;

			//get ip if with NGINX proxy
			ip = request.headers().get("X-Real-IP");
            if (ip == null) {
                InetSocketAddress insocket = (InetSocketAddress) ctx.channel()
                        .remoteAddress();
                ip = insocket.getAddress().getHostAddress();
            }
            
            if(request.method().equals(HttpMethod.GET)){
            	isPost = false;
            }
            if(request.method().equals(HttpMethod.POST)){
            	isPost = true;
            }
        }
		if (msg instanceof HttpContent) {
			//only post method have request body
			if(isPost != null && isPost){
				HttpContent content = (HttpContent) msg;
	            ByteBuf buf = content.content();
	            postContent += buf.toString(CharsetUtil.UTF_8);
			}	
    	}
		//if last request content(maybe content split)
		if (msg instanceof LastHttpContent) {
			if(handler == null){
				ctx.write(setResponse(new DefaultHapiHttpResponseImpl()));
	            ctx.flush();
	            ctx.close();
	            //release object
	            ReferenceCountUtil.release(msg);
	            channel.close();
	            return ;
			}
			boolean init = true;
			try {
				requestImpl.initUrl(request);
			} catch (HttpUrlErrorException e) {
				// TODO: handle exception
				init = false;
			}
			if(!init){
				ctx.write(setResponse(new DefaultHapiHttpResponseImpl(HttpResponseStatus.BAD_REQUEST, "400,bad request...")));
	            ctx.flush();
	            ctx.close();
	            //release object
	            ReferenceCountUtil.release(msg);
	            channel.close();
	            return ;
			}
			
			//get method request
			if(!isPost){
				// initialization response instance
				DefaultHapiHttpResponseImpl responseImpl = new DefaultHapiHttpResponseImpl();
				handler.onGet(requestImpl, responseImpl);
				ctx.write(setResponse(responseImpl));
	            ctx.flush();
	            ctx.close();
	            //release object
	            ReferenceCountUtil.release(msg);
	            channel.close();
	            return ;
			}
			//post method request
			Map<String, String> postParameter = null;
			boolean parse = true;
			try {
				postParameter = requestImpl.parseParameter(postContent);
			} catch (HttpUrlErrorException e) {
				// TODO: handle exception
				parse = false;
			}
			// request body parse error
			if(!parse){
				ctx.write(setResponse(new DefaultHapiHttpResponseImpl(HttpResponseStatus.BAD_REQUEST, "400,bad request...")));
	            ctx.flush();
	            ctx.close();
	            //release object
	            ReferenceCountUtil.release(msg);
	            channel.close();
	            return ;
			}
			// add request body to parameters
			requestImpl.addParameters(postParameter);
			// initialization response instance
			DefaultHapiHttpResponseImpl responseImpl = new DefaultHapiHttpResponseImpl();
			handler.onPost(requestImpl, responseImpl);
			ctx.write(setResponse(responseImpl));
            ctx.flush();
            ctx.close();
            //release object
            ReferenceCountUtil.release(msg);
            channel.close();
		}
	}
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		// TODO Auto-generated method stub
		cause.printStackTrace();
        ctx.close();
	}
}

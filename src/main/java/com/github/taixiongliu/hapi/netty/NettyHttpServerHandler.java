package com.github.taixiongliu.hapi.netty;

import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.TimeZone;

import javax.activation.MimetypesFileTypeMap;

import com.github.taixiongliu.hapi.HapiHttpContextFactory;
import com.github.taixiongliu.hapi.http.BaseHapiHttpRequestImpl;
import com.github.taixiongliu.hapi.http.DefaultHapiHttpResponseImpl;
import com.github.taixiongliu.hapi.http.HttpUrlErrorException;
import com.github.taixiongliu.hapi.route.HapiRouteType;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelProgressiveFuture;
import io.netty.channel.ChannelProgressiveFutureListener;
import io.netty.channel.DefaultFileRegion;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpChunkedInput;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.FileUpload;
import io.netty.handler.codec.http.multipart.HttpPostMultipartRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.stream.ChunkedFile;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;

/**
 * <b>Netty read handler, modify from netty example demo</b>
 * 
 * @author taixiong.liu
 * 
 */
@SuppressWarnings("restriction")
public class NettyHttpServerHandler extends ChannelInboundHandlerAdapter {
	public static final String HTTP_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz";
	public static final String HTTP_DATE_GMT_TIMEZONE = "GMT";
	public static final int HTTP_CACHE_SECONDS = 60;

	private HttpRequest request;
	private Boolean isPost = null;
	private String ip;
	private String postContent = "";
	private HttpRequestHandler handler;
	private SocketChannel channel;
	private BaseHapiHttpRequestImpl requestImpl;
	private String uploadPath;
	// multipart/form-data
	private boolean isMFD;
	private Map<String, String> mfdParameter;
	private Random random;
	private int mdfError;

	public NettyHttpServerHandler(SocketChannel channel, HttpRequestHandler handler,
			BaseHapiHttpRequestImpl requestImpl, String uploadPath) {
		this.channel = channel;
		this.handler = handler;
		this.requestImpl = requestImpl;
		this.uploadPath = uploadPath;
		isMFD = false;
		mfdParameter = new HashMap<String, String>();
		random = new Random();
		mdfError = 0;
	}

	private FullHttpResponse setResponse(DefaultHapiHttpResponseImpl nettyResponse)
			throws UnsupportedEncodingException {
		if (nettyResponse.getStatus() == null) {
			nettyResponse.setStatus(HttpResponseStatus.NOT_FOUND);
		}
		if (nettyResponse.getContent() == null) {
			nettyResponse.setContent("404, not found...");
		}

		FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, nettyResponse.getStatus(),
				Unpooled.wrappedBuffer(nettyResponse.getContent().getBytes("UTF-8")));
		response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain");
		response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());

		Map<String, String> map = nettyResponse.heads();
		if (map != null) {
			for (String key : map.keySet()) {
				response.headers().set(key, map.get(key));
			}
		}
		return response;
	}

	private void fileResponse(ChannelHandlerContext ctx, DefaultHapiHttpResponseImpl nettyResponse) throws Exception {
		final boolean keepAlive = HttpUtil.isKeepAlive(request);

		File file = new File(nettyResponse.getContent());
		RandomAccessFile raf;
		try {
			raf = new RandomAccessFile(file, "r");
		} catch (FileNotFoundException ignore) {
			nettyResponse.setStatus(HttpResponseStatus.NOT_FOUND);
			nettyResponse.setContent("404, file not found...");
			ctx.write(setResponse(nettyResponse));
			return;
		}
		long fileLength = raf.length();

		HttpResponse response = new DefaultHttpResponse(HTTP_1_1, OK);
		HttpUtil.setContentLength(response, fileLength);
		setContentTypeHeader(response, file);
		setDateAndCacheHeaders(response, file);

		Map<String, String> map = nettyResponse.heads();
		if (map != null) {
			for (String key : map.keySet()) {
				response.headers().set(key, map.get(key));
			}
		}
		// Write the initial line and the header.
		ctx.write(response);

		// Write the content.
		ChannelFuture sendFileFuture;
		ChannelFuture lastContentFuture;
		if (ctx.pipeline().get(SslHandler.class) == null) {
			sendFileFuture = ctx.write(new DefaultFileRegion(raf.getChannel(), 0, fileLength),
					ctx.newProgressivePromise());
			// Write the end marker.
			lastContentFuture = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
		} else {
			sendFileFuture = ctx.writeAndFlush(new HttpChunkedInput(new ChunkedFile(raf, 0, fileLength, 8192)),
					ctx.newProgressivePromise());
			// HttpChunkedInput will write the end marker (LastHttpContent) for
			// us.
			lastContentFuture = sendFileFuture;
		}

		sendFileFuture.addListener(new ChannelProgressiveFutureListener() {
			public void operationProgressed(ChannelProgressiveFuture future, long progress, long total) {
				if (total < 0) { // total unknown
					// -
					// -
					// System.err.println(future.channel() + " Transfer
					// progress: " + progress);
				} else {
					// -
					// -
					// System.err.println(future.channel() + " Transfer
					// progress: " + progress + " / " + total);
				}
			}

			public void operationComplete(ChannelProgressiveFuture future) {
				// -
				// -
				// System.err.println(future.channel() + " Transfer complete.");
			}
		});

		// Decide whether to close the connection or not.
		if (!keepAlive) {
			// Close the connection when the whole content is written out.
			lastContentFuture.addListener(ChannelFutureListener.CLOSE);
		}
	}

	/**
	 * Sets the content type header for the HTTP Response
	 *
	 * @param response
	 *            HTTP response
	 * @param file
	 *            file to extract content type
	 */
	private static void setContentTypeHeader(HttpResponse response, File file) {
		MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();
		response.headers().set(HttpHeaderNames.CONTENT_TYPE, mimeTypesMap.getContentType(file.getPath()));
	}

	/**
	 * Sets the Date and Cache headers for the HTTP Response
	 *
	 * @param response
	 *            HTTP response
	 * @param fileToCache
	 *            file to extract content type
	 */
	private static void setDateAndCacheHeaders(HttpResponse response, File fileToCache) {
		SimpleDateFormat dateFormatter = new SimpleDateFormat(HTTP_DATE_FORMAT, Locale.US);
		dateFormatter.setTimeZone(TimeZone.getTimeZone(HTTP_DATE_GMT_TIMEZONE));

		// Date header
		Calendar time = new GregorianCalendar();
		response.headers().set(HttpHeaderNames.DATE, dateFormatter.format(time.getTime()));

		// Add cache headers
		time.add(Calendar.SECOND, HTTP_CACHE_SECONDS);
		response.headers().set(HttpHeaderNames.EXPIRES, dateFormatter.format(time.getTime()));
		response.headers().set(HttpHeaderNames.CACHE_CONTROL, "private, max-age=" + HTTP_CACHE_SECONDS);
		response.headers().set(HttpHeaderNames.LAST_MODIFIED,
				dateFormatter.format(new Date(fileToCache.lastModified())));
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		// TODO Auto-generated method stub
		if (msg instanceof HttpRequest) {
			request = (HttpRequest) msg;

			// get ip if with NGINX proxy
			ip = request.headers().get("X-Real-IP");
			if (ip == null) {
				InetSocketAddress insocket = (InetSocketAddress) ctx.channel().remoteAddress();
				ip = insocket.getAddress().getHostAddress();
			}

			if (request.method().equals(HttpMethod.GET)) {
				isPost = false;
			}
			if (request.method().equals(HttpMethod.POST)) {
				isPost = true;
			}
			String contentType = request.headers().get(HttpHeaderNames.CONTENT_TYPE);
			if (contentType != null && contentType.startsWith("multipart/form-data")) {
				isMFD = true;
			}
		}
		if (msg instanceof HttpContent) {
			// only post method have request body
			if (isPost != null && isPost) {
				
				if (isMFD) {
					HttpPostMultipartRequestDecoder decoder = new HttpPostMultipartRequestDecoder(request);
					while (decoder.hasNext()) {
						InterfaceHttpData data = decoder.next();
						if (data != null) {
							if (data.getHttpDataType() == InterfaceHttpData.HttpDataType.FileUpload) {
								FileUpload fileUpload = (FileUpload) data;
								if (fileUpload.isCompleted()) {
									fileUpload.isInMemory();// tells if the file is in Memory
									
									Integer maxLen = HapiHttpContextFactory.getInstance().getMaxLength();
									//size over max.
									if(maxLen != null && maxLen.intValue() < fileUpload.length()){
										mdfError = 2;
										break;
									}
									
									// or on File
									// enable to move into another
									String[] fileNameArr = fileUpload.getFilename().split("\\.");
									String suffix = null;
									if(fileNameArr.length > 1){
										suffix = fileNameArr[fileNameArr.length - 1];
									}
									
									String fileName = getFileName(suffix);
									File temp = new File(uploadPath + fileName);
									//if exists, try again
									if(temp.exists()){
										fileName = getFileName(suffix);
										temp = new File(uploadPath + fileName);
										//give up.
										if(temp.exists()){
											mdfError = 1;
											break;
										}
									}
									
									fileUpload.renameTo(temp);
									
									//add parameter
									mfdParameter.put(fileUpload.getName(), fileName);
									
									// File dest
									decoder.removeHttpDataFromClean(fileUpload); // remove
								}

							}
							if (data.getHttpDataType() == InterfaceHttpData.HttpDataType.Attribute) {
								Attribute attribute  = (Attribute)data;
								//add parameter
								mfdParameter.put(attribute.getName(), attribute.getValue());
								decoder.removeHttpDataFromClean(attribute);
							}
						}
					}
				} else {
					HttpContent content = (HttpContent) msg;
					ByteBuf buf = content.content();
					postContent += buf.toString(CharsetUtil.UTF_8);
				}
			}
		}
		// if last request content(maybe content split)
		if (msg instanceof LastHttpContent) {
			if (handler == null) {
				ctx.write(setResponse(new DefaultHapiHttpResponseImpl()));
				ctx.flush();
				ctx.close();
				// release object
				ReferenceCountUtil.release(msg);
				channel.close();
				return;
			}
			boolean init = true;
			try {
				requestImpl.initUrl(request);
			} catch (HttpUrlErrorException e) {
				// TODO: handle exception
				init = false;
			}
			if (!init) {
				ctx.write(setResponse(
						new DefaultHapiHttpResponseImpl(HttpResponseStatus.BAD_REQUEST, "400,bad request...")));
				ctx.flush();
				ctx.close();
				// release object
				ReferenceCountUtil.release(msg);
				channel.close();
				return;
			}

			requestImpl.setIpAddress(ip);

			// get method request
			if (!isPost) {
				// initialization response instance
				DefaultHapiHttpResponseImpl responseImpl = new DefaultHapiHttpResponseImpl();
				handler.onGet(requestImpl, responseImpl);
				// file route type
				if (responseImpl.getRouteType().equals(HapiRouteType.FILE)) {
					fileResponse(ctx, responseImpl);
				} else {
					ctx.write(setResponse(responseImpl));
				}
				ctx.flush();
				ctx.close();
				// release object
				ReferenceCountUtil.release(msg);
				channel.close();
				return;
			}
			// post method request
			Map<String, String> postParameter = null;
			boolean parse = true;
			if(!isMFD){
				try {
					postParameter = requestImpl.parseParameter(postContent);
				} catch (HttpUrlErrorException e) {
					// TODO: handle exception
					parse = false;
				}
			}else{
				postParameter = this.mfdParameter;
				parse = mdfError == 0;
			}
			
			// request body parse error
			if (!parse) {
				HttpResponseStatus es = HttpResponseStatus.BAD_REQUEST;
				String errorMessage = "400,bad request...";
				if(mdfError == 1){
					es = HttpResponseStatus.SEE_OTHER;
					errorMessage = "303,file create busy,try agin.";
				}
				if(mdfError == 2){
					es = HttpResponseStatus.SEE_OTHER;
					errorMessage = "303,file size over max "+HapiHttpContextFactory.getInstance().getMaxLength().intValue();
				}
				
				ctx.write(setResponse(
						new DefaultHapiHttpResponseImpl(es, errorMessage)));
				ctx.flush();
				ctx.close();
				// release object
				ReferenceCountUtil.release(msg);
				channel.close();
				return;
			}
			// add request body to parameters
			requestImpl.addParameters(postParameter);
			// initialization response instance
			DefaultHapiHttpResponseImpl responseImpl = new DefaultHapiHttpResponseImpl();
			handler.onPost(requestImpl, responseImpl);
			ctx.write(setResponse(responseImpl));
			ctx.flush();
			ctx.close();
			// release object
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

	private String getFileName(String suffix){
		StringBuilder sb = new StringBuilder();
		sb.append(System.currentTimeMillis());
		for(int i = 0; i < 4; i++){
			sb.append(random.nextInt(10));
		}
		if(suffix != null && !suffix.trim().equals("")){
			sb.append(".").append(suffix);
		}
		return sb.toString();
	}
}

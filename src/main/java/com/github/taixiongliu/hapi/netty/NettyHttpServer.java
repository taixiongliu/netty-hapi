package com.github.taixiongliu.hapi.netty;
import java.lang.reflect.Constructor;

import com.github.taixiongliu.hapi.HapiHttpSslContextFactory;
import com.github.taixiongliu.hapi.http.AutoHapiHttpRequestImpl;
import com.github.taixiongliu.hapi.http.BaseHapiHttpRequestImpl;
import com.github.taixiongliu.hapi.ssl.KeystoreEntity;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.ssl.OptionalSslHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.stream.ChunkedWriteHandler;

/**
 * <b>Bind and start to receive request, modify from netty example demo</b>
 * @author taixiong.liu
 * 
 */
public class NettyHttpServer{

    private int port;
    private HttpRequestHandler handler;
    private Class<? extends BaseHapiHttpRequestImpl> clazz;
    private boolean isSSL;
    private KeystoreEntity entity;
    private String uploadPath;
    //default max receive data one time. 
    private int maxReceive = Integer.MAX_VALUE;

    public NettyHttpServer(int port, HttpRequestHandler handler, String uploadPath){
    	this(port, handler, uploadPath, null);
    }
    public NettyHttpServer(int port, HttpRequestHandler handler, String uploadPath, Class<? extends BaseHapiHttpRequestImpl> clazz) {
        this.port = port;
        this.handler = handler;
        this.clazz = clazz;
        this.isSSL = false;
        this.entity = null;
        this.uploadPath = uploadPath;
    }
    
    /**
     * build https support.
     * @param entity
     * @return self
     */
    public NettyHttpServer buildHttps(KeystoreEntity entity){
    	if(entity == null){
    		return this;
    	}
    	this.isSSL = true;
    	this.entity = entity;
    	
    	return this;
    }
    
    /**
     * set max receive length
     * @param length
     */
    public void setMaxReceiveLength(int length){
    	if(length < 1){
    		return ;
    	}
    	this.maxReceive = length;
    }

    public void run() throws Exception {
    	if(isSSL && entity != null){
    		//must initialization configuration.
   			HapiHttpSslContextFactory.getInstance().init(entity);
    	}
        EventLoopGroup bossGroup = new NioEventLoopGroup(); // (1)
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap(); // (2)
            b.group(bossGroup, workerGroup)
             .channel(NioServerSocketChannel.class) // (3)
             .childHandler(new ChannelInitializer<SocketChannel>() { // (4)
                 @Override
                 public void initChannel(SocketChannel ch) throws Exception {
                	 if(isSSL && entity != null){
                		 //--only protocol https can be support.
                		 //ch.pipeline().addLast("sslHandler", new SslHandler(HapiHttpSslContextFactory.getInstance().createSSLEngine()));
                		 
                		 //supprot protocol https and http.
                		 SslContext sslContext = HapiHttpSslContextFactory.getInstance().createSslContext();
                		 if(sslContext != null){
                			 ch.pipeline().addLast("sslHandler", new OptionalSslHandler(sslContext));
                		 }
                	 }
                	 
                	 // encode HTTP response message
                     ch.pipeline().addLast(new HttpResponseEncoder());
                     // decode HTTP request message
                     ch.pipeline().addLast(new HttpRequestDecoder());
                     // max receive length 4MB
                     ch.pipeline().addLast(new HttpObjectAggregator(NettyHttpServer.this.maxReceive));
                     ch.pipeline().addLast(new ChunkedWriteHandler());
                     
                     BaseHapiHttpRequestImpl base = null;
                     //default parse request with Automatic 
                     if(clazz == null){
                    	 base = new AutoHapiHttpRequestImpl();
                     }else{
                    	 Constructor<? extends BaseHapiHttpRequestImpl> constructor = clazz.getDeclaredConstructor();
                    	 base = constructor.newInstance();
                     }
                     
                     NettyHttpServerHandler hd = new NettyHttpServerHandler(ch, handler, base, uploadPath);
                     ch.pipeline().addLast(hd);
                 }
             })
             .option(ChannelOption.SO_BACKLOG, 128)          // (5)
             .childOption(ChannelOption.SO_KEEPALIVE, false); // (6)

            // Bind and start to accept incoming connections.
            ChannelFuture f = b.bind(port).sync(); // (7)

            System.out.println("netty http server start on port:"+port);
            // Wait until the server socket is closed.
            // In this example, this does not happen, but you can do that to gracefully
            // shut down your server.
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}
/*
 * Copyright 2010 Jeff McHugh (Segue Development LLC)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.seguedevelopment.gwtrpccommlayer.client;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.seguedevelopment.gwtrpccommlayer.shared.GwtRpcCommLayerPojoConstants;
import com.seguedevelopment.gwtrpccommlayer.shared.GwtRpcCommLayerPojoRequest;
import com.seguedevelopment.gwtrpccommlayer.shared.GwtRpcCommLayerPojoResponse;

/**
 * This class handles all the HTTP(S) client interaction with the GwtRpcCommLayer Servlet
 * 
 * @author jeff mchugh
 * @param <T>
 * @param <T>
 *
 */
@SuppressWarnings("deprecation")
public class GwtRpcClientSideProxy implements IGwtRpcClientSideProxy
{	
	
	static int DEFAULT_STANDARD_PORT 	= 80;
	static int DEFAULT_SECURE_PORT 		= 443;
	
	private HashMap<String,Cookie> cookies = null;
	
	private String scheme;
	private String host;
	private Integer port;
	private String path;
	private String queryString;
	private boolean showResponseHeaders = false;

  public GwtRpcClientSideProxy()
	{
		cookies = new HashMap<>();
	}
	
	public GwtRpcClientSideProxy(URL url)
	{
		this();
		setUrl(url);
	}
	
  @SuppressWarnings("rawtypes")
	boolean doesListIncludeGwtAsynchCallback(Class[] interfaces)
	{
		Class target = AsyncCallback.class;
		for (Class intf : interfaces) 
		{
			if ( intf.getName().equals(target.getName()))
			{
				return true;
			}
		}
		return false;
	}
	
	Object invoke_asynchronousMode(Object proxy, Method method, Object[] args) throws Throwable
	{
		if ( args.length == 0 )
		{
			throw new RuntimeException("A minimum of (1) object is required for asynchronous mode");
		}
		
    @SuppressWarnings("rawtypes")
    AsyncCallback theCallback = (AsyncCallback) args[args.length-1];
		
		/*
		 * Wrap the Method (and Method arguments)
		 */
		GwtRpcCommLayerPojoRequest pojoRequest = new GwtRpcCommLayerPojoRequest();
		
		pojoRequest.setMethodName(method.getName());
		if ( args != null )
		{
			
			for (Object object : args)
			{
				if ( object != theCallback )//the last object is always going to be the callback implementation
				{
					pojoRequest.getMethodParameters().add( (Serializable) object);
				}
			}
		}
		
		/*
		 * This block now makes it possible to execute in a "asynchronous" mode
		 */
		
		GwtRpcCommLayerPojoResponse pojoResp = null;
		try
		{
			pojoResp = executeRequest(pojoRequest);
			if ( pojoResp != null )
			{
				Object result = pojoResp.getResponseInstance();
	      @SuppressWarnings({ "rawtypes", "unchecked" })
				AsynchCallBackRunnable runnable = new AsynchCallBackRunnable(theCallback, result);
				Thread thread = new Thread(runnable);
				thread.start();
				return null;
			}
			else
			{
				throw new RuntimeException("No valid GwtRpcCommLayerPojoResponse returned. Check for http errors in log.");	
			}
		}
		catch(Throwable caught)
		{
	    @SuppressWarnings({ "rawtypes", "unchecked" })
	    AsynchCallBackRunnable runnable = new AsynchCallBackRunnable(theCallback, caught);
			Thread thread = new Thread(runnable);
			thread.start();
			return null;
		}
	}
	
	Object invoke_synchronousMode(Object proxy, Method method, Object[] args) throws Throwable
	{
		/*
		 * Wrap the Method (and Method arguments)
		 */
		GwtRpcCommLayerPojoRequest pojoRequest = new GwtRpcCommLayerPojoRequest();
		
		pojoRequest.setMethodName(method.getName());
    pojoRequest.setParameterTypes(method.getParameterTypes());
		if ( args != null )
		{
			for (Object object : args)
			{
				pojoRequest.getMethodParameters().add( (Serializable) object);
			}
		}
		
		
		/*
		 * This is the original block of code that executed for the "synchronous" mode
		 */
		GwtRpcCommLayerPojoResponse pojoResp = executeRequest(pojoRequest);
		if ( pojoResp != null )
		{
			return pojoResp.getResponseInstance();
		}
		else
		{
			throw new RuntimeException("No valid GwtRpcCommLayerPojoResponse returned (name="+method.getName()+"). Check for http errors in log.");
		}
		
		
	}
	
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
	{
		int mode = 0;//default to synchronous mode
		
		if ( args != null && args.length > 0 )
		{
			Object last = args[args.length-1];
			if (last != null && doesListIncludeGwtAsynchCallback(last.getClass().getInterfaces()))
			{
				mode = 1;
			}
		}
		
		if ( mode == 0 )
		{
			return invoke_synchronousMode(proxy, method, args);
		}
		else
		{
			return invoke_asynchronousMode(proxy, method, args);
		}
	}
	
	/**
	 * The URL that this client shall interact with
	 */
  @Override
  public void setUrl(URL url)
	{
		setScheme(url.getProtocol());
		setHost(url.getHost());
		setPort(url.getPort());
		setPath(url.getPath());	
		setQueryString(url.getQuery());
	}

	/**
	 * Set this to true for debugging
	 */
  @Override
  public void setShowResponseHeaders(boolean b)
	{
		this.showResponseHeaders = b;
	}
	
	/**
	 * Main method that executes to make call to remote server
	 * this method uses an HTTP POST method and can keep state if needed (using cookies)
	 * @throws IOException
	 */
	protected synchronized GwtRpcCommLayerPojoResponse executeRequest(GwtRpcCommLayerPojoRequest pojoRequest) throws IOException
	{
		/*
		 * Create HTTP CLIENT
		 */
		DefaultHttpClient httpclient = new DefaultHttpClient();
		
		/*
		 * Add Cookies to the request
		 * this enables a state-full set of transactions
		 * to take place. While this is NOT required for the
		 * GwtRpcCommLayer framework, the actually developer might have this
		 * requirement embedded into his/her servlet(s) and thus
		 * this makes to possible to communicate just like
		 * a browser would
		 * 
		 */
		if ( getCookies().size() > 0 )
		{
			for (Cookie cookie : getCookies().values()) 
			{
				httpclient.getCookieStore().addCookie(cookie);
			}
		}
		
		
		/*
		 * SERIALZED THE POJO-REQUEST OBJECT INTO BYTES
		 */
		byte[] pojoByteArray = serializeIntoBytes(pojoRequest);
		long length = pojoByteArray.length;
        ByteArrayInputStream in = new ByteArrayInputStream(pojoByteArray);
        InputStreamEntity reqEntity = new InputStreamEntity(in, length);
        reqEntity.setContentType("binary/octet-stream");
        reqEntity.setChunked(false);

        
		/*
		 * CONSTRUCT THE URL
		 */
		String url = createFullyQualifiedURL();
        
		/*
		 * Create POST instance
		 */
		HttpPost httppost = new HttpPost(url);
		httppost.setEntity(reqEntity);
		
		/*
		 * Add the correct user-agent
		 */
        httppost.addHeader(GwtRpcCommLayerPojoConstants.GWT_RPC_COMM_LAYER_CLIENT_KEY, GwtRpcCommLayerPojoConstants.GWT_RPC_COMM_LAYER_POJO_CLIENT);
        
		/*
		 * Notify any last minute listener
		 */
        onBeforeRequest(httppost);
        HttpResponse response = httpclient.execute(httppost);
        
        /*
         * Provide a call back for timing, error handling, etc
         */
        onAfterRequest(httppost, response);
        
        /*
         * Check for server error
         */
        if ( response.getStatusLine().getStatusCode() >= 400 && response.getStatusLine().getStatusCode() <= 599 )
        {
        	onResponseError(response.getStatusLine().getStatusCode(), response);
          httpclient.close();
        	return null;
        }
        else if (response.getFirstHeader("Content-Type") != null && !response.getFirstHeader("Content-Type").getValue().equals("binary/octet-stream"))
        {
        	onResponseError("Content-Type must be 'binary/octet-stream'");
          httpclient.close();
        	return null;        	
        }
        else
        {
        	/*
        	 * Provide a call-back for examining the response headers
        	 */
        	if ( isShowResponseHeaders() && response.getAllHeaders() != null )
        	{
        		dumpResponseHeaders(response.getAllHeaders());
        	}
        	
        	/*
        	 * Provide a call-back for examining the response cookies
        	 */
        	List<Cookie> cookies = httpclient.getCookieStore().getCookies();
        	if ( cookies != null )
        	{
        		recordCookies(cookies);
        	}
        	
        	
        	HttpEntity resEntity = response.getEntity();
        	byte[] respData = deserializeIntoBytes(resEntity);
        	try
        	{
        		GwtRpcCommLayerPojoResponse pojoResp = createInstanceFromBytes(respData);
        		return pojoResp;
        	}
        	catch(ClassNotFoundException e)
        	{
        		throw new IOException(e);
        	} finally {
            httpclient.close();
        	}
        	
        }
	}
	
	/*
	 * ---------------- METHODS THAT CAN BE EXTENDED BY SUB-CLASSES -------------
	 */
	
	protected String createFullyQualifiedURL()
	{
		/*
		 * Configure the URL and PATH
		 */
		StringBuilder buff = new StringBuilder();
		buff.append(getScheme());
		buff.append("://");
		buff.append(getHost());
		if ( getPort() != null && getPort() != DEFAULT_STANDARD_PORT && getPort() != DEFAULT_SECURE_PORT )
		{
			buff.append(":"+getPort());
		}
		buff.append(getPath());
		if ( getQueryString() != null )
		{
			buff.append("?");
			buff.append(getQueryString());
		}
		return buff.toString();
	}
	
	protected byte[] serializeIntoBytes(GwtRpcCommLayerPojoRequest pojoRequest) throws IOException
	{
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream objOut = new ObjectOutputStream(bos);
        objOut.writeObject(pojoRequest);
        objOut.flush();
        objOut.close();
        
        byte[] all = bos.toByteArray();
        return all;
	}
	
	protected byte[] deserializeIntoBytes(HttpEntity respEntity ) throws IOException
	{
    	byte[] b = new byte[512];
    	ByteArrayOutputStream buff = new ByteArrayOutputStream();
    	InputStream respIn = respEntity.getContent();
    	while(true)
    	{
    		int vv = respIn.read(b);
    		if ( vv == -1 )
    		{
    			break;
    		}
    		buff.write(b, 0, vv);
    	} 
    	return buff.toByteArray();
	}
	
	protected GwtRpcCommLayerPojoResponse createInstanceFromBytes(byte[] data) throws IOException, ClassNotFoundException
	{
    	ObjectInputStream objIn = new ObjectInputStream(new ByteArrayInputStream(data));
    	Object instance = objIn.readObject();
    	GwtRpcCommLayerPojoResponse pojoResp = (GwtRpcCommLayerPojoResponse) instance;
    	
    	return pojoResp;
	}
	
	protected void recordCookies(List<Cookie> cookies)
	{
		 for (Cookie cookie : cookies) 
         {
         	getCookies().put(cookie.getName(), cookie);
 		}
	}
	
	protected void dumpResponseHeaders(Header header[])
	{
    	for (Header header2 : header)
    	{
    		System.out.println("" + header2.getName() + ":" + header2.getValue() );
		} 
	}
	
	/**
	 * Provides a call-back for future functionality. Will get called before the actual HTTP Request is executed
	 * @param httppost
	 */
	protected void onBeforeRequest(HttpPost httppost)
	{
	}
	
	/**
	 * Provides a call-back for future functionality. Will get called after the response is returned
	 * @param httppost
	 * @param response
	 */
	protected void onAfterRequest(HttpPost httppost, HttpResponse response)
	{
	}
	
	/**
	 * Called in the event of an HTTP Response Error (400 through 599)
	 * @param errorCode
	 * @param response
	 */
	protected void onResponseError(int errorCode, HttpResponse response)
	{	
		System.out.println("HTTP_ERROR code=" + errorCode + " " + response.getStatusLine().getReasonPhrase() );
	}
	
	/**
	 * Called in the event of an general error outside of the http protocol
	 * @param errorCode
	 * @param response
	 */
	protected void onResponseError(String error)
	{	
		System.out.println("Response Error=" + error);
	}
	
	




	
	
	
	
	
	/*
	 * ---------------------------- GETTER/SETTER METHODS ---------------------------
	 */
	
	/**
	 * @param cookies the cookies to set
	 */
	public void setCookies(HashMap<String,Cookie> cookies)
	{
		this.cookies = cookies;
	}

	/**
	 * @return the cookies
	 */
	public HashMap<String,Cookie> getCookies()
	{
		return cookies;
	}

	/**
	 * @param scheme the scheme to set
	 */
	public void setScheme(String scheme)
	{
		this.scheme = scheme;
	}

	/**
	 * @return the scheme
	 */
	public String getScheme()
	{
		return scheme;
	}

	/**
	 * @param host the host to set
	 */
	public void setHost(String host)
	{
		this.host = host;
	}

	/**
	 * @return the host
	 */
	public String getHost()
	{
		return host;
	}

	/**
	 * @param port the port to set
	 */
	public void setPort(Integer port)
	{
		this.port = port;
	}

	/**
	 * @return the port
	 */
	public Integer getPort()
	{
		return port;
	}

	/**
	 * @param path the path to set
	 */
	public void setPath(String path)
	{
		this.path = path;
	}

	/**
	 * @return the path
	 */
	public String getPath()
	{
		return path;
	}

	/**
	 * @param queryString the queryString to set
	 */
	public void setQueryString(String queryString)
	{
		this.queryString = queryString;
	}

	/**
	 * @return the queryString
	 */
	public String getQueryString()
	{
		return queryString;
	}

	/**
	 * @return the showResponseHeaders
	 */
	boolean isShowResponseHeaders()
	{
		return showResponseHeaders;
	}


	/*
	 * This class is for use with the asynchronous mode 
	 */
	static class AsynchCallBackRunnable<T> implements Runnable
	{

		AsyncCallback<T> callback;
		Throwable caughtThrowable;
		T result;
		
		public AsynchCallBackRunnable(AsyncCallback<T> callback, T result)
		{
			this.callback = callback;
			this.result = result;
		}
		
		public AsynchCallBackRunnable(AsyncCallback<T> callback, Throwable caughtThrowable)
		{
			this.callback = callback;
			this.caughtThrowable = caughtThrowable;
		}
		
    @Override
    public void run() 
		{
			try
			{
				if ( this.result != null )
				{
					this.callback.onSuccess(this.result);
				}
				else if ( this.caughtThrowable != null )
				{
					this.callback.onFailure(this.caughtThrowable);
				}
				else
				{
					throw new RuntimeException("Both 'success' and 'failure' payload objects are null. This should never occur.");
				}
			}
			catch(Throwable caught)
			{
				System.err.println("Failure in Asynch dispatch thread. " + caught.toString() );
				caught.printStackTrace();
			}
		}
		
	}




}

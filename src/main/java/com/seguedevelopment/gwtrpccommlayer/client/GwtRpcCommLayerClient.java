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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.net.URL;

/**
 * This class is the main client-side hook for all RPC interactions.
 * It hides the proxy class slightly and makes it easy for the developer
 * to quickly initiate a RPC class
 * @author jeff mchugh
 *
 */
public class GwtRpcCommLayerClient
{
	/**
	 * This is the default class used unless a runtime directive changes it (which is perfectly fine)
	 */
	public static String DEFAULT_RPC_CLIENT_SIDE_PROXY_IMPL_CLASS = "com.seguedevelopment.gwtrpccommlayer.client.GwtRpcClientSideProxy";
	
	private URL url = null;
	private String gwtRpcClientSizeProxyImplClass = null;
	
	public GwtRpcCommLayerClient(URL url)
	{
		setUrl(url);
		setGwtRpcClientSizeProxyImplClass(DEFAULT_RPC_CLIENT_SIDE_PROXY_IMPL_CLASS);
	}
	
	/**
	 * This method is the main method through which a GWT interface shall be "wrapped" into a working pojo-proxy-client object.
	 * 
	 * @param serviceInterface (this class is your basic interface class which the developer should have already defined according the GWT-RPC specification 
	 * @return a PojoProxy class that uses standard Java Object serialization instead of GWT-RPC servialization
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws SecurityException 
	 * @throws NoSuchMethodException 
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 */
	public Object createRemoteServicePojoProxy(Class<?> serviceInterface) throws ClassNotFoundException, IllegalAccessException, InstantiationException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException 
	{
		InvocationHandler handler = createGwtRpcClientSideProxy();
		Object proxy = Proxy.newProxyInstance(serviceInterface.getClassLoader(), new Class[] { serviceInterface }, handler);
		
		return proxy;
	}
	
	
	/**
	 * Method called internally. Can be overridden for future functionality
	 * @return
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws SecurityException 
	 * @throws NoSuchMethodException 
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 */
	protected IGwtRpcClientSideProxy createGwtRpcClientSideProxy() throws ClassNotFoundException, IllegalAccessException, InstantiationException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException
	{
		Class<?> clazz = Class.forName(getGwtRpcClientSizeProxyImplClass());
		IGwtRpcClientSideProxy proxy = (IGwtRpcClientSideProxy) clazz.getConstructor().newInstance();
		proxy.setUrl(getUrl());
		proxy.setShowResponseHeaders(false);
		
		return proxy;
	}
	


	/**
	 * @param url the url to set
	 */
	private void setUrl(URL url)
	{
		this.url = url;
	}

	/**
	 * @return the url
	 */
	public URL getUrl()
	{
		return url;
	}

	/**
	 * @param gwtRpcClientSizeProxyImplClass the gwtRpcClientSizeProxyImplClass to set
	 */
	public void setGwtRpcClientSizeProxyImplClass(String gwtRpcClientSizeProxyImplClass)
	{
		this.gwtRpcClientSizeProxyImplClass = gwtRpcClientSizeProxyImplClass;
	}

	/**
	 * @return the gwtRpcClientSizeProxyImplClass
	 */
	public String getGwtRpcClientSizeProxyImplClass()
	{
		return gwtRpcClientSizeProxyImplClass;
	}


}

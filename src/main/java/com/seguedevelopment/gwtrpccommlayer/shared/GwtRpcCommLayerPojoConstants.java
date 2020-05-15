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
package com.seguedevelopment.gwtrpccommlayer.shared;

/**
 * Some basic constants that are found within this API
 * @author jeff mchugh	
 *
 */
public class GwtRpcCommLayerPojoConstants
{
	/*
	 * Used by GwtRpcExtServlet to identify the client (and expectation of the servlet request)
	 */
	public static final String GWT_RPC_COMM_LAYER_CLIENT_KEY = "GwtRpcCommLayerClient-UserAgent";
	
	/*
	 * The name of the client (passed in header)
	 */
	public static final String GWT_RPC_COMM_LAYER_POJO_CLIENT = "GwtRpcCommLayerPojoClient";
	

	/*
	 * The name of the servlet implementation class (if required)
	 */
	public static final String GWT_RPC_COMM_LAYER_SERVLET_IMPL_CLASS = "GwtRpcCommLayerServletImplClass";
	
}

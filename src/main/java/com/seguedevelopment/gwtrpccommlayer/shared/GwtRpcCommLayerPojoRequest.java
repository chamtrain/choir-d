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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Abstracts the Request made from the GwtRpcCommLayer-Client
 * @author jeff mchugh
 *
 */
public class GwtRpcCommLayerPojoRequest implements Serializable
{
	/**
	 * 
	 */
	private static final long	serialVersionUID	= 6032112000081674584L;

	private ArrayList<Serializable> methodParameters 	= null;
	private String methodName							= null;
	private HashMap<String,String> metaData 			= null;
  private Class<?>[] parameterTypes;
	
	public GwtRpcCommLayerPojoRequest()
	{
		setMethodParameters( new ArrayList<Serializable>() );
		setMetaData(new HashMap<String, String>());
	}

	/**
	 * @param methodParameters the methodParameters to set
	 */
	public void setMethodParameters(ArrayList<Serializable> methodParameters)
	{
		this.methodParameters = methodParameters;
	}

	/**
	 * @return the methodParameters
	 */
	public ArrayList<Serializable> getMethodParameters()
	{
		return methodParameters;
	}

	/**
	 * @param methodName the methodName to set
	 */
	public void setMethodName(String methodName)
	{
		this.methodName = methodName;
	}

	/**
	 * @return the methodName
	 */
	public String getMethodName()
	{
		return methodName;
	}

	/**
	 * @param metaData the metaData to set
	 */
	public void setMetaData(HashMap<String,String> metaData)
	{
		this.metaData = metaData;
	}

	/**
	 * @return the metaData
	 */
	public HashMap<String,String> getMetaData()
	{
		return metaData;
	}

  public Class<?>[] getParameterTypes() {
    return parameterTypes;
  }

  public void setParameterTypes(Class<?>[] parameterTypes) {
    this.parameterTypes = parameterTypes;
  }
}

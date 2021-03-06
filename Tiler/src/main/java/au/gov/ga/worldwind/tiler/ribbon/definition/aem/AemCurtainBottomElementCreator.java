/*******************************************************************************
 * Copyright 2012 Geoscience Australia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package au.gov.ga.worldwind.tiler.ribbon.definition.aem;

import java.io.BufferedReader;
import java.io.FileReader;

import au.gov.ga.worldwind.tiler.ribbon.RibbonTilingContext;

public class AemCurtainBottomElementCreator extends AemElementCreatorBase
{

	@Override
	public String getElementName()
	{
		return "CurtainBottom";
	}

	@Override
	public String getElementString(int level, RibbonTilingContext context)
	{
		return formatLine(level, "<CurtainBottom>" + getCurtainTop(context) + "</CurtainBottom>");
	}

	private String getCurtainTop(RibbonTilingContext context)
	{
		FileReader dataFileReader = getDataFileReader(context);
		if (dataFileReader == null)
		{
			return "";
		}
		try
		{
			BufferedReader bufferedReader = new BufferedReader(dataFileReader);
			bufferedReader.readLine();
			
			String[] dataLine = bufferedReader.readLine().split("\t");
			return dataLine[3];
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return "";
		}
	}
}

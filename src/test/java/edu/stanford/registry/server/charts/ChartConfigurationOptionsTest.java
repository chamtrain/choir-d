/*
 * Copyright 2013 The Board of Trustees of The Leland Stanford Junior University.
 * All Rights Reserved.
 *
 * See the NOTICE and LICENSE files distributed with this work for information
 * regarding copyright ownership and licensing. You may not use this file except
 * in compliance with a written license agreement with Stanford University.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See your
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package edu.stanford.registry.server.charts;

import edu.stanford.registry.shared.ConfigurationOptions;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import java.awt.*;

@RunWith(MockitoJUnitRunner.class)
public class ChartConfigurationOptionsTest {
    @InjectMocks
    private ChartConfigurationOptions classToTest;
    //private HashMap<Integer, Integer[]> colors = new HashMap<>();
    @Mock
    private ConfigurationOptions opts;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void givenNoColorOptionExpectWhite() throws Exception {
        Mockito.when(opts.getStringOption(ConfigurationOptions.OPTION_CHART_FONT)).thenReturn("Helvetica");
        classToTest = new ChartConfigurationOptions(opts);
        Assert.assertArrayEquals(ConfigurationOptions.WHITE, classToTest.getColorOption(null));
    }

    @Test
    public void givenHighColorIndexExpectWhite() throws Exception {
        Mockito.when(opts.getStringOption(ConfigurationOptions.OPTION_CHART_FONT)).thenReturn("Helvetica");
        classToTest = new ChartConfigurationOptions(opts);
        Assert.assertEquals(Color.WHITE, ChartConfigurationOptions.getColor(20));
    }

    @Test
    public void givenInRangeColorIndexExpectColor() throws Exception {
        Mockito.when(opts.getStringOption(ConfigurationOptions.OPTION_CHART_FONT)).thenReturn("Helvetica");
        classToTest = new ChartConfigurationOptions(opts);
        Assert.assertEquals(new Color(255, 255, 255, 255), ChartConfigurationOptions.getColor(0));
        Assert.assertEquals(new Color(242, 241, 235, 255), ChartConfigurationOptions.getColor(1));
        Assert.assertEquals(new Color(233, 230, 223, 255), ChartConfigurationOptions.getColor(2));
        Assert.assertEquals(new Color(227, 223, 213, 255), ChartConfigurationOptions.getColor(3));
        Assert.assertEquals(new Color(213, 208, 192, 255), ChartConfigurationOptions.getColor(4));
        Assert.assertEquals(new Color(172, 166, 141, 255), ChartConfigurationOptions.getColor(5));
        Assert.assertEquals(new Color(138, 136, 125, 255), ChartConfigurationOptions.getColor(6));
        Assert.assertEquals(new Color(86, 83, 71, 255), ChartConfigurationOptions.getColor(7));
        Assert.assertEquals(new Color(63, 60, 48, 255), ChartConfigurationOptions.getColor(8));
        Assert.assertEquals(new Color(88, 87, 84, 255), ChartConfigurationOptions.getColor(9));
        Assert.assertEquals(new Color(248, 246, 234, 255), ChartConfigurationOptions.getColor(10));
        Assert.assertEquals(new Color(246, 243, 229, 255), ChartConfigurationOptions.getColor(11));
        Assert.assertEquals(new Color(243, 239, 216, 255), ChartConfigurationOptions.getColor(12));
        Assert.assertEquals(new Color(238, 230, 203, 255), ChartConfigurationOptions.getColor(13));
        Assert.assertEquals(new Color(230, 228, 219, 255), ChartConfigurationOptions.getColor(14));
        Assert.assertEquals(new Color(196, 191, 169, 255), ChartConfigurationOptions.getColor(15));
        Assert.assertEquals(new Color(157, 149, 115, 255), ChartConfigurationOptions.getColor(16));
        Assert.assertEquals(new Color(140, 21, 21, 255), ChartConfigurationOptions.getColor(17));
        Assert.assertEquals(new Color(0, 0, 0, 255), ChartConfigurationOptions.getColor(18));
    }

    @Test
    public void givenValidIntArrayExpectColor() throws Exception {
        Integer[] rgb = {230, 228, 219};
        Assert.assertEquals(new Color(230, 228, 219, 255), ChartConfigurationOptions.getColor(rgb, 255));
    }
}
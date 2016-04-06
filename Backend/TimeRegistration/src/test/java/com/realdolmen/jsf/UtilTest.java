package com.realdolmen.jsf;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import java.util.*;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UtilTest {

    @Mock
    private FacesContext facesContext;

    @InjectMocks
    private Util util = new Util();

    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void testGetOrDefaultReturnsValueAtIndexIfPresent() throws Exception {
        List<String> values = Arrays.asList("list", "of", "strings");
        Assert.assertEquals("of", util.getOrDefault(values, 1, "default"));
    }

    @Test
    public void testGetOrDefaultReturnsDefaultValueIfNotPresent() throws Exception {
        List<String> values = Arrays.asList("list", "of", "strings");
        Assert.assertEquals("default", util.getOrDefault(values, 3, "default"));
    }

    @Test
    public void testGetMessageForClientReturnsEmptyStringIfEmpty() throws Exception {
        String clientId = "clientId";
        when(facesContext.getMessageList(clientId)).thenReturn(new ArrayList<>());
        Assert.assertEquals("", util.getMessageForClient(clientId));
    }

    @Test
    public void testGetMessageForClientReturnsStringIfNotEmpty() throws Exception {
        String clientId = "clientId";
        when(facesContext.getMessageList(clientId)).thenReturn(Arrays.asList(new FacesMessage("Summary", "Detail")));
        Assert.assertEquals("Summary", util.getMessageForClient(clientId));
    }

    @Test
    public void testSetToListReturnsSetAsListIfNotNull() throws Exception {
        Set<String> set = new HashSet<>(Arrays.asList("set", "of", "values"));
        List<String> list = util.setToList(set);
        Assert.assertEquals(list.size(), set.size());
        set.forEach(t -> Assert.assertTrue("value should be present in the list", list.contains(t)));
        list.forEach(t -> Assert.assertTrue("value should be present in the set", set.contains(t)));
    }

    @Test
    public void testSetToListReturnsEmptyListIfNull() throws Exception {
        Assert.assertEquals(new ArrayList<>(), util.setToList(null));
    }
}
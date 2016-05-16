package com.realdolmen.timeregistration.service.location.beacon;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;

import com.realdolmen.timeregistration.RC;
import com.realdolmen.timeregistration.model.BeaconAction;
import com.realdolmen.timeregistration.service.repository.BeaconRepository;

import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.Region;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class BeaconDwellServiceTest {

	@Mock private Handler timer;
	@Mock private BeaconEventHandler eventHandler;
	@Mock private BeaconManager beaconManager;
	@Mock private BeaconListener beaconListener;
	@Mock private BeaconRepository beaconRepository;
	@Mock private Context context;

	@InjectMocks private BeaconDwellService dwellService;

	@Before
	public void setUp() throws Exception {
		dwellService = new BeaconDwellService();
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testRunKeepsRepeatingWithConstantDelay() throws Exception {
		dwellService.run();
		verify(timer).postDelayed(dwellService, RC.beacon.PROCESS_DELAY);
	}

	@Test
	public void testRunDelegatesToEventHandler() throws Exception {
		dwellService.run();
		verify(eventHandler).process();
	}

	@Test
	public void testBeaconServiceConnectSetsListenerAsMonitorAndRangeNotifier() throws Exception {
		dwellService.onBeaconServiceConnect();
		verify(beaconManager).setMonitorNotifier(beaconListener);
		verify(beaconManager).setRangeNotifier(beaconListener);
	}

	@Test
	public void testBeaconServiceConnectStartsRangeAndRegularMonitorsForEachRegion() throws Exception {
		dwellService.testing.setBeaconRepository(beaconRepository);
		HashMap<Region, BeaconAction> regionMap = mock(HashMap.class);
		List<Region> regions = Arrays.asList(mock(Region.class), mock(Region.class), mock(Region.class), mock(Region.class));
		when(beaconRepository.getRegionMap()).thenReturn(regionMap);
		when(regionMap.keySet()).thenReturn(new HashSet<>(regions));

		dwellService.onBeaconServiceConnect();
		for (Region region : regions) {
			verify(beaconManager).startRangingBeaconsInRegion(region);
			verify(beaconManager).startMonitoringBeaconsInRegion(region);
		}
	}

	@Test(expected = IllegalStateException.class)
	public void testOnBeaconServiceConnectThrowsIllegalStateExceptionWhenBeaconManagerIsNotInitialized() throws Exception {
		dwellService.testing.setBeaconManager(null);
		dwellService.onBeaconServiceConnect();
	}

	@Test(expected = IllegalStateException.class)
	public void testOnBeaconServiceConnectThrowsIllegalStateExceptionWhenListenerIsNotInitialized() throws Exception {
		dwellService.testing.setListener(null);
		dwellService.onBeaconServiceConnect();
	}

	@SuppressLint("Assert")
	@Test
	public void testOnCreateOnlyCreatesPowerSavingWhenEnabled() throws Exception {
		dwellService.testing.setInitialized(true); //to prevent initialization
		when(dwellService.getApplicationContext()).thenReturn(context);
		if(RC.beacon.SAVE_POWER) {
			dwellService.onCreate();
			assert dwellService.testing.isPowersaving();
		}
	}
}
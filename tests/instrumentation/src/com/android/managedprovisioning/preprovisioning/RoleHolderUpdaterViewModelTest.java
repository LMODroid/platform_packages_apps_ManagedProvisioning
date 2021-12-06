/*
 * Copyright (C) 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.managedprovisioning.preprovisioning;

import static com.android.managedprovisioning.preprovisioning.RoleHolderUpdaterViewModel.LaunchRoleHolderUpdaterFailureEvent.REASON_EXCEEDED_MAXIMUM_NUMBER_UPDATER_LAUNCH_RETRIES;
import static com.android.managedprovisioning.preprovisioning.RoleHolderUpdaterViewModel.LaunchRoleHolderUpdaterFailureEvent.REASON_EXCEEDED_MAXIMUM_NUMBER_UPDATE_RETRIES;

import static com.google.common.truth.Truth.assertThat;

import android.app.Application;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.filters.SmallTest;
import androidx.test.platform.app.InstrumentationRegistry;

import com.android.bedstead.nene.utils.Poll;
import com.android.managedprovisioning.common.DefaultPackageInstallChecker;
import com.android.managedprovisioning.common.DeviceManagementRoleHolderUpdaterHelper;
import com.android.managedprovisioning.common.RoleHolderUpdaterProvider;
import com.android.managedprovisioning.common.Utils;
import com.android.managedprovisioning.common.ViewModelEvent;
import com.android.managedprovisioning.preprovisioning.RoleHolderUpdaterViewModel.LaunchRoleHolderUpdaterEvent;
import com.android.managedprovisioning.preprovisioning.RoleHolderUpdaterViewModel.LaunchRoleHolderUpdaterFailureEvent;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.time.Duration;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


@SmallTest
@RunWith(JUnit4.class)
public class RoleHolderUpdaterViewModelTest {
    private static final int LAUNCH_ROLE_HOLDER_UPDATER_PERIOD_MILLIS = 100;
    private static final int NO_EVENT_TIMEOUT_MILLIS = 200;
    private static final int LAUNCH_ROLE_HOLDER_MAX_RETRIES = 1;
    private static final int ROLE_HOLDER_UPDATE_MAX_RETRIES = 1;
    private static final LaunchRoleHolderUpdaterEvent
            LAUNCH_ROLE_HOLDER_UPDATER_EVENT = createLaunchRoleHolderUpdaterEvent();
    private static final LaunchRoleHolderUpdaterFailureEvent
            EXCEED_MAX_NUMBER_LAUNCH_RETRIES_EVENT = createExceedMaxNumberLaunchRetriesEvent();
    private static final LaunchRoleHolderUpdaterFailureEvent
            EXCEED_MAX_NUMBER_UPDATE_RETRIES_EVENT = createExceedMaxNumberUpdateRetriesEvent();

    private final Context mApplicationContext = ApplicationProvider.getApplicationContext();
    private Handler mHandler;
    private TestConfig mTestConfig;
    private boolean mCanLaunchRoleHolderUpdater = true;
    private RoleHolderUpdaterViewModel mViewModel;
    private Set<ViewModelEvent> mEvents;
    private Utils mUtils = new Utils();

    @Before
    public void setUp() {
        mTestConfig = new TestConfig(
                LAUNCH_ROLE_HOLDER_UPDATER_PERIOD_MILLIS,
                LAUNCH_ROLE_HOLDER_MAX_RETRIES,
                ROLE_HOLDER_UPDATE_MAX_RETRIES);
        mCanLaunchRoleHolderUpdater = true;
        InstrumentationRegistry.getInstrumentation().runOnMainSync(
                () -> mHandler = new Handler(Looper.myLooper()));
        mViewModel = createViewModel();
        mEvents = subscribeToViewModelEvents();
    }

    @Test
    public void tryStartRoleHolderUpdater_launchUpdater_works() {
        mViewModel.tryStartRoleHolderUpdater();
        blockUntilNextUiThreadCycle();

        assertThat(mEvents).containsExactly(LAUNCH_ROLE_HOLDER_UPDATER_EVENT);
    }

    @Test
    public void tryStartRoleHolderUpdater_exceedsMaxRetryLimit_fails() {
        mTestConfig.launchRoleHolderMaxRetries = 1;

        mViewModel.tryStartRoleHolderUpdater();
        blockUntilNextUiThreadCycle();
        mViewModel.tryStartRoleHolderUpdater();
        blockUntilNextUiThreadCycle();

        assertThat(mEvents)
                .containsExactly(
                        LAUNCH_ROLE_HOLDER_UPDATER_EVENT,
                        EXCEED_MAX_NUMBER_UPDATE_RETRIES_EVENT);
    }

    @Test
    public void tryStartRoleHolderUpdater_rescheduleLaunchUpdater_works() {
        mTestConfig.launchRoleHolderMaxRetries = 2;
        mCanLaunchRoleHolderUpdater = false;

        mViewModel.tryStartRoleHolderUpdater();
        mCanLaunchRoleHolderUpdater = true;

        pollForEvent(mEvents, LAUNCH_ROLE_HOLDER_UPDATER_EVENT);
    }

    @Test
    public void tryStartRoleHolderUpdater_rescheduleLaunchUpdater_exceedsMaxRetryLimit_fails() {
        mTestConfig.roleHolderUpdateMaxRetries = 1;
        mCanLaunchRoleHolderUpdater = false;

        mViewModel.tryStartRoleHolderUpdater();

        pollForEvent(mEvents, EXCEED_MAX_NUMBER_LAUNCH_RETRIES_EVENT);
    }

    @Test
    public void stopLaunchRetries_works() {
        mTestConfig.roleHolderUpdateMaxRetries = 1;
        mCanLaunchRoleHolderUpdater = false;

        mViewModel.tryStartRoleHolderUpdater();
        mViewModel.stopLaunchRetries();

        pollForNoEvent(mEvents);
    }

    private void pollForEvent(
            Set<ViewModelEvent> capturedViewModelEvents, ViewModelEvent viewModelEvent) {
        Poll.forValue("CapturedViewModelEvents", () -> capturedViewModelEvents)
                .toMeet(viewModelEvents -> viewModelEvents.size() == 1
                                && viewModelEvents.contains(viewModelEvent))
                .errorOnFail("Expected CapturedViewModelEvents to contain only " + viewModelEvent)
                .await();
    }

    private void pollForNoEvent(Set<ViewModelEvent> capturedViewModelEvents) {
        // TODO(b/208237942): A pattern for testing that something does not happen
        assertThat(Poll.forValue("CapturedViewModelEvents", () -> capturedViewModelEvents)
                .toMeet(viewModelEvents -> !viewModelEvents.isEmpty())
                .timeout(Duration.ofMillis(NO_EVENT_TIMEOUT_MILLIS))
                .await())
                .isEqualTo(Collections.emptySet());
    }

    private static LaunchRoleHolderUpdaterFailureEvent createExceedMaxNumberUpdateRetriesEvent() {
        return new LaunchRoleHolderUpdaterFailureEvent(
                REASON_EXCEEDED_MAXIMUM_NUMBER_UPDATE_RETRIES);
    }

    private static LaunchRoleHolderUpdaterFailureEvent createExceedMaxNumberLaunchRetriesEvent() {
        return new LaunchRoleHolderUpdaterFailureEvent(
                REASON_EXCEEDED_MAXIMUM_NUMBER_UPDATER_LAUNCH_RETRIES);
    }

    private static LaunchRoleHolderUpdaterEvent createLaunchRoleHolderUpdaterEvent() {
        Intent intent = new Intent(DevicePolicyManager.ACTION_UPDATE_DEVICE_MANAGEMENT_ROLE_HOLDER);
        return new LaunchRoleHolderUpdaterEvent(intent);
    }

    private Set<ViewModelEvent> subscribeToViewModelEvents() {
        Set<ViewModelEvent> capturedViewModelEvents =
                Collections.newSetFromMap(new ConcurrentHashMap<>());
        InstrumentationRegistry.getInstrumentation().runOnMainSync(
                () -> mViewModel.observeViewModelEvents()
                        .observeForever(capturedViewModelEvents::add));
        return capturedViewModelEvents;
    }

    private void blockUntilNextUiThreadCycle() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {});
    }

    private RoleHolderUpdaterViewModel createViewModel() {
        return new RoleHolderUpdaterViewModel(
                (Application) mApplicationContext,
                mHandler,
                (context, intent) -> mCanLaunchRoleHolderUpdater,
                mTestConfig,
                new DeviceManagementRoleHolderUpdaterHelper(
                        RoleHolderUpdaterProvider.DEFAULT.getPackageName(mApplicationContext),
                        new DefaultPackageInstallChecker(mUtils)));
    }

    private static final class TestConfig implements RoleHolderUpdaterViewModel.Config {
        public int launchRoleHolderUpdaterPeriodMillis;
        public int launchRoleHolderMaxRetries;
        public int roleHolderUpdateMaxRetries;

        TestConfig(
                int launchRoleHolderUpdaterPeriodMillis,
                int launchRoleHolderMaxRetries,
                int roleHolderUpdateMaxRetries) {
            this.launchRoleHolderUpdaterPeriodMillis = launchRoleHolderUpdaterPeriodMillis;
            this.launchRoleHolderMaxRetries = launchRoleHolderMaxRetries;
            this.roleHolderUpdateMaxRetries = roleHolderUpdateMaxRetries;
        }

        @Override
        public int getLaunchRoleHolderUpdaterPeriodMillis() {
            return launchRoleHolderUpdaterPeriodMillis;
        }

        @Override
        public int getLaunchRoleHolderMaxRetries() {
            return launchRoleHolderMaxRetries;
        }

        @Override
        public int getRoleHolderUpdateMaxRetries() {
            return roleHolderUpdateMaxRetries;
        }
    }
}

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

package com.android.managedprovisioning.finalization;

import static com.google.common.truth.Truth.assertThat;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;

import androidx.test.InstrumentationRegistry;
import androidx.test.filters.SmallTest;

import com.android.managedprovisioning.analytics.DeferredMetricsReader;
import com.android.managedprovisioning.common.NotificationHelper;
import com.android.managedprovisioning.common.SettingsFacade;
import com.android.managedprovisioning.common.Utils;
import com.android.managedprovisioning.model.ProvisioningParams;
import com.android.managedprovisioning.provisioning.Constants;

import org.junit.After;
import org.junit.Before;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;


@SmallTest
public class FinalizationControllerTest {
    private static final String DEVICE_ADMIN_PACKAGE_NAME = "com.example.package";
    private static final ComponentName DEVICE_ADMIN_COMPONENT_NAME =
            new ComponentName(DEVICE_ADMIN_PACKAGE_NAME, "com.android.AdminReceiver");

    private final Context mContext = InstrumentationRegistry.getTargetContext();
    private FinalizationController mFinalizationController;
    private File mTempFile;

    @Before
    public void setUp() throws IOException {
        mTempFile = createTempFile();
        mFinalizationController = createFinalizationController(mTempFile);
    }

    @After
    public void tearDown() {
        mTempFile.delete();
    }

    public void shouldKeepScreenOn_setTrue_works() {
        ProvisioningParams params = createProvisioningParamsBuilder()
                .setKeepScreenOn(true)
                .build();

        params.save(mTempFile);

        assertThat(mFinalizationController.shouldKeepScreenOn()).isTrue();
    }

    public void shouldKeepScreenOn_setFalse_works() {
        ProvisioningParams params = createProvisioningParamsBuilder()
                .setKeepScreenOn(false)
                .build();

        params.save(mTempFile);

        assertThat(mFinalizationController.shouldKeepScreenOn()).isFalse();
    }

    public void shouldKeepScreenOn_notSet_works() {
        ProvisioningParams params = createProvisioningParamsBuilder()
                .build();

        params.save(mTempFile);

        assertThat(mFinalizationController.shouldKeepScreenOn()).isFalse();
    }

    private ProvisioningParams.Builder createProvisioningParamsBuilder() {
        return new ProvisioningParams.Builder()
                .setDeviceAdminComponentName(DEVICE_ADMIN_COMPONENT_NAME)
                .setProvisioningAction(DevicePolicyManager.ACTION_PROVISION_MANAGED_PROFILE);
    }

    private FinalizationController createFinalizationController(File provisioningParamsFile) {

        return new FinalizationController(
                createActivity(),
                new TestFinalizationControllerLogic(),
                new Utils(),
                new SettingsFacade(),
                new UserProvisioningStateHelper(mContext),
                new NotificationHelper(mContext),
                new DeferredMetricsReader(Constants.getDeferredMetricsFile(mContext)),
                new ProvisioningParamsUtils(context -> provisioningParamsFile));
    }

    private Activity createActivity() {
        return new Activity();
    }

    private File createTempFile() throws IOException {
        return Files.createTempFile("testPrefix", "testSuffix").toFile();
    }

    private class TestFinalizationControllerLogic implements FinalizationControllerLogic {

        @Override
        public int notifyDpcManagedProfile(ProvisioningParams params, int requestCode) {
            return 0;
        }

        @Override
        public int notifyDpcManagedDeviceOrUser(ProvisioningParams params, int requestCode) {
            return 0;
        }

        @Override
        public boolean shouldFinalizePrimaryProfile(ProvisioningParams params) {
            return false;
        }

        @Override
        public void saveInstanceState(Bundle outState) {

        }

        @Override
        public void restoreInstanceState(Bundle savedInstanceState, ProvisioningParams params) {

        }

        @Override
        public void activityDestroyed(boolean isFinishing) {

        }
    }
}

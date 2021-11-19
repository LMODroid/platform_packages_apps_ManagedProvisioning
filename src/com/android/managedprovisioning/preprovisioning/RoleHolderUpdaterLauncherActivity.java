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

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.android.managedprovisioning.R;
import com.android.managedprovisioning.common.DefaultPackageInstallChecker;
import com.android.managedprovisioning.common.DeviceManagementRoleHolderUpdaterHelper;
import com.android.managedprovisioning.common.ProvisionLogger;
import com.android.managedprovisioning.common.RoleHolderUpdaterProvider;
import com.android.managedprovisioning.common.SetupGlifLayoutActivity;
import com.android.managedprovisioning.preprovisioning.RoleHolderUpdaterViewModel.LaunchRoleHolderUpdaterEvent;
import com.android.managedprovisioning.preprovisioning.RoleHolderUpdaterViewModel.RoleHolderUpdaterViewModelFactory;

/**
 * An {@link android.app.Activity} which handles the launch of the device management role holder
 * updater.
 *
 * Upon successful update of the device management role holder, {@link #RESULT_OK} is returned,
 * otherwise the result is {@link #RESULT_CANCELED}.
 *
 * If the device management role holder updater is not currently available (for example, if it is
 * getting updated at this time), there is retry logic in place. If none of the retries works,
 * {@link #RESULT_CANCELED} is returned.
 */
public final class RoleHolderUpdaterLauncherActivity extends SetupGlifLayoutActivity {

    private static final int LAUNCH_ROLE_HOLDER_UPDATER_REQUEST_CODE = 1;

    private RoleHolderUpdaterViewModel mViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel = createViewModel();
        setupViewModelObservation();

        initializeUi();

        if (savedInstanceState == null) {
            mViewModel.tryStartRoleHolderUpdater();
        }
    }

    private void setupViewModelObservation() {
        mViewModel.observeViewModelEvents().observe(this, viewModelEvent -> {
            switch (viewModelEvent.getType()) {
                case RoleHolderUpdaterViewModel.VIEW_MODEL_EVENT_LAUNCH_UPDATER:
                    launchRoleHolderUpdater(
                            ((LaunchRoleHolderUpdaterEvent) viewModelEvent).getIntent());
                    break;
                case RoleHolderUpdaterViewModel.VIEW_MODEL_EVENT_LAUNCH_FAILURE:
                    finishWithCancelResult();
                    break;
            }
        });
    }

    private RoleHolderUpdaterViewModel createViewModel() {
        DeviceManagementRoleHolderUpdaterHelper roleHolderUpdaterHelper =
                new DeviceManagementRoleHolderUpdaterHelper(
                        RoleHolderUpdaterProvider.DEFAULT.getPackageName(this),
                        new DefaultPackageInstallChecker(mUtils));
        return new ViewModelProvider(this,
                new RoleHolderUpdaterViewModelFactory(
                        getApplication(), mUtils, roleHolderUpdaterHelper))
                .get(RoleHolderUpdaterViewModel.class);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mViewModel.stopLaunchRetries();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode != LAUNCH_ROLE_HOLDER_UPDATER_REQUEST_CODE) {
            super.onActivityResult(requestCode, resultCode, data);
            return;
        }
        ProvisionLogger.logi("Device management role holder updater result code: "
                + resultCode);
        if (resultCode == RESULT_OK) {
            finishWithOkResult();
        } else {
            mViewModel.tryStartRoleHolderUpdater();
        }
    }

    private void initializeUi() {
        int headerResId = R.string.downloading_administrator_header;
        int titleResId = R.string.setup_device_progress;
        initializeLayoutParams(R.layout.empty_loading_layout, headerResId);
        setTitle(titleResId);
    }

    private void launchRoleHolderUpdater(Intent intent) {
        getTransitionHelper().startActivityForResultWithTransition(
                this,
                intent,
                LAUNCH_ROLE_HOLDER_UPDATER_REQUEST_CODE);
    }

    private void finishWithResult(int resultCode) {
        setResult(resultCode);
        getTransitionHelper().finishActivity(this);
    }

    private void finishWithOkResult() {
        finishWithResult(RESULT_OK);
    }

    private void finishWithCancelResult() {
        finishWithResult(RESULT_CANCELED);
    }
}

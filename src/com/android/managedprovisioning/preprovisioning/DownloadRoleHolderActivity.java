/*
 * Copyright (C) 2022 The Android Open Source Project
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

import static com.android.managedprovisioning.model.ProvisioningParams.EXTRA_PROVISIONING_PARAMS;

import android.content.Intent;
import android.os.Bundle;

import androidx.lifecycle.ViewModelProvider;

import com.android.managedprovisioning.ManagedProvisioningBaseApplication;
import com.android.managedprovisioning.R;
import com.android.managedprovisioning.common.SetupGlifLayoutActivity;
import com.android.managedprovisioning.model.ProvisioningParams;
import com.android.managedprovisioning.preprovisioning.DownloadRoleHolderViewModel.DownloadRoleHolderViewModelFactory;
import com.android.managedprovisioning.preprovisioning.DownloadRoleHolderViewModel.ErrorWrapper;

/**
 * Spinner which takes care of network connectivity if needed, and downloading of the role holder.
 */
public class DownloadRoleHolderActivity extends SetupGlifLayoutActivity {

    public static final String EXTRA_DIALOG_TITLE_ID = "dialog_title_id";
    public static final String EXTRA_ERROR_MESSAGE_RES_ID =
            "dialog_error_message_res_id";
    public static final String EXTRA_FACTORY_RESET_REQUIRED =
            "factory_reset_required";

    private DownloadRoleHolderViewModel mViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ProvisioningParams params = getIntent().getParcelableExtra(EXTRA_PROVISIONING_PARAMS);
        if (params.roleHolderDownloadInfo == null) {
            setResult(RESULT_CANCELED);
            getTransitionHelper().finishActivity(this);
            return;
        }

        mViewModel = new ViewModelProvider(
                this,
                new DownloadRoleHolderViewModelFactory(
                        (ManagedProvisioningBaseApplication) getApplication(),
                        params,
                        mUtils,
                        mSettingsFacade))
                .get(DownloadRoleHolderViewModel.class);
        mViewModel.observeState().observe(this, this::onStateChanged);
        mViewModel.connectToNetworkAndDownloadRoleHolder(getApplicationContext());
        initializeUi();
    }

    private void onStateChanged(Integer state) {
        switch(state) {
            case DownloadRoleHolderViewModel.STATE_IDLE:
                break;
            case DownloadRoleHolderViewModel.STATE_DOWNLOADING:
                break;
            case DownloadRoleHolderViewModel.STATE_DOWNLOADED:
                setResult(RESULT_OK);
                getTransitionHelper().finishActivity(this);
                break;
            case DownloadRoleHolderViewModel.STATE_ERROR:
                ErrorWrapper error = mViewModel.getError();
                setResult(RESULT_CANCELED, createResultIntent(error));
                getTransitionHelper().finishActivity(this);
                break;
        }
    }

    private Intent createResultIntent(ErrorWrapper error) {
        Intent intent = new Intent();
        if (error.dialogTitleId != 0) {
            intent.putExtra(EXTRA_DIALOG_TITLE_ID, error.dialogTitleId);
        }
        if (error.errorMessageResId != 0) {
            intent.putExtra(EXTRA_ERROR_MESSAGE_RES_ID, error.errorMessageResId);
        }
        intent.putExtra(EXTRA_FACTORY_RESET_REQUIRED, error.factoryResetRequired);
        return intent;
    }

    private void initializeUi() {
        // TODO(b/220175163): figure out proper strings
        final int headerResId = R.string.downloading_administrator_header;
        final int titleResId = R.string.setup_device_progress;
        initializeLayoutParams(R.layout.empty_loading_layout, headerResId);
        setTitle(titleResId);
    }

    @Override
    protected boolean isWaitingScreen() {
        return true;
    }
}

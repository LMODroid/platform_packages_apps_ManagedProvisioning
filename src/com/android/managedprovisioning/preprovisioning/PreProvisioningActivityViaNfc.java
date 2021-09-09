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


import android.app.Activity;
import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.android.managedprovisioning.R;
import com.android.managedprovisioning.common.ProvisionLogger;
import com.android.managedprovisioning.common.SettingsFacade;

//TODO(b/181323689) Create tests for activity
public class PreProvisioningActivityViaNfc extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent provisioningIntent = transformIntentToProvisioningIntent();

        if (provisioningIntent != null && new SettingsFacade().isDuringSetupWizard(this)) {
            //TODO(b/194486707) Decide dialog options available for user
            startActivity(provisioningIntent);
            finish();
        } else {
            ProvisionLogger.loge("NFC tag data is invalid.");
            AlertDialog alertDialog = createAlertDialog();
            alertDialog.show();
        }
    }

    private Intent transformIntentToProvisioningIntent() {
        DevicePolicyManager devicePolicyManager =
                getApplicationContext().getSystemService(DevicePolicyManager.class);
        return devicePolicyManager.createProvisioningIntentFromNfcIntent(getIntent());
    }

    //TODO(b/194486033) Decide appropriate message and title
    private AlertDialog createAlertDialog() {
        return new AlertDialog.Builder(this)
                .setMessage(R.string.if_questions_contact_admin)
                .setTitle(R.string.device_already_set_up)
                .setPositiveButton(android.R.string.ok, createDialogOnClickListener())
                .setCancelable(false)
                .create();
    }

    private DialogInterface.OnClickListener createDialogOnClickListener() {
        return (dialog, id) -> finish();
    }
}

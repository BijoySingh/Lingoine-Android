/*******************************************************************************
 * Copyright 2015 � GENBAND US LLC, All Rights Reserved
 * <p/>
 * This software embodies materials and concepts which are
 * proprietary to GENBAND and/or its licensors and is made
 * available to you for use solely in association with GENBAND
 * products or services which must be obtained under a separate
 * agreement between you and GENBAND or an authorized GENBAND
 * distributor or reseller.
 * <p/>
 * THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER
 * AND/OR ITS LICENSORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * THE WARRANTY AND LIMITATION OF LIABILITY CONTAINED IN THIS
 * AGREEMENT ARE FUNDAMENTAL PARTS OF THE BASIS OF GENBAND�S BARGAIN
 * HEREUNDER, AND YOU ACKNOWLEDGE THAT GENBAND WOULD NOT BE ABLE TO
 * PROVIDE THE PRODUCT TO YOU ABSENT SUCH LIMITATIONS.  IN THOSE
 * STATES AND JURISDICTIONS THAT DO NOT ALLOW CERTAIN LIMITATIONS OF
 * LIABILITY, GENBAND�S LIABILITY SHALL BE LIMITED TO THE GREATEST
 * EXTENT PERMITTED UNDER APPLICABLE LAW.
 * <p/>
 * Restricted Rights legend:
 * Use, duplication, or disclosure by the U.S. Government is
 * subject to restrictions set forth in subdivision (c)(1) of
 * FAR 52.227-19 or in subdivision (c)(1)(ii) of DFAR 252.227-7013.
 *******************************************************************************/
package com.tch.lingoine.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.widget.Toast;

import com.tch.lingoine.R;


/**
 * Handles notification on UI thread from other threads
 *
 */
public class UIUtils {

    private static ProgressDialog mProgressDialog;


    public static void showProgressDialogOnUiThread(final Activity activity, final String message) {
        activity.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                dismissProgressDialog();
                mProgressDialog = ProgressDialog.show(activity, null, message);
            }
        });
    }

    public static ProgressDialog showProgressDialogOnUiThreadWithProgress(final Activity activity, final String message) {
        activity.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                dismissProgressDialog();
                mProgressDialog = new ProgressDialog(activity);
                mProgressDialog.setMessage(message);
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                mProgressDialog.setProgressNumberFormat(null);
                mProgressDialog.show();
            }
        });
        return mProgressDialog;
    }


    public static ProgressDialog showCancelableProgressDialogOnUiThreadWithProgress(final Activity activity, final String message, final OnCancelListener listener) {
        activity.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                dismissProgressDialog();
                mProgressDialog = new ProgressDialog(activity);
                mProgressDialog.setCancelable(true);
                mProgressDialog.setOnCancelListener(listener);
                mProgressDialog.setMessage(message);
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                mProgressDialog.setProgressNumberFormat(null);
                mProgressDialog.show();
            }
        });
        return mProgressDialog;
    }

    public static ProgressDialog showCancelableProgressDialogOnUiThreadWithProgressAndButton(final Activity activity, final String message, final OnCancelListener listener, final DialogInterface.OnClickListener clickListener) {
        activity.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                dismissProgressDialog();
                mProgressDialog = new ProgressDialog(activity);
                mProgressDialog.setCancelable(true);
                mProgressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, activity.getString(R.string.ui_dialog_cancel_button_label), clickListener);
                mProgressDialog.setOnCancelListener(listener);
                mProgressDialog.setMessage(message);
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                mProgressDialog.setProgressNumberFormat(null);
                mProgressDialog.show();
            }
        });
        return mProgressDialog;
    }

    public static void showProgressInDialogOnUiThread(final int progress) {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.setProgress(progress);
        }
    }

    public static void showProgressDialogWithMessage(Context context, String pMessage) {
        dismissProgressDialog();
        mProgressDialog = ProgressDialog.show(context, null, pMessage);
    }

    public static void dismissProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

    /**
     * Show toast with message. Must be called from UI Thread
     * @param context
     * @param pMessage message to be shown
     */
    public static void showToastWithMessage(Context context, String pMessage) {
        dismissProgressDialog();
        Toast.makeText(context, pMessage, Toast.LENGTH_SHORT).show();
    }

    /**
     * Show dialog with message. Must be called from UI Thread
     * @param context
     * @param pErrorMessage message to be shown
     */
    public static void showDialogWithErrorMessage(Context context, String pErrorMessage) {
        dismissProgressDialog();

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Error");
        builder.setMessage(pErrorMessage);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.create().show();
    }

    /**
     * Show titled dialog with message. Must be called from UI Thread
     * @param context
     * @param pTitle title of the dialog
     * @param pMessage message to be shown
     */
    public static void showDialogWithTitledMessage(Context context, String pTitle, String pMessage) {
        dismissProgressDialog();

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(pTitle);
        builder.setMessage(pMessage);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.create().show();
    }

    /**
     * Handles the result of requests on UI thread(Hides the progress dialog, shows the toast/dialog)
     * @param isFail result of request - succeed<tt>(false)</tt>/failed<tt>(true)</tt>
     * @param pMessage response to show
     */
    public static void handleResultOnUiThread(final Activity pActivity, final boolean isFail, final String pMessage) {
        pActivity.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                dismissProgressDialog();

                if (isFail) {
                    showDialogWithErrorMessage(pActivity, pMessage);
                } else {
                    showToastWithMessage(pActivity, pMessage);
                }
            }
        });
    }

    public static void showDialogOnUiThread(final Activity pActivity, final String pTitle, final String pMessage) {
        pActivity.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                dismissProgressDialog();
                showDialogWithTitledMessage(pActivity, pTitle, pMessage);
            }
        });
    }
}

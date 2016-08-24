package org.pptik.bpgrealtimecapture.utilities;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;

/**
 * Created by hynra on 8/24/16.
 */
public class WarningDialog {

    private Context context;
    private boolean closeActivity;

    public WarningDialog(Context _context, boolean _closeActivity){
        context = _context;
        closeActivity = _closeActivity;
    }

    public void showWarning(String msg, String title){
        final android.support.v7.app.AlertDialog.Builder alertDialog = new android.support.v7.app.AlertDialog.Builder(context);
        alertDialog.setTitle(title);
        alertDialog.setCancelable(false);
        alertDialog.setMessage(msg);
        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if(closeActivity){
                    dialog.dismiss();
                    ((Activity)context).finish();
                }else {
                    dialog.dismiss();
                }
            }
        });
        alertDialog.show();
    }
}

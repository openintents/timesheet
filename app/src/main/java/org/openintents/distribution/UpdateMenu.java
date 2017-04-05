package org.openintents.distribution;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.view.Menu;
import android.view.MenuItem;

import org.openintents.timesheet.R;

public class UpdateMenu {
    public static final String UPDATE_CHECKER = "org.openintents.updatechecker";
    private static final String TAG = "UpdateMenu";

    public static MenuItem addUpdateMenu(Context context, Menu menu, int groupId, int itemId, int order, int titleRes) {
        PackageInfo pi = null;
        try {
            pi = context.getPackageManager().getPackageInfo(UPDATE_CHECKER, 0);
        } catch (NameNotFoundException e) {
        }
        if (pi == null) {
            return menu.add(groupId, itemId, order, titleRes).setIcon(android.R.drawable.ic_menu_info_details).setShortcut('9', 'u');
        }
        return null;
    }

    public static void showUpdateBox(Context context) {
        String version = null;
        try {
            version = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        Intent intent = new Intent("android.intent.action.VIEW");
        Intent intent2 = new Intent("android.intent.action.VIEW");
        new Builder(context).setMessage(context.getString(R.string.update_box_text, new Object[]{version}))
                .setPositiveButton(R.string.update_check_now, new C00061(intent, context, intent2))
                .setNegativeButton(R.string.update_get_updater,
                        new C00072(intent, context, intent2)).show();
    }

    /* renamed from: org.openintents.distribution.UpdateMenu.1 */
    public static class C00061 implements OnClickListener {
        private final /* synthetic */ Context val$context;
        private final /* synthetic */ Intent val$intent;
        private final /* synthetic */ Intent val$intent2;

        C00061(Intent intent, Context context, Intent intent2) {
            this.val$intent = intent;
            this.val$context = context;
            this.val$intent2 = intent2;
        }

        public void onClick(DialogInterface arg0, int arg1) {
            this.val$intent.setData(Uri.parse(this.val$context.getString(R.string.update_app_url)));
            this.val$intent2.setData(Uri.parse(this.val$context.getString(R.string.update_app_developer_url)));
            GetFromMarketDialog.startSaveActivity(this.val$context, this.val$intent, this.val$intent2);
        }
    }

    /* renamed from: org.openintents.distribution.UpdateMenu.2 */
    public static class C00072 implements OnClickListener {
        private final /* synthetic */ Context val$context;
        private final /* synthetic */ Intent val$intent;
        private final /* synthetic */ Intent val$intent2;

        C00072(Intent intent, Context context, Intent intent2) {
            this.val$intent = intent;
            this.val$context = context;
            this.val$intent2 = intent2;
        }

        public void onClick(DialogInterface dialog, int which) {
            this.val$intent.setData(Uri.parse(this.val$context.getString(R.string.update_checker_url)));
            this.val$intent2.setData(Uri.parse(this.val$context.getString(R.string.update_checker_developer_url)));
            GetFromMarketDialog.startSaveActivity(this.val$context, this.val$intent, this.val$intent2);
        }
    }
}

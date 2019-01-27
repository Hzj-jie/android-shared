package org.gemini.shared;

import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Process;
import java.util.ArrayList;
import java.util.Arrays;

public abstract class PermissionRequestActivity extends SelfDestroyActivity {
  private final String[] permissions;
  private final int requestCode = (int) (Math.random() * 1000000000);

  public PermissionRequestActivity(String... permissions) {
    Preconditions.isNotNull(permissions);
    Preconditions.isTrue(permissions.length > 0);
    this.permissions = permissions;
  }

  @Override
  protected final void onCreate(Bundle bundle) {
    super.onCreate(bundle);
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
      onPermissionGranted();
      return;
    }

    ArrayList<String> needRequest = new ArrayList<>();
    for (String permission : permissions) {
      if (checkSelfPermission(permission) == PackageManager.PERMISSION_DENIED) {
        needRequest.add(permission);
      }
    }
    if (needRequest.isEmpty()) {
      onPermissionGranted();
      return;
    }

    requestPermissions(needRequest.toArray(new String[0]), requestCode);
  }

  @Override
  public void onRequestPermissionsResult(
      int requestCode,
      String[] permissions,
      int[] grantResults) {
    Preconditions.isTrue(requestCode == this.requestCode);
    Preconditions.isTrue(Arrays.deepEquals(permissions, this.permissions));
    Preconditions.isNotNull(grantResults);
    Preconditions.isTrue(grantResults.length == permissions.length);

    for (int result : grantResults) {
      if (result == PackageManager.PERMISSION_DENIED) {
        onPermissionDenied();
        return;
      }
    }
    onPermissionGranted();
  }

  protected abstract void onPermissionGranted();

  protected void onPermissionDenied() {
    onDestroy();
  }
}

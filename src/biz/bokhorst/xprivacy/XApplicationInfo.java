package biz.bokhorst.xprivacy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.SparseArray;

public class XApplicationInfo implements Comparable<XApplicationInfo> {
	private Drawable mDrawable;
	private List<String> mListApplicationName;
	private String mPackageName;
	private boolean mHasInternet;
	private int mUid;
	private String mVersion;
	private boolean mSystem;
	private boolean mInstalled;

	public XApplicationInfo(String packageName, Context context) {
		// Get app info
		try {
			ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(packageName, 0);
			this.Initialize(appInfo, context);
		} catch (NameNotFoundException ex) {
			mInstalled = false;
		} catch (Throwable ex) {
			XUtil.bug(null, ex);
			return;
		}
	}

	private XApplicationInfo(ApplicationInfo appInfo, Context context) {
		this.Initialize(appInfo, context);
	}

	private void Initialize(ApplicationInfo appInfo, Context context) {
		PackageManager pm = context.getPackageManager();
		mDrawable = appInfo.loadIcon(pm);
		mListApplicationName = new ArrayList<String>();
		mListApplicationName.add(getApplicationName(appInfo, pm));
		mPackageName = appInfo.packageName;
		mHasInternet = XRestriction.hasInternet(context, appInfo.packageName);
		mUid = appInfo.uid;
		try {
			mVersion = pm.getPackageInfo(appInfo.packageName, 0).versionName;
			mInstalled = true;
		} catch (NameNotFoundException ex) {
			mInstalled = false;
		} catch (Throwable ex) {
			mInstalled = false;
			XUtil.bug(null, ex);
		}
		mSystem = ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0);
		mSystem = mSystem || appInfo.packageName.equals(XApplicationInfo.class.getPackage().getName());
		mSystem = mSystem || appInfo.packageName.equals("de.robv.android.xposed.installer");
	}

	public static List<XApplicationInfo> getXApplicationList(Context context) {
		// Get references
		PackageManager pm = context.getPackageManager();
		boolean expert = Boolean.parseBoolean(XRestriction.getSetting(null, context, XRestriction.cSettingExpert,
				Boolean.FALSE.toString(), false));

		// Get app list
		SparseArray<XApplicationInfo> mapApp = new SparseArray<XApplicationInfo>();
		List<XApplicationInfo> listApp = new ArrayList<XApplicationInfo>();
		for (ApplicationInfo appInfo : pm.getInstalledApplications(PackageManager.GET_META_DATA)) {
			XApplicationInfo xAppInfo = new XApplicationInfo(appInfo, context);
			if (xAppInfo.getIsSystem() ? expert : true) {
				XApplicationInfo yAppInfo = mapApp.get(appInfo.uid);
				if (yAppInfo == null) {
					mapApp.put(appInfo.uid, xAppInfo);
					listApp.add(xAppInfo);
				} else
					yAppInfo.AddApplicationName(getApplicationName(appInfo, pm));
			}
		}

		// Sort result
		Collections.sort(listApp);
		return listApp;
	}

	private static String getApplicationName(ApplicationInfo appInfo, PackageManager pm) {
		return (String) pm.getApplicationLabel(appInfo);
	}

	private void AddApplicationName(String Name) {
		mListApplicationName.add(Name);
	}

	public String getPackageName() {
		return mPackageName;
	}

	public Drawable getDrawable() {
		return mDrawable;
	}

	public boolean hasInternet() {
		return mHasInternet;
	}

	public int getUid() {
		return mUid;
	}

	public String getVersion() {
		return mVersion;
	}

	public boolean getIsSystem() {
		return mSystem;
	}

	public boolean getIsInstalled() {
		return mInstalled;
	}

	@Override
	@SuppressLint("DefaultLocale")
	public String toString() {
		return String.format("%s", TextUtils.join(", ", mListApplicationName));
	}

	@Override
	public int compareTo(XApplicationInfo other) {
		return toString().compareToIgnoreCase(other.toString());
	}
}

package com.helloworld.bartender;

/**
 * Created by wilybear on 2018-03-23.
 */

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;

import com.helloworld.bartender.SettingConponents.AppCompatPreferenceActivity;
import com.helloworld.bartender.SettingConponents.VersionChecker.MarketVersionChecker;
import com.kakao.kakaolink.KakaoLink;
import com.kakao.kakaolink.KakaoTalkLinkMessageBuilder;

import net.rdrei.android.dirchooser.DirectoryChooserActivity;
import net.rdrei.android.dirchooser.DirectoryChooserConfig;

import java.util.ArrayList;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class SettingsPrefActivity extends AppCompatPreferenceActivity {
    private static final String TAG = SettingsPrefActivity.class.getSimpleName();
    private static String appPackageName;
    private static final int REQUEST_DIRECTORY = 0;
    private static String device_version = "";
    private static final int OPENLICENSE_CODE = 0;
    private static final int TERMS_CODE = 1;
    private static String mImagePath = "sdcard/Images/product_icon.png";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.title_activity_setting));
        appPackageName = getApplicationContext().getPackageName();
        // load settings fragment
        getFragmentManager().beginTransaction().replace(android.R.id.content, new MainPreferenceFragment()).commit();
    }

    public static class MainPreferenceFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
        @Override
        public void onResume() {
            super.onResume();
            getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
            getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
            super.onPause();
        }

        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preference_layout_setting);
            Preference galleryPath = (findPreference(getString(R.string.key_gallery_name)));
            Preference openLicensePref = (findPreference(getString(R.string.key_open_license)));
            Preference termsPref = (findPreference(getString(R.string.key_terms)));
            Preference faqPref = (findPreference(getString(R.string.key_faq)));
            Preference versionPref = (findPreference(getString(R.string.key_app_version)));


            SharedPreferences sp = getActivity().getSharedPreferences(getString(R.string.gallery_pref), 0);
            String path = sp.getString(getString(R.string.key_gallery_name), "Picture");
            if (path != null) {
                galleryPath.setSummary(path);
            }
            // gallery EditText change listener
            galleryPath.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    final Intent chooserIntent = new Intent(
                            getActivity(),
                            DirectoryChooserActivity.class);

                    final DirectoryChooserConfig config = DirectoryChooserConfig.builder()
                            .newDirectoryName("DirChooserSample")
                            .allowReadOnlyDirectory(true)
                            .allowNewDirectoryNameModification(true)
                            .build();

                    chooserIntent.putExtra(
                            DirectoryChooserActivity.EXTRA_CONFIG,
                            config);

                    startActivityForResult(chooserIntent, REQUEST_DIRECTORY);
                    return true;
                }
            });

            try {
                device_version = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionName;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            versionPref.setSummary(device_version);

            versionPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    String store_version = "1";
                    try {
                        store_version = MarketVersionChecker.getMarketVersion(getActivity().getPackageName());
                    } catch (Exception e) {
                        Log.d("MarketNotExist", e.toString());
                    }
                    if (store_version.compareTo(device_version) > 0) {
                        new SweetAlertDialog(getActivity(), SweetAlertDialog.WARNING_TYPE)
                                .setTitleText("New Update")
                                .setContentText(getString(R.string.update_message))
                                .showCancelButton(true)
                                .setCancelText("Not Now")
                                .setConfirmText("Update Now")
                                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                    @Override
                                    public void onClick(SweetAlertDialog sDialog) {
                                        sDialog.dismissWithAnimation();
                                        Intent marketLaunch = new Intent(
                                                Intent.ACTION_VIEW);
                                        marketLaunch.setData(Uri
                                                .parse("https://play.google.com/store/apps/details?id=" + getActivity().getPackageName()));
                                        startActivity(marketLaunch);
                                    }
                                })
                                .show();
                    } else {
                        new SweetAlertDialog(getActivity(), SweetAlertDialog.NORMAL_TYPE)
                                .setTitleText(getString(R.string.title_new_update))
                                .setConfirmText("Okay")
                                .show();
                    }

                    return true;
                }
            });

            openLicensePref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    startDetailActivity(getActivity(), OPENLICENSE_CODE);
                    return true;
                }
            });

            termsPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    startDetailActivity(getActivity(), TERMS_CODE);
                    return true;
                }
            });

            faqPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent(getActivity(), FaqActivity.class);
                    startActivity(intent);
                    return true;
                }
            });

            // feedback preference click listener
            Preference feedbackPref = findPreference(getString(R.string.key_send_feedback));
            feedbackPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    sendFeedback(getActivity());
                    return true;
                }
            });

            //open playstore app
            Preference review = findPreference(getString(R.string.key_review));
            review.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    openPlayStore(getActivity());
                    return true;
                }
            });

            //shared intent
            Preference share = findPreference(getString(R.string.key_share));
            share.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    shareApp(getActivity());
                    return true;
                }
            });
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == REQUEST_DIRECTORY) {
                Log.i(TAG, String.format("Return from DirChooser with result %d",
                        resultCode));

                if (resultCode == DirectoryChooserActivity.RESULT_CODE_DIR_SELECTED) {
                    SharedPreferences pref = getActivity().getSharedPreferences(getString(R.string.gallery_pref), 0);
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putString(getString(R.string.key_gallery_name), data
                            .getStringExtra(DirectoryChooserActivity.RESULT_SELECTED_DIR));
                    editor.commit();
                    Preference galleryPath = findPreference(getString(R.string.key_gallery_name));
                    galleryPath
                            .setSummary(data
                                    .getStringExtra(DirectoryChooserActivity.RESULT_SELECTED_DIR));
                } else {

                }
            }
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    private static void startDetailActivity(Context context, int pageCode) {
        Intent intent = new Intent(context, DetailSettingActivity.class);
        intent.putExtra("pageCode", pageCode);
        context.startActivity(intent);
    }

    /**
     * Email client intent to send support mail
     * Appends the necessary device information to email body
     * useful when providing support
     */
    private static void sendFeedback(Context context) {
        String body = null;
        try {
            body = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
            body = "\n\n-----------------------------\nPlease don't remove this information\n Device OS: Android \n Device OS version: " +
                    Build.VERSION.RELEASE + "\n App Version: " + body + "\n Device Brand: " + Build.BRAND +
                    "\n Device Model: " + Build.MODEL + "\n Device Manufacturer: " + Build.MANUFACTURER;
        } catch (PackageManager.NameNotFoundException e) {
        }
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc822");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"samerj9712@gmail.com"});
        intent.putExtra(Intent.EXTRA_SUBJECT, "Query from android app");
        intent.putExtra(Intent.EXTRA_TEXT, body);
        context.startActivity(Intent.createChooser(intent, context.getString(R.string.choose_email_client)));
    }

    private static void shareApp(Context context) {
        List<Intent> targetShareIntents=new ArrayList<Intent>();
        Intent shareIntent=new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        List<ResolveInfo> resInfos=context.getPackageManager().queryIntentActivities(shareIntent, 0);
        if(!resInfos.isEmpty()){
            System.out.println("Have package");
            for(ResolveInfo resInfo : resInfos){
                String packageName=resInfo.activityInfo.packageName;
                if(packageName.contains("com.facebook.katana")) {
                    Intent intent=new Intent();
                    intent.setComponent(new ComponentName(packageName, resInfo.activityInfo.name));
                    intent.setAction(Intent.ACTION_SEND);
                    intent.setType("text/plain");
                    intent.putExtra(Intent.EXTRA_TEXT, "http://www.google.com");
                    intent.setPackage(packageName);
                    targetShareIntents.add(intent);
                } else if(packageName.contains("com.twitter.android")) {
                    Intent intent=new Intent();
                    intent.setComponent(new ComponentName(packageName, resInfo.activityInfo.name));
                    intent.setAction(Intent.ACTION_SEND);
                    //intent.setType("image/*");
                    intent.setType("text/plain");
                    intent.putExtra(Intent.EXTRA_TEXT, "제목 http://www.google.com #제목");
                    //intent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file:///"+mImagePath));
                    intent.setPackage(packageName);
                    targetShareIntents.add(intent);
                } else if(packageName.contains("com.kakao.talk")) {
                    Intent intent=new Intent();
                    intent.setComponent(new ComponentName(packageName, resInfo.activityInfo.name));
                    intent.setAction(Intent.ACTION_SEND);
                    intent.setType("text/plain");
                    intent.putExtra(Intent.EXTRA_SUBJECT, "제목");
                    intent.putExtra(Intent.EXTRA_TEXT, "http://www.google.com");
                    intent.setPackage(packageName);
                    targetShareIntents.add(intent);
                } else {
                    Intent intent=new Intent();
                    intent.setComponent(new ComponentName(packageName, resInfo.activityInfo.name));
                    intent.setAction(Intent.ACTION_SEND);
                    intent.setType("text/plain");
                    intent.putExtra(Intent.EXTRA_SUBJECT, "제목");
                    intent.putExtra(Intent.EXTRA_TEXT, "http://www.google.com");
                    intent.setPackage(packageName);
                    targetShareIntents.add(intent);
                }
            }
            if(!targetShareIntents.isEmpty()){
                Intent chooserIntent=Intent.createChooser(targetShareIntents.remove(0), "Choose app to share");
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, targetShareIntents.toArray(new Parcelable[]{}));
                context.startActivity(chooserIntent);
            }else{
                System.out.println("Do not Have Intent");
                //showDialaog(this);
            }
        }
    }

    private static void openPlayStore(Context context) {
        try {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
        } catch (android.content.ActivityNotFoundException anfe) {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
        }
    }

    public static void shareKakao(Context context) {
        try {
            final KakaoLink kakaoLink = KakaoLink.getKakaoLink(context);
            final KakaoTalkLinkMessageBuilder kakaoBuilder = kakaoLink.createKakaoTalkLinkMessageBuilder();

            //메세지 추가
            kakaoBuilder.addText("카카오 테스트");

            //이미지
            String url = "";
            kakaoBuilder.addImage(url, 160, 160);

            //실행버튼
            kakaoBuilder.addAppButton("앱 실행 및 다운로드");

            //메세지 발송
            kakaoLink.sendMessage(kakaoBuilder, context);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
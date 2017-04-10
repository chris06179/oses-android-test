-dontwarn com.codetroopers.betterpickers.**
-dontwarn org.spongycastle.**
-dontwarn com.itextpdf.**
-dontwarn com.google.android.gms.internal.zzbns
-dontwarn com.google.firebase.iid.zzc

-keepattributes *Annotation*
-keepclassmembers class ** {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }
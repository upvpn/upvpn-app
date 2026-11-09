# R8 rules for the release build. Library rules (kotlinx-serialization, Retrofit,
# Sandwich, Room, OkHttp, Billing, Credentials) ship inside their artifacts and are
# applied automatically; only app-specific rules live here.

# Keep file names and line numbers so Play Console / crash reports stay readable
# after obfuscation. The mapping file is bundled in the AAB automatically.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# JNI: libwg-go exports symbols named Java_app_upvpn_upvpn_service_VPNService_*
# (see app/tunnel/libwg-go/jni.c), so this class and its native methods must keep
# their exact names. The default Android rules already do this; stated explicitly
# because the app does not work without it.
-keepclasseswithmembernames,includedescriptorclasses class app.upvpn.upvpn.service.VPNService {
    native <methods>;
}

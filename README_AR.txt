SUPER COIN RUNNER - مشروع لعبة Android بلغة Java

المميزات:
- لعبة تفادي عقبات وجمع عملات.
- تحريك اللاعب بسحب الإصبع يميناً ويساراً.
- 3 فرص (قلوب).
- نقاط للعملات وتجاوز العقبات.
- مستويات تزداد سرعتها وصعوبتها.
- شاشة بداية ونهاية وإعادة اللعب.
- رسومات Canvas؛ لا تحتاج صوراً أو ملفات صوتية خارجية.
- لا تحتاج صلاحيات أو اتصال إنترنت.

البنية:
- MainActivity.java: تشغيل اللعبة ووضع ملء الشاشة.
- GameView.java: الرسم وحلقة اللعبة واللمس والتصادم والنقاط.
- AndroidManifest.xml: تعريف التطبيق.
- ملفات Gradle: إعدادات البناء.

إعدادات:
Package: com.example.supercoinrunner
Minimum SDK: 23 (Android 6.0)
Compile SDK: 35
Language: Java
Orientation: Portrait

التشغيل:
1. فك ضغط الملف.
2. استورد مجلد SuperCoinRunner إلى Klencod IDE إذا كان يدعم Android/Java projects.
3. نفّذ Sync/Build ثم شغّل التطبيق.

ملاحظة:
نجاح البناء يعتمد على دعم نسخة Klencod IDE لمشاريع Android الأصلية بلغة Java. إذا ظهر خطأ، أرسل صورة رسالة الخطأ وملف app/build.gradle حتى نعدّل الإعدادات.


الأيقونة:
تمت إضافة أيقونة مخصصة للعبة وربطها في AndroidManifest.xml باستخدام:
android:icon="@mipmap/ic_launcher"
وتوجد نسخ متعددة الدقة داخل app/src/main/res/mipmap-*.

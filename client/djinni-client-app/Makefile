./build_ios/libclientapp.xcodeproj: libclientapp.gyp ./deps/djinni/support-lib/support_lib.gyp client.djinni
	sh ./run_djinni.sh
	deps/gyp/gyp --depth=. -f xcode -DOS=ios --generator-output ./build_ios -Ideps/djinni/common.gypi ./libclientapp.gyp

ios: ./build_ios/libclientapp.xcodeproj
	xcodebuild -workspace ios_project/ClientApp.xcworkspace \
		-scheme ClientApp \
		-configuration 'Debug' \
		-sdk iphonesimulator

GypAndroid.mk: libclientapp.gyp ./deps/djinni/support-lib/support_lib.gyp client.djinni
	sh ./run_djinni.sh
	ANDROID_BUILD_TOP=$(shell dirname `which ndk-build`) deps/gyp/gyp --depth=. -f android -DOS=android -Ideps/djinni/common.gypi ./libclientapp.gyp --root-target=libclientapp_jni

android: GypAndroid.mk
	cd android_project/ClientApp/ && ./gradlew app:assembleDebug
	@echo "Apks produced at:"
	@python deps/djinni/example/glob.py ./ '*.apk'

sqlite: ./build_ios/libclientapp.xcodeproj

clean:
	rm -rf ./build_ios ./generated-src .*~ src/.*~

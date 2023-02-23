# Wallbreak

This app demonstrates two high severity permanent denial-of-service vulnerabilities in Android's `WallpaperManagerService`: [CVE-2021-39670](https://www.cve.org/CVERecord?id=CVE-2021-39670) and [CVE-2021-39690](https://www.cve.org/CVERecord?id=CVE-2021-39690).
After running either exploit, the device will keep repeatedly crashing and rebooting. 

Blog post coming soon! :)

## CVE-2021-39670 "Stream Exploit"

- Exploits the `setStream` API in `WallpaperManager` to exhaust device memory by setting a malicious bitmap file as the wallpaper.
- Appears to be very portable across manufacturers and device versions.
- Patch released in [May 2022 Android Security Bulletin](https://source.android.com/docs/security/bulletin/2022-05-01).
- Was [patched by](https://android.googlesource.com/platform/frameworks/base/+/b1b01433f5b8dc0702c0e1abde5f7b86b708a849) using a more efficient wallpaper decoder in `WallpaperManagerService`, and adding a file-based recovery system in case wallpaper still fails to be decoded.

## CVE-2021-39690 "Padding Exploit"

- Exploits the display padding functionality in some Android phones to either crash `SurfaceFlinger` or exhaust device memory.
- I could only reproduce this vulnerability in Pixel devices with animated live wallpapers.
- Requires Android P or higher.
- Initial patch released in [March 2022 Android Security Bulletin](https://source.android.com/docs/security/bulletin/2022-03-01).
- Was [initially patched by](https://android.googlesource.com/platform/frameworks/native/+/2914a57d755051a3e5f05154d784a08019500946) adding stricter input validation in `SurfaceFlinger`, and then [fully mitigated by](https://android.googlesource.com/platform/frameworks/base/+/f6b503a8c18a6b9179ff8d416544a6651facd805) adding a padding limit in `WallpaperManager`.

As far as I'm aware devices bricked due to these vulnerabilities can't be fixed excpet through factory reset. Please run this app at your own risk.
Note that this project is provided for educational purposes only; please don't use it for malicious activities.

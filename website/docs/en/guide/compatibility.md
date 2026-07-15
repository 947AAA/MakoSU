---
title: KMI compatibility
description: Determine whether a MakoSU formal KMI matches an Android device kernel.
---

# KMI compatibility

KMI compatibility does not mean that a matching kernel major version is enough. MakoSU uses the Android KMI marker in the kernel version string, then still depends on vendor ABI, symbols, configuration, and integration details.

## Formal KMI matrix

| Android generation | Kernel | KMI              |
| ------------------ | ------ | ---------------- |
| Android 12         | 5.10   | `android12-5.10` |
| Android 13         | 5.10   | `android13-5.10` |
| Android 13         | 5.15   | `android13-5.15` |
| Android 14         | 5.15   | `android14-5.15` |
| Android 14         | 6.1    | `android14-6.1`  |
| Android 15         | 6.6    | `android15-6.6`  |
| Android 16         | 6.12   | `android16-6.12` |

These seven modules are the complete formal release set. Files outside the release bundle do not gain support merely because their names look similar.

## Check in this order

1. **Integration mode:** determine whether the device uses a formal GKI/LKM build or a vendor-maintained built-in kernel.
2. **Complete KMI:** confirm the full `androidXX-X.XX` marker instead of reading only the major kernel version.
3. **Vendor ABI:** check that vendor changes preserve the target GKI ABI and symbol versions.
4. **Kernel configuration:** verify module loading and any options needed by LKM, hooks, or SuSFS.
5. **Release identity:** use a Manager, APK certificate, and kernel artifact from the same MakoSU release contract.

Collect initial information with:

```bash
adb shell uname -a
adb shell uname -r
adb shell getprop ro.build.version.release
adb shell getprop ro.product.device
adb shell getprop ro.boot.slot_suffix
```

These values are a starting point, not a replacement for the kernel maintainer's ABI and symbol checks.

## Why two 5.10 kernels can differ

`android12-5.10` and `android13-5.10` are different KMI generations. A vendor can also change exported symbols, module signing, module loading, or configuration while keeping a similar version string. A forced mismatch can fail immediately or crash after reboot.

## GKI 1.0 and kernel 5.4

::: danger Not formally supported
Android 11 / 5.4 (GKI 1.0) is outside the current MakoSU formal release and bundled KMI range. Experimental source cannot prove that one module works across all 5.4 devices.
:::

5.4 devices require device-specific source, configuration, ABI validation, and a tested recovery path.

## Manager identity contract

| Field                | Release value                                                      |
| -------------------- | ------------------------------------------------------------------ |
| Application package  | `com.makosu.manager`                                               |
| Certificate DER size | `0x0549`                                                           |
| Certificate SHA-256  | `7eb729e2d76e05488cc4150825e69be9a8beca33bf606ea9217e163eea3b3943` |

Changing any identity field requires rebuilding every formal KMI and verifying the APK v2 certificate again.

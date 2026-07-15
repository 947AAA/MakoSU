---
title: Troubleshooting and recovery
description: Diagnose MakoSU detection, LKM, boot, and SuSFS failures.
---

# Troubleshooting and recovery

Separate Manager UI problems from kernel or identity mismatches and from post-flash boot failures. Do not keep trying different LKMs without preserving the last usable state.

## Manager reports not installed

1. The APK is installed, but the running kernel does not integrate MakoSU/KernelSU.
2. The package is not `com.makosu.manager`.
3. The APK uses a different Release or Debug certificate than the kernel expects.
4. The kernel still expects another downstream Manager identity.
5. The KMI module is not loaded or belongs to another Android KMI generation.

Confirm the APK source, complete KMI, and kernel integration notes. Changing only the display name cannot fix an identity mismatch.

## LKM installation or loading fails

- Stop when automatic matching returns no compatible module.
- Check the complete KMI instead of treating every `5.10` or `5.15` module as interchangeable.
- Keep the install log and identify whether verification, writing, loading, or reboot failed.
- If the device is still running, do not reboot or try another module before restoring the prior state.
- Redownload a release when its SHA-256 does not match.

## The device will not boot after flashing

::: danger Recover first
Enter Bootloader or Recovery and restore the original image saved before flashing. Do not stack another patch on the failing image.
:::

1. Confirm that the device can still enter Bootloader, Fastboot, or Recovery.
2. Check the active slot before restoring anything.
3. Restore the same partition that was modified; do not assume every device uses `boot`.
4. Once the device boots, preserve the failing image, logs, and full kernel information for analysis.
5. If the kernel boots and only a normal Root module is broken, disable that module through safe mode or Recovery before reflashing the kernel.

The exact partition and command depend on the device. Follow the device maintainer's documented recovery procedure.

## SuSFS configuration problems

1. Disable auto-start so the failing configuration is not reapplied.
2. Restore the last known-good backup from the Manager.
3. Without a backup, restore defaults category by category.
4. Check which SuSFS features the kernel actually reports as enabled.
5. Reboot and verify the minimal state before enabling auto-start again.

## Collect diagnostics

```bash
adb shell uname -a
adb shell uname -r
adb shell getprop ro.build.version.release
adb shell getprop ro.product.device
adb shell getprop ro.boot.slot_suffix
adb logcat -d > makosu-logcat.txt
```

When opening an issue, include the device, ROM, Android version, full kernel string, KMI, MakoSU release, hashes, install mode, target partition, active slot, and reproducible steps. Redact serial numbers, accounts, keys, and unrelated app data. Use [GitHub Issues](https://github.com/Spring-bulid/MakoSU/issues) for reproducible bugs and [Security Advisories](https://github.com/Spring-bulid/MakoSU/security/advisories/new) for vulnerabilities.

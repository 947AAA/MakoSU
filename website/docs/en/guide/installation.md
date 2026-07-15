---
title: Installation and updates
description: Safely install and update the MakoSU Manager and formal KMI modules.
---

# Installation and updates

Installing the APK alone does not provide Root. The MakoSU Manager must work with a kernel that integrates MakoSU/KernelSU or with an exactly matching formal LKM.

## Before you start

- The bootloader is unlocked and you understand whether unlocking will wipe data.
- You have backed up the original image for the current slot and can enter Fastboot or Recovery.
- The device is within the [formal KMI range](/en/guide/compatibility), or its kernel maintainer provides a verified device-specific build.
- The package is a signed MakoSU Release with application id `com.makosu.manager`.
- You know the active slot and the device's actual boot partition. Do not guess the partition from the Android version.

::: warning Keep the release identity together
The Manager package, APK Release certificate, kernel certificate expectations, and KMI modules form one release contract. A repackaged APK with the same display name may still be rejected by the kernel.
:::

## Verify downloads

The release bundle should contain `SHA256SUMS.txt`. Verify the APK, ZIP, and LKM hashes before flashing:

::: code-group

```powershell [Windows PowerShell]
Get-FileHash .\MakoSU_*.apk -Algorithm SHA256
Get-FileHash .\MakoSU_*.zip -Algorithm SHA256
```

```bash [Linux / macOS]
sha256sum MakoSU_*.apk MakoSU_*.zip
```

:::

Do not continue when a hash differs from the release page.

## First installation

1. Install the MakoSU Manager from the matching release.
2. Open the home screen and check that the kernel and mode are detected.
3. If a compatible kernel is already running, finish the authorization check before flashing anything again.
4. If an LKM is required, let the Manager read the complete Android KMI marker.
5. Manually select a KMI only when the device kernel, vendor ABI, and maintainer notes all agree.
6. Confirm the target partition, slot, and original-image backup immediately before patching or installing.
7. Reboot once and verify Root authorization, modules, and SuSFS status.

## Formal LKM set

The release contains exactly: `android12-5.10`, `android13-5.10`, `android13-5.15`, `android14-5.15`, `android14-6.1`, `android15-6.6`, and `android16-6.12`.

Do not force-load an LKM because the device is also `5.10` or `5.15`. Android KMI generation, vendor ABI, exported symbols, and kernel configuration must match.

## Updating

1. Read the release notes and confirm whether Manager, KMI modules, and signing identity changed together.
2. Back up the original image, usable image, and important SuSFS configuration.
3. Install the Manager from the same release as the KMI or kernel artifact.
4. Do not mix a new Manager with an old downstream `kernelsu.ko`.
5. On A/B devices, install to the inactive slot only for the documented post-OTA, pre-reboot workflow and verify the target slot.

If the Manager reports that it is not installed or the device fails after flashing, stop trying different LKMs and read [troubleshooting and recovery](/en/guide/troubleshooting).

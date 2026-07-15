---
title: MakoSU Documentation
description: Installation, KMI compatibility, SuSFS, and recovery guides for MakoSU.
outline: false
---

# MakoSU Documentation

These guides cover the current MakoSU release: installation, compatibility checks, SuSFS configuration, and recovery. They describe verified release behavior and do not treat experimental code as formal support.

::: danger Flashing can brick a device
MakoSU patches boot images and loads kernel modules. Back up the original image, confirm a working Fastboot or Recovery path, and record the active slot before making changes. A mismatched kernel, LKM, signing identity, or partition can prevent the device from booting.
:::

## Start here

- [Installation and updates](/en/guide/installation): verify downloads, install the Manager, select an LKM, and update safely.
- [KMI compatibility](/en/guide/compatibility): check the seven formal KMI targets and understand why a kernel major version is not enough.
- [Using SuSFS](/en/guide/susfs): review prerequisites, configuration order, backups, and rollback.
- [Troubleshooting and recovery](/en/guide/troubleshooting): diagnose Manager detection, LKM failures, boot problems, and logs.

## Current formal range

| Area                | Formal release range       |
| ------------------- | -------------------------- |
| Manager minimum     | Android 8.0 / API 26       |
| Kernel mode         | GKI 2.0                    |
| Kernel versions     | 5.10, 5.15, 6.1, 6.6, 6.12 |
| Bundled KMI modules | 7                          |
| Manager package     | `com.makosu.manager`       |

Android 11 / 5.4 (GKI 1.0) is not part of the current formal release or bundled KMI set. Experimental 5.4 source does not imply universal compatibility.

## Get a release

Download the APK and matching bundle from [GitHub Releases](https://github.com/Spring-bulid/MakoSU/releases). Do not mix packages signed by another certificate or use an unknown `kernelsu.ko`.

MakoSU is provided as-is. Users are responsible for unlocking, flashing, Root, data loss, warranty impact, and device damage risks.

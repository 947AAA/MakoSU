---
title: Using SuSFS
description: Configure MakoSU SuSFS hiding features, auto-start, backups, and rollback.
---

# Using SuSFS

The MakoSU Manager provides the SuSFS userspace configuration surface. Available features still depend on the device kernel; the Manager cannot add kernel-side SuSFS support to a kernel that does not include it.

## Requirements

- The Manager detects a working MakoSU kernel and Root state.
- The SuSFS screen can read enabled features instead of showing an unsupported state.
- The kernel maintainer confirms a SuSFS version compatible with the Manager.
- You have a configuration backup and a bootable image before changing rules.

## Recommended order

1. Read the enabled-feature report before changing anything.
2. Create a complete backup and record the usable state.
3. Change one category at a time, such as hidden paths or Maps rules.
4. Apply and verify the result before enabling auto-start.
5. Reboot when required and check the target app and system stability.
6. Restore the backup at the first sign of trouble instead of adding more rules.

## Configuration categories

### Hidden paths

Hide explicitly selected paths from a target process. Avoid broad rules that cover system startup or directories required by the target app.

### Maps and memory information

Adjust the memory-map information visible to a target process. Keep rules limited to confirmed checks and avoid importing untrusted templates in bulk.

### Kstat

Configure file-state values for selected paths. Size, timestamps, and inode values should remain internally consistent.

### uname and build time

Change the kernel version or build time visible to an app. This does not change the real kernel and cannot fix a KMI mismatch.

### Auto-start

Auto-start applies non-default configuration during boot. MakoSU stages, synchronizes, switches, and rolls back updates when possible, but incorrect rules can still affect apps and services. Enable it only after manual application is stable.

## Save and restore behavior

The current userspace implementation uses cross-process locking, temporary files, `fsync`, atomic replacement, strict parsing, one root read for complete configuration, and rollback attempts for failed auto-start updates.

Restoring a backup replaces the current configuration. Check the backup source, device, and version before importing it.

## When a change does not apply

1. Confirm that the kernel reports the requested SuSFS feature as enabled.
2. Check that the rule was saved and uses supported separators.
3. Check whether the setting requires a reboot.
4. Check whether auto-start rolled back during boot.
5. Consider isolated processes, services, or user profiles used by the target app.

Return to a minimal configuration and use [troubleshooting and recovery](/en/guide/troubleshooting) when the state is unclear.

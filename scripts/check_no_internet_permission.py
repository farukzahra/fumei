#!/usr/bin/env python3
"""Fail when the merged Android manifest asks for the INTERNET permission.

The app promises offline use: the only update check goes through the Play Store
app, which does its own networking. The Fumei process must never hold network
permission, so this guard runs after a build in CI.
"""

from __future__ import annotations

import sys
import xml.etree.ElementTree as ElementTree
from pathlib import Path

ANDROID_NAMESPACE = "http://schemas.android.com/apk/res/android"
FORBIDDEN_PERMISSION = "android.permission.INTERNET"
REPO_ROOT = Path(__file__).resolve().parents[1]
MERGED_MANIFEST_PATTERNS = (
    "app/build/intermediates/merged_manifest/debug/processDebugMainManifest/AndroidManifest.xml",
    "app/build/intermediates/merged_manifests/debug/processDebugManifest/AndroidManifest.xml",
)


def find_forbidden_permissions(manifest_xml: str) -> list[str]:
    """Return every forbidden permission declared in the manifest text."""
    root = ElementTree.fromstring(manifest_xml)
    found = []
    for element in root.iter("uses-permission"):
        name = element.get(f"{{{ANDROID_NAMESPACE}}}name")
        if name == FORBIDDEN_PERMISSION:
            found.append(name)
    return found


def merged_manifests() -> list[Path]:
    return [path for path in (REPO_ROOT / pattern for pattern in MERGED_MANIFEST_PATTERNS) if path.is_file()]


def main() -> int:
    manifests = merged_manifests()
    if not manifests:
        print(
            "merged manifest not found, run ./gradlew assembleDebug first",
            file=sys.stderr,
        )
        return 1

    errors = []
    for manifest in manifests:
        forbidden = find_forbidden_permissions(manifest.read_text(encoding="utf-8"))
        if forbidden:
            errors.append(f"{manifest}: app must not request {FORBIDDEN_PERMISSION}")

    if errors:
        print("\n".join(errors), file=sys.stderr)
        return 1

    print(f"Offline promise OK: no {FORBIDDEN_PERMISSION} in {len(manifests)} merged manifest(s)")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

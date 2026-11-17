#!/usr/bin/env python3
"""Validate the network policy of the merged Android manifest.

The Fale conosco feature sends the message over HTTPS (Web3Forms), so the app
now requests INTERNET. Cleartext HTTP stays forbidden.
"""

from __future__ import annotations

import sys
import xml.etree.ElementTree as ElementTree
from pathlib import Path

ANDROID_NAMESPACE = "http://schemas.android.com/apk/res/android"
REQUIRED_PERMISSION = "android.permission.INTERNET"
REPO_ROOT = Path(__file__).resolve().parents[1]
MERGED_MANIFEST_PATTERNS = (
    "app/build/intermediates/merged_manifest/debug/processDebugMainManifest/AndroidManifest.xml",
    "app/build/intermediates/merged_manifests/debug/processDebugManifest/AndroidManifest.xml",
)


def permissions(manifest_xml: str) -> list[str]:
    """Return every permission name declared in the manifest text."""
    root = ElementTree.fromstring(manifest_xml)
    return [
        element.get(f"{{{ANDROID_NAMESPACE}}}name") or ""
        for element in root.iter("uses-permission")
    ]


def allows_cleartext(manifest_xml: str) -> bool:
    """Return True when the application explicitly allows cleartext HTTP."""
    root = ElementTree.fromstring(manifest_xml)
    application = root.find("application")
    if application is None:
        return False
    value = application.get(f"{{{ANDROID_NAMESPACE}}}usesCleartextTraffic")
    return (value or "").strip().lower() == "true"


def merged_manifests() -> list[Path]:
    return [
        path
        for path in (REPO_ROOT / pattern for pattern in MERGED_MANIFEST_PATTERNS)
        if path.is_file()
    ]


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
        text = manifest.read_text(encoding="utf-8")
        if REQUIRED_PERMISSION not in permissions(text):
            errors.append(f"{manifest}: app must request {REQUIRED_PERMISSION}")
        if allows_cleartext(text):
            errors.append(f"{manifest}: app must not allow cleartext traffic")

    if errors:
        print("\n".join(errors), file=sys.stderr)
        return 1

    print(
        f"Network policy OK: {REQUIRED_PERMISSION} present, cleartext disabled "
        f"in {len(manifests)} merged manifest(s)"
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

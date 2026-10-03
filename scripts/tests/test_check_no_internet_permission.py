import unittest

from scripts.check_no_internet_permission import find_forbidden_permissions

NAMESPACE = 'xmlns:android="http://schemas.android.com/apk/res/android"'


class NoInternetPermissionTest(unittest.TestCase):
    def test_accepts_manifest_without_permissions(self):
        manifest = f"<manifest {NAMESPACE}><application /></manifest>"

        self.assertEqual([], find_forbidden_permissions(manifest))

    def test_accepts_other_permissions(self):
        manifest = (
            f"<manifest {NAMESPACE}>"
            '<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />'
            "</manifest>"
        )

        self.assertEqual([], find_forbidden_permissions(manifest))

    def test_rejects_internet_permission(self):
        manifest = (
            f"<manifest {NAMESPACE}>"
            '<uses-permission android:name="android.permission.INTERNET" />'
            "</manifest>"
        )

        self.assertEqual(
            ["android.permission.INTERNET"],
            find_forbidden_permissions(manifest),
        )


if __name__ == "__main__":
    unittest.main()

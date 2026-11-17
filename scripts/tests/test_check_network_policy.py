import unittest

from scripts.check_network_policy import allows_cleartext, permissions

NAMESPACE = 'xmlns:android="http://schemas.android.com/apk/res/android"'


class NetworkPolicyTest(unittest.TestCase):
    def test_lists_permissions(self):
        manifest = (
            f"<manifest {NAMESPACE}>"
            '<uses-permission android:name="android.permission.INTERNET" />'
            '<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />'
            "</manifest>"
        )

        self.assertEqual(
            ["android.permission.INTERNET", "android.permission.POST_NOTIFICATIONS"],
            permissions(manifest),
        )

    def test_accepts_manifest_without_permissions(self):
        manifest = f"<manifest {NAMESPACE}><application /></manifest>"

        self.assertEqual([], permissions(manifest))

    def test_detects_cleartext_true(self):
        manifest = (
            f"<manifest {NAMESPACE}>"
            '<application android:usesCleartextTraffic="true" />'
            "</manifest>"
        )

        self.assertTrue(allows_cleartext(manifest))

    def test_ignores_cleartext_false(self):
        manifest = (
            f"<manifest {NAMESPACE}>"
            '<application android:usesCleartextTraffic="false" />'
            "</manifest>"
        )

        self.assertFalse(allows_cleartext(manifest))

    def test_ignores_missing_application(self):
        manifest = f"<manifest {NAMESPACE} />"

        self.assertFalse(allows_cleartext(manifest))


if __name__ == "__main__":
    unittest.main()

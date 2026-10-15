# Marketplace credentials and v4 migration

## Universal v4

Universal v4 JARs are identical for every account.
They never import embedded resources or rewritten marketplace placeholders.
All local tools are free.
To enable hosted networks, link a purchase to an account in the dashboard.

Plugin Portal still supports `Authentication.ApiKey`, `pluginportal.txt`, and `mclicense.txt` on disk.
These credentials never enroll or control a server.
For each installation, run the console `link` command.
Review the link details. Run `link confirm` to confirm them.

Use trusted artifacts. A malicious JAR can execute code with the server process's privileges.
Enrollment prevents accidental account links from credentials in redistributed official JARs.
It cannot make untrusted executable code safe.

The notes below describe historical 3.x behavior only.
They do not describe the v4 runtime or its release acceptance process.

Before Plugin Portal checks premium entitlement, it imports delivery credentials.
An imported credential does not grant access.
`/premium/validate` remains the authority for premium actions.

- Plugin Portal uses a rewritten `%%__BBB_LICENSE__%%` unchanged.
- It stores a rewritten `%%__LICENSE__%%` with MCLicense 1.5.1's `pm_` prefix.
  It does not require the `%%__POLYMART__%%` marker, regardless of its value.
- It ignores blank or unrewritten placeholders.
- Configured keys and legacy key files take precedence.

Before the importer calls MCLicense, it writes `plugins/PluginPortal/mclicense.txt`.
It then saves the imported key in `Authentication.ApiKey`.
This bypasses the library's discovery through markers.
It preserves the library's validation and marketplace license creation.
A provider outage does not discard the key.

## 3.8.7 regression

The public `v3.8.7` release JAR contains a disabled Polymart check in `EntitlementManager.loadMarketplaceKey`.
At build time, Kotlin evaluated the constant comparison `POLYMART_MARKER == "1"` and emitted `false`.
Changes to strings in that JAR after compilation cannot restore the branch.
BBB retained a runtime check. This bug did not affect BBB.

The replacement resolver passes delivery strings into a runtime function.
`MarketplaceKeyTest` rewrites the compiled class with synthetic credentials and loads it separately.
It checks behavior after compilation, not only source-level inputs.
MockBukkit tests cover file/config persistence, restart recovery, preservation of configured keys, and entitlement checks.

Before you supply a build to a marketplace, run `./gradlew test :plugin:shadowJar`.
For final delivery acceptance, download a fresh buyer-specific JAR.
Verify that premium activates.
Do not publish a buyer-specific JAR or its credentials.

References:

- [MCLicense 1.5.1 integration](https://docs.mclicense.org/license-check)
- [Voxel Shop delivery](https://docs.mclicense.org/marketplace/voxel-shop)
- [BuiltByBit delivery](https://docs.mclicense.org/marketplace/builtbybit)

## Voxel and MC License audit, 2026-10-02

Plugin Portal uses MC License in two places:

1. The plugin bundles `org.mclicense:library:1.5.1`. At first marketplace import, it calls `MCLicense.validateKey` with Plugin Portal's MC License plugin ID.
2. When the Plugin Portal API checks Premium access, it calls MC License's HTTPS validation endpoint. The plugin sends its saved key to `/premium/validate`.

Voxel still documents `%%__LICENSE__%%` and the legacy Polymart placeholder names.
Keep these exact strings. The rebrand does not require new names.
Voxel replaces the license placeholder for paid downloads.
A normal public JAR must contain the placeholder, not a buyer's key.

The audit used `javap` to inspect the compiled 3.8.9 JAR.
Its importer retains a runtime call with both marketplace placeholders.
Tests with synthetic changes to the final JAR's class passed for Voxel and BuiltByBit, also with special characters.
The unchanged class returns no key.
The public JAR contains the MC License classes and no `mclicense.txt` customer credential.

The bundled library URL-encodes the license.
It checks the response signature, nonce, plugin ID, and key.
The API adapter uses HTTPS on our trusted server.
MC License's non-Minecraft guide permits that integration without client-side signature verification.
The adapter also URL-encodes license path segments. Voxel explicitly allows URL-unsafe characters.

If validation fails, do not log request URLs that contain license keys.

The return value from the first library call does not enable Premium.
Our API validation still controls access.
This lets the free plugin load and preserves the imported key for retry after a provider outage.
Configured keys take precedence over a new marketplace key.

### Final marketplace acceptance

Local checks do not verify the seller dashboard or Voxel's delivery service.
Before you publish the release as verified, complete these checks:

1. Make sure that the MC License plugin points to the correct Voxel product.
2. Make sure that MC License's Keys page has the seller's Voxel API key.
3. Download a fresh buyer-specific JAR from the paid Voxel entry.
4. Start it with a fresh Plugin Portal data folder on a disposable server.
5. Confirm that the key imports and Premium validation succeeds.
6. Restart the server.
7. Confirm that Premium access remains active.
8. Check the related managed license in MC License.

Do not print the key.
Do not attach the buyer-specific JAR to a public release.
Do not replace a customer's configured key to perform this test.

Additional references:

- [Voxel placeholders](https://voxel.shop/wiki/placeholders)
- [Voxel Developer API](https://voxel.shop/wiki/api)
- [MC License validation API](https://docs.mclicense.org/public-api/validate/get)
- [MC License security](https://docs.mclicense.org/security)
- [MC License server-side integration](https://docs.mclicense.org/non-minecraft-plugins)

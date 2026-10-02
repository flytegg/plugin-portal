# Marketplace license import

Plugin Portal imports delivery credentials before checking premium entitlement.
Importing a credential does not grant access: `/premium/validate` remains the
authority for premium actions.

- A rewritten `%%__BBB_LICENSE__%%` is used unchanged.
- A rewritten `%%__LICENSE__%%` is stored with MCLicense 1.5.1's `pm_` prefix.
  The `%%__POLYMART__%%` marker is not required, regardless of its value.
- Blank or unrewritten placeholders are ignored.
- Existing configured keys and legacy key files take precedence.

The importer writes `plugins/PluginPortal/mclicense.txt` before calling
MCLicense, then saves the imported key in `Authentication.ApiKey`. This bypasses
the library's marker-dependent discovery, but preserves its validation and
marketplace license provisioning. A provider outage does not discard the key.

## 3.8.7 regression

The public `v3.8.7` release JAR contains a compiled-out Polymart gate in
`EntitlementManager.loadMarketplaceKey`. Kotlin evaluated the constant
comparison `POLYMART_MARKER == "1"` at build time and emitted `false`.
Replacing strings in that JAR after compilation cannot restore the branch.
BBB retained a runtime check and was not affected by this particular bug.

The replacement resolver passes delivery strings into a runtime function.
`MarketplaceKeyTest` rewrites the compiled class with synthetic credentials and
loads it separately, checking behavior after compilation rather than only
testing source-level inputs. MockBukkit tests cover file/config persistence,
restart recovery, preservation of configured keys, and the entitlement gate.

Run `./gradlew test :plugin:shadowJar` before handing a build to a marketplace.
For final delivery acceptance, download a fresh buyer-specific JAR and verify
that premium activates. Never publish a buyer-specific JAR or its credentials.

References:

- [MCLicense 1.5.1 integration](https://docs.mclicense.org/license-check)
- [Voxel Shop delivery](https://docs.mclicense.org/marketplace/voxel-shop)
- [BuiltByBit delivery](https://docs.mclicense.org/marketplace/builtbybit)


## Voxel and MC License audit, 2026-10-02

Plugin Portal uses MC License in two places:

1. The plugin bundles `org.mclicense:library:1.5.1`. On first marketplace import,
   it calls `MCLicense.validateKey` with Plugin Portal's MC License plugin ID.
2. The Plugin Portal API calls MC License's HTTPS validation endpoint when it
   checks Premium access. The plugin sends its saved key to `/premium/validate`.

Voxel still documents `%%__LICENSE__%%` and the legacy Polymart placeholder
names. Keep these exact strings. The rebrand does not require renaming them.
Voxel replaces the license placeholder for paid downloads. A normal public JAR
must contain the placeholder, not a buyer's key.

The 3.8.9 compiled JAR was inspected with `javap`. Its importer retains a runtime
call with both marketplace placeholders. Synthetic rewriting of the final JAR's
class passed for Voxel and BuiltByBit, including special characters. The unchanged
class returns no key. The public JAR contains the MC License classes and no
`mclicense.txt` customer credential.

The bundled library URL-encodes the license and checks the response signature,
nonce, plugin ID, and key. The API-side adapter uses HTTPS on our trusted server.
MC License's non-Minecraft guide permits that integration without client-side
signature verification. The adapter now also URL-encodes license path segments;
Voxel explicitly allows URL-unsafe characters. Do not log request URLs containing
license keys when validation fails.

The return value from the first library call does not enable Premium. Our API
validation remains the access gate. This lets the free plugin load and preserves
the imported key for retry after a provider outage. Existing configured keys take
precedence over a newly downloaded marketplace key.

### Final marketplace acceptance

Local checks do not verify the seller dashboard or Voxel's delivery service.
Before publishing the release as verified:

1. Check that the MC License plugin points to the correct Voxel product.
2. Check that the seller's Voxel API key is configured on MC License's Keys page.
3. Download a fresh buyer-specific JAR from the paid Voxel listing.
4. Start it with a fresh Plugin Portal data folder on a disposable server.
5. Confirm that the key imports and Premium validation succeeds.
6. Restart the server and confirm that Premium access remains active.
7. Check the corresponding managed license in MC License.

Do not print the key, attach the buyer-specific JAR to a public release, or replace
an existing customer's configured key to perform this test.

Additional references:

- [Voxel placeholders](https://voxel.shop/wiki/placeholders)
- [Voxel Developer API](https://voxel.shop/wiki/api)
- [MC License validation API](https://docs.mclicense.org/public-api/validate/get)
- [MC License security](https://docs.mclicense.org/security)
- [MC License server-side integration](https://docs.mclicense.org/non-minecraft-plugins)

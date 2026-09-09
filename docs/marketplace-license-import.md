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

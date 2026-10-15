# Plugin Portal Source Available License 1.2

Copyright (c) Flyte. All rights reserved.

This license applies to the Plugin Portal source code in this repository.
"Plugin Portal" means the software in this repository, including code and
features historically distributed as "Plugin Portal", "PluginPortal",
"Plugin Portal Premium", "PluginPortalPremium", free builds, premium builds,
single-jar builds, and any merged free/premium release line.

It is intended to let server owners, contributors, and curious developers inspect,
build, modify, and privately use the free features of the plugin while protecting
Premium Features, Network Features, the official Plugin Portal service, the
entitlement system, and the project identity.

This is not an OSI-approved open-source license.

## Definitions

- "Free Features" means searching, viewing, listing, installing, updating, and
  uninstalling individual plugins on one installation, and any other function
  that the official build provides without a Valid Entitlement.
- "Premium Features" means the functions that the official build makes available
  only with a Valid Entitlement, and any equivalent or substantially similar
  function in a modified version. At the time of this version of the license,
  this includes bulk updates (`updateAll`), recognition of installed plugins
  (`recognize`, `recognizeAll`), external plugin sources (`external`), the web
  editor (`editor`), import and export (`import`, `export`), and plugin scanning
  (`scan`). It also includes all features formerly shipped in Plugin Portal
  Premium.
- "Network Features" means managing more than one installation from a proxy,
  dashboard, or other central client, including node enrollment, shared network
  inventory, cross-server operations, and access to the hosted network relay.
- "Valid Entitlement" means a license key, purchase, or account entitlement that
  the official Plugin Portal API has issued or verified for you and that is
  currently active. A key that is shared, leaked, generated, forged, or used
  outside the terms of its purchase is not a Valid Entitlement.

## Allowed Use

Subject to the Restrictions below, you may:

- View, study, and build the software.
- Fork and modify the software.
- Use the software, including modified versions, on Minecraft servers that you own
  or operate. Premium Features and Network Features require a Valid Entitlement
  in every version, official or modified.
- Share patches, pull requests, bug reports, and other contributions with the
  Plugin Portal project.
- Distribute non-commercial forks if they clearly identify themselves as forks,
  preserve this license, keep every entitlement check intact, and do not provide
  Premium Features or Network Features without a Valid Entitlement.

## Restrictions

You may not:

- Sell, resell, sublicense, rent, lease, or commercially distribute the software
  or a modified version of it.
- Publish, distribute, or operate a competing public plugin, marketplace service,
  updater service, or entitlement service derived from this software.
- Remove, disable, bypass, weaken, or alter license-key, premium-entitlement,
  API-authentication, or feature-gating functionality. This applies to private
  and unpublished modifications as well as distributed ones.
- Use, or give anyone access to, Premium Features without a Valid Entitlement,
  including by modifying the source or compiled code, reimplementing a gated
  function in a fork, patching or replacing the entitlement check, or using a
  key that is not yours.
- Use, or give anyone access to, Network Features without a Valid Entitlement,
  including by forging grants, sharing node credentials outside their assigned
  network, or impersonating the official API or relay.
- Publish or distribute patches, builds, instructions, tools, or keys whose
  purpose or effect is to unlock Premium Features or Network Features without a
  Valid Entitlement.
- Remove, obscure, or alter copyright, attribution, license, or security notices.
- Misrepresent a fork or modified version as the official Plugin Portal project.
- Use Plugin Portal names, logos, icons, marketplace listings, or other brand
  assets except as allowed by `TRADEMARKS.md`.

## Free, Premium, and Network Features

The same JAR runs on supported backend servers and Velocity proxies and contains
Free Features, Premium Features, and Network Features. The presence of source code
for a Premium Feature or Network Feature in this repository is not a license to
use it. Building, modifying, or decompiling the JAR does not grant a Valid
Entitlement, a subscription, or access to the hosted services. Local feature flags,
configuration values, and client-side checks are not proof of entitlement.

Flyte may decide which functions are Premium Features in the official build. A
function that the official build gates when you obtain or update the software is
a Premium Feature for that version.

## Termination

Your rights under this license end automatically if you breach it. On
termination, you must stop using and distributing the software and any modified
versions. Flyte may also revoke keys, entitlements, and access to the hosted
services that are involved in a breach.

## Hosted Services and APIs

The hosted Plugin Portal API, dashboard, release storage, entitlement checks,
and related infrastructure are not licensed by this repository. Access to those
services may be limited, changed, or revoked independently of this source license.

You may not use this software to create, operate, or distribute a replacement
service, emulator, or proxy for the official API whose purpose or effect is to
bypass Plugin Portal premium licensing, entitlement checks, API access controls, or
marketplace/update enforcement.

## Contributions

By submitting a contribution to this repository, you grant Flyte a perpetual,
worldwide, royalty-free license to use, copy, modify, distribute, sublicense, and
commercialize that contribution as part of Plugin Portal. You represent that you
have the right to submit the contribution.

## No Warranty

The software is provided "as is", without warranties or conditions of any kind,
express or implied, including warranties of merchantability, fitness for a
particular purpose, and non-infringement. Flyte is not liable for damages arising
from use of the software.

## Third-Party Components

Dependencies and third-party code remain subject to their own licenses. This
license does not restrict rights those licenses grant for those components.

## Version History

This version replaces all earlier versions of this license. Every published
release of Plugin Portal was distributed under version 1.0 of this license or
under this version, and both protect Premium Features.

Text titled "version 1.1" appeared only on an unreleased development branch,
starting on 5 October 2026. It was a draft. It was never the license of any
release, and it grants no rights to Premium Features or Network Features. That
draft also forbade the removal or bypass of feature-gating and entitlement
checks. Software obtained from that branch is subject to this version.

## Other Terms

If you want to use Plugin Portal outside this license, including commercial
distribution, resale, or a derived public service, contact Flyte for separate
written permission.

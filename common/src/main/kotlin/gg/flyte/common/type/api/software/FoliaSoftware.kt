package gg.flyte.common.type.api.software

import gg.flyte.common.api.PaperMCAPI
import gg.flyte.common.type.api.software.`interface`.PaperMCSoftwareInterface
import gg.flyte.common.type.api.software.`interface`.SoftwareInterface

object FoliaSoftware : PaperMCSoftwareInterface { override fun getPlatformName(): String = "folia" }
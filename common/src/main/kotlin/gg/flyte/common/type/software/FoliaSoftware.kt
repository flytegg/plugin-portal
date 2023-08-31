package gg.flyte.common.type.software

import gg.flyte.common.api.PaperMCAPI
import gg.flyte.common.type.software.`interface`.PaperMCSoftwareInterface
import gg.flyte.common.type.software.`interface`.SoftwareInterface

object FoliaSoftware : PaperMCSoftwareInterface { override fun getPlatformName(): String = "folia" }
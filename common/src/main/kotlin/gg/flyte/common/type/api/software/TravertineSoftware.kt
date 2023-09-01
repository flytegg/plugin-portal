package gg.flyte.common.type.api.software

import gg.flyte.common.type.api.software.`interface`.PaperMCSoftwareInterface
import gg.flyte.common.type.api.software.`interface`.SoftwareInterface

object TravertineSoftware : PaperMCSoftwareInterface { override fun getPlatformName(): String = "travertine" }
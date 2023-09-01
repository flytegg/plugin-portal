package gg.flyte.common.type.api.software

import gg.flyte.common.type.api.software.`interface`.PaperMCSoftwareInterface
import gg.flyte.common.type.api.software.`interface`.SoftwareInterface

object PaperSoftware : PaperMCSoftwareInterface { override fun getPlatformName(): String = "paper" }